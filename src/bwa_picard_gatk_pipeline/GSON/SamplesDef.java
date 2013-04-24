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
public class SamplesDef {
    
    String name;
    List<ReadgroupsDef> readgroupsDef;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ReadgroupsDef> getReadgroupsDef() {
        return readgroupsDef;
    }

    public void setReadgroupsDef(List<ReadgroupsDef> ReadgroupsDef) {
        this.readgroupsDef = ReadgroupsDef;
    }
    
    
    
}
