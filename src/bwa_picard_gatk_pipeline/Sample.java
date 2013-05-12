/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bwa_picard_gatk_pipeline;

import bwa_picard_gatk_pipeline.enums.TargetEnum;
import bwa_picard_gatk_pipeline.sge.GATKAnnotateVariantsJob;
import bwa_picard_gatk_pipeline.sge.GATKCallRawVariantsJob;
import bwa_picard_gatk_pipeline.sge.GATKRealignIndelsJob;
import bwa_picard_gatk_pipeline.sge.QualimapJob;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.picard.sam.PicardBamMerger;
import net.sf.picard.sam.PicardMarkDuplicates;
import org.apache.commons.io.FilenameUtils;
import org.ggf.drmaa.DrmaaException;

/**
 *
 * @author Wim Spee
 */
public class Sample {

    private String name;
    private List<ReadGroup> readGroups;
    private List<File> readGroupBamFiles;
    private GlobalConfiguration globalConfiguration;
    private File sampleOutputDir;
    private File mergedBamFile;
    private File mergedBamFileDedup;
    private File mergedBamDedupRealigned;
    private File rawVCFFile;
    private File annotatedVCFFile;

    public void startProcessing() {

        initalizeUnsetList();           //initialize the unset list to empty list  

        //process the readGroups is any were set
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
                runQualimap();
                
            }
            if (globalConfiguration.getTargetEnum().getRank() >= TargetEnum.DEDUP_BAM.getRank()) {
                markDuplicates();
            }
            if (globalConfiguration.getTargetEnum().getRank() >= TargetEnum.REALIGN_BAM.getRank()) {
                realignBam();
            }
            if (globalConfiguration.getTargetEnum().getRank() >= TargetEnum.SAMPLE_RAW_VCF.getRank()) {
                callRawSNPs();
            }
            if (globalConfiguration.getTargetEnum().getRank() >= TargetEnum.SAMPLE_ANNOTATED_VCF.getRank()) {
                annotateRawSNPs();
            }            
            


          


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

    private void initalizeUnsetList() {
        if (readGroups == null) {
            readGroups = new ArrayList<ReadGroup>();
        }
        if (readGroupBamFiles == null) {
            readGroupBamFiles = new ArrayList<File>();
        }
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


        for (ReadGroup readGroup : readGroups) {
            readGroupBamFiles.add(readGroup.getMergedBam());
        }

        if (readGroupBamFiles.size() == 1) {
            mergedBamFile = readGroupBamFiles.get(0);
        }

        if (readGroupBamFiles.size() > 1) {
            try {
                mergedBamFile = new File(sampleOutputDir, name+".bam");
                
                PicardBamMerger picardBamMerger = new PicardBamMerger();
                picardBamMerger.mergeBamFilesUsingPicard(readGroupBamFiles, mergedBamFile, globalConfiguration.getTmpDir());
            } catch (IOException ex) {
                Logger.getLogger(Sample.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }
    
      private void runQualimap() throws IOException, InterruptedException, DrmaaException {

        File qualimapReport = new File(sampleOutputDir, name+"_qualimap.pdf");        
        QualimapJob qualimapJob = new QualimapJob(mergedBamFile, qualimapReport, globalConfiguration, "fedor8");
        
         qualimapJob.executeOffline(); // temporary always execute oflline, untill I know to execute via SGE on fedor8 or another suitable node    
//        if(globalConfiguration.getOffline())
//        {
//            qualimapJob.executeOffline();
//        }
//        else
//        {
//            qualimapJob.submit();
//        }              
    }
    

    private void markDuplicates() throws IOException {
        //if a merged bam file was created by processing the readgroups or was set by json 
        //deduplicate the bam
        if (mergedBamFile == null) {  return;   }
        
        PicardMarkDuplicates picardMarkDuplicates = new PicardMarkDuplicates();
        mergedBamFileDedup = picardMarkDuplicates.markDuplicates(mergedBamFile, globalConfiguration.getTmpDir());


    }

    private void realignBam() throws IOException, InterruptedException, DrmaaException {

        if(mergedBamFileDedup == null) {return;}
        
        mergedBamDedupRealigned = new File(sampleOutputDir, FilenameUtils.getBaseName(mergedBamFileDedup.getName()) + "_realigned.bam");

        GATKRealignIndelsJob gATKRealignIndelsJob = new GATKRealignIndelsJob(mergedBamFileDedup, mergedBamDedupRealigned, globalConfiguration);

        if (globalConfiguration.getOffline()) {
            gATKRealignIndelsJob.executeOffline();
        } else {
            gATKRealignIndelsJob.submit();
        }
    }

    private void callRawSNPs() throws IOException, InterruptedException, DrmaaException{
        
        if(mergedBamDedupRealigned == null){ return;}
        
        rawVCFFile = new File(sampleOutputDir, FilenameUtils.getBaseName(mergedBamDedupRealigned.getName())+ "_raw.vcf");
        
        GATKCallRawVariantsJob gATKCallRawVariants = new GATKCallRawVariantsJob(mergedBamDedupRealigned, rawVCFFile, globalConfiguration);
        
        if (globalConfiguration.getOffline()) {
            gATKCallRawVariants.executeOffline();
        } else {
            gATKCallRawVariants.submit();
        }
        
    }
    
    private void annotateRawSNPs() throws IOException, InterruptedException, DrmaaException{
        
        if(rawVCFFile == null){ return;}
        
        annotatedVCFFile = new File(sampleOutputDir, FilenameUtils.getBaseName(mergedBamDedupRealigned.getName())+ "_annotated.vcf");
        
        GATKAnnotateVariantsJob gATKAnnotateVariantsJob = new GATKAnnotateVariantsJob(rawVCFFile, annotatedVCFFile, globalConfiguration);
        
        if (globalConfiguration.getOffline()) {
            gATKAnnotateVariantsJob.executeOffline();
        } else {
            gATKAnnotateVariantsJob.submit();
        }
        
    }
}
