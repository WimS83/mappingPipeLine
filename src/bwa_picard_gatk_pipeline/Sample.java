/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bwa_picard_gatk_pipeline;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Wim Spee
 */
public class Sample {

    
    List<ReadGroup> readGroups;

    public Sample() {
        readGroups = new ArrayList<ReadGroup>();
        
    }   
    
    
    
    public void startProcessing() {
        
        for(ReadGroup readGroup : readGroups)
        {
            readGroup.startProcessing();
        } 
        
        //merge readgroup bam files
        
        
        //call snps 
        
        //cal sv's         
    }
    
    
    
}
