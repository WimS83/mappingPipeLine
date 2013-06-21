/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bwa_picard_gatk_pipeline;

import bwa_picard_gatk_pipeline.enums.GATKVariantCallers;
import bwa_picard_gatk_pipeline.sge.Job;
import bwa_picard_gatk_pipeline.sge.solid.BWA.mappingJob.BwaSolidMappingJobTest;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author wim
 */
public class SampleTest {
    
    private static final File tmpDir = new File(System.getProperty("java.io.tmpdir"));
    private static File outputDir;
    
    public SampleTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        
          Assert.assertTrue("Unable to create " + tmpDir.getAbsolutePath(), tmpDir.exists() || tmpDir.mkdirs());

        outputDir = new File(tmpDir, "sampleUnitTest");

        if (outputDir.exists()) {
            try {
                FileUtils.deleteDirectory(outputDir);
            } catch (IOException ex) {
                Logger.getLogger(BwaSolidMappingJobTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        outputDir.mkdir();
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of startProcessing method, of class Sample.
     */
    @Test
    public void testCreateRawSnpJobs() {
        
        File inputBam  = new File("/home/wim/Analysis/BWA_Picard_GATK_pipeline_test/testInput/BNSSN_F1_F3.bam");
        
        File gatk = new File("/home/wim/GenomeAnalysisTK-2.4-7-g5e89f01/GenomeAnalysisTK.jar");

        if (!gatk.canExecute()) {
            fail("Cannot execute GATK on location " + gatk.getAbsolutePath());
        }
        
        File referenceFile = new File(getClass().getResource("Rnor_chr1_10000000_11000000.fa").getFile());
        
        GlobalConfiguration gc = new GlobalConfiguration();
        gc.setBaseOutputDir(outputDir);
        gc.setGatk(gatk);
        gc.setReferenceFile(referenceFile);
        gc.setGatkCallReference(false);
        gc.setGatkSGEMemory(1);
        gc.setGatkSGEThreads(1);
        gc.setgATKVariantCaller(GATKVariantCallers.HaplotypeCaller);
        
        
        
        List<File> vcfChunks = new ArrayList<File>();
        
        
        Sample sample = new Sample();
//        try {
//           List<Job> snpCallJobs =  sample.createSNPCallingJobs(inputBam, outputDir, vcfChunks, gc);
//           String blaat = "blaat";
//        } catch (IOException ex) {
//            Logger.getLogger(SampleTest.class.getName()).log(Level.SEVERE, null, ex);
//        }
        
        
       
        
        
        
       
    }


   
}