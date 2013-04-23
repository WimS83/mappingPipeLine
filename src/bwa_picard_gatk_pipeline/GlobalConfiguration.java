/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bwa_picard_gatk_pipeline;

import bwa_picard_gatk_pipeline.enums.TargetEnum;
import java.io.File;

/**
 *
 * @author root
 */
public class GlobalConfiguration {
    
    
    private File baseOutputDir;
    private File csFastaToFastQFile;
    private Long ChunkSize;
    
    private TargetEnum targetEnum;
    
    
    

    public File getBaseOutputDir() {
        return baseOutputDir;
    }

    public void setBaseOutputDir(File baseOutputDir) {
        this.baseOutputDir = baseOutputDir;
    }

    public File getCsFastaToFastQFile() {
        return csFastaToFastQFile;
    }

    public void setCsFastaToFastQFile(File csFastaToFastQFile) {
        this.csFastaToFastQFile = csFastaToFastQFile;
    }

    public Long getChunkSize() {
        return ChunkSize;
    }

    public void setChunkSize(Long ChunkSize) {
        this.ChunkSize = ChunkSize;
    }

    public TargetEnum getTargetEnum() {
        return targetEnum;
    }

    public void setTargetEnum(TargetEnum targetEnum) {
        this.targetEnum = targetEnum;
    }
    
    
    
    
    
    
    
    
    
    
}
