/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bwa_picard_gatk_pipeline.readGroup;

import bwa_picard_gatk_pipeline.fileWrappers.FastQChunk;
import bwa_picard_gatk_pipeline.fileWrappers.FastQFile;
import bwa_picard_gatk_pipeline.sge.Job;
import bwa_picard_gatk_pipeline.sge.ilumina.BWA.mappingJob.BwaIluminaMappingJob;
import bwa_picard_gatk_pipeline.sge.ilumina.BWAmem.mappingJob.BwaMemIluminaMappingJob;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author wim
 */
public class ReadGroupIluminaFragment extends ReadGroupIlumina {
    
    
    private FastQFile firstReadsFastQFile;    
    private File fastqChunkDir;
    private String fastqChunkDirFileNameToMatch;
    private String forward_identifier;    
    private Boolean pairsInterleaved = false;
    
    
    private List<FastQChunk> firstReadsChunks;
    

    @Override
    protected void prepareReadsForMapping() throws IOException {

        firstReadsChunks = new ArrayList<FastQChunk>();        


        if (firstReadsFastQFile != null) {
            firstReadsFastQFile.initializeFastqReader();           

            firstReadsChunks = firstReadsFastQFile.splitFastQFile(gc.getChunkSize(), readGroupOutputDir, id);
            
        }

        if (fastqChunkDir != null) {
            addExistingChunks();
        }

    }

    private void addExistingChunks() {

        String[] fastqExtensions = new String[]{"fastq", "fq", "gz"};
        List<File> fastqFiles = (List<File>) FileUtils.listFiles(fastqChunkDir, fastqExtensions, true);
        List<File> existingFirstFileChunks = new ArrayList<File>();
        
        for (File fastqFile : fastqFiles) {
            //skip files not matching the filename part, in the case that multiple readGroups or lanes are in the same fastqChunkDir
            if (!fastqFile.getName().contains(fastqChunkDirFileNameToMatch)) {
                continue;
            }

            if (fastqFile.getName().contains(forward_identifier)) {
                existingFirstFileChunks.add(fastqFile);
            }            
        }

        Collections.sort(existingFirstFileChunks);
        

        for (File firstFastQFileChunk : existingFirstFileChunks) {
            firstReadsChunks.add(new FastQChunk(firstFastQFileChunk));
        }

    }

    @Override
    protected List<Job> createMappingJobs() throws IOException {

        List<Job> bwaMappingJobs = new ArrayList<Job>();

        for (int x = 0; x < firstReadsChunks.size(); x++) {

            File bamFile = new File(readGroupOutputDir, FilenameUtils.getBaseName(firstReadsChunks.get(x).getFastqFile().getPath()) + ".bam");
            bamChunks.add(bamFile);
            Job bwaIluminaMappingJob = null;
            if (gc.getUseBWAMEM()) {
                bwaIluminaMappingJob = new BwaMemIluminaMappingJob(firstReadsChunks.get(x).getFastqFile(), null, bamFile, this, pairsInterleaved);
            } else {
                bwaIluminaMappingJob = new BwaIluminaMappingJob(firstReadsChunks.get(x).getFastqFile(), null, bamFile, this);
            }

            bwaMappingJobs.add(bwaIluminaMappingJob);
        }

        return bwaMappingJobs;

    }

    @Override
    protected Long getReadsInChunks() {

        Long counter = new Long(-1);

        //only try to count reads in chunks when there is no existing chunks dir. Otherwise return -1
        if (fastqChunkDir == null) 
        {
            for (FastQChunk fastQChunk : firstReadsChunks) {
                counter = counter + fastQChunk.getRecordNr();
            }            
        }

        return counter;
    }

    public FastQFile getFirstReadsFastQFile() {
        return firstReadsFastQFile;
    }

    public void setFirstReadsFastQFile(FastQFile firstReadsFastQFile) {
        this.firstReadsFastQFile = firstReadsFastQFile;
    }   

    public File getFastqChunkDir() {
        return fastqChunkDir;
    }

    public void setFastqChunkDir(File fastqChunkDir) {
        this.fastqChunkDir = fastqChunkDir;
    }

    public String getFastqChunkDirFileNameToMatch() {
        return fastqChunkDirFileNameToMatch;
    }

    public void setFastqChunkDirFileNameToMatch(String fastqChunkDirFileNameToMatch) {
        this.fastqChunkDirFileNameToMatch = fastqChunkDirFileNameToMatch;
    }

    public String getForward_identifier() {
        return forward_identifier;
    }

    public void setForward_identifier(String forward_identifier) {
        this.forward_identifier = forward_identifier;
    }

    public Boolean isPairsInterleaved() {
        return pairsInterleaved;
    }

    public void setPairsInterleaved(Boolean pairsInterleaved) {
        this.pairsInterleaved = pairsInterleaved;
    }
    
    
  
    
}
