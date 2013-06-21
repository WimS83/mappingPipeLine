/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bwa_picard_gatk_pipeline;

import bwa_picard_gatk_pipeline.exceptions.JobFaillureException;
import bwa_picard_gatk_pipeline.sge.Job;
import bwa_picard_gatk_pipeline.sge.gatk.gatkAnnotateSNP.GATKAnnotateVariantsJob;
import bwa_picard_gatk_pipeline.sge.gatk.gatkAnnotateSNP.gatkCombineVariants.GATKCombineVariants;
import bwa_picard_gatk_pipeline.sge.gatk.gatkCallRawSNP.GATKCallRawVariantsJob;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMSequenceRecord;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.ggf.drmaa.DrmaaException;

/**
 *
 * @author wim
 */
public class Variants {

    List<Sample> samples;
    GlobalConfiguration gc;
    File vcfOutputDir;

    public Variants(List<Sample> samples, GlobalConfiguration gc) {
        this.samples = samples;
        this.gc = gc;
        this.vcfOutputDir = new File(gc.getBaseOutputDir(), "vcfOutput");
        vcfOutputDir.mkdir();


    }

    public void callRawSNPs() {

        //check if all the samples have a dedup realigned bam
        Boolean allSamplesHaveRealignedBam = true;
        for (Sample sample : samples) {
            if (sample.getMergedBamDedupRealigned() == null) {
                allSamplesHaveRealignedBam = false;
            }
        }
        if (!allSamplesHaveRealignedBam) {
            return;
        }

        try {
            //check if gatk should do multi sample calling
            if (gc.getMultiSampleCalling()) 
            {
                callRawSNPs(samples);
            } 
            //for each sample create a list with just that sample and do single sample calling
            else 
            {
                for(Sample sample : samples)
                {
                    List<Sample> singleSampleList = new ArrayList<Sample>();
                    singleSampleList.add(sample);
                    callRawSNPs(singleSampleList);
                    
                }  
            }
        } catch (IOException ex) {
            Logger.getLogger(Variants.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(Variants.class.getName()).log(Level.SEVERE, null, ex);
        } catch (DrmaaException ex) {
            Logger.getLogger(Variants.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JobFaillureException ex) {
            Logger.getLogger(Variants.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

   

    private void callRawSNPs(List<Sample> samples) throws IOException, InterruptedException, DrmaaException, JobFaillureException {

        List<File> sampleBamFiles = new ArrayList<File>();
        List<String> sampleNames = new ArrayList<String>();

        for (Sample sample : samples) {
            sampleBamFiles.add(sample.getMergedBamDedupRealigned());
            sampleNames.add(sample.getName());
        }

        String sampleNamesConcat = StringUtils.join(sampleNames, "_");
        File rawVCFFile = new File(vcfOutputDir, sampleNamesConcat + ".vcf");

        List<File> rawVCFFileChunks = new ArrayList<File>();

        List<Job> callRawSNPJobs = createSNPCallingJobs(sampleBamFiles, sampleNamesConcat, vcfOutputDir, rawVCFFileChunks, gc);

        if (gc.getOffline()) {

            executeSNPCallingOffline(callRawSNPJobs);

        } else {
            executeSNPcallingOnline(callRawSNPJobs);
            waitForOnlineExecution(callRawSNPJobs);
        }

        //combine the vcf chunks
        GATKCombineVariants combineVariants = new GATKCombineVariants(rawVCFFileChunks, rawVCFFile, gc);
        combineVariants.executeOffline();
        combineVariants.waitForOfflineExecution();

    }

    private void executeSNPCallingOffline(List<Job> callRawSNPJobs) throws IOException, InterruptedException {
        for (Job job : callRawSNPJobs) {
            job.executeOffline();
            job.waitForOfflineExecution();
        }
    }

    private void executeSNPcallingOnline(List<Job> callRawSNPJobs) throws DrmaaException {
        for (Job job : callRawSNPJobs) {
            job.submit();
        }
    }

    private void waitForOnlineExecution(List<Job> callRawSNPJobs) throws DrmaaException, JobFaillureException {
        for (Job job : callRawSNPJobs) {
            job.waitFor();
        }
    }

    protected List<Job> createSNPCallingJobs(List<File> sampleBamFiles, String sampleNames, File outputDir, List<File> rawVCFFileChunks, GlobalConfiguration gc) throws IOException {
        List<Job> callRawSNPJobs = new ArrayList<Job>();

        SAMFileReader in = new SAMFileReader(sampleBamFiles.get(0));

        List<SAMSequenceRecord> sequences = in.getFileHeader().getSequenceDictionary().getSequences();
        System.out.println("Number of sequences is " + sequences.size());

        for (SAMSequenceRecord sequence : sequences) {
            File rawVCFChromFile = new File(outputDir, sampleNames + "_" + sequence.getSequenceName() + "_raw.vcf");
            rawVCFFileChunks.add(rawVCFChromFile);
            GATKCallRawVariantsJob gATKCallRawVariants = new GATKCallRawVariantsJob(sampleBamFiles, rawVCFChromFile, gc, sequence);

            callRawSNPJobs.add(gATKCallRawVariants);
        }

        System.out.println("Number of snp call jobs  is " + callRawSNPJobs.size());
        System.out.println("Number of snp call chunks  is " + rawVCFFileChunks.size());

        return callRawSNPJobs;



    }

//    private void annotateRawSNPs() throws IOException, InterruptedException, DrmaaException, JobFaillureException {
//
//        if (rawVCFFile == null) {
//            return;
//        }
//
//        annotatedVCFFile = new File(sampleOutputDir, FilenameUtils.getBaseName(mergedBamDedupRealigned.getName()) + "_annotated.vcf");
//
//        GATKAnnotateVariantsJob gATKAnnotateVariantsJob = new GATKAnnotateVariantsJob(rawVCFFile, annotatedVCFFile, globalConfiguration);
//
//        if (globalConfiguration.getOffline()) {
//            gATKAnnotateVariantsJob.executeOffline();
//            gATKAnnotateVariantsJob.waitForOfflineExecution();
//        } else {
//            gATKAnnotateVariantsJob.submit();
//            gATKAnnotateVariantsJob.waitFor();
//        }
//
//    }
//
    public void annotateRawSNPs() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
