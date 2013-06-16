/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bwa_picard_gatk_pipeline.readGroup;

import bwa_picard_gatk_pipeline.fileWrappers.FastQChunk;
import bwa_picard_gatk_pipeline.fileWrappers.FastQFile;
import bwa_picard_gatk_pipeline.sge.Job;
import bwa_picard_gatk_pipeline.sge.ilumina.BWA.mappingJob.BwaIluminaMappingJob;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author wim
 */
public class ReadGroupIluminaPE extends ReadGroupIlumina {
    
    private FastQFile firstReadsFastQFile;
    private FastQFile secondReadsFastQFile;
    
    private List<FastQChunk> firstReadsChunks;
    private List<FastQChunk> secondReadsChunks;
        
    

    @Override
    protected void prepareReadsForMapping() throws IOException {
        
        firstReadsChunks = new ArrayList<FastQChunk>();
        secondReadsChunks = new ArrayList<FastQChunk>();
        
        
        if(firstReadsFastQFile != null)
        {
            firstReadsFastQFile.initializeFastqReader();
            secondReadsFastQFile.initializeFastqReader();
            
            firstReadsChunks = firstReadsFastQFile.splitFastQFile(gc.getChunkSize(), readGroupOutputDir,id);
            secondReadsChunks = secondReadsFastQFile.splitFastQFile(gc.getChunkSize(), readGroupOutputDir,id);
        }  
        
    }

    @Override
    protected List<Job> createMappingJobs() throws IOException {
        
        List<Job> bwaMappingJobs = new ArrayList<Job>();
        
        for(int x = 0; x < firstReadsChunks.size(); x++) {
            
            File bamFile = new File(readGroupOutputDir, FilenameUtils.getBaseName(firstReadsChunks.get(x).getFastqFile().getPath()) + ".bam");
            bamChunks.add(bamFile);        
            BwaIluminaMappingJob bwaIluminaMappingJob = new BwaIluminaMappingJob(firstReadsChunks.get(x).getFastqFile(), secondReadsChunks.get(x).getFastqFile(), bamFile, this);      
            bwaMappingJobs.add(bwaIluminaMappingJob);
        }
       
        return bwaMappingJobs;
        
    }

    @Override
    protected Long getReadsInChunks() {
        
        Long counter = new Long(0);
        for (FastQChunk fastQChunk : firstReadsChunks) {
            counter = counter + fastQChunk.getRecordNr();
        }
        
        for (FastQChunk fastQChunk : secondReadsChunks) {
            counter = counter + fastQChunk.getRecordNr();
        }

        return counter;
    }  

    public FastQFile getFirstReadsFastQFile() {
        return firstReadsFastQFile;
    }

    public void setFirstReadsFastQFile(FastQFile firstReadsFastQFile) {
        this.firstReadsFastQFile = firstReadsFastQFile;
    }

    public FastQFile getSecondReadsFastQFile() {
        return secondReadsFastQFile;
    }

    public void setSecondReadsFastQFile(FastQFile secondReadsFastQFile) {
        this.secondReadsFastQFile = secondReadsFastQFile;
    }
    
    
    
    
    
    
}
