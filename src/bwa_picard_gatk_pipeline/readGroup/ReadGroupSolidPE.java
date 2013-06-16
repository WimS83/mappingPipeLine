/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bwa_picard_gatk_pipeline.readGroup;

import bwa_picard_gatk_pipeline.enums.TagEnum;
import bwa_picard_gatk_pipeline.exceptions.JobFaillureException;
import bwa_picard_gatk_pipeline.fileWrappers.CsFastaFilePair;
import bwa_picard_gatk_pipeline.fileWrappers.FastQChunk;
import bwa_picard_gatk_pipeline.fileWrappers.FastQFile;
import bwa_picard_gatk_pipeline.sge.Job;
import bwa_picard_gatk_pipeline.sge.solid.BWA.mappingJob.BwaSolidMappingJob;
import bwa_picard_gatk_pipeline.sge.picard.mergeBAM.PicardMergeBamJob;
import bwa_picard_gatk_pipeline.sge.solid.BWA.pairReads.PiclPairReadsJob;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FilenameUtils;
import org.ggf.drmaa.DrmaaException;

/**
 *
 * @author wim
 */
public class ReadGroupSolidPE extends ReadGroupSolid{

   
    //the possible input for the processing of the tag
    private CsFastaFilePair csfastaFileF3;    
    private CsFastaFilePair csfastaFileF5;  
    private FastQFile fastQFileF3;
    private FastQFile fastQFileF5;
    private List<FastQChunk> fastQChunksF3;
    private List<FastQChunk> fastQChunksF5;
    
    //intermediate output
    private List<File> bamChunksF3;
    private List<File> bamChunksF5;
    private File bamF3;
    private File bamF5;
    
            
    
    
    
    @Override
    protected void prepareReadsForMapping() throws IOException {
        
       fastQChunksF3 = new ArrayList<FastQChunk>();
       fastQChunksF5 = new ArrayList<FastQChunk>();
       bamChunksF3 = new ArrayList<File>();
       bamChunksF5 = new ArrayList<File>();
        
        if(csfastaFileF3 != null)
        {
             lookupCsFastaAndQualFiles(csfastaFileF3 );        //lookup the csfasta and qual files for given csfasta paths
             convertCSFastaToFastQChunks(csfastaFileF3, fastQChunksF3);      //convert csfasta to fastq chunks
        }
        
        if(csfastaFileF5 != null)
        {
              lookupCsFastaAndQualFiles(csfastaFileF5 );        //lookup the csfasta and qual files for given csfasta paths
              convertCSFastaToFastQChunks(csfastaFileF5, fastQChunksF5);      //convert csfasta to fastq chunks
        }            
               
        if(fastQFileF3 != null)
        {
            fastQFileF3.initializeFastqReader();
            convertFastQFilesToFastQChunks(fastQFileF3, fastQChunksF3);   //if fastq files were given split them to fastq chunks
        }
        
        if(fastQFileF5 != null)
        {
            fastQFileF5.initializeFastqReader();
            convertFastQFilesToFastQChunks(fastQFileF5, fastQChunksF5);   //if fastq files were given split them to fastq chunks
        }  
    }   
    

   

   

    @Override
    protected List<Job> createMappingJobs() throws IOException {
        List<Job> bwaMappingJobs = new ArrayList<Job>();

        for (FastQChunk fastQChunk : fastQChunksF3) {
            File bamFile = new File(readGroupOutputDir, FilenameUtils.getBaseName(fastQChunk.getFastqFile().getPath()) + ".bam");
            bamChunksF3.add(bamFile);

            Job bwaMappingJob = new BwaSolidMappingJob(fastQChunk.getFastqFile(), bamFile, this);
            bwaMappingJobs.add(bwaMappingJob);
        }
        
        for (FastQChunk fastQChunk : fastQChunksF5) {
            File bamFile = new File(readGroupOutputDir, FilenameUtils.getBaseName(fastQChunk.getFastqFile().getPath()) + ".bam");
            bamChunksF5.add(bamFile);

            Job bwaMappingJob = new BwaSolidMappingJob(fastQChunk.getFastqFile(), bamFile, this);
            bwaMappingJobs.add(bwaMappingJob);
        }
        return bwaMappingJobs;
    }
    
