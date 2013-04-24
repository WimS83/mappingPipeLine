/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bwa_picard_gatk_pipeline;

import bwa_picard_gatk_pipeline.enums.TargetEnum;
import java.io.File;
import java.util.List;

/**
 *
 * @author Wim Spee
 */
public class ReadGroup {

    private String name;
    private String library;
    private String sample;
    
    private File readGroupOutputDir;
    
    private ReadGroupLogFile log;
    
    private List<Tag> tags;  
    
    private GlobalConfiguration globalConfiguration;
    
    private File mergedBam;
    
    
    public void startProcessing()
    {
        log = new ReadGroupLogFile(readGroupOutputDir, name);
        
        for(Tag tag : tags)
        {
            tag.setReadGroup(this);
            tag.createOutputDir(readGroupOutputDir);
            tag.startProcessing();        
        }        
        
        if (globalConfiguration.getTargetEnum().getRank() >= TargetEnum.READGROUP_BAM.getRank()) 
        {         
             mergeTagBams();
        }   
    
    }
    
    private void mergeTagBams() {
        
//        File F3Bam = tagFileCollectionMap.get(TagEnum.F3).getMergedBamFile();
//        File F5Bam = tagFileCollectionMap.get(TagEnum.F5).getMergedBamFile();
//        File R3Bam = tagFileCollectionMap.get(TagEnum.R3).getMergedBamFile();
//        
//        mergedBam = null;
//        
//        if(F3Bam != null && F5Bam != null)
//        {
//            mergedBam = mergeF3AndF5Bam();
//        }
//        else
//        {
//             if(F3Bam != null && R3Bam != null)
//             {
//                 mergedBam = mergeF3AndR3Bam();
//             
//             }
//             else
//             {
//                 mergedBam = F3Bam;
//             }        
//        }        
    }
    
    private File mergeF3AndF5Bam() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private File mergeF3AndR3Bam() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }    

    public String getLibrary() {
        return library;
    }

    public String getSample() {
        return sample;
    }

    public void setSample(String sample) {
        this.sample = sample;
    }  
    

    public File getReadGroupOutputDir() {
        return readGroupOutputDir;
    }   

    public ReadGroupLogFile getLog() {
        return log;
    }

    public GlobalConfiguration getGlobalConfiguration() {
        return globalConfiguration;
    }
    
     public void setGlobalConfiguration(GlobalConfiguration globalConfiguration) {
        this.globalConfiguration = globalConfiguration;
    }
    

    public File getMergedBam() {
        return mergedBam;
    }

    void createOutputDir(File sampleOutputDir) {
        readGroupOutputDir = new File(sampleOutputDir, name);
        readGroupOutputDir.mkdir();
        
    }

    

    

    
    
    
    
    
    
}
