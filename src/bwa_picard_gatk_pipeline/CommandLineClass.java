/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bwa_picard_gatk_pipeline;

import bwa_picard_gatk_pipeline.GSON.JSONConfig;
import bwa_picard_gatk_pipeline.GSON.JSONTest;
import bwa_picard_gatk_pipeline.GSON.ReadgroupsDef;
import bwa_picard_gatk_pipeline.GSON.SamplesDef;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.picard.sam.PicardBamMerger;
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
        options.addOption("t", "target", false, "target point of the pipeline. One of the following:  FASTQ, CHUNKS_BAM, TAG_BAM, READGROUP_BAM, SAMPLE_BAM, SAMPLE_VCF ");

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

        File JsonConfigFile = new File(cmd.getOptionValue("i"));
        
        List<Sample> samples = new ArrayList<Sample>();
        
        ObjectMapper mapper = new ObjectMapper(); 
         try {
             JSONConfig jsconConfig = mapper.readValue(JsonConfigFile, JSONConfig.class); // 'src' can be File, InputStream, Reader, String
            
             for(SamplesDef samplesDef : jsconConfig.getSamplesDef())
             {
                 Sample sample = new Sample();
                 
                 for(ReadgroupsDef readgroupsDef : samplesDef.getReadgroupsDef())
                 {
                 
                 
                 }
                 
                 
             
             }
             
             
             
         } catch (IOException ex) {
             Logger.getLogger(JSONTest.class.getName()).log(Level.SEVERE, null, ex);
         }

    
    }
    
    



    private File mergeBamFilesUsingPicard(List<File> bamFiles) throws IOException {
        PicardBamMerger picardBamMerger = new PicardBamMerger();
        return picardBamMerger.mergeBamFilesUsingPicard(bamFiles);
    }

    private static void printHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("BWA Picard GATK pipeline.  ", options);
        System.exit(1);
    }
}
