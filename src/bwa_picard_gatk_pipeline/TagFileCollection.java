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
import bwa_picard_gatk_pipeline.exceptions.csFastaToFastqException;
import bwa_picard_gatk_pipeline.fileWrappers.CsFastaFilePair;
import bwa_picard_gatk_pipeline.fileWrappers.FastQFile;
import bwa_picard_gatk_pipeline.sge.BwaMappingJob;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.picard.sam.PicardBamMerger;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.ggf.drmaa.DrmaaException;

/**
 *
 * @author Wim Spee
 */
public class TagFileCollection {

    private List<CsFastaFilePair> csFastaFilePairs;
    private List<FastQFile> fastQFiles;
    private List<File> bamFiles;
    private File mergedBamFile;
    private TagEnum tagEnum;
    private ReadGroup readGroup;
    private File outputDirTag;

    public TagFileCollection(TagEnum tagEnum, ReadGroup readGroup) {
        this.tagEnum = tagEnum;
        this.readGroup = readGroup;


        outputDirTag = new File(readGroup.getOutputDir(), tagEnum.toString());
        outputDirTag.mkdir();

        csFastaFilePairs = new ArrayList<CsFastaFilePair>();
        fastQFiles = new ArrayList<FastQFile>();
        bamFiles = new ArrayList<File>();
    }

    public void startProcessing() {
        try {

            readGroup.getLog().append("Started processing of read group " + tagEnum.toString());
            deleteFastQChunks();

            //process the csfasta files if there are any
            if (!csFastaFilePairs.isEmpty()) {
                convertCSFastaToFastQ();
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
                 if (readGroup.getGlobalConfiguration().getTargetEnum().getRank() >= TargetEnum.TAG_BAM.getRank())
                 {                        
                    if (!bamFiles.isEmpty()) {
                        mergeBamFiles();
                    }
                 
                 }
            }


        } catch (csFastaToFastqException ex) {
            readGroup.getLog().append("Could not convert csFasta file " + ex.getMessage());
        } catch (SplitFastQException ex) {
            readGroup.getLog().append("Could not split fastq file " + ex.getMessage());
            deleteFastQChunks();
        } catch (MappingException ex) {
            readGroup.getLog().append("Could not map fastq chunks: " + ex.getMessage());
        } finally {

            readGroup.getLog().close();
        }

    }

    public List<FastQFile> getSplitFastQFiles() {
        List<FastQFile> splitFastQFiles = new ArrayList<FastQFile>();

        for (FastQFile fastQFile : fastQFiles) {
            splitFastQFiles.addAll(fastQFile.getSplitFastQFiles());
        }

        return splitFastQFiles;
    }

    public CsFastaFilePair addCSFastaFile(String csFastaFilePath) throws IOException {
        CsFastaFilePair csFastaFilePair = new CsFastaFilePair();
        csFastaFilePair.setCsFastaFile(new File(csFastaFilePath));
        csFastaFilePair.setTag(tagEnum);

        csFastaFilePair.lookupQualFile();
        csFastaFilePairs.add(csFastaFilePair);

        return csFastaFilePair;
    }

    public FastQFile addFastQFile(String filePath) throws IOException {

        File fastqFile = new File(filePath);
        if (!fastqFile.canRead()) {
            throw new IOException("Cannot read fastq file " + fastqFile.getPath());
        }

        FastQFile fastQFile = new FastQFile(new File(filePath));
        fastQFile.setTag(tagEnum);

        fastQFiles.add(fastQFile);

        return fastQFile;
    }

    public void convertCSFastaToFastQ() throws csFastaToFastqException {

        System.out.println("Converting csFasta to fastq ");
        for (CsFastaFilePair csFastaFilePair : csFastaFilePairs) {
            FastQFile fastqFile;
            try {
                fastqFile = csFastaFilePair.convertToFastQFile(outputDirTag, readGroup.getGlobalConfiguration().getCsFastaToFastQFile());
                fastqFile.setTag(tagEnum);
                fastQFiles.add(fastqFile);

                readGroup.getLog().append("Converted csFastaFilePair to Fastq");
                readGroup.getLog().append(csFastaFilePair.toString());
                readGroup.getLog().append(fastqFile.toString());


            } catch (IOException ex) {
                readGroup.getLog().append("Could not convert csFastaFilePair to Fastq");
                readGroup.getLog().append(csFastaFilePair.toString());
                readGroup.getLog().append("error: " + ex.getMessage());
                throw new csFastaToFastqException("Could not convert csFastaFilePair to Fastq: " + ex.getMessage());

            } catch (InterruptedException ex) {
                readGroup.getLog().append("Could not convert csFastaFilePair to Fastq");
                readGroup.getLog().append(csFastaFilePair.toString());
                readGroup.getLog().append("error: " + ex.getMessage());
                throw new csFastaToFastqException("Could not convert csFastaFilePair to Fastq: " + ex.getMessage());
            }
        }
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

    public List<BwaMappingJob> createMappingJobs() throws IOException {
        List<BwaMappingJob> bwaMappingJobs = new ArrayList<BwaMappingJob>();



        for (FastQFile splitFastQFile : getSplitFastQFiles()) {
            File bamFile = new File(outputDirTag, FilenameUtils.getBaseName(splitFastQFile.getFastqFile().getPath()) + ".bam");
            bamFiles.add(bamFile);

            BwaMappingJob bwaMappingJob = new BwaMappingJob(splitFastQFile.getFastqFile(), bamFile, readGroup);
            bwaMappingJobs.add(bwaMappingJob);
        }
        return bwaMappingJobs;
    }

    public void submitMappingJobs(List<BwaMappingJob> mappingJobs) throws DrmaaException {
        for (BwaMappingJob bwaMappingJob : mappingJobs) {
            bwaMappingJob.submit();
        }

    }

    public void mapFastqFiles() throws MappingException {

        System.out.println("Starting submitting of mapping jobs");
        List<BwaMappingJob> bwaMappingJobs = new ArrayList<BwaMappingJob>();
        try {
            bwaMappingJobs = createMappingJobs();
        } catch (IOException ex) {
            throw new MappingException(ex.getMessage());
        }

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

        mergeBamFiles();


    }

    private void waitForMappingJobs(List<BwaMappingJob> bwaMappingJobs) throws DrmaaException, JobFaillureException {

        for (BwaMappingJob bwaMappingJob : bwaMappingJobs) {
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

    public List<CsFastaFilePair> getCsFastaFilePairs() {
        return csFastaFilePairs;
    }

    public List<FastQFile> getFastQFiles() {
        return fastQFiles;
    }

    public File getMergedBamFile() {
        return mergedBamFile;
    }
}
