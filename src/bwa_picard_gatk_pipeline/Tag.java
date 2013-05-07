/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bwa_picard_gatk_pipeline;

import bwa_picard_gatk_pipeline.enums.TagEnum;
import bwa_picard_gatk_pipeline.enums.TargetEnum;
import bwa_picard_gatk_pipeline.exceptions.JobFaillureException;
import bwa_picard_gatk_pipeline.exceptions.MappingException;
import bwa_picard_gatk_pipeline.exceptions.SplitFastQException;
import bwa_picard_gatk_pipeline.exceptions.TagProcessingException;
import bwa_picard_gatk_pipeline.exceptions.csFastaToFastqException;
import bwa_picard_gatk_pipeline.fileWrappers.CsFastaFilePair;
import bwa_picard_gatk_pipeline.fileWrappers.FastQChunk;
import bwa_picard_gatk_pipeline.fileWrappers.FastQFile;
import bwa_picard_gatk_pipeline.sge.BwaSolidMappingJob;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.sf.picard.sam.PicardGetReadCount;
import net.sf.picard.sam.PicardBamMerger;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.ggf.drmaa.DrmaaException;

/**
 *
 * @author Wim Spee
 */
public class Tag {
    
    private List<CsFastaFilePair> csfastaFiles;
    private List<FastQFile> fastQFiles;
    private List<FastQChunk> fastQChunks;
    
    private List<File> bamFiles;
    private File mergedBamFile;
    private TagEnum name;
    private ReadGroup readGroup;
    private File outputDirTag;    
    
    public void startProcessing() throws TagProcessingException {
        try {
            
            readGroup.getLog().append("Started processing of read group " + name.toString());
            deleteFastQChunks();
            
            fastQChunks = new ArrayList<FastQChunk>();

            //process the csfasta files if there are any
            if (csfastaFiles != null && !csfastaFiles.isEmpty()) {
                lookupCsFastaAndQualFiles();
                fastQChunks.addAll(convertCSFastaToFastQ());
            }
            if (fastQFiles != null &&!fastQFiles.isEmpty()) {
                   fastQChunks.addAll(splitFastQFiles());
            }
            
            if (readGroup.getGlobalConfiguration().getTargetEnum().getRank() >= TargetEnum.CHUNKS_BAM.getRank()) {
                //process the fastq files if there any (given in the properties file or converted from csfasta)
                
                //map the fastqChunks if there are any
                if (!getFastqChunks().isEmpty()) {
                    mapFastqFiles();
                }

                //merge the bam files
                if (readGroup.getGlobalConfiguration().getTargetEnum().getRank() >= TargetEnum.TAG_BAM.getRank()) {
                    if (!bamFiles.isEmpty()) {
                        mergeBamFiles();
                        checkAllReadsAreAcountedFor();
                    }                    
                }
            }
            
            
        } catch (csFastaToFastqException ex) {
            readGroup.getLog().append("Could not convert csFasta file " + ex.getMessage());
            throw new TagProcessingException("Error processing tag " + name.toString() + ": " + ex.getMessage());
        } catch (SplitFastQException ex) {
            readGroup.getLog().append("Could not split fastq file " + ex.getMessage());            
            deleteFastQChunks();
            throw new TagProcessingException("Error processing tag " + name.toString() + ": " + ex.getMessage());
        } catch (MappingException ex) {
            readGroup.getLog().append("Could not map fastq chunks: " + ex.getMessage());
            throw new TagProcessingException("Error processing tag " + name.toString() + ": " + ex.getMessage());
        } catch (IOException ex) {
            readGroup.getLog().append("Could not find csfasta or qual file: " + ex.getMessage());
            throw new TagProcessingException("Error processing tag " + name.toString() + ": " + ex.getMessage());
        } finally {
            
            
        }
        
    }
    
    public List<FastQChunk> getFastqChunks() {
               
        return fastQChunks;
    }
    
