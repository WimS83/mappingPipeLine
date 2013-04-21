/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bwa_picard_gatk_pipeline;

import bwa_picard_gatk_pipeline.enums.FileTypeEnum;
import bwa_picard_gatk_pipeline.enums.TagEnum;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

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
        options.addOption("i", "input", true, "read group file describing the read groups to process.   ");
        options.addOption("o", "output", true, "output directory");
        options.addOption("h", "help", false, "print this message");

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

        File readGroupFile = new File(cmd.getOptionValue("i"));





        Map<String, ReadGroupProcecesser> readGroups;
        try {
            readGroups = readReadGroupsFile(readGroupFile);

            for (ReadGroupProcecesser readGroup : readGroups.values()) {
                readGroup.startProcessing();
            }
        } catch (IOException ex) {
            Logger.getLogger(CommandLineClass.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ConfigurationException ex) {
            Logger.getLogger(CommandLineClass.class.getName()).log(Level.SEVERE, null, ex);
        }


        //split the fastqFiles

        //map the fastq files
//            for(ReadGroupProcecesser readGroup : readGroups.values())
//            {
//               readGroup.mapFastqFiles(); 
//            }






    }

    public static Map<String, ReadGroupProcecesser> readReadGroupsFile(File readGroupFile) throws IOException, ConfigurationException {


        if (!readGroupFile.canRead()) {
            throw new IOException("Cannot read read group file " + readGroupFile.getPath());
        }


        config = new PropertiesConfiguration(readGroupFile);

        Map<String, ReadGroupProcecesser> readGroupMap = new HashMap<String, ReadGroupProcecesser>();

        //create the readGroups and readGroups processers
        for (String readGroupId : config.getStringArray("readGroup")) {
            ReadGroup readGroup = new ReadGroup();
            readGroup.setId(config.getString(readGroupId + ".id"));
            readGroup.setLibrary(config.getString(readGroupId + ".library"));
            readGroup.setSample(config.getString(readGroupId + ".sample"));
            readGroup.setReferenceFile(new File(config.getString("referenceFile")));            

            File readGroupOutputDir = new File(outputDir, config.getString(readGroupId + ".id"));
            File csFastaToFastqConverterFile = new File(config.getString("csFastaToFastQConverter"));

            ReadGroupProcecesser readGroupProcesser = new ReadGroupProcecesser(readGroup, readGroupOutputDir, csFastaToFastqConverterFile);
            readGroupProcesser.setChunkSize(new Long(config.getString("chunkSize")));

            readGroupMap.put(readGroupId, readGroupProcesser);
        }

        //add the file to process to the readgroup processer
        for (String fileId : config.getStringArray("File")) {
            String readGroupId = config.getString(fileId + ".readGroup");
            String filePath = config.getString(fileId + ".path");
            FileTypeEnum fileType = FileTypeEnum.valueOf(config.getString(fileId + ".type").toUpperCase());
            TagEnum tag = TagEnum.valueOf(config.getString(fileId + ".tag").toUpperCase());

            readGroupMap.get(readGroupId).getReadGroupFileCollection().addFile(filePath, fileType, tag);

        }

        return readGroupMap;


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
