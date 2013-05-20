/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bwa_picard_gatk_pipeline.sge;

import bwa_picard_gatk_pipeline.ReadGroup;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author wim
 */
public class PiclPairReadsJob extends Job {
    
    
    private String sgeName;    
    private File F3Bam;
    private File F5Bam;
    private File pairedBamFile;
    
    private ReadGroup readGroup;

    public PiclPairReadsJob(File F3Bam, File F5Bam, File pairedBamFile,  ReadGroup readGroup, String hostNameArg) throws IOException {

        super(FilenameUtils.removeExtension(F3Bam.getAbsolutePath()) + "_pairBam.sh");

        this.F3Bam = F3Bam;
        this.readGroup = readGroup;
        this.F5Bam = F5Bam;
        this.pairedBamFile = pairedBamFile;
        
        hostName = hostNameArg;

        addCommands();

        sgeName = "Pair" + F3Bam.getName();
        close();
    }

    @Override
    public String getSGEName() {
        return sgeName;
    }

    private void addCommands() throws IOException {
        String baseName = readGroup.getId();
        File tmpDir = new File("/tmp/" + baseName);
        
//        File copiedF3Bam = new File(tmpDir, F3Bam.getName());
//        File copiedF5Bam = new File(tmpDir, F5Bam.getName());
        
        File F3BamSortedByQueryName = new File(tmpDir, FilenameUtils.getBaseName(F3Bam.getAbsolutePath()) + "F3_queryNameSorted.bam");    
        File F5BamSortedByQueryName = new File(tmpDir, FilenameUtils.getBaseName(F5Bam.getAbsolutePath()) + "F5_queryNameSorted.bam"); 
       
        File F3_F5BamSortedByQueryName =  new File(tmpDir, baseName + "_F3_F5_queryNameSorted.bam");            
//        File F3_F5BamSortedByCoordinate = new File(tmpDir, pairedBamFile.getName());    
//        File F3_F5BamSortedByCoordinateIndex = new File(tmpDir, FilenameUtils.getBaseName(F3_F5BamSortedByCoordinate.getName())+".bai");  
            
        
        File logFile = new File(pairedBamFile.getParentFile(), baseName + "_pairing.log");        
        
        File picardDir = readGroup.getGlobalConfiguration().getPicardDirectory();
        File picardSortSam = new File(picardDir, "SortSam.jar");
        File picl = readGroup.getGlobalConfiguration().getPicl();
        
        String appendAlloutputToLog = " >> "+ logFile.getAbsolutePath() + " 2>&1";
               
        
        //add sge hostname and date information to log
        addCommand("uname -n " + appendAlloutputToLog);
        addCommand("\n");
        addCommand("date " + appendAlloutputToLog);
        addCommand("\n");
        //create a tmp dir
        addCommand("mkdir " + tmpDir + appendAlloutputToLog);
        addCommand("\n");
        //copy the fastQFile to the tmp dir 
        //addCommand("echo starting copying of first bam file " + appendAlloutputToLog);
//        addCommand("cp " + F3Bam.getAbsolutePath()+ " " + tmpDir.getAbsolutePath());
//        addCommand("\n");
//        addCommand("echo starting copying of second bam file >> " + logFile.getAbsolutePath());
//        addCommand("cp " + F5Bam.getAbsolutePath()+ " " + tmpDir.getAbsolutePath());
//        addCommand("\n");
        //sort the bam files by queryname
        addCommand("java -jar "+picardSortSam.getAbsolutePath() +" I="+F3Bam.getAbsolutePath() +" O="+ F3BamSortedByQueryName.getAbsolutePath() + " TMP_DIR="+tmpDir.getAbsolutePath()+ " VALIDATION_STRINGENCY=LENIENT SO=queryname CREATE_INDEX=true " + appendAlloutputToLog);
        addCommand("java -jar "+picardSortSam.getAbsolutePath() +" I="+F5Bam.getAbsolutePath() +" O="+ F5BamSortedByQueryName.getAbsolutePath() + " TMP_DIR="+tmpDir.getAbsolutePath()+ " VALIDATION_STRINGENCY=LENIENT SO=queryname CREATE_INDEX=true " + appendAlloutputToLog);
        //pair the bam files
        addCommand(picl.getAbsolutePath()+" pairedbammaker -ori ni -first "+ F3BamSortedByQueryName.getAbsolutePath() + " -second "+ F5BamSortedByQueryName + " -output "+ F3_F5BamSortedByQueryName.getAbsolutePath() + appendAlloutputToLog);
        //sort the bam file
        addCommand("java -jar "+picardSortSam.getAbsolutePath() +" I="+F3_F5BamSortedByQueryName.getAbsolutePath() + " TMP_DIR="+tmpDir.getAbsolutePath() +" O="+ pairedBamFile.getAbsolutePath() + " VALIDATION_STRINGENCY=LENIENT SO=coordinate CREATE_INDEX=true " + appendAlloutputToLog);
        //copy the bamFile back to the server
        //addCommand("echo starting copying of bam back to the server " + appendAlloutputToLog);
        addCommand("date  " + appendAlloutputToLog);
//        addCommand("cp " + F3_F5BamSortedByCoordinate.getAbsolutePath() + " " + pairedBamFile.getParentFile().getAbsolutePath());
//        addCommand("cp " + F3_F5BamSortedByCoordinateIndex.getAbsolutePath() + " " + pairedBamFile.getParentFile().getAbsolutePath());
        
        
        
        addCommand("\n");
        //remove the tmp dir from the sge host
        addCommand("rm -rf " + tmpDir.getAbsolutePath() + appendAlloutputToLog);
        addCommand("\n");
        addCommand("echo finished" + appendAlloutputToLog);
        addCommand("date  " + appendAlloutputToLog);
    }
    

    
    
    
}
