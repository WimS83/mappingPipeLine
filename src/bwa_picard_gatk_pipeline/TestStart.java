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
        //arguments.add("/home/wim/Analysis/BWA_Picard_GATK_pipeline_test/testInput/readGroupTestCSFasta_existinBamChunks.json");
        arguments.add("/home/wim/Analysis/BWA_Picard_GATK_pipeline_test/testInput/readGroupTestCSFasta_singleFragmentReadgroup.json");
        arguments.add("-o");
        arguments.add("/home/wim/Analysis/BWA_Picard_GATK_pipeline_test/testOutput/");
        arguments.add("-t");
        arguments.add("SAMPLE_ANNOTATED_VCF");   
        arguments.add("-r");
        arguments.add("/home/wim/Analysis/BWA_Picard_GATK_pipeline_test/testInput/Rnor5.0_chr1and2/Rattus_norvegicus.Rnor_5.0.71.dna.chromosome.1and2.fa");
        arguments.add("-f");
        arguments.add("-c");
        arguments.add("-400");
        arguments.add("-m");
        arguments.add("/tmp/blaat");       
        arguments.add("-s");
        arguments.add("/home/wim/NetBeansProjects/java_libs/picard-tools-1.90/picard-tools-1.90/SortSam.jar");       
        arguments.add("-p");
        arguments.add("/usr/local/Picl/picl ");   
        arguments.add("-q");
        arguments.add("/home/wim/qualimap_v0.7.1/qualimap");   
        arguments.add("-g");
        arguments.add("/home/wim/GenomeAnalysisTK-2.5-2-gf57256b/GenomeAnalysisTK.jar");   
        
        
        
        
        CommandLineClass.main(arguments.toArray(new String[0]));
        
    }
    
    
    
    
   
    
}
