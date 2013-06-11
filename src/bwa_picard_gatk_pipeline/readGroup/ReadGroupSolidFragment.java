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
    private List<CsFastaFilePair> csfastaFiles;    
    private List<FastQFile> fastQFiles;
    private List<FastQChunk> fastQChunks;
  

    @Override
    protected void prepareReadsForMapping() throws IOException {
        
        if (csfastaFiles == null) {
            csfastaFiles = new ArrayList<CsFastaFilePair>();
        }
        
        if (fastQFiles == null) {
            fastQFiles = new ArrayList<FastQFile>();
        }
        
        if (fastQChunks == null) {
            fastQChunks = new ArrayList<FastQChunk>();
        }
        
        lookupCsFastaAndQualFiles(csfastaFiles);        //lookup the csfasta and qual files for given csfasta paths
        convertCSFastaToFastQChunks(csfastaFiles, fastQChunks);      //convert csfasta to fastq chunks
        convertFastQFilesToFastQChunks(fastQFiles, fastQChunks);   //if fastq files were given split them to fastq chunks
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

    public List<CsFastaFilePair> getCsfastaFiles() {
        return csfastaFiles;
    }

    public void setCsfastaFiles(List<CsFastaFilePair> csfastaFiles) {
        this.csfastaFiles = csfastaFiles;
    }

    public List<FastQFile> getFastQFiles() {
        return fastQFiles;
    }

    public void setFastQFiles(List<FastQFile> fastQFiles) {
        this.fastQFiles = fastQFiles;
    }
    
    

    

    
}
