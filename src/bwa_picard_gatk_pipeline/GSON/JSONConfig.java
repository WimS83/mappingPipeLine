/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bwa_picard_gatk_pipeline.GSON;

import java.util.List;

/**
 *
 * @author Wim Spee
 */
public class JSONConfig {
    
    List<SamplesDef> samplesDef;

    public List<SamplesDef> getSamplesDef() {
        return samplesDef;
    }

    public void setSamplesDef(List<SamplesDef> SamplesDef) {
        this.samplesDef = SamplesDef;
    }

    
   
    
    
    
    
    
}
