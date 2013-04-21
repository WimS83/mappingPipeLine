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

    public FastQFile(File fastqFile) {
        this.fastqFile = fastqFile;
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
        long chunkCounter = new Long(1);
        
        String baseName = FilenameUtils.getBaseName(fastqFile.getPath());
        
        File outputChunkFile = new File(outputDir, baseName + "_chunk"+chunkCounter+".fastq");        
        FileWriter fstream = new FileWriter(outputChunkFile);
        BufferedWriter out = new BufferedWriter(fstream);        
        
        BufferedReader br = new BufferedReader(new FileReader(fastqFile));
        String line;  

        while ((line = br.readLine()) != null) {

            //if linecounter is multiple of chunksize
            if(lineCounterIn % chunkSize == 0 && lineCounterIn > 0)
            {
                out.close();
                FastQFile fastqFileWrapper = new FastQFile(outputChunkFile);
                fastqFileWrapper.setRecordNr(lineCounterOut / new Long(4));
                splitFastQFiles.add(fastqFileWrapper);

                chunkCounter++;
                File newOutputChunkFile = new File(outputDir, baseName + "_chunk"+chunkCounter+".fastq");
                FileWriter fstreamNew = new FileWriter(newOutputChunkFile);
                out = new BufferedWriter(fstreamNew);
                lineCounterOut = new Long(0);  
                
                                
                
            }                

            out.write(line);
            out.write("\n");
            lineCounterOut++;
            lineCounterIn++; 

        }
        br.close();
        out.close();
        //ad the last chunk to the list
        FastQFile fastqFileWrapper = new FastQFile(outputChunkFile);
        fastqFileWrapper.setRecordNr(lineCounterOut / new Long(4));
        splitFastQFiles.add(fastqFileWrapper);
        
        recordNr = lineCounterIn / new Long(4); 
        Long recordsInChunks = getRecordNrInChunks();
        
        
        if(!recordNr.equals(recordsInChunks))
        {
            throw new SplitFastQException(toString()+"Record nr in chunks is not equal to record nr in fastq file");
        }
        
        
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
