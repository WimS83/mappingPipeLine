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
 * @author Wim Spee
 */
public class BwaSolidMappingJob extends Job {

    private String sgeName;
    private File fastqFile;
    private File bamFile;
    
    private ReadGroup readGroup;

    public BwaSolidMappingJob(File fastqFile, File bamFile,  ReadGroup readGroup) throws IOException {

        super(FilenameUtils.removeExtension(fastqFile.getAbsolutePath()) + ".sh");

        this.fastqFile = fastqFile;
        this.readGroup = readGroup;
        this.bamFile = bamFile;

        addCommands();

        sgeName = "BWA_" + fastqFile.getName();
        close();
    }

    @Override
    public String getSGEName() {
        return sgeName;
    }

    private void addCommands() throws IOException {
        String baseName = FilenameUtils.getBaseName(fastqFile.getPath());
        File tmpDir = new File("/tmp/" + baseName);
        File copiedFastqFile = new File(tmpDir, fastqFile.getName());
        File bwaOutputFile = new File(tmpDir, baseName + ".out");
        File samFile = new File(tmpDir, baseName + ".sam");
        File tmpBamFile = new File(tmpDir, baseName + ".bam" );
       
        File bamFileSorted = new File(tmpDir, baseName + "_sorted.bam");

        File referenceFile = readGroup.getGlobalConfiguration().getReferenceFile();
        File referenceIndex = new File(referenceFile.getAbsolutePath() + ".fai");

        File parentDir = fastqFile.getParentFile();
        File logFile = new File(parentDir, baseName + ".log");
        File bwaFile = readGroup.getGlobalConfiguration().getColorSpaceBWA();
        String bwaOptions = "aln -c -l 25 -k 2 -n 10";
        File samtoolsFile = new File("/usr/local/samtools/samtools");

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
        addCommand("echo starting copying of fastq file " + appendAlloutputToLog);
        addCommand("cp " + fastqFile.getAbsolutePath()+ " " + tmpDir.getAbsolutePath());
        addCommand("\n");
        //map using bwa
        addCommand("echo starting mapping of fastq file " + appendAlloutputToLog);
        addCommand("date  " + appendAlloutputToLog);
        addCommand(bwaFile.getPath() + " " + bwaOptions + " " + referenceFile.getAbsolutePath() + " " + copiedFastqFile.getAbsolutePath() + " > " + bwaOutputFile.getAbsolutePath() + " 2>> " + logFile.getAbsolutePath());
        addCommand("\n");
        //create sam file from output
        addCommand("echo starting converting to sam " + appendAlloutputToLog);
        addCommand("date " + appendAlloutputToLog);
        addCommand(bwaFile.getPath() + " samse -r \"@RG\\tID:" + readGroup.getId()+ "\\tPL:SOLID\\tLB:"+  readGroup.getLibrary() + "\\tSM:" + readGroup.getSample() + "\\tDS:" + readGroup.getDescription()+ "\" " +referenceFile.getAbsolutePath()+ " "  + bwaOutputFile.getAbsolutePath() + " " + copiedFastqFile.getAbsolutePath() + " > " + samFile.getAbsolutePath() + " 2>> " + logFile.getAbsolutePath());
        addCommand("\n");
        //create bam file from sam file
        addCommand("echo starting converting to bam " + appendAlloutputToLog);
        addCommand("date  " + appendAlloutputToLog );
        addCommand(samtoolsFile.getPath() + " import " + referenceIndex.getAbsolutePath() + " " + samFile.getAbsolutePath() + " " + tmpBamFile.getAbsolutePath() + appendAlloutputToLog);
        addCommand("\n");
        //sort the bam file
        addCommand("echo starting sorting of bam " + appendAlloutputToLog);
        addCommand("date  " + appendAlloutputToLog);
        addCommand(samtoolsFile.getPath() + " sort " + tmpBamFile.getAbsolutePath() + " " + FilenameUtils.removeExtension(bamFileSorted.getAbsolutePath()) + appendAlloutputToLog);
        addCommand("\n");
        //copy the bamFile back to the server
        addCommand("echo starting copying of bam back to the server " + appendAlloutputToLog);
        addCommand("date " + appendAlloutputToLog);
        addCommand("cp " + bamFileSorted.getAbsolutePath() + " " + bamFile.getAbsolutePath());
        addCommand("\n");
        //remove the tmp dir from the sge host
        addCommand("rm -rf " + tmpDir.getAbsolutePath() + appendAlloutputToLog);
        addCommand("\n");
        addCommand("echo finished " + appendAlloutputToLog);
        addCommand("date  " + appendAlloutputToLog);
    }
   

}