    public List<FastQChunk> convertCSFastaToFastQ() throws csFastaToFastqException {
        
        System.out.println("Converting csFasta to fastq ");        
        List<FastQChunk> fastQChunksConverted = new ArrayList<FastQChunk>();
        
        for (CsFastaFilePair csFastaFilePair : csfastaFiles) {
           try {
                fastQChunks = csFastaFilePair.convertToFastQ(outputDirTag, readGroup.getId(), readGroup.getGlobalConfiguration().getChunkSize());
                                
                readGroup.getLog().append("Converted csFastaFilePair to Fastq");
                readGroup.getLog().append(csFastaFilePair.toString());                
            } catch (IOException ex) {
                readGroup.getLog().append("Could not convert csFastaFilePair to Fastq");
                readGroup.getLog().append(csFastaFilePair.toString());
                readGroup.getLog().append("error: " + ex.getMessage());
                throw new csFastaToFastqException("Could not convert csFastaFilePair to Fastq: " + ex.getMessage());
            }
        }
        
        return fastQChunksConverted;
    }
    
    public List<FastQChunk> splitFastQFiles() throws SplitFastQException {
        readGroup.getLog().append("Start splitting fastqFiles");
        List<FastQChunk> fastQChunksConverted = new ArrayList<FastQChunk>();
        
        try {
            
            for (FastQFile fastQFile : fastQFiles) {
                
                //skip the fastq files that are already split
                if(fastQFile.getIsSplit()){continue;}
                
                
                fastQChunksConverted = fastQFile.splitFastQFile(readGroup.getGlobalConfiguration().getChunkSize(), outputDirTag);
                readGroup.getLog().append("Splitted fastQFile:");
                readGroup.getLog().append(fastQFile.toString());
            }
        } catch (FileNotFoundException ex) {
            readGroup.getLog().append("Could not split fastq file");
            readGroup.getLog().append("error: " + ex.getMessage());
            throw new SplitFastQException("Could not split fastq file: " + ex.getMessage());
        } catch (IOException ex) {
            readGroup.getLog().append("Could not split fastq file");
            readGroup.getLog().append("error: " + ex.getMessage());
            throw new SplitFastQException("Could not split fastq file: " + ex.getMessage());
        } catch (SplitFastQException ex) {
            readGroup.getLog().append("Could not split fastq file");
            readGroup.getLog().append("error: " + ex.getMessage());
            throw ex;
            
        }
        
        return fastQChunksConverted;
    }
    
    public List<BwaSolidMappingJob> createMappingJobs() throws IOException {
        List<BwaSolidMappingJob> bwaMappingJobs = new ArrayList<BwaSolidMappingJob>();

        //initialize the bam files list if not set by json
        if (bamFiles == null) {
            bamFiles = new ArrayList<File>();
        }
        
        
        for (FastQChunk fastQChunk : getFastqChunks()) {
            File bamFile = new File(outputDirTag, FilenameUtils.getBaseName(fastQChunk.getFastqFile().getPath()) + ".bam");
            bamFiles.add(bamFile);
            
            BwaSolidMappingJob bwaMappingJob = new BwaSolidMappingJob(fastQChunk.getFastqFile(), bamFile, readGroup);
            bwaMappingJobs.add(bwaMappingJob);
        }
        return bwaMappingJobs;
    }
    
    public void submitMappingJobs(List<BwaSolidMappingJob> mappingJobs) throws DrmaaException {
        for (BwaSolidMappingJob bwaMappingJob : mappingJobs) {
            bwaMappingJob.submit();
        }
        
    }
    
    public void mapFastqFiles() throws MappingException {
        
        System.out.println("Starting submitting of mapping jobs");
        List<BwaSolidMappingJob> bwaMappingJobs = new ArrayList<BwaSolidMappingJob>();
        try {
            bwaMappingJobs = createMappingJobs();
        } catch (IOException ex) {
            throw new MappingException(ex.getMessage());
        }
        
        if (readGroup.getGlobalConfiguration().getOffline()) {
            mapOffline(bwaMappingJobs);
            
            
        } else {
            
            try {
                submitMappingJobs(bwaMappingJobs);
            } catch (DrmaaException ex) {
                throw new MappingException("Cannot submit job : " + ex.getMessage());
                
            }
            try {
                waitForMappingJobs(bwaMappingJobs);
            } catch (DrmaaException ex) {
                throw new MappingException("Cannot determine status of mapping jobs : " + ex.getMessage());
            } catch (JobFaillureException ex) {
                throw new MappingException("A mapping job failed : " + ex.getMessage());
            }
        }
        
       
      
        
    }
    
