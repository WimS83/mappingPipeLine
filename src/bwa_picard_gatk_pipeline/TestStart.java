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
public class TestStart {
    
    
     public static void main(String[] args) {
         new TestStart();
     }
    
    

    public TestStart() {
        
        List<String> arguments = new ArrayList<String>();
        
        arguments.add("-i");
        arguments.add("/home/wim/Analysis/pipelineTest/testInput/readGroupTestFastQ.csv");
        arguments.add("-o");
        arguments.add("/home/wim/Analysis/pipelineTest/testOutput");
        
        CommandLineClass.main(arguments.toArray(new String[0]));
        
    }
    
    
    
    
   
    
}
