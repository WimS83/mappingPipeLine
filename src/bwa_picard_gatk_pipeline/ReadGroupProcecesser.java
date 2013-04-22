/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bwa_picard_gatk_pipeline;

import bwa_picard_gatk_pipeline.fileWrappers.CsFastaFilePair;
import bwa_picard_gatk_pipeline.fileWrappers.FastQFile;
import bwa_picard_gatk_pipeline.sge.BwaMappingJob;
import bwa_picard_gatk_pipeline.exceptions.JobFaillureException;
import bwa_picard_gatk_pipeline.exceptions.csFastaToFastqException;
import bwa_picard_gatk_pipeline.exceptions.SplitFastQException;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.ggf.drmaa.DrmaaException;

/**
 *
 * @author Wim Spee
 */
public class ReadGroupProcecesser {

    private ReadGroup readGroup;
    private ReadGroupFileCollection readGroupFileCollection;
    private File outputDir;
    private ReadGroupLogFile log;
    private Long chunkSize = new Long(1000000);
    private File csFastaToFastQFile;
    
    

    public ReadGroupProcecesser(ReadGroup readGroup, File outputDir, File csFastaToFastQFile) {

        this.readGroup = readGroup;
        this.outputDir = outputDir;
        if (!outputDir.exists()) {
            outputDir.mkdir();
        }

        log = new ReadGroupLogFile(outputDir, readGroup.getId());

        readGroupFileCollection = new ReadGroupFileCollection(log, readGroup.getId());

        log.append("Started read group processer " + readGroup.getId());
        
       this.csFastaToFastQFile = csFastaToFastQFile;

    }

    public void startProcessing() {

        try {

            System.out.println("Started processing of read group "+readGroup.getId());
            deleteFastQChunks();
            
            //process the csfasta files if there are any
            if (!readGroupFileCollection.getCsFastaFilePairs().isEmpty()) {
                convertCSFastaToFastQ(csFastaToFastQFile);
            }
            //process the fastq files if there any (given in the properties file or converted from csfasta)
            if (!readGroupFileCollection.getFastQFiles().isEmpty()) {
                splitFastQFiles();
            }
            //map the fastqChunks if there are any
            if(!readGroupFileCollection.getSplitFastQFiles().isEmpty())
            {
                 mapFastqFiles();       
            }
            

        } catch (csFastaToFastqException ex) {
            System.out.println(ex.getMessage());
        } catch (SplitFastQException ex) {

            String blaat = "blaat";
            log.append("Deleting fastq chunks");
            deleteFastQChunks();
        } finally {

            log.close();
        }

        String blaat = "bkaat";



    }

    public void convertCSFastaToFastQ(File csFastaToFastqConverter) throws csFastaToFastqException {

        System.out.println("Converting csFasta to fastq ");
        for (CsFastaFilePair csFastaFilePair : readGroupFileCollection.getCsFastaFilePairs()) {
            FastQFile fastqFile;
            try {
                fastqFile = csFastaFilePair.convertToFastQFile(outputDir, csFastaToFastqConverter);
                readGroupFileCollection.addFastQFile(fastqFile, csFastaFilePair.getTag());

                log.append("Converted csFastaFilePair to Fastq");
                log.append(csFastaFilePair.toString());
                log.append(fastqFile.toString());


            } catch (IOException ex) {
                log.append("Could not convert csFastaFilePair to Fastq");
                log.append(csFastaFilePair.toString());
                log.append("error: " + ex.getMessage());
                throw new csFastaToFastqException("Could not convert csFastaFilePair to Fastq: " + ex.getMessage());

            } catch (InterruptedException ex) {
                log.append("Could not convert csFastaFilePair to Fastq");
                log.append(csFastaFilePair.toString());
                log.append("error: " + ex.getMessage());
                throw new csFastaToFastqException("Could not convert csFastaFilePair to Fastq: " + ex.getMessage());
            }
        }
    }

    public void splitFastQFiles() throws SplitFastQException {
        log.append("Start splitting fastqFiles");

        try {

            for (FastQFile fastQFile : readGroupFileCollection.getFastQFiles()) {

                fastQFile.splitFastQFile(chunkSize, outputDir);
                log.append("Splitted fastQFile:");
                log.append(fastQFile.toString());
            }
        } catch (FileNotFoundException ex) {
            log.append("Could not split fastq file");
            log.append("error: " + ex.getMessage());
            throw new SplitFastQException("Could not split fastq file: " + ex.getMessage());
        } catch (IOException ex) {
            log.append("Could not split fastq file");
            log.append("error: " + ex.getMessage());
            throw new SplitFastQException("Could not split fastq file: " + ex.getMessage());
        } catch (SplitFastQException ex) {
            log.append("Could not split fastq file");
            log.append("error: " + ex.getMessage());
            throw ex;

        }
    }
    
    public List<BwaMappingJob> createMappingJobs()
    {
         List<BwaMappingJob> bwaMappingJobs = new ArrayList<BwaMappingJob>();

         for (FastQFile splitFastQFile : readGroupFileCollection.getFastQFiles()) {
            BwaMappingJob bwaMappingJob = new BwaMappingJob(splitFastQFile.getFastqFile(), readGroup);
            bwaMappingJobs.add(bwaMappingJob);
         }
         return bwaMappingJobs;
    }
    
    
    
    
    
    

    public void mapFastqFiles() {

        System.out.println("Starting submitting of mapping jobs");
       

        for (BwaMappingJob bwaMappingJob : bwaMappingJobs) {
            try {
                bwaMappingJob.submit();
            } catch (DrmaaException ex) {
                System.out.println("Cannot submit job " + bwaMappingJob.getSGEName() + " : " + ex.getMessage());
            }

        }
        try {
            for (BwaMappingJob bwaMappingJob : bwaMappingJobs) {

                int finishedJobs = 0;
                if (bwaMappingJob.isFinished()) {
                    finishedJobs++;
                }
                if (finishedJobs == bwaMappingJobs.size()) {
                    break;
                } else {
                    try {
                        TimeUnit.MINUTES.MINUTES.sleep(5);
                    } catch (InterruptedException ex) {
                        System.out.println(ex.getMessage());
                    }
                }
            }

        } catch (DrmaaException ex) {
            System.out.println("Cannot connect to sge_qeue master ");
        } catch (JobFaillureException ex) {
            System.out.println("Mapping job failed  " + " : " + ex.getMessage());

            for (BwaMappingJob bwaMappingJob : bwaMappingJobs) {
                try {
                    bwaMappingJob.deleleteJob();
                } catch (DrmaaException ex1) {
                    System.out.println("Could not delete job " + " : " + ex.getMessage());
                }
            }


            String blaat = "blaat";

        }
    }

    private void deleteFastQChunks() {

        FileFilter fileFilter = new WildcardFileFilter("*_chunk*.fastq");
        File[] files = outputDir.listFiles(fileFilter);
        for (int i = 0; i < files.length; i++) {
            files[i].delete();
        }
    }

    public void setChunkSize(Long chunkSize) {
        this.chunkSize = chunkSize;
    }

    public ReadGroupFileCollection getReadGroupFileCollection() {
        return readGroupFileCollection;
    }
    
    
}