    private void mapOffline(List<BwaSolidMappingJob> bwaMappingJobs) throws MappingException {
        
        for (BwaSolidMappingJob bwaSolidMappingJob : bwaMappingJobs) {
            try {
                bwaSolidMappingJob.executeOffline();
            } catch (IOException ex) {
                throw new MappingException("A offline mapping job failed : " + ex.getMessage());
            } catch (InterruptedException ex) {
                throw new MappingException("A offline mapping job failed : " + ex.getMessage());
            }
        }
        
        
        
    }
    
    private void waitForMappingJobs(List<BwaSolidMappingJob> bwaMappingJobs) throws DrmaaException, JobFaillureException {
        
        for (BwaSolidMappingJob bwaMappingJob : bwaMappingJobs) {
            bwaMappingJob.waitFor();
        }
        
    }
    
    private void mergeBamFiles() {
        PicardBamMerger picardBamMerger = new PicardBamMerger();
        try {
            mergedBamFile = picardBamMerger.mergeBamFilesUsingPicard(bamFiles, readGroup.getGlobalConfiguration().getTmpDir());
        } catch (IOException ex) {
            readGroup.getLog().append("Could not merge bam files in dir  " + outputDirTag.getAbsolutePath() + " :" + ex.getMessage());
        }
        
    }
    
    private void deleteFastQChunks() {
        
        readGroup.getLog().append("Deleting fastq chunk in dir " + outputDirTag.getAbsolutePath());
        FileFilter fileFilter = new WildcardFileFilter("*_chunk*.fastq");
        File[] files = outputDirTag.listFiles(fileFilter);
        for (int i = 0; i < files.length; i++) {
            files[i].delete();
        }
    }
    
    public List<FastQFile> getFastQFiles() {
        return fastQFiles;
    }
    
    public File getMergedBamFile() {
        return mergedBamFile;
    }
    
    public TagEnum getName() {
        return name;
    }
    
    public void setName(TagEnum name) {
        this.name = name;
    }
    
    public List<CsFastaFilePair> getCsfastaFiles() {
        return csfastaFiles;
    }
    
    public void setCsfastaFiles(List<CsFastaFilePair> csfastaFiles) {
        this.csfastaFiles = csfastaFiles;
    }
    
    public ReadGroup getReadGroup() {
        return readGroup;
    }
    
    public void setReadGroup(ReadGroup readGroup) {
        this.readGroup = readGroup;
    }
    
    void createOutputDir(File readGroupOutputDir) {
        outputDirTag = new File(readGroupOutputDir, name.toString());
        outputDirTag.mkdir();
    }
    
    private void lookupCsFastaAndQualFiles() throws IOException {
        for (CsFastaFilePair csFastaFilePair : csfastaFiles) {
            csFastaFilePair.lookupCsFastaFile();
            csFastaFilePair.lookupQualFile();
        }
    }
    
    
    private Long getReadsInChunks()
    {
        Long counter = new Long(0);
        for(FastQChunk fastQChunk: fastQChunks)
        {
            counter = counter + fastQChunk.getRecordNr();
        }
        
        return counter;
    
    }
    
    private void checkAllReadsAreAcountedFor() throws MappingException {
        
        Long fastQRecords = getReadsInChunks();
        PicardGetReadCount picardGetReadCount = new PicardGetReadCount();
        Long readInBamFile = picardGetReadCount.getReadCount(mergedBamFile);
        
        
        
        if (fastQRecords.equals(readInBamFile)) {
            readGroup.getLog().append("Merged bam file and fastq contain same amount of reads: " + fastQRecords.toString());
        } else {
             readGroup.getLog().append("Merged bam file and fastq do not contain same amount of reads. Fastq: "+fastQRecords.toString()+" Bam: "+readInBamFile.toString());
             throw new MappingException("Merged bam file and fastq do not contain same amount of reads. Fastq: "+fastQRecords.toString()+" Bam: "+readInBamFile.toString());
            
        }
        
        
        
    }
}
