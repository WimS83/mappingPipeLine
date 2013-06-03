/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bwa_picard_gatk_pipeline.sge.solid.BWA.gatkAnnotateSNP;

import bwa_picard_gatk_pipeline.GlobalConfiguration;
import bwa_picard_gatk_pipeline.sge.solid.BWA.gatkCallRawSNP.GATKCallRawVariantsJobTest;
import bwa_picard_gatk_pipeline.sge.solid.BWA.mappingJob.BwaSolidMappingJobTest;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author wim
 */
public class GATKAnnotateVariantsJobTest {
    
     private static final File tmpDir = new File(System.getProperty("java.io.tmpdir"));
     private static File outputDir;
    
    public GATKAnnotateVariantsJobTest() {
        
        
    }
    
    @BeforeClass
    public static void setUpClass() {
        
        Assert.assertTrue("Unable to create " + tmpDir.getAbsolutePath(), tmpDir.exists() || tmpDir.mkdirs());

        outputDir = new File(tmpDir, "gatkAnnotateSNPJobUnitTest");

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
     * Test of getSGEName method, of class GATKAnnotateVariantsJob.
     */
    @Test
    public void testGatkAnnotateSNP() {
       
        File referenceFile = new File(getClass().getResource("Rnor_chr1_10000000_11000000.fa").getFile());
        File expectedRawSNPCalls = new File(getClass().getResource("expectedSNPCalls_raw.vcf").getFile());
        File expectedRawSNPCallsIdx = new File(getClass().getResource("expectedSNPCalls_raw.vcf.idx").getFile());
        
        File expectedAnnotatedSNPCalls = new File(getClass().getResource("expectedSNPCalls_annotated.vcf").getFile());
        
        
        File tmpRawVCFFile = new File(outputDir, expectedRawSNPCalls.getName());     
        File tmpRawVCFIdxFile = new File(outputDir, expectedRawSNPCallsIdx.getName());  
        try {
            FileUtils.copyFile(expectedRawSNPCalls, tmpRawVCFFile);
            FileUtils.copyFile(expectedRawSNPCallsIdx, tmpRawVCFIdxFile);
        } catch (IOException ex) {
            Logger.getLogger(BwaSolidMappingJobTest.class.getName()).log(Level.SEVERE, null, ex);
        } 
        
        GlobalConfiguration gc = new GlobalConfiguration();
        File gatk = new File("/home/wim/GenomeAnalysisTK-2.4-7-g5e89f01/GenomeAnalysisTK.jar");        
        
        gc.setReferenceFile(referenceFile);
        gc.setGatk(gatk);
        gc.setOffline(true);
        gc.setTmpDir(outputDir);       
        gc.setGatkSGEMemory(1);
        gc.setGatkSGEThreads(1);
        gc.setGatkCallReference(true);
        
        File foundAnnotatedSNPCalls = new File(outputDir, "foundSNPCalls_annotated.vcf");
         try {
             GATKAnnotateVariantsJob gATKAnnotateVariantsJob = new GATKAnnotateVariantsJob(expectedRawSNPCalls, foundAnnotatedSNPCalls, gc);
             gATKAnnotateVariantsJob.executeOffline();
             gATKAnnotateVariantsJob.waitForOfflineExecution();
         } catch (IOException ex) {
             Logger.getLogger(GATKAnnotateVariantsJobTest.class.getName()).log(Level.SEVERE, null, ex);
         } catch (InterruptedException ex) {
             Logger.getLogger(GATKAnnotateVariantsJobTest.class.getName()).log(Level.SEVERE, null, ex);
         }
         
         try {
             Assert.assertEquals(FileUtils.readLines(expectedAnnotatedSNPCalls), FileUtils.readLines(foundAnnotatedSNPCalls));
         } catch (IOException ex) {
             Logger.getLogger(GATKCallRawVariantsJobTest.class.getName()).log(Level.SEVERE, null, ex);
         }
         
         
        
        
    }
}