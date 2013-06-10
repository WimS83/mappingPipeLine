/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bwa_picard_gatk_pipeline.sge.solid.BWA.pairReads;

import bwa_picard_gatk_pipeline.sge.solid.BWA.mappingJob.BwaSolidMappingJobTest;
import bwa_picard_gatk_pipeline.GlobalConfiguration;
import bwa_picard_gatk_pipeline.readGroup.ReadGroup;
import bwa_picard_gatk_pipeline.enums.TagEnum;
import bwa_picard_gatk_pipeline.readGroup.ReadGroupSolid;
import bwa_picard_gatk_pipeline.readGroup.ReadGroupSolidPE;
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
public class PiclPairReadsJobTest {

    private static final File tmpDir = new File(System.getProperty("java.io.tmpdir"));
    private static File outputDir;
    private static ReadGroupSolid rg;

    public PiclPairReadsJobTest() {
    }

    @BeforeClass
    public static void setUpClass() {

        Assert.assertTrue("Unable to create " + tmpDir.getAbsolutePath(), tmpDir.exists() || tmpDir.mkdirs());

        outputDir = new File(tmpDir, "piclJobUnitTest");

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
     * Test of getSGEName method, of class PiclPairReadsJob.
     */
    @Test
    public void testPairTagsUsingPicl() {

        File f3BamFile = new File(getClass().getResource("F3_expected.bam").getFile());
        File f5BamFile = new File(getClass().getResource("F5_expected.bam").getFile());
        
        File expectedPairedBamFile = new File(getClass().getResource("expectedPairedBamFile.bam").getFile());
        
        File tmpf3BamFile = new File(outputDir, f3BamFile.getName());     
        File tmpf5BamFile = new File(outputDir, f5BamFile.getName());    
        
        try {
            FileUtils.copyFile(f3BamFile, tmpf3BamFile);    
            FileUtils.copyFile(f5BamFile, tmpf5BamFile); 
        } catch (IOException ex) {
            Logger.getLogger(BwaSolidMappingJobTest.class.getName()).log(Level.SEVERE, null, ex);
        }         

        rg = new ReadGroupSolidPE();
        rg.setId("LE");

        GlobalConfiguration gc = new GlobalConfiguration();

        File picl = new File("/usr/local/Picl/picl");
        File picardDir = new File("/home/wim/NetBeansProjects/java_libs/picard-tools-1.89/");
        File picardCompareBam = new File(picardDir, "CompareSAMs.jar");

        gc.setPicl(picl);
        gc.setPicardDirectory(picardDir);

        rg.setGlobalConfiguration(gc);

        if (!rg.getGlobalConfiguration().getPicl().exists()) {
            fail("Picl could not be found in location " + picl.getAbsolutePath());
        }
        if (!rg.getGlobalConfiguration().getPicardDirectory().exists()) {
            fail("Picard 1.89 directory could not be found in location " + picardDir.getAbsolutePath());
        }



        File foundPairedBamFile = new File(outputDir, "foundPairedBamFile.bam");
        try {
            PiclPairReadsJob pairReadsJob = new PiclPairReadsJob(tmpf3BamFile, tmpf5BamFile, foundPairedBamFile, rg, null, TagEnum.SOLID_F5);
            pairReadsJob.executeOffline();
            pairReadsJob.waitForOfflineExecution();
        } catch (IOException ex) {
            Logger.getLogger(PiclPairReadsJobTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(PiclPairReadsJobTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        PicardCompareBamViaCommandLine picardCompareBamViaCommandLine = new PicardCompareBamViaCommandLine();
        Boolean equal = false;
        try {
            equal = picardCompareBamViaCommandLine.compareBamFiles(expectedPairedBamFile, foundPairedBamFile, picardCompareBam);
        } catch (IOException ex) {
            Logger.getLogger(PiclPairReadsJobTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(PiclPairReadsJobTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        assertTrue("Expected and found paired bam file are not equal", equal);






    }
}
