/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bwa_picard_gatk_pipeline.fileWrappers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author root
 */
public class FastQFile {
    
    long recordNr;
    File fastqFile;
    
        
    List<FastQFile> splitFastQFiles;

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
    
    public void splitFastQFile(Long chunkSize, File outputDir)
    {
        long lineCounter = new Long(0);    
        long chunkCounter = 1;
        
        String baseName = FilenameUtils.getBaseName(fastqFile.getPath());
        
        List<File> splitFiles = new ArrayList<File>();
        
        try {
            
           
            
            BufferedReader br = new BufferedReader(new FileReader(fastqFile));
            String line;            
            
            File outputFile = new File(outputDir, baseName + "_"+chunkCounter+".fastq");
            splitFiles.add(outputFile);
            FileWriter fstream = new FileWriter(outputFile);
            BufferedWriter out = new BufferedWriter(fstream);
            
            while ((line = br.readLine()) != null) {
                
                if(lineCounter % chunkSize == 0 && lineCounter != 0)
                {
                    chunkCounter++;
                    out.close();
                    File newOutputFile = new File(outputDir, baseName + "_"+chunkCounter+".fastq");
                    splitFiles.add(newOutputFile);
                    FileWriter fstreamNew = new FileWriter(newOutputFile);
                    out = new BufferedWriter(fstreamNew);
                }                
                
                out.write(line);
                out.write("\n");
                lineCounter++; 
                
            }
            br.close();
            out.close();
        } catch (Exception e) {//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }
        
        for(File file : splitFiles)
        {
            splitFastQFiles.add(new FastQFile(file));
        }
        
        
        
    
    }

    

    public List<FastQFile> getSplitFastQFiles() {
        return splitFastQFiles;
    }
    
    
    
    
    
    
    
    
    
    
    
    
}
