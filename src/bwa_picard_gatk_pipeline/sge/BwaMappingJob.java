/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bwa_picard_gatk_pipeline.sge;

import bwa_picard_gatk_pipeline.ReadGroup;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author Wim Spee
 */
public class BwaMappingJob extends Job {

    private String command;
    private String sgeName;
    private File mappingJobFile;

    public BwaMappingJob(File fastqFile, ReadGroup readGroup) {

        String baseName = FilenameUtils.getBaseName(fastqFile.getPath());
        File tmpDir = new File("/tmp/" + baseName);
        File copiedFastqFile = new File(tmpDir, fastqFile.getName());
        File bwaOutputFile = new File(tmpDir, baseName + ".out");
        File samFile = new File(tmpDir, baseName + ".sam");
        File bamFile = new File(tmpDir, baseName + ".bam");
        File bamFileSorted = new File(tmpDir, baseName + "_sorted.bam");

        File referenceIndex = new File(readGroup.getReferenceFile().getPath() + ".fai");

        File parentDir = fastqFile.getParentFile();
        File logFile = new File(parentDir, baseName + ".log");
        File bwaFile = new File("/usr/local/bwa/0.5.9/bwa");
        String bwaOptions = "aln -c -l 25 -k 2 -n 10";
        File samtoolsFile = new File("/usr/local/samtools/samtools");


        StringBuilder sb = new StringBuilder();

        //add sge hostname and date information to log
        sb.append("uname -n >> " + logFile.getPath());
        sb.append("\n\n");
        sb.append("date -n >> " + logFile.getPath());
        sb.append("\n\n");
        //create a tmp dir
        sb.append("mkdir " + tmpDir);
        sb.append("\n\n");
        //copy the fastQFile to the tmp dir
        sb.append("echo starting copying of fastq file >> " + logFile.getPath() + "\n");
        sb.append("cp " + fastqFile.getPath() + " " + tmpDir.getPath());
        sb.append("\n\n");
        //map using bwa
        sb.append("echo starting mapping of fastq file >> " + logFile.getPath() + "\n");
        sb.append("date -n > " + logFile.getPath() + "\n");
        sb.append(bwaFile.getPath() + " " + bwaOptions + " " + readGroup.getReferenceFile().getPath() + " " + copiedFastqFile.getPath() + " > " + bwaOutputFile.getPath() + " 2>> " + logFile.getPath());
        sb.append("\n\n");
        //create sam file from output
        sb.append("echo starting converting to sam >> " + logFile.getPath() + "\n");
        sb.append("date -n > " + logFile.getPath() + "\n");
        sb.append(bwaFile.getPath() + " samse -r \"@RG\tID:" + readGroup.getId() + "\tLB:" + readGroup.getLibrary() + "\tSM:" + readGroup.getSample() + "\" " + bwaOutputFile.getPath() + " " + copiedFastqFile.getPath() + " > " + samFile.getPath() + " 2>> " + logFile.getPath());
        sb.append("\n\n");
        //create bam file from sam file
        sb.append("echo starting converting to bam >> " + logFile.getPath() + "\n");
        sb.append("date -n > " + logFile.getPath() + "\n");
        sb.append(samtoolsFile.getPath() + " import " + referenceIndex.getPath() + " " + samFile.getPath() + " " + bamFile.getPath() + " 2>> " + logFile.getPath());
        sb.append("\n\n");
        //sort the bam file
        sb.append("echo starting sorting of bam >> " + logFile.getPath() + "\n");
        sb.append("date -n > " + logFile.getPath() + "\n");
        sb.append(samtoolsFile.getPath() + " sort " + bamFile.getPath() + " " + bamFileSorted.getPath() + " 2>> " + logFile.getPath());
        sb.append("\n\n");
        //copy the bamFile back to the server
        sb.append("echo starting copying of bam back to the server >> " + logFile.getPath() + "\n");
        sb.append("date -n > " + logFile.getPath() + "\n");
        sb.append("cp " + bamFileSorted.getPath() + " " + fastqFile.getParentFile().getPath());
        sb.append("\n\n");
        //remove the tmp dir from the sge host
        sb.append("rm -rf " + tmpDir.getPath() + " 2>> " + logFile.getPath());
        sb.append("\n\n");
        sb.append("finished >> " + logFile.getPath() + "\n");
        sb.append("date -n > " + logFile.getPath() + "\n");


        command = sb.toString();

        sgeName = "BWA_" + baseName;

        mappingJobFile = new File(fastqFile.getParentFile(), baseName + ".sh");

        try {
            // Create file 
            FileWriter fstream = new FileWriter(mappingJobFile);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(command.toString());
            //Close the output stream
            out.close();
        } catch (Exception e) {//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }





    }

    @Override
    public String getCommand() {
        return command;
    }

    @Override
    public String getSGEName() {
        return sgeName;
    }
}
