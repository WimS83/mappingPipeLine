/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bwa_picard_gatk_pipeline.GSON;

import java.util.List;

/**
 *
 * @author root
 */
public class ReadgroupsDef {
    
    String name;
    String library;
    
    List<CsfastaDef> csfastaDef;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLibrary() {
        return library;
    }

    public void setLibrary(String library) {
        this.library = library;
    }

    public List<CsfastaDef> getCsfastaDef() {
        return csfastaDef;
    }

    public void setCsfastaDef(List<CsfastaDef> csfastaDef) {
        this.csfastaDef = csfastaDef;
    }
    
    

   
    
    
    
    
}
