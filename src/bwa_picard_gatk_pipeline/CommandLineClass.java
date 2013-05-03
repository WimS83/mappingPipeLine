/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bwa_picard_gatk_pipeline;

import bwa_picard_gatk_pipeline.GSON.JSONConfig;
import bwa_picard_gatk_pipeline.enums.TargetEnum;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration.Configuration;

/**
 *
 * @author Wim spee
 */
public class CommandLineClass {

    static File outputDir;
    static Configuration config;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here

        Options options = new Options();
        options.addOption("i", "input", true, "the Json config file describing the samples, read groups, tags and files to process. ");
        options.addOption("o", "output", true, "base output directory. Subdirectories will be created in this dir for each sample, read group and tag. ");
        options.addOption("h", "help", false, "print this message");
        options.addOption("t", "target", true, "target point of the pipeline. One of the following:  FASTQ, CHUNKS_BAM, TAG_BAM, READGROUP_BAM, SAMPLE_BAM, SAMPLE_VCF ");
        options.addOption("r", "reference", true, "reference file. fai and BWA indexes should be next to this file.");
        options.addOption("c", "chunk size", true, "chunk size for mapping. Default is 1.000.000 .");        
        options.addOption("f", "offline", false, "do all the processing without using the Sun Grid Engine Cluster. Thist option is mainly for development and debugging purposes, running a real dataset offline will take to long. Default is false");
        options.addOption("z", "color-space-bwa", true, "Location of the last version of BWA that supports color space (0.5.9). Default is /usr/local/bwa/0.5.9/bwa");
        options.addOption("m", "tmpDir", true, "Temporary directory to use for merging bam files. To save IO  and network traffic it is wise to use a directory on the cluster master were the pipeline controller is running. ");

        CommandLineParser parser = new GnuParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException ex) {
            System.out.println("Could not parse arguments");
        }

        if (cmd.hasOption("h")) {
            printHelp(options);
        }

        outputDir = new File(cmd.getOptionValue("o"));
        outputDir.mkdirs();
        
        GlobalConfiguration globalConfiguration = new GlobalConfiguration();
        globalConfiguration.setBaseOutputDir(outputDir);
        
        globalConfiguration.setChunkSize( new Integer(cmd.getOptionValue("c", "1000000")));   
        globalConfiguration.setColorSpaceBWA(new File(cmd.getOptionValue("z", "/usr/local/bwa/0.5.9/bwa")));  
        globalConfiguration.setReferenceFile(new File(cmd.getOptionValue("r")));
        globalConfiguration.setTmpDir(new File(cmd.getOptionValue("m")));
        String targetString = cmd.getOptionValue("t");
        
        if(cmd.hasOption("f")){globalConfiguration.setOffline(true);}else{globalConfiguration.setOffline(false);}
        
        
        globalConfiguration.setTargetEnum(TargetEnum.valueOf(targetString));        

        File JsonConfigFile = new File(cmd.getOptionValue("i"));
        
        List<Sample> samples = new ArrayList<Sample>();
        
        ObjectMapper mapper = new ObjectMapper(); 
        try {
            JSONConfig jsconConfig = mapper.readValue(JsonConfigFile, JSONConfig.class); // 'src' can be File, InputStream, Reader, String
                        
            samples = jsconConfig.getSamples();
            
            for(Sample sample : samples)
            {
                sample.setGlobalConfiguration(globalConfiguration);
                sample.startProcessing();
            }
            
            
        } catch (IOException ex) {
            Logger.getLogger(CommandLineClass.class.getName()).log(Level.SEVERE, null, ex);
     
        }   
    }
   

    private static void printHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("BWA Picard GATK pipeline.  ", options);
        System.exit(1);
    }
}
