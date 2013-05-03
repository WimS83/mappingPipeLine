/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bwa_picard_gatk_pipeline.fileWrappers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 *
 * @author wim
 */
public class FastQChunk {
    
    private File fastqChunkFile;
    private Long recordNr;

    FastQChunk(File outputChunkFile) {
        fastqChunkFile = outputChunkFile;
    }
    
    public long getRecordNr() {
        return recordNr;
    }

    public void setRecordNr(long recordNr) {
        this.recordNr = recordNr;
    }

    public File getFastqFile() {
        return fastqChunkFile;
    }

   public long countNumberOfrecords() {
        
        if(fastqChunkFile == null){ return 0;}
        
        long lineCounter = new Long(0);        
        
        try {
            
            BufferedReader br = new BufferedReader(new FileReader(fastqChunkFile));
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
    
}
