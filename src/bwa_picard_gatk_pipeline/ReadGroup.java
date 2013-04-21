/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bwa_picard_gatk_pipeline;

import java.io.File;

/**
 *
 * @author Wim Spee
 */
public class ReadGroup {
    
    private String id;
    private String library;
    private String sample;
    
    File referenceFile;
    
    public void setId(String readGroupId) {
        this.id = readGroupId;
    }

    public void setLibrary(String library) {
        this.library = library;
    }

    public void setSample(String sample) {
        this.sample = sample;
    }
    
     public String getId() {
        return id;
    }

    public String getLibrary() {
        return library;
    }

    public String getSample() {
        return sample;
    }
    
    public void setReferenceFile(File referenceFile) {
        this.referenceFile = referenceFile;
    }

    public File getReferenceFile() {
        return referenceFile;
    }
    
    
    
}
