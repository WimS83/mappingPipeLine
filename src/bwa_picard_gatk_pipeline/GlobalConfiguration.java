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
    
    
    //general options
    private File baseOutputDir;   
    private Boolean offline;
    private TargetEnum targetEnum;
    private Integer ChunkSize; 
    private File referenceFile;
    private File tmpDir;
    
    //bwa options  
    private File colorSpaceBWA;
    private File BWA;
    
    //picard options 
    private File picardDirectory;
    
    //picl options
    private File picl;
    
    //tophat options 
    private File tophat;
   
    //gatk options 
    private File gatk;
    private File knownIndels;
    private Integer gatkSGEThreads;
    private Integer gatkSGEMemory;
    private Boolean gatkCallReference;
    
    //qualimap options 
    private File qualiMap;
    private Integer qualimapSGEThreads;
    private Integer qualimapSGEMemory;
            
        
    
    

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

    public File getBWA() {
        return BWA;
    }

    public void setBWA(File BWA) {
        this.BWA = BWA;
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

    public File getPicardDirectory() {
        return picardDirectory;
    }

    public void setPicardDirectory(File picardDirectory) {
        this.picardDirectory = picardDirectory;
    }

    public File getPicl() {
        return picl;
    }

    public void setPicl(File picl) {
        this.picl = picl;
    }

    public File getQualiMap() {
        return qualiMap;
    }

    public void setQualiMap(File qualiMap) {
        this.qualiMap = qualiMap;
    }

    public File getGatk() {
        return gatk;
    }

   

   
  
    
   //qualimap getter and setters 

    public Integer getQualimapSGEMemory() {
        return qualimapSGEMemory;
    }

    public void setQualimapSGEMemory(Integer qualimapSGEMemory) {
        this.qualimapSGEMemory = qualimapSGEMemory;
    }
    
    public Integer getQualimapSGEThreads() {
        return qualimapSGEThreads;
    }        

    public void setQualimapSGEThreads(Integer qualimapSGEThreads) {
        this.qualimapSGEThreads = qualimapSGEThreads;
    }
    
    
    // gatk getters and setters 
    public void setGatk(File gatk) {
        this.gatk = gatk;
    }
    
    public Integer getGatkSGEThreads() {
        return gatkSGEThreads;
    }

    
    public void setGatkSGEThreads(Integer gatkSGEThreads) {
        this.gatkSGEThreads = gatkSGEThreads;
    }

    public Integer getGatkSGEMemory() {
        return gatkSGEMemory;
    }
    
    
    public void setGatkSGEMemory(Integer gatkSGEMemory) {
        this.gatkSGEMemory = gatkSGEMemory;
    }

   
    
    public Boolean getGatkCallReference() {
        return gatkCallReference;
    }

    public void setGatkCallReference(Boolean gatkCallReference) {
        this.gatkCallReference = gatkCallReference;
    }
    
    public File getKnownIndels() {
        return knownIndels;
    }

    public void setKnownIndels(File knownIndels) {
        this.knownIndels = knownIndels;
    }
    
    //tophat getters and setters 
    public File getTophat() {
        return tophat;
    }
    
    public void setTophat(File tophat) {
        this.tophat = tophat;
    }
    
    

   
    
    
    
    

    
    
    
    
    

    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
}
