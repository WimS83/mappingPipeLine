/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bwa_picard_gatk_pipeline;

import bwa_picard_gatk_pipeline.fileWrappers.CsFastaFilePair;
import bwa_picard_gatk_pipeline.fileWrappers.FastQFile;
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

/**
 *
 * @author root
 */
public class BWA_PICARD_GATK_Pipeline {

    static File outputDir;
        
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        
        Options options = new Options();
        options.addOption("i","input", true, "csfasta files. A qual file with the same name or with just _QV extra in the basename should be in the same directory.  ");
        options.addOption("o","output", true, "output directory");
        options.addOption("h","help", false, "print this message");
        
        CommandLineParser parser = new GnuParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse( options, args);
        } catch (ParseException ex) {
            Logger.getLogger(BWA_PICARD_GATK_Pipeline.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if(cmd.hasOption("h")){ printHelp(options); }
        
        outputDir = new File(cmd.getOptionValue("o"));        
        
        
        try {            
            List<CsFastaFilePair> csFastaFilePairs = createCcFastaFilePairs(cmd);  
            List<FastQFile> fastQFiles = convertCSFastaToFastQ(csFastaFilePairs);
            
            
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            System.exit(-1);
        }
        
        
        
        
        
        

       
    }
   
    
    
    
    
    
    
    
  
//   
//   private List<File> splitFastQFile(File fastqFile)
//   {
//       
//   
//   }
//    
//    
//    
//    
//   private List<File> mapFastQFilesUsingBWA(List<File> fastFiles)
//   {  
//       
//       
//       
//       
//   }

    private static List<CsFastaFilePair> createCcFastaFilePairs(CommandLine cmd) throws IOException {
        String[] csFastaFileNames = cmd.getOptionValues("i"); 
        
        List<CsFastaFilePair> csFastaFilePairs = new ArrayList<CsFastaFilePair>();
        for(String csFastaFileName: csFastaFileNames)
        {
            CsFastaFilePair csFastaFilePair = new CsFastaFilePair();
            csFastaFilePair.SetFilesBasedOnCsFastaFilePath(csFastaFileName);
        }
        
        return csFastaFilePairs;  
    }
    
   private static List<FastQFile> convertCSFastaToFastQ(List<CsFastaFilePair> csFastaFilePairs) throws IOException, InterruptedException
   {
       List<FastQFile> fastqFiles = new ArrayList<FastQFile>();
       
       for(CsFastaFilePair csFastaFilePair: csFastaFilePairs)
       {
           File csFastaFilePairOutputDir = new File(outputDir, csFastaFilePair.getBaseName());
           csFastaFilePairOutputDir.mkdir();
           
           csFastaFilePair.setOutputdir(csFastaFilePairOutputDir);           
           fastqFiles.add(csFastaFilePair.convertToFastQFile());
       }
       
       return fastqFiles;
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

   


