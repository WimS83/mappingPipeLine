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
    private Integer ChunkSize;
    private File referenceFile;
    private File colorSpaceBWA;
    private File tmpDir;
    private File picardSortSam;
    private File picl;
    
    
    private TargetEnum targetEnum;
    private Boolean offline;
    

    public File getBaseOutputDir() {
        return baseOutputDir;
    }

    public void setBaseOutputDir(File baseOutputDir) {
        this.baseOutputDir = baseOutputDir;
    }

    public Integer getChunkSize() {
        return ChunkSize;
    }

    public void setChunkSize(Integer ChunkSize) {
        this.ChunkSize = ChunkSize;
    }

    public TargetEnum getTargetEnum() {
        return targetEnum;
    }

    public void setTargetEnum(TargetEnum targetEnum) {
        this.targetEnum = targetEnum;
    }

    public File getReferenceFile() {
        return referenceFile;
    }

    public void setReferenceFile(File referenceFile) {
        this.referenceFile = referenceFile;
    }

    public File getColorSpaceBWA() {
        return colorSpaceBWA;
    }

    public void setColorSpaceBWA(File colorSpaceBWA) {
        this.colorSpaceBWA = colorSpaceBWA;
    }

    public Boolean getOffline() {
        return offline;
    }

    public void setOffline(Boolean offline) {
        this.offline = offline;
    }

    public File getTmpDir() {
        return tmpDir;
    }

    public void setTmpDir(File tmpDir) {
        this.tmpDir = tmpDir;
    }

    public File getPicardSortSam() {
        return picardSortSam;
    }

    public void setPicardSortSam(File picardSortSam) {
        this.picardSortSam = picardSortSam;
    }

    public File getPicl() {
        return picl;
    }

    public void setPicl(File picl) {
        this.picl = picl;
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
}
