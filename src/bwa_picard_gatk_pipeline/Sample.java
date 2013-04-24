/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bwa_picard_gatk_pipeline;

import java.io.File;
import java.util.List;

/**
 *
 * @author Wim Spee
 */
public class Sample {

    
    private String name;
    private List<ReadGroup> readGroups;
    
    private GlobalConfiguration globalConfiguration;
    
    private File sampleOutputDir;
    
    
    public void startProcessing() {
        
        for(ReadGroup readGroup : readGroups)
        {
            sampleOutputDir = new File(globalConfiguration.getBaseOutputDir(), name);
            sampleOutputDir.mkdir();
            
            readGroup.createOutputDir(sampleOutputDir);
            
            readGroup.setGlobalConfiguration(globalConfiguration);
            readGroup.setSample(name);
            
            readGroup.startProcessing();
        } 
        
        //merge readgroup bam files
        
        
        //call snps 
        
        //cal sv's         
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ReadGroup> getReadGroups() {
        return readGroups;
    }

    public void setReadGroups(List<ReadGroup> readGroups) {
        this.readGroups = readGroups;
    }

    public void setGlobalConfiguration(GlobalConfiguration globalConfiguration) {
        this.globalConfiguration = globalConfiguration;
    }
    
    
    
    
    
    
    
    
    
    
    
}
