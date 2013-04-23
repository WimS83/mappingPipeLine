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
            
    private List<FastQFile> splitFastQFiles;
    
    private BufferedWriter splitFastQout = null;  
    private long chunkCounter = new Long(0);
    private String baseName;
    
    

    public FastQFile(File fastqFile) {
        this.fastqFile = fastqFile;
        this.baseName = FilenameUtils.getBaseName(fastqFile.getPath());                
        
        splitFastQFiles = new ArrayList<FastQFile>();
        
        
    }    
    

    public long getRecordNr() {
        return recordNr;
    }

    public void setRecordNr(long recordNr) {
        this.recordNr = recordNr;
    }

    public File getFastqFile() {
        return fastqFile;
    }

   

    public long countNumberOfrecords() {
        
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
    
    public void splitFastQFile(Long chunkSize, File outputDir) throws FileNotFoundException, IOException, SplitFastQException
    {
        long lineCounterIn = new Long(0);    
        long lineCounterOut = new Long(0);   
        
        openNextChunk(outputDir);  
        
        BufferedReader br = new BufferedReader(new FileReader(fastqFile));
        String line;  

        while ((line = br.readLine()) != null) {

            //if linecounter is multiple of chunksize
            if(lineCounterIn % chunkSize == 0 && lineCounterIn > 0)
            {
                //close the current chunk      
                closeCurrentChunk(lineCounterOut);
                lineCounterOut = new Long(0);
                openNextChunk(outputDir);
                
            }                

            splitFastQout.write(line);
            splitFastQout.write("\n");
            lineCounterOut++;
            lineCounterIn++; 

        }
        br.close();
        closeCurrentChunk(lineCounterOut);       
        
        recordNr = lineCounterIn / new Long(4); 
        Long recordsInChunks = getRecordNrInChunks();
        
        
        if(!recordNr.equals(recordsInChunks))
        {
            throw new SplitFastQException(toString()+"Record nr in chunks is not equal to record nr in fastq file");
        }
        
        
    }
    
    private void openNextChunk(File outputDir) throws IOException
    {
        chunkCounter++;
        File outputChunkFile = new File(outputDir, baseName + "_chunk"+chunkCounter+".fastq");
        FastQFile fastQChunk = new FastQFile(outputChunkFile);
        splitFastQFiles.add(fastQChunk);
        
        FileWriter fstream = new FileWriter(outputChunkFile);
        splitFastQout = new BufferedWriter(fstream);        
    
    }
    
    private void closeCurrentChunk(Long lineCounterOut) throws IOException {
        splitFastQFiles.get(splitFastQFiles.size() -1).setRecordNr(lineCounterOut / new Long(4));
        splitFastQout.close();
        
    }   
    

    public List<FastQFile> getSplitFastQFiles() {
        return splitFastQFiles;
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
        
        if(!splitFastQFiles.isEmpty())
        {            
            sb.append("records in chunks: "+getRecordNrInChunks());
        }
         sb.append("\n"); 
        
        
        
        return sb.toString();
    }
    
    public Long getRecordNrInChunks()
    {
        Long recordInChunks = new Long(0);
        for(FastQFile fastQFileChunk : splitFastQFiles)
        {
            recordInChunks= recordInChunks+fastQFileChunk.getRecordNr();
        }
        
        return recordInChunks;
    
    }

    

    
        
    
    
    
    
    
    
    
    
    
    
    
}
