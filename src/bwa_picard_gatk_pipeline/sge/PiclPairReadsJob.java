/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bwa_picard_gatk_pipeline.sge;

import bwa_picard_gatk_pipeline.ReadGroup;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
        
        File copiedF3Bam = new File(tmpDir, F3Bam.getName());
        File copiedF5Bam = new File(tmpDir, F5Bam.getName());
        
        File F3BamSortedByQueryName = new File(tmpDir, FilenameUtils.getBaseName(copiedF3Bam.getAbsolutePath()) + "F3_queryNameSorted.bam");    
        File F5BamSortedByQueryName = new File(tmpDir, FilenameUtils.getBaseName(copiedF5Bam.getAbsolutePath()) + "F5_queryNameSorted.bam"); 
       
        File F3_F5BamSortedByQueryName =  new File(tmpDir, baseName + "_F3_F5_queryNameSorted.bam");            
        File F3_F5BamSortedByCoordinate = new File(tmpDir, pairedBamFile.getName());    
            
        
        File logFile = new File(pairedBamFile.getParentFile(), baseName + "_pairing.log");        
        
        File picardSortSam = new File("/home/sge_share_fedor8/common_scripts/picard/picard-tools-1.89/picard-tools-1.89/SortSam.jar");

        //add sge hostname and date information to log
        addCommand("uname -n >> " + logFile.getAbsolutePath());
        addCommand("\n");
        addCommand("date >> " + logFile.getAbsolutePath());
        addCommand("\n");
        //create a tmp dir
        addCommand("mkdir " + tmpDir);
        addCommand("\n");
        //copy the fastQFile to the tmp dir 
        addCommand("echo starting copying of first bam file >> " + logFile.getAbsolutePath());
        addCommand("cp " + F3Bam.getAbsolutePath()+ " " + tmpDir.getAbsolutePath());
        addCommand("\n");
        addCommand("echo starting copying of second bam file >> " + logFile.getAbsolutePath());
        addCommand("cp " + F5Bam.getAbsolutePath()+ " " + tmpDir.getAbsolutePath());
        addCommand("\n");
        //sort the bam files by queryname
        addCommand("java -jar "+picardSortSam.getAbsolutePath() +" I="+copiedF3Bam.getAbsolutePath() +" O="+ F3BamSortedByQueryName.getAbsolutePath() + " TMP_DIR="+tmpDir.getAbsolutePath()+ " VALIDATION_STRINGENCY=LENIENT SO=queryname CREATE_INDEX=true &>> " + logFile.getAbsolutePath());
        addCommand("java -jar "+picardSortSam.getAbsolutePath() +" I="+copiedF5Bam.getAbsolutePath() +" O="+ F5BamSortedByQueryName.getAbsolutePath() + " TMP_DIR="+tmpDir.getAbsolutePath()+ " VALIDATION_STRINGENCY=LENIENT SO=queryname CREATE_INDEX=true &>> " + logFile.getAbsolutePath());
        //pair the bam files
        addCommand("/usr/local/Picl/picl pairedbammaker -ori ni -first "+ F3BamSortedByQueryName.getAbsolutePath() + " -second "+ F5BamSortedByQueryName + " -output "+ F3_F5BamSortedByQueryName.getAbsolutePath() + " &>> " + logFile.getAbsolutePath());
        //sort the bam file
        addCommand("java -jar "+picardSortSam.getAbsolutePath() +" I="+F3_F5BamSortedByQueryName.getAbsolutePath() + " TMP_DIR="+tmpDir.getAbsolutePath() +" O="+ F3_F5BamSortedByCoordinate.getAbsolutePath() + " VALIDATION_STRINGENCY=LENIENT SO=coordinate CREATE_INDEX=true &>> " + logFile.getAbsolutePath());
        //copy the bamFile back to the server
        addCommand("echo starting copying of bam back to the server >> " + logFile.getAbsolutePath());
        addCommand("date  >> " + logFile.getAbsolutePath());
        addCommand("cp " + F3_F5BamSortedByCoordinate.getAbsolutePath() + " " + pairedBamFile.getParentFile().getAbsolutePath());
        addCommand("\n");
        //remove the tmp dir from the sge host
        addCommand("rm -rf " + tmpDir.getAbsolutePath() + " 2>> " + logFile.getAbsolutePath());
        addCommand("\n");
        addCommand("finished >> " + logFile.getAbsolutePath());
        addCommand("date  >> " + logFile.getAbsolutePath());
    }
    
    public void pairOffline() throws IOException, InterruptedException
    {
        List<String> commands = new ArrayList<String>();
        commands.add("/bin/sh");
        commands.add(this.getAbsolutePath()); 
        
        ProcessBuilder processBuilder = new ProcessBuilder(commands);
        processBuilder.directory(this.getParentFile());   
        Process proces = processBuilder.start();        
        proces.waitFor();
        
    
    }
    
    
    
}
