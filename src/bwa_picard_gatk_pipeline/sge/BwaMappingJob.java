/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bwa_picard_gatk_pipeline.sge;

import bwa_picard_gatk_pipeline.ReadGroup;
import java.io.File;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author Wim Spee
 */
public class BwaMappingJob extends Job{

    
    String command;
    String sgeName;
    
    public BwaMappingJob(File fastqFile, File referenceFile, ReadGroup readGroup) {
        
        String baseName = FilenameUtils.getBaseName(fastqFile.getPath());
        File tmpDir = new File("/tmp/"+baseName);
        File copiedFastqFile = new File(tmpDir, fastqFile.getName());
        File bwaOutputFile = new File(tmpDir, baseName+".out");
        File samFile = new File(tmpDir, baseName+".sam");
        File bamFile = new File(tmpDir, baseName+".bam");
        File bamFileSorted = new File(tmpDir, baseName+ "_sorted.bam");
        
        File referenceIndex = new File(referenceFile.getPath()+".fai");
        
        File parentDir = fastqFile.getParentFile();        
        File logFile = new File(parentDir, baseName+".log");
        File bwaFile = new File("/usr/local/bwa/0.5.9/bwa");
        String bwaOptions = "aln -c -l 25 -k 2 -n 10";
        File samtoolsFile = new File("/usr/local/samtools/samtools");
        
        
        StringBuilder sb = new StringBuilder();
        
        //add sge hostname and date information to log
        sb.append("uname -n > "+logFile.getPath());
        sb.append("\n");
        sb.append("date -n > "+logFile.getPath());
        sb.append("\n");
        //create a tmp dir
        sb.append("mkdir"+ tmpDir);
        sb.append("\n");
        //copy the fastQFile to the tmp dir
        sb.append("echo starting copying of fastq file >> "+logFile.getPath()+"\n");
        sb.append("cp " +fastqFile.getPath()+ " "+tmpDir.getPath() );
        sb.append("\n");
        //map using bwa
        sb.append("echo starting mapping of fastq file >> "+logFile.getPath()+"\n");
        sb.append("date -n > "+logFile.getPath());
        sb.append(bwaFile.getPath() + " " + bwaOptions + " " + referenceFile.getPath() + " "+ copiedFastqFile.getPath() + " > "+ bwaOutputFile.getPath() +" 2>> "+logFile.getPath());
        sb.append("\n");
        //create sam file from output
        sb.append("echo starting converting to sam >> "+logFile.getPath()+"\n");
        sb.append("date -n > "+logFile.getPath());
        sb.append(bwaFile.getPath() + " samse -r \"@RG\tID:"+readGroup.getReadGroupId()+"\tLB:"+readGroup.getLibrary()+"\tSM:"+readGroup.getSample()+"\" "+bwaOutputFile.getPath()+" "+copiedFastqFile.getPath()+" > "+samFile.getPath() +" 2>> "+logFile.getPath());
        sb.append("\n");
        //create bam file from sam file
        sb.append("echo starting converting to bam >> "+logFile.getPath()+"\n");
        sb.append("date -n > "+logFile.getPath());
        sb.append(samtoolsFile.getPath() +" import "+referenceIndex.getPath() + " " + samFile.getPath() + " " + bamFile.getPath()+" 2>> " +logFile.getPath());
        sb.append("\n");
        //sort the bam file
        sb.append("echo starting sorting of bam >> "+logFile.getPath()+"\n");
        sb.append("date -n > "+logFile.getPath());
        sb.append(samtoolsFile.getPath() + " sort "+bamFile.getPath() + " " + bamFileSorted.getPath()+" 2>> " +logFile.getPath());
        sb.append("\n");
        //copy the bamFile back to the server
        sb.append("echo starting copying of bam back to the server >> "+logFile.getPath()+"\n");
        sb.append("date -n > "+logFile.getPath());
        sb.append("cp " + bamFileSorted.getPath() + " " + fastqFile.getParentFile().getPath() );
        sb.append("\n");
        //remove the tmp dir from the sge host
        sb.append("rm -rf " +tmpDir.getPath()+" 2>> " +logFile.getPath() );
        sb.append("finished >> "+logFile.getPath()+"\n");
        sb.append("date -n > "+logFile.getPath());
        
        
        command = sb.toString();
        
        sgeName = "BWA_"+baseName;        
        
    }

    
    
    
    
    @Override
    public String getCommand() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getSGEName() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
    
    
    
}
