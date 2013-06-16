/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bwa_picard_gatk_pipeline.sge.gatk.realignJob;

import bwa_picard_gatk_pipeline.sge.gatk.realignJob.GATKRealignIndelsJob;
import bwa_picard_gatk_pipeline.GlobalConfiguration;
import bwa_picard_gatk_pipeline.sge.solid.BWA.mappingJob.BwaSolidMappingJobTest;
import bwa_picard_gatk_pipeline.sge.picard.mergeBAM.PicardMergeBamJobTest;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.picard.sam.PicardCompareBamViaCommandLine;
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
public class GATKRealignIndelsJobTest {
    
     private static final File tmpDir = new File(System.getProperty("java.io.tmpdir"));
     private static File outputDir;
    
    public GATKRealignIndelsJobTest() {
        
       
        
    }
    
    @BeforeClass
    public static void setUpClass() {
        
        Assert.assertTrue("Unable to create " + tmpDir.getAbsolutePath(), tmpDir.exists() || tmpDir.mkdirs());

        outputDir = new File(tmpDir, "gatkRealignJobUnitTest");

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
     * Test of getSGEName method, of class GATKRealignIndelsJob.
     */
    @Test
    public void testGATKRealignIndels() {
        
        
        File expectedDedupBamFile = new File(getClass().getResource("LE_expectedDedup.bam").getFile());
        File expectedDedupBaiFile = new File(getClass().getResource("LE_expectedDedup.bai").getFile());
        File referenceFile = new File(getClass().getResource("Rnor_chr1_10000000_11000000.fa").getFile());
        File expectedRealignBamFile = new File(getClass().getResource("LE_expectedRealign.bam").getFile());
        
        
        File tmpDedupBamFile = new File(outputDir, expectedDedupBamFile.getName());     
        File tmpDedupBaiFile = new File(outputDir, expectedDedupBaiFile.getName());  
        try {
            FileUtils.copyFile(expectedDedupBamFile, tmpDedupBamFile);
            FileUtils.copyFile(expectedDedupBaiFile, tmpDedupBaiFile);
        } catch (IOException ex) {
            Logger.getLogger(BwaSolidMappingJobTest.class.getName()).log(Level.SEVERE, null, ex);
        } 
        
        
        File foundRealignBamFile = new File(outputDir, "LE_foundRealign.bam");
        
        GlobalConfiguration gc = new GlobalConfiguration();
        File gatk = new File("/home/wim/GenomeAnalysisTK-2.4-7-g5e89f01/GenomeAnalysisTK.jar");
        File picardDir = new File("/home/wim/NetBeansProjects/java_libs/picard-tools-1.89/");        
        File picardCompareBam = new File(picardDir, "CompareSAMs.jar");
        
        if(!gatk.canExecute())
        {
            fail("Cannot execute GATK on location "+ gatk.getAbsolutePath());
        }
        
         
        if(!picardCompareBam.canExecute())
        {
            fail("Cannot execute Picard compare bam on location "+ picardCompareBam.getAbsolutePath());
        }
        
        
        
        
        gc.setReferenceFile(referenceFile);
        gc.setPicardDirectory(picardDir);
        gc.setGatk(gatk);
        gc.setOffline(true);
        gc.setTmpDir(outputDir);       
        gc.setGatkSGEMemory(1);
        gc.setGatkSGEThreads(1);
        
         try {
             GATKRealignIndelsJob gATKRealignIndelsJob = new GATKRealignIndelsJob(tmpDedupBamFile, foundRealignBamFile, gc);
             gATKRealignIndelsJob.executeOffline();
             gATKRealignIndelsJob.waitForOfflineExecution();
         } catch (IOException ex) {
             Logger.getLogger(GATKRealignIndelsJobTest.class.getName()).log(Level.SEVERE, null, ex);
         } catch (InterruptedException ex) {
             Logger.getLogger(GATKRealignIndelsJobTest.class.getName()).log(Level.SEVERE, null, ex);
         }
         
         String blaat = "blaat";
         
        PicardCompareBamViaCommandLine picardCompareBamViaCommandLine = new PicardCompareBamViaCommandLine();
        Boolean equal = false;
        try {
            equal = picardCompareBamViaCommandLine.compareBamFiles(expectedRealignBamFile, foundRealignBamFile, picardCompareBam);
        } catch (IOException ex) {
            Logger.getLogger(PicardMergeBamJobTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(PicardMergeBamJobTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        assertTrue("Expected and found merged bam file are not equal", equal);
        
        
      
    }
}