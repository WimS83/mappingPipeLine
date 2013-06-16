/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bwa_picard_gatk_pipeline.sge.picard.dedupJob;

import bwa_picard_gatk_pipeline.sge.picard.dedupJob.PicardDedupBamJob;
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
public class PicardDedupBamJobTest {

    private static final File tmpDir = new File(System.getProperty("java.io.tmpdir"));
    private static File outputDir;

    public PicardDedupBamJobTest() {
    }

    @BeforeClass
    public static void setUpClass() {

        Assert.assertTrue("Unable to create " + tmpDir.getAbsolutePath(), tmpDir.exists() || tmpDir.mkdirs());

        outputDir = new File(tmpDir, "dedupBamsJobUnitTest");

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
     * Test of getSGEName method, of class PicardDedupBamJob.
     */
    @Test
    public void dedupBamTest() {

        File expectedMergedBamFile = new File(getClass().getResource("LE_expectedMerged.bam").getFile());
        File expectedDedupBamFile = new File(getClass().getResource("LE_expectedDedup.bam").getFile());
        
        File tmpMergedBamFile = new File(outputDir, expectedMergedBamFile.getName());     
        try {
            FileUtils.copyFile(expectedMergedBamFile, tmpMergedBamFile);
        } catch (IOException ex) {
            Logger.getLogger(BwaSolidMappingJobTest.class.getName()).log(Level.SEVERE, null, ex);
        } 
        

        File foundDedupBamFile = new File(outputDir, "LE_foundDedup.bam");

        GlobalConfiguration gc = new GlobalConfiguration();
        File picardDir = new File("/home/wim/NetBeansProjects/java_libs/picard-tools-1.89/");       
        File picardCompareBam = new File(picardDir, "CompareSAMs.jar");
        
        if(!picardCompareBam.canExecute())
        {
            fail("Cannot execute Picard compare bam on location "+ picardCompareBam.getAbsolutePath());
        }
        
        gc.setOffline(true);

        gc.setPicardDirectory(picardDir);
        gc.setTmpDir(tmpDir);
        try {
            PicardDedupBamJob picardDedupBamJob = new PicardDedupBamJob(tmpMergedBamFile, foundDedupBamFile, gc);
            picardDedupBamJob.executeOffline();
            picardDedupBamJob.waitForOfflineExecution();
        } catch (IOException ex) {
            Logger.getLogger(PicardDedupBamJobTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(PicardDedupBamJobTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        PicardCompareBamViaCommandLine picardCompareBamViaCommandLine = new PicardCompareBamViaCommandLine();
        Boolean equal = false;
        try {
            equal = picardCompareBamViaCommandLine.compareBamFiles(foundDedupBamFile, expectedDedupBamFile, picardCompareBam);
        } catch (IOException ex) {
            Logger.getLogger(PicardMergeBamJobTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(PicardMergeBamJobTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        assertTrue("Expected and found merged bam file are not equal", equal);
        
         
      
    }
}