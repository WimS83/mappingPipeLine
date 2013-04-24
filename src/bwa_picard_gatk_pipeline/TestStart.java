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
        arguments.add("/home/wim/Analysis/BWA_Picard_GATK_pipeline_test/testInput/readGroupTestCSFasta.json");
        arguments.add("-o");
        arguments.add("/home/wim/Analysis/BWA_Picard_GATK_pipeline_test/testOutput/");
        arguments.add("-t");
        arguments.add("FASTQ");   
        arguments.add("-r");
        arguments.add("/home/wim/Analysis/BWA_Picard_GATK_pipeline_test/testInput/R_norvegicus_Rnor_5.0.fa");
        
        
        
        
        
        
        CommandLineClass.main(arguments.toArray(new String[0]));
        
    }
    
    
    
    
   
    
}
