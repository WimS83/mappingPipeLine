/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bwa_picard_gatk_pipeline.fileWrappers;

import bwa_picard_gatk_pipeline.enums.TagEnum;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author root
 */
public class FastQFile {
    
    private Long recordNr;
    private File fastqFile;
    
    private TagEnum tag;    
    
    private BufferedReader fastqReader;
    
    private BufferedWriter chunkOut = null;  
    private long chunkCounter = new Long(0);
    private String baseName;
    
    private List<FastQChunk> fastQChunks;
    
    private String readNameMask;
    
    private String path;
    

    public long getRecordNr() {
        return recordNr;
    }

    public void setRecordNr(long recordNr) {
        this.recordNr = recordNr;
    }

    public File getFastqFile() {
        return fastqFile;
    }

   

    public Long countNumberOfrecords() throws FileNotFoundException {
        
        initializeFastqReader();
        
        if(fastqFile == null){ return new Long(0);}
        
        long lineCounter = new Long(0);        
        
        try {
            
            BufferedReader br = new BufferedReader(new FileReader(fastqFile));
            String line;
            while ((line = br.readLine()) != null) {
                lineCounter++;
            }
            br.close();
        } catch (Exception e) {//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }
        
        long divider = new Long(4);
        recordNr = lineCounter / divider;
        
        return recordNr;               
    }
    
    public void initializeFastqReader() throws FileNotFoundException
    {
        fastqFile = new File(path);
        fastqReader = new BufferedReader(new FileReader(fastqFile));
        
    }
    
    public List<FastQChunk> splitFastQFile(Integer chunkSize, File outputDir, String readGroupId) throws FileNotFoundException, IOException
    {
       
        this.baseName = FilenameUtils.getBaseName(fastqFile.getPath());  
        
        fastQChunks = new ArrayList<FastQChunk>();
        
        long fastqEntriesRead = new Long(0);    
        long fastqEntriesWritten = new Long(0);   
        long fastqEntriesWrittenInChunk = new Long(0);   
        
        initializeFastqReader();
        openNextChunk(outputDir);  
        
       
//        String line;  
//        
//        Integer lineInRecord = 1;

//        while ((line = .readLine()) != null) {
//
//            //if linecounter is multiple of chunksize
//            if(lineCounterIn % chunksizeInLines == 0 && lineCounterIn > 0)
//            {
//                //close the current chunk      
//                closeCurrentChunk(lineCounterOut);
//                lineCounterOut = new Long(0);
//                openNextChunk(outputDir);
//                
//            }
//            
//            //do something special based on which line in the record this is
//            if(lineInRecord == 1)
//            {
//                if(readNameMask != null){line = "@"+ readGroupId + ":" + line.replace(readNameMask, "");}               
//            }
//            lineInRecord++;
//            
//            //reset lineInRecord to 1 if the we are on a 5th line            
//            if(lineInRecord == 5 )
//            {     
//                lineInRecord = 1;
//            
//            }
//
//            chunkOut.write(line);
//            chunkOut.write("\n");
//            lineCounterOut++;
//            lineCounterIn++; 
//
//        }
        
        FastqEntry fastqEntry;
        while((fastqEntry = readNextEntry()) != null)
        {
            //reformat the seqname
            String seqName = fastqEntry.getSeqName();
            if(readNameMask != null)
            {
                 seqName = seqName.replace(readNameMask, "");
            }            
            seqName = readGroupId+":"+seqName;            
            fastqEntry.setSeqName(seqName);
            
            //close old and open new chunk if chunksize is hit
            if(fastqEntriesWritten % chunkSize == 0 && fastqEntriesWritten != 0)
            {
                closeCurrentChunk(fastqEntriesWrittenInChunk);
                fastqEntriesWrittenInChunk = new Long(0);
                openNextChunk(outputDir);
            }
            
            chunkOut.write(fastqEntry.toString());
            
            
            fastqEntriesRead++;
            fastqEntriesWritten++;
            fastqEntriesWrittenInChunk++;        
        }
        
        
        fastqReader.close();
        closeCurrentChunk(fastqEntriesWrittenInChunk);       
        
        recordNr = fastqEntriesRead;
        Long recordsInChunks = getRecordNrInChunks();
        
        
        if(!recordNr.equals(recordsInChunks))
        {
            throw new IOException(toString()+"Record nr in chunks is not equal to record nr in fastq file");
        }
        
        return fastQChunks;
        
        
    }    
    
    private FastqEntry readNextEntry() throws IOException
    {
        FastqEntry fastqEntry = new FastqEntry();        
             
        String seqName = fastqReader.readLine();
        String csValues = fastqReader.readLine();
        String description = fastqReader.readLine();
        String qualValue = fastqReader.readLine();  
        
        //if end of file is reached return null
        if(seqName == null || csValues == null || description == null || qualValue == null )
        {
            return null;
        }       
        
        fastqEntry.setSeqName(seqName.substring(1));
        fastqEntry.setCsValues(csValues);
        fastqEntry.setDescription(description);
        fastqEntry.setQualValues(qualValue);
        
        return fastqEntry;   
    }
    
    private void openNextChunk(File outputDir) throws IOException
    {
        chunkCounter++;
        File outputChunkFile = new File(outputDir, baseName + "_chunk"+chunkCounter+".fastq");
        FastQChunk fastQChunk = new FastQChunk(outputChunkFile);       
        fastQChunks.add(fastQChunk);
        
        FileWriter fstream = new FileWriter(outputChunkFile);
        chunkOut = new BufferedWriter(fstream);        
    
    }
    
    private void closeCurrentChunk(Long fastqEntriesInChunk) throws IOException {
        fastQChunks.get(fastQChunks.size() -1).setRecordNr(fastqEntriesInChunk);
        chunkOut.close();        
    }   
    

    public List<FastQChunk> getSplitFastQFiles() {
        return fastQChunks;
    }

    public void setTag(TagEnum tag) {
        this.tag = tag;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        sb.append("FastQ: "+fastqFile.getPath());
        sb.append("\n");      
        sb.append("tag: "+tag);
        sb.append("\n");
        
        if(recordNr == null)
        {
            sb.append("recordNr: unknown");        
        }
        else
        {
            sb.append("recordNr: "+recordNr);        
        }
        sb.append("\n"); 
        
        if(!fastQChunks.isEmpty())
        {            
            sb.append("records in chunks: "+getRecordNrInChunks());
        }
         sb.append("\n"); 
        
        
        
        return sb.toString();
    }
    
    public Long getRecordNrInChunks()
    {
        Long recordInChunks = new Long(0);
        for(FastQChunk fastQChunk : fastQChunks)
        {
            recordInChunks= recordInChunks+fastQChunk.getRecordNr();
        }
        
        return recordInChunks;
    
    }

   

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getReadNameMask() {
        return readNameMask;
    }

    public void setReadNameMask(String readNameMask) {
        this.readNameMask = readNameMask;
    }
    
    
    
    

    

   

    

   
    
  
        
    
    
    
    
    
    
    
    
    
    
    
}
