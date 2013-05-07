/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bwa_picard_gatk_pipeline;

import bwa_picard_gatk_pipeline.enums.TargetEnum;
import bwa_picard_gatk_pipeline.sge.GATKRealignIndelsJob;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.picard.sam.PicardBamMerger;
import net.sf.picard.sam.PicardMarkDuplicates;
import org.ggf.drmaa.DrmaaException;

/**
 *
 * @author Wim Spee
 */
public class Sample {

    private String name;
    private List<ReadGroup> readGroups;
    private GlobalConfiguration globalConfiguration;
    private File sampleOutputDir;
    private File mergedBamFile;
    private File mergedBamFileDedup;
    private File mergedBamRealigned;

    public void startProcessing() {

        for (ReadGroup readGroup : readGroups) {
            sampleOutputDir = new File(globalConfiguration.getBaseOutputDir(), name);
            sampleOutputDir.mkdir();

            readGroup.createOutputDir(sampleOutputDir);

            readGroup.setGlobalConfiguration(globalConfiguration);
            readGroup.setSample(name);

            readGroup.startProcessing();
        }

        //do the sample processing
        try {
            //merge readgroup bam files
            if (globalConfiguration.getTargetEnum().getRank() >= TargetEnum.SAMPLE_BAM.getRank()) {
                mergeReadGroupBamFiles();
            }
            if (globalConfiguration.getTargetEnum().getRank() >= TargetEnum.DEDUP_BAM.getRank()) {
                markDuplicates();
            }
            if (globalConfiguration.getTargetEnum().getRank() >= TargetEnum.REALIGN_BAM.getRank()) {
                realignBam();
            }

            //realign the bam around indels
            String args[] = new String[]{"-T", "UnifiedGenotyper", "-R ", "my.fasta", "-I", "my.bam- my.vcf"};


        } catch (IOException ex) {
            Logger.getLogger(Sample.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(Sample.class.getName()).log(Level.SEVERE, null, ex);
        } catch (DrmaaException ex) {
            Logger.getLogger(Sample.class.getName()).log(Level.SEVERE, null, ex);
        }




        //call snps 

        //cal sv's         
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ReadGroup> getReadGroups() {
        return readGroups;
    }

    public void setReadGroups(List<ReadGroup> readGroups) {
        this.readGroups = readGroups;
    }

    public void setGlobalConfiguration(GlobalConfiguration globalConfiguration) {
        this.globalConfiguration = globalConfiguration;
    }

    private void mergeReadGroupBamFiles() throws IOException {

        List<File> readGroupBamFiles = new ArrayList<File>();

        if (readGroups != null) {
            for (ReadGroup readGroup : readGroups) {
                readGroupBamFiles.add(readGroup.getMergedBam());
            }
        }

        if (readGroupBamFiles.size() == 1) {
            mergedBamFile = readGroupBamFiles.get(0);
        }

        if (readGroupBamFiles.size() > 1) {
            try {
                PicardBamMerger picardBamMerger = new PicardBamMerger();
                mergedBamFile = picardBamMerger.mergeBamFilesUsingPicard(readGroupBamFiles, globalConfiguration.getTmpDir());
            } catch (IOException ex) {
                Logger.getLogger(Sample.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    private void markDuplicates() throws IOException {
        //if a merged bam file was created by processing the readgroups or was set by json 
        //deduplicate the bam
        if (mergedBamFile != null) {
            PicardMarkDuplicates picardMarkDuplicates = new PicardMarkDuplicates();
            mergedBamFileDedup = picardMarkDuplicates.markDuplicates(mergedBamFile, globalConfiguration.getTmpDir());
        }

    }

    private void realignBam() throws IOException, InterruptedException, DrmaaException {
        
        GATKRealignIndelsJob gATKRealignIndelsJob = new GATKRealignIndelsJob(mergedBamFileDedup, globalConfiguration);
        
        if(globalConfiguration.getOffline())
        {
            gATKRealignIndelsJob.executeOffline();
        }
        else
        {
            gATKRealignIndelsJob.submit();
        }
        
        
        
    }
}
