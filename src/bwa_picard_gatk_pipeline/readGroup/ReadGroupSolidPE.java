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
import bwa_picard_gatk_pipeline.sge.solid.BWA.mergeBAM.PicardMergeBamJob;
import bwa_picard_gatk_pipeline.sge.solid.BWA.pairReads.PiclPairReadsJob;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FilenameUtils;
import org.ggf.drmaa.DrmaaException;

/**
 *
 * @author wim
 */
public class ReadGroupSolidPE extends ReadGroupSolid{

   
    //the possible input for the processing of the tag
    private List<CsFastaFilePair> csfastaFilesF3;    
    private List<CsFastaFilePair> csfastaFilesF5;  
    private List<FastQFile> fastQFilesF3;
    private List<FastQFile> fastQFilesF5;
    private List<FastQChunk> fastQChunksF3;
    private List<FastQChunk> fastQChunksF5;
    
    //intermediate output
    private List<File> bamChunksF3;
    private List<File> bamChunksF5;
    private File bamF3;
    private File bamF5;
    
            
    
    
    
    @Override
    protected void prepareReadsForMapping() throws IOException {
        
        if (csfastaFilesF3 == null) {
            csfastaFilesF3 = new ArrayList<CsFastaFilePair>();
        }
        
         if (csfastaFilesF5 == null) {
            csfastaFilesF5 = new ArrayList<CsFastaFilePair>();
        }
        
        if (fastQFilesF3 == null) {
            fastQFilesF3 = new ArrayList<FastQFile>();
        }
        
        if (fastQFilesF5 == null) {
            fastQFilesF5 = new ArrayList<FastQFile>();
        }
        
        if (fastQChunksF3 == null) {
            fastQChunksF3 = new ArrayList<FastQChunk>();
        }
        
        if (fastQChunksF5 == null) {
            fastQChunksF5 = new ArrayList<FastQChunk>();
        }
        
        lookupCsFastaAndQualFiles(csfastaFilesF3 );        //lookup the csfasta and qual files for given csfasta paths
        lookupCsFastaAndQualFiles(csfastaFilesF5 );        //lookup the csfasta and qual files for given csfasta paths
        convertCSFastaToFastQChunks(csfastaFilesF3, fastQChunksF3);      //convert csfasta to fastq chunks
        convertCSFastaToFastQChunks(csfastaFilesF5, fastQChunksF5);      //convert csfasta to fastq chunks
        convertFastQFilesToFastQChunks(fastQFilesF3, fastQChunksF3);   //if fastq files were given split them to fastq chunks
        convertFastQFilesToFastQChunks(fastQFilesF5, fastQChunksF5);   //if fastq files were given split them to fastq chunks
       
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
        
        if (bamChunksF3.isEmpty()) {
            return;
        } //return if there are no bamchunks        
        
        File mergedBamDir = new File(readGroupOutputDir, "MergedBam");
        mergedBamDir.mkdir();
        
        
        bamF3 = new File(mergedBamDir, id+ "_" +"F3"+ "_"+ ".bam");        
        bamF5 = new File(mergedBamDir, id+ "_" +"F5"+ "_"+ ".bam");    
        
        File picardMergeSam = new File(gc.getPicardDirectory(), "MergeSamFiles.jar");

        PicardMergeBamJob picardMergeBamJobF3 = new PicardMergeBamJob(bamChunksF3, bamF3, null, gc.getTmpDir(), picardMergeSam);
        PicardMergeBamJob picardMergeBamJobF5 = new PicardMergeBamJob(bamChunksF5, bamF5, null, gc.getTmpDir(), picardMergeSam);
        
        picardMergeBamJobF3.executeOffline();
        picardMergeBamJobF5.executeOffline();
        picardMergeBamJobF3.waitForOfflineExecution();
        picardMergeBamJobF5.waitForOfflineExecution();
        
        readGroupBam = new File(mergedBamDir, id+ "_" + ".bam");
        
        PiclPairReadsJob piclPairReadsJob = new PiclPairReadsJob(bamF3, bamF5, readGroupBam, this, "fedor35", TagEnum.SOLID_F5);

        if (gc.getOffline()) {
            piclPairReadsJob.executeOffline();
            piclPairReadsJob.waitForOfflineExecution();
        } else {
            piclPairReadsJob.submit();           
            piclPairReadsJob.waitFor();
        }
        
    }
    
    
    
    
    
    
    
    
    
}
