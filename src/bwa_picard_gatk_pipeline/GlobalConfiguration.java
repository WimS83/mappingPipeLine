/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bwa_picard_gatk_pipeline;

import bwa_picard_gatk_pipeline.enums.GATKVariantCallers;
import bwa_picard_gatk_pipeline.enums.TargetEnum;
import java.io.File;
import java.util.List;

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
    private File samtools;
    private Boolean useBWAMEM;
    
    //picard options 
    private File picardDirectory;
    
    //picl options
    private File picl;
    
    //tophat options 
    private File tophat;
   
    //gatk options 
    private File gatk;
   
    private Integer gatkSGEThreads;
    private Integer gatkSGEMemory;
    private Boolean gatkCallReference;
    private GATKVariantCallers gATKVariantCaller;
    private Boolean multiSampleCalling;
    
    private List<File> realignKnownIndels;
    private List<File> bqsrKnownVariants;   
    
    
    //qualimap options 
    private File qualiMap;
    private Integer qualimapSGEThreads;
    private Integer qualimapSGEMemory;
    
    //fastqc
    private File fastqQCFile;
            
        
    
    

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

    public Boolean getUseBWAMEM() {
        return useBWAMEM;
    }

    public void setUseBWAMEM(Boolean useBWAMEM) {
        this.useBWAMEM = useBWAMEM;
    }
    
    

    
    public File getSamtools() {
        return samtools;
    }

    public void setSamtools(File samtools) {
        this.samtools = samtools;
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

    public List<File> getRealignKnownIndels() {
        return realignKnownIndels;
    }

    public void setRealignKnownIndels(List<File> realignKnownIndels) {
        this.realignKnownIndels = realignKnownIndels;
    }

    public List<File> getBqsrKnownVariants() {
        return bqsrKnownVariants;
    }

    public void setBqsrKnownVariants(List<File> bqsrKnownVariants) {
        this.bqsrKnownVariants = bqsrKnownVariants;
    }
    
    

   
    
    public Boolean getGatkCallReference() {
        return gatkCallReference;
    }

    public void setGatkCallReference(Boolean gatkCallReference) {
        this.gatkCallReference = gatkCallReference;
    }  

    public Boolean getMultiSampleCalling() {
        return multiSampleCalling;
    }

    public void setMultiSampleCalling(Boolean multiSampleCalling) {
        this.multiSampleCalling = multiSampleCalling;
    }
    
    
    
    
    //tophat getters and setters 
    public File getTophat() {
        return tophat;
    }
    
    public void setTophat(File tophat) {
        this.tophat = tophat;
    }

    public GATKVariantCallers getgATKVariantCaller() {
        return gATKVariantCaller;
    }

    public void setgATKVariantCaller(GATKVariantCallers gATKVariantCaller) {
        this.gATKVariantCaller = gATKVariantCaller;
    }

    public File getFastqQCFile() {
        return fastqQCFile;
    }

    public void setFastqQCFile(File fastqQCFile) {
        this.fastqQCFile = fastqQCFile;
    }
    
    
    
    
    
    
    
    

   
    
    
    
    

    
    
    
    
    

    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
}
