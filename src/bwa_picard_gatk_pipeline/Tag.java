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
import bwa_picard_gatk_pipeline.fileWrappers.FastQFile;
import bwa_picard_gatk_pipeline.sge.BwaSolidMappingJob;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.sf.picard.sam.PicardBamIndexStats;
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
    private List<File> bamFiles;
    private File mergedBamFile;
    private TagEnum name;
    private ReadGroup readGroup;
    private File outputDirTag;
    private Long fastqRecordCounter;
    
    public void startProcessing() throws TagProcessingException {
        try {
            
            readGroup.getLog().append("Started processing of read group " + name.toString());
            deleteFastQChunks();

            //process the csfasta files if there are any
            if (!csfastaFiles.isEmpty()) {
                lookupCsFastaAndQualFiles();
                fastqRecordCounter = convertCSFastaToFastQ();
            }
            
            if (readGroup.getGlobalConfiguration().getTargetEnum().getRank() >= TargetEnum.CHUNKS_BAM.getRank()) {
                //process the fastq files if there any (given in the properties file or converted from csfasta)
                if (!fastQFiles.isEmpty()) {
                    splitFastQFiles();
                }
                //map the fastqChunks if there are any
                if (!getSplitFastQFiles().isEmpty()) {
                    mapFastqFiles();
                }

                //merge the bam files
                if (readGroup.getGlobalConfiguration().getTargetEnum().getRank() >= TargetEnum.TAG_BAM.getRank()) {
                    if (!bamFiles.isEmpty()) {
                        mergeBamFiles();
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
    
    public List<FastQFile> getSplitFastQFiles() {
        List<FastQFile> splitFastQFiles = new ArrayList<FastQFile>();
        
        for (FastQFile fastQFile : fastQFiles) {
            splitFastQFiles.addAll(fastQFile.getSplitFastQFiles());
        }
        
        return splitFastQFiles;
    }
    
    public Long convertCSFastaToFastQ() throws csFastaToFastqException {
        
        System.out.println("Converting csFasta to fastq ");
        
        Long fastqRecordCounter = new Long(0);

        //initialize the fastq list if no fastqFiles were set on the list from json
        if (fastQFiles == null) {
            fastQFiles = new ArrayList<FastQFile>();
        }
        
        
        for (CsFastaFilePair csFastaFilePair : csfastaFiles) {
            FastQFile fastqFile;
            try {
                fastqFile = csFastaFilePair.convertToFastQ(outputDirTag, readGroup.getId(), fastqRecordCounter);
                fastqFile.setTag(name);
                fastQFiles.add(fastqFile);
                
                readGroup.getLog().append("Converted csFastaFilePair to Fastq");
                readGroup.getLog().append(csFastaFilePair.toString());
                readGroup.getLog().append(fastqFile.toString());
                
                
            } catch (IOException ex) {
                readGroup.getLog().append("Could not convert csFastaFilePair to Fastq");
                readGroup.getLog().append(csFastaFilePair.toString());
                readGroup.getLog().append("error: " + ex.getMessage());
                throw new csFastaToFastqException("Could not convert csFastaFilePair to Fastq: " + ex.getMessage());
                
            }
        }
        
        return fastqRecordCounter;
    }
    
    public void splitFastQFiles() throws SplitFastQException {
        readGroup.getLog().append("Start splitting fastqFiles");
        
        try {
            
            for (FastQFile fastQFile : fastQFiles) {
                
                fastQFile.splitFastQFile(readGroup.getGlobalConfiguration().getChunkSize(), outputDirTag);
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
    }
    
    public List<BwaSolidMappingJob> createMappingJobs() throws IOException {
        List<BwaSolidMappingJob> bwaMappingJobs = new ArrayList<BwaSolidMappingJob>();

        //initialize the bam files list if not set by json
        if (bamFiles == null) {
            bamFiles = new ArrayList<File>();
        }
        
        
        for (FastQFile splitFastQFile : getSplitFastQFiles()) {
            File bamFile = new File(outputDirTag, FilenameUtils.getBaseName(splitFastQFile.getFastqFile().getPath()) + ".bam");
            bamFiles.add(bamFile);
            
            BwaSolidMappingJob bwaMappingJob = new BwaSolidMappingJob(splitFastQFile.getFastqFile(), bamFile, readGroup);
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
        
        mergeBamFiles();        
        checkAllReadsAreAcountedFor();
        
    }
    
    private void mapOffline(List<BwaSolidMappingJob> bwaMappingJobs) throws MappingException {
        
        for (BwaSolidMappingJob bwaSolidMappingJob : bwaMappingJobs) {
            try {
                bwaSolidMappingJob.mapOffline();
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
            mergedBamFile = picardBamMerger.mergeBamFilesUsingPicard(bamFiles);
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
    
    private void checkAllReadsAreAcountedFor() throws MappingException {
        
        
        PicardBamIndexStats picardBamIndexStats = new PicardBamIndexStats();
        Long readInBamFile = picardBamIndexStats.getReadCount(mergedBamFile);
        
        if (fastqRecordCounter.equals(readInBamFile)) {
            readGroup.getLog().append("Merged bam file and fastq contain same amount of reads: " + fastqRecordCounter);
        } else {
             readGroup.getLog().append("Merged bam file and fastq do not contain same amount of reads. Fastq: "+fastqRecordCounter+" Bam: "+readInBamFile);
             throw new MappingException("Merged bam file and fastq do not contain same amount of reads. Fastq: "+fastqRecordCounter+" Bam: "+readInBamFile);
            
        }
        
        
        
    }
}
