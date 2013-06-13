/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bwa_picard_gatk_pipeline.readGroup;

import bwa_picard_gatk_pipeline.fileWrappers.CsFastaFilePair;
import bwa_picard_gatk_pipeline.fileWrappers.FastQChunk;
import bwa_picard_gatk_pipeline.fileWrappers.FastQFile;
import bwa_picard_gatk_pipeline.sge.Job;
import bwa_picard_gatk_pipeline.sge.solid.BWA.mappingJob.BwaSolidMappingJob;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author wim
 */
public class ReadGroupSolidFragment extends ReadGroupSolid{

           
    //the possible input for the processing of the tag
    private CsFastaFilePair csfastaFilePair;    
    private FastQFile fastQFile;
    private List<FastQChunk> fastQChunks;
  

    @Override
    protected void prepareReadsForMapping() throws IOException {
        
        fastQChunks = new ArrayList<FastQChunk>();
        
        if (csfastaFilePair != null) {
             lookupCsFastaAndQualFiles(csfastaFilePair);        //lookup the csfasta and qual files for given csfasta paths
             convertCSFastaToFastQChunks(csfastaFilePair, fastQChunks);      //convert csfasta to fastq chunks
        }
        
        if (fastQFile != null) {
           convertFastQFilesToFastQChunks(fastQFile, fastQChunks);   //if fastq files were given split them to fastq chunks
        }   
        
    }   

    

    @Override
    protected List<Job> createMappingJobs() throws IOException {
        List<Job> bwaMappingJobs = new ArrayList<Job>();

        for (FastQChunk fastQChunk : fastQChunks) {
            File bamFile = new File(readGroupOutputDir, FilenameUtils.getBaseName(fastQChunk.getFastqFile().getPath()) + ".bam");
            bamChunks.add(bamFile);

            Job bwaMappingJob = new BwaSolidMappingJob(fastQChunk.getFastqFile(), bamFile, this);
            bwaMappingJobs.add(bwaMappingJob);
        }
        return bwaMappingJobs;
    }
    
    @Override
    protected Long getReadsInChunks() {
        Long counter = new Long(0);
        for (FastQChunk fastQChunk : fastQChunks) {
            counter = counter + fastQChunk.getRecordNr();
        }

        return counter;
    }

    //getters and setters 
    public CsFastaFilePair getCsfastaFilePair() {
        return csfastaFilePair;
    }

    public void setCsfastaFilePair(CsFastaFilePair csfastaFilePair) {
        this.csfastaFilePair = csfastaFilePair;
    }

    public FastQFile getFastQFile() {
        return fastQFile;
    }

    public void setFastQFile(FastQFile fastQFile) {
        this.fastQFile = fastQFile;
    }

    
    
    

    

    
}