    @Override
    protected Long getReadsInChunks() {
        Long counter = new Long(0);
        for (FastQChunk fastQChunk : fastQChunksF3) {
            counter = counter + fastQChunk.getRecordNr();
        }
        
        for (FastQChunk fastQChunk : fastQChunksF5) {
            counter = counter + fastQChunk.getRecordNr();
        }

        return counter;
    }    

    @Override
    protected void mergeBamChunks() throws IOException, InterruptedException, DrmaaException, JobFaillureException {
        
        
        File mergedBamDir = new File(readGroupOutputDir, "MergedBam");
        mergedBamDir.mkdir();
        
        File picardMergeSam = new File(gc.getPicardDirectory(), "MergeSamFiles.jar");
        
        if (!bamChunksF3.isEmpty()) {
            bamF3 = new File(mergedBamDir, id+ "_" +"F3"+  ".bam");
            PicardMergeBamJob picardMergeBamJobF3 = new PicardMergeBamJob(bamChunksF3, bamF3, null, gc.getTmpDir(), picardMergeSam);
            picardMergeBamJobF3.executeOffline();
            picardMergeBamJobF3.waitForOfflineExecution();
            runQualimap(bamF3);
        } 
        
        if (!bamChunksF5.isEmpty()) {
             bamF5 = new File(mergedBamDir, id+ "_" +"F5"+ ".bam");    
             PicardMergeBamJob picardMergeBamJobF5 = new PicardMergeBamJob(bamChunksF5, bamF5, null, gc.getTmpDir(), picardMergeSam);
             picardMergeBamJobF5.executeOffline();        
             picardMergeBamJobF5.waitForOfflineExecution();   
             runQualimap(bamF5);
        }         
        
        readGroupBam = new File(mergedBamDir, id+ ".bam");
        
        PiclPairReadsJob piclPairReadsJob = new PiclPairReadsJob(bamF3, bamF5, readGroupBam, this, "fedor35", TagEnum.SOLID_F5);

        if (gc.getOffline()) {
            piclPairReadsJob.executeOffline();
            piclPairReadsJob.waitForOfflineExecution();
        } else {
            piclPairReadsJob.submit();           
            piclPairReadsJob.waitFor();
        }
        
    }

    //getters and setters 
    public CsFastaFilePair getCsfastaFileF3() {
        return csfastaFileF3;
    }

    public void setCsfastaFileF3(CsFastaFilePair csfastaFileF3) {
        this.csfastaFileF3 = csfastaFileF3;
    }

    public CsFastaFilePair getCsfastaFileF5() {
        return csfastaFileF5;
    }

    public void setCsfastaFileF5(CsFastaFilePair csfastaFileF5) {
        this.csfastaFileF5 = csfastaFileF5;
    }

    public FastQFile getFastQFilesF3() {
        return fastQFileF3;
    }

    public void setFastQFilesF3(FastQFile fastQFilesF3) {
        this.fastQFileF3 = fastQFilesF3;
    }

    public FastQFile getFastQFilesF5() {
        return fastQFileF5;
    }

    public void setFastQFilesF5(FastQFile fastQFilesF5) {
        this.fastQFileF5 = fastQFilesF5;
    }

    public FastQFile getFastQFileF3() {
        return fastQFileF3;
    }

    public void setFastQFileF3(FastQFile fastQFileF3) {
        this.fastQFileF3 = fastQFileF3;
    }

    public FastQFile getFastQFileF5() {
        return fastQFileF5;
    }

    public void setFastQFileF5(FastQFile fastQFileF5) {
        this.fastQFileF5 = fastQFileF5;
    }
    
    
   

    public File getBamF3() {
        return bamF3;
    }

    public void setBamF3(File bamF3) {
        this.bamF3 = bamF3;
    }

    public File getBamF5() {
        return bamF5;
    }

    public void setBamF5(File bamF5) {
        this.bamF5 = bamF5;
    }
    
    
    
    
    
    
    
    
}
