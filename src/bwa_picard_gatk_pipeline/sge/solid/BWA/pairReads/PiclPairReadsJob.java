/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bwa_picard_gatk_pipeline.sge.solid.BWA.pairReads;

import bwa_picard_gatk_pipeline.enums.TagEnum;
import bwa_picard_gatk_pipeline.readGroup.ReadGroupSolid;
import bwa_picard_gatk_pipeline.sge.Job;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author wim
 */
public class PiclPairReadsJob extends Job {
    
    
    private String sgeName;    
    private File firstBam;
    private File secondBam;
    private File pairedBamFile;
    
    private ReadGroupSolid readGroup;
    private TagEnum secondTag;
    

    public PiclPairReadsJob(File firstBam, File secondBam, File pairedBamFile,  ReadGroupSolid readGroup, String hostNameArg, TagEnum secondTag) throws IOException {

        super(FilenameUtils.removeExtension(firstBam.getPath()) + "_pairBam.sh");

        this.firstBam = firstBam;
        this.readGroup = readGroup;
        this.secondBam = secondBam;
        this.pairedBamFile = pairedBamFile;
        this.secondTag = secondTag;
        
        hostName = hostNameArg;

        addCommands();

        sgeName = "Pair" + firstBam.getName();
        close();
    }

    @Override
    public String getSGEName() {
        return sgeName;
    }

    private void addCommands() throws IOException {
        String baseName = readGroup.getId();
        File tmpDir = new File("/tmp/" + baseName);
        
//        File copiedF3Bam = new File(tmpDir, firstBam.getName());
//        File copiedF5Bam = new File(tmpDir, secondBam.getName());
        
        File F3BamSortedByQueryName = new File(tmpDir, FilenameUtils.getBaseName(firstBam.getPath()) + "F3_queryNameSorted.bam");    
        File F5BamSortedByQueryName = new File(tmpDir, FilenameUtils.getBaseName(secondBam.getPath()) + "F5_queryNameSorted.bam"); 
       
        File F3_F5BamSortedByQueryName =  new File(tmpDir, baseName + "_F3_F5_queryNameSorted.bam");            
//        File F3_F5BamSortedByCoordinate = new File(tmpDir, pairedBamFile.getName());    
//        File F3_F5BamSortedByCoordinateIndex = new File(tmpDir, FilenameUtils.getBaseName(F3_F5BamSortedByCoordinate.getName())+".bai");  
            
        
        File logFile = new File(pairedBamFile.getParentFile(), baseName + "_pairing.log");        
        
        File picardDir = readGroup.getGlobalConfiguration().getPicardDirectory();
        File picardSortSam = new File(picardDir, "SortSam.jar");
        File picl = readGroup.getGlobalConfiguration().getPicl();
        
        String appendAlloutputToLog = " >> "+ logFile.getPath() + " 2>&1";
        
        String ori;
        if(secondTag == TagEnum.SOLID_F5)
        {
            ori = "ni";            
        }
        if(secondTag == TagEnum.SOLID_R3)
        {
            ori = "";
            throw new UnsupportedOperationException("R3 tag orientation to use for pairing has not been implemented");
        }
        
        
               
        
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
//        addCommand("cp " + firstBam.getPath()+ " " + tmpDir.getPath());
//        addCommand("\n");
//        addCommand("echo starting copying of second bam file >> " + logFile.getPath());
//        addCommand("cp " + secondBam.getPath()+ " " + tmpDir.getPath());
//        addCommand("\n");
        //sort the bam files by queryname
        addCommand("java -jar "+picardSortSam.getPath() +" I="+firstBam.getPath() +" O="+ F3BamSortedByQueryName.getPath() + " TMP_DIR="+tmpDir.getPath()+ " VALIDATION_STRINGENCY=LENIENT SO=queryname CREATE_INDEX=true " + appendAlloutputToLog);
        addCommand("java -jar "+picardSortSam.getPath() +" I="+secondBam.getPath() +" O="+ F5BamSortedByQueryName.getPath() + " TMP_DIR="+tmpDir.getPath()+ " VALIDATION_STRINGENCY=LENIENT SO=queryname CREATE_INDEX=true " + appendAlloutputToLog);
        //pair the bam files
        addCommand(picl.getPath()+" pairedbammaker -ori ni -first "+ F3BamSortedByQueryName.getPath() + " -second "+ F5BamSortedByQueryName + " -output "+ F3_F5BamSortedByQueryName.getPath() + appendAlloutputToLog);
        //sort the bam file
        addCommand("java -jar "+picardSortSam.getPath() +" I="+F3_F5BamSortedByQueryName.getPath() + " TMP_DIR="+tmpDir.getPath() +" O="+ pairedBamFile.getPath() + " VALIDATION_STRINGENCY=LENIENT SO=coordinate CREATE_INDEX=true " + appendAlloutputToLog);
        //copy the bamFile back to the server
        //addCommand("echo starting copying of bam back to the server " + appendAlloutputToLog);
        addCommand("date  " + appendAlloutputToLog);
//        addCommand("cp " + F3_F5BamSortedByCoordinate.getPath() + " " + pairedBamFile.getParentFile().getPath());
//        addCommand("cp " + F3_F5BamSortedByCoordinateIndex.getPath() + " " + pairedBamFile.getParentFile().getPath());
        
        
        
        addCommand("\n");
        //remove the tmp dir from the sge host
        addCommand("rm -rf " + tmpDir.getPath() + appendAlloutputToLog);
        addCommand("\n");
        addCommand("echo finished" + appendAlloutputToLog);
        addCommand("date  " + appendAlloutputToLog);
    }
    

    
    
    
}
