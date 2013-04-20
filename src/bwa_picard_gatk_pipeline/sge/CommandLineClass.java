/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bwa_picard_gatk_pipeline.sge;

import bwa_picard_gatk_pipeline.ReadGroup;
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
    
    static Configuration  config;
    
        
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        
        Options options = new Options();
        options.addOption("i","input", true, "read group file describing the read groups to process.   ");
        options.addOption("o","output", true, "output directory");
        options.addOption("h","help", false, "print this message");
        
        CommandLineParser parser = new GnuParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse( options, args);
        } catch (ParseException ex) {
            Logger.getLogger(CommandLineClass.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if(cmd.hasOption("h")){ printHelp(options); }
        
        outputDir = new File(cmd.getOptionValue("o"));   
        outputDir.mkdirs();
        
        File readGroupFile = new File(cmd.getOptionValue("i"));
        
        
        
        
        try {            
            Map<String, ReadGroup> readGroups = readReadGroupsFile(readGroupFile);
            
                        
            //split the fastqFiles
            for(ReadGroup readGroup : readGroups.values())
            {
               readGroup.splitFastQFiles(); 
            }
            //map the fastq files
            for(ReadGroup readGroup : readGroups.values())
            {
               readGroup.mapFastqFiles(); 
            }
            
            
            
            
            
            
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            System.exit(-1);
        }         
    }     
  
    
    
   

    public static Map<String, ReadGroup> readReadGroupsFile(File readGroupFile) throws IOException, ConfigurationException {        
        
        
        if(!readGroupFile.canRead()){ throw new IOException("Cannot read read group file "+ readGroupFile.getPath());}
        
      
        config = new PropertiesConfiguration(readGroupFile);          
        
        Map<String, ReadGroup> readGroupMap = new HashMap<String, ReadGroup>();
        
        for(String readGroupId: config.getStringArray("readGroup"))
        {
            ReadGroup readGroup = new ReadGroup();              
            readGroup.setReadGroupId(config.getString(readGroupId+".id"));
            readGroup.setLibrary(config.getString(readGroupId+".library"));
            readGroup.setSample(config.getString(readGroupId+".sample"));
            
            readGroup.setOutputDir(new File(outputDir, readGroup.getReadGroupId()));
            readGroup.setReferenceFile(new File(config.getString("referenceFile")));
            readGroupMap.put(readGroupId, readGroup);
        }
        
        for(String fileId: config.getStringArray("File"))
        {
            String readGroupId= config.getString(fileId+".readGroup");
            String filePath = config.getString(fileId+".path");
            FileTypeEnum fileType = FileTypeEnum.valueOf(config.getString(fileId+".type").toUpperCase());
            TagEnum tag = TagEnum.valueOf(config.getString(fileId+".tag").toUpperCase());
            
            readGroupMap.get(readGroupId).addFile(filePath, fileType, tag);            
        
        }
        
        return readGroupMap;  
        
        
    }
    
   
   private File mergeBamFilesUsingPicard(List<File> bamFiles) throws IOException
   {
       PicardBamMerger picardBamMerger = new PicardBamMerger();
       return picardBamMerger.mergeBamFilesUsingPicard(bamFiles);
   }
   
       
    
    private static void printHelp(Options options)
    {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp( "Lifescope bam reheader. For all bam files in the given directory and it's subdirectories, create a copy of the bam where the Read Group Sample is set to the same as the Read Group Library. This to make the lifescope bam files compatible with multisample processing in GATK. ", options );
        System.exit(1);
    }
    
}

   


