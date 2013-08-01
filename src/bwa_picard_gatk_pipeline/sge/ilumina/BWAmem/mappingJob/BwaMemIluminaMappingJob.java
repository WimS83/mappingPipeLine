/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bwa_picard_gatk_pipeline.sge.ilumina.BWAmem.mappingJob;

import bwa_picard_gatk_pipeline.sge.ilumina.BWA.mappingJob.*;
import bwa_picard_gatk_pipeline.readGroup.ReadGroupIlumina;
import bwa_picard_gatk_pipeline.sge.Job;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author wim
 */
public class BwaMemIluminaMappingJob extends Job{
    
    private String sgeName;
    private File firstReadsFastqFile;
    private File secondReadsFastqFile;
    private File bamFile;
    
    private ReadGroupIlumina readGroup;

    public BwaMemIluminaMappingJob(File firstReadsFastqFile, File secondReadsFastqFile,  File bamFile,  ReadGroupIlumina readGroup) throws IOException {

        super(FilenameUtils.removeExtension(firstReadsFastqFile.getPath()) + ".sh");

        this.firstReadsFastqFile = firstReadsFastqFile;
        this.secondReadsFastqFile = secondReadsFastqFile;
        this.readGroup = readGroup;
        this.bamFile = bamFile;
        
       
        addCommands();
        
      

        

        sgeName = "BWA-mem_" + firstReadsFastqFile.getName();
        close();
    }

    @Override
    public String getSGEName() {
        return sgeName;
    }

    private void addCommands() throws IOException {
        
        File referenceFile = readGroup.getGlobalConfiguration().getReferenceFile();
        File referenceIndex = new File(referenceFile.getPath() + ".fai");
        File samtoolsFile = new File("/usr/local/samtools/samtools");
        File bwaFile = readGroup.getGlobalConfiguration().getBWA();      
        String bwaOptions = "mem ";
        String readGroupOption = " -R \"@RG\\tID:" + readGroup.getId()+ "\\tPL:ILLUMINA\\tLB:"+  readGroup.getLibrary() + "\\tSM:" + readGroup.getSample() + "\\tDS:" + readGroup.getDescription()+ "\" ";
        bwaOptions = bwaOptions + readGroupOption;
         
        String baseNameFirst = FilenameUtils.getBaseName(firstReadsFastqFile.getPath());
        File tmpDir = new File("/tmp/" + baseNameFirst);
        
        String fastqFiles = "";
        
        File copiedFirstFastqFile = new File(tmpDir, firstReadsFastqFile.getName());
        fastqFiles = copiedFirstFastqFile.getPath();
       
        //if there also is a second read file
        if(secondReadsFastqFile  != null)
        {
            File copiedSecondFastqFile = new File(tmpDir, secondReadsFastqFile.getName());
            fastqFiles = fastqFiles + " " + copiedSecondFastqFile.getPath();
        }
        
        
       
        //File bwaOutputFirstFile = new File(tmpDir, baseNameFirst + ".out");          
        
        File samFile = new File(tmpDir, baseNameFirst + ".sam");
        File tmpBamFile = new File(tmpDir, baseNameFirst + ".bam" );
       
        File bamFileSorted = new File(tmpDir, baseNameFirst + "_sorted.bam");
        
        File parentDir = firstReadsFastqFile.getParentFile();
        File logFile = new File(parentDir, baseNameFirst + ".log");

        String appendAlloutputToLog = " >> "+ logFile.getPath()+ " 2>&1";

        

        //add sge hostname and date information to log
        addCommand("uname -n " + appendAlloutputToLog);
        addCommand("\n");
        addCommand("date " + appendAlloutputToLog);
        addCommand("\n");
        //create a tmp dir
        addCommand("mkdir " + tmpDir + appendAlloutputToLog);
        addCommand("\n");
        //copy the fastQFile to the tmp dir
        addCommand("echo starting copying of fastq file " + appendAlloutputToLog);
        addCommand("cp " + firstReadsFastqFile.getPath()+ " " + tmpDir.getPath());
        if(secondReadsFastqFile != null)
        {
            addCommand("cp " + secondReadsFastqFile.getPath()+ " " + tmpDir.getPath());
        }       
        
        addCommand("\n");
        //map using bwa
        addCommand("echo starting mapping of fastq file " + appendAlloutputToLog);
        addCommand("date  " + appendAlloutputToLog);
        addCommand(bwaFile.getPath() + " " + bwaOptions + " " + referenceFile.getPath() + " " + fastqFiles + " > " + samFile.getPath() + " 2>> " + logFile.getPath());
        
        addCommand("\n");
        //create bam file from sam file
        addCommand("echo starting converting to bam " + appendAlloutputToLog);
        addCommand("date  " + appendAlloutputToLog );
        addCommand(samtoolsFile.getPath() + " import " + referenceIndex.getPath() + " " + samFile.getPath() + " " + tmpBamFile.getPath() + appendAlloutputToLog);
        addCommand("\n");
        //sort the bam file
        addCommand("echo starting sorting of bam " + appendAlloutputToLog);
        addCommand("date  " + appendAlloutputToLog);
        addCommand(samtoolsFile.getPath() + " sort " + tmpBamFile.getPath() + " " + FilenameUtils.removeExtension(bamFileSorted.getPath()) + appendAlloutputToLog);
        addCommand("\n");
        //copy the bamFile back to the server
        addCommand("echo starting copying of bam back to the server " + appendAlloutputToLog);
        addCommand("date " + appendAlloutputToLog);
        addCommand("cp " + bamFileSorted.getPath() + " " + bamFile.getPath());
        addCommand("\n");
        //remove the tmp dir from the sge host
        addCommand("rm -rf " + tmpDir.getPath() + appendAlloutputToLog);
        addCommand("\n");
        addCommand("echo finished " + appendAlloutputToLog);
        addCommand("date  " + appendAlloutputToLog);
    }
        
    
    
}
