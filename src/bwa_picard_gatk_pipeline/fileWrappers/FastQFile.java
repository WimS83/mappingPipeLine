/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bwa_picard_gatk_pipeline.fileWrappers;

import bwa_picard_gatk_pipeline.enums.TagEnum;
import bwa_picard_gatk_pipeline.exceptions.SplitFastQException;
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
    
    private BufferedWriter chunkOut = null;  
    private long chunkCounter = new Long(0);
    private String baseName;
    
    private List<FastQChunk> fastQChunks;
    
    
    
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

   

    public Long countNumberOfrecords() {
        
         fastqFile = new File(path);
        
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
    
    public List<FastQChunk> splitFastQFile(Integer chunkSize, File outputDir) throws FileNotFoundException, IOException
    {
        fastqFile = new File(path);
        this.baseName = FilenameUtils.getBaseName(fastqFile.getPath());   
        
        Integer chunksizeInLines = 4 * chunkSize;
        
        fastQChunks = new ArrayList<FastQChunk>();
        
        long lineCounterIn = new Long(0);    
        long lineCounterOut = new Long(0);   
        
        openNextChunk(outputDir);  
        
        BufferedReader br = new BufferedReader(new FileReader(fastqFile));
        String line;  

        while ((line = br.readLine()) != null) {

            //if linecounter is multiple of chunksize
            if(lineCounterIn % chunksizeInLines == 0 && lineCounterIn > 0)
            {
                //close the current chunk      
                closeCurrentChunk(lineCounterOut);
                lineCounterOut = new Long(0);
                openNextChunk(outputDir);
                
            }                

            chunkOut.write(line);
            chunkOut.write("\n");
            lineCounterOut++;
            lineCounterIn++; 

        }
        br.close();
        closeCurrentChunk(lineCounterOut);       
        
        recordNr = lineCounterIn / new Long(4); 
        Long recordsInChunks = getRecordNrInChunks();
        
        
        if(!recordNr.equals(recordsInChunks))
        {
            throw new IOException(toString()+"Record nr in chunks is not equal to record nr in fastq file");
        }
        
        return fastQChunks;
        
        
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
    
    private void closeCurrentChunk(Long lineCounterOut) throws IOException {
        fastQChunks.get(fastQChunks.size() -1).setRecordNr(lineCounterOut / new Long(4));
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
    
    

    

   

    

   
    
  
        
    
    
    
    
    
    
    
    
    
    
    
}
