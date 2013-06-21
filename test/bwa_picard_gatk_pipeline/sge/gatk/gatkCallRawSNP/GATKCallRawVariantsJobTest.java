/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bwa_picard_gatk_pipeline.sge.gatk.gatkCallRawSNP;

import bwa_picard_gatk_pipeline.GlobalConfiguration;
import bwa_picard_gatk_pipeline.enums.GATKVariantCallers;
import bwa_picard_gatk_pipeline.sge.solid.BWA.mappingJob.BwaSolidMappingJobTest;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMSequenceRecord;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
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
public class GATKCallRawVariantsJobTest {

    private static final File tmpDir = new File(System.getProperty("java.io.tmpdir"));
    private static File outputDir;

    public GATKCallRawVariantsJobTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        Assert.assertTrue("Unable to create " + tmpDir.getAbsolutePath(), tmpDir.exists() || tmpDir.mkdirs());

        outputDir = new File(tmpDir, "gatkCallRawSNPJobUnitTest");

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
     * Test of getSGEName method, of class GATKCallRawVariantsJob.
     */
    @Test
    public void testGatkCallRawSNP() {

        File expectedRealignBamFile = new File(getClass().getResource("LE_expectedRealign.bam").getFile());
        File expectedRealignBaiFile = new File(getClass().getResource("LE_expectedRealign.bai").getFile());
        File referenceFile = new File(getClass().getResource("Rnor_chr1_10000000_11000000.fa").getFile());
        File expectedRawSNPCalls = new File(getClass().getResource("expectedSNPCalls_raw.vcf").getFile());


        File tmpRealignBamFile = new File(outputDir, expectedRealignBamFile.getName());
        File tmpRealignBaiFile = new File(outputDir, expectedRealignBaiFile.getName());
        try {
            FileUtils.copyFile(expectedRealignBamFile, tmpRealignBamFile);
            FileUtils.copyFile(expectedRealignBaiFile, tmpRealignBaiFile);
        } catch (IOException ex) {
            Logger.getLogger(BwaSolidMappingJobTest.class.getName()).log(Level.SEVERE, null, ex);
        }

        GlobalConfiguration gc = new GlobalConfiguration();
        File gatk = new File("/home/wim/GenomeAnalysisTK-2.4-7-g5e89f01/GenomeAnalysisTK.jar");

        if (!gatk.canExecute()) {
            fail("Cannot execute GATK on location " + gatk.getAbsolutePath());
        }

        gc.setReferenceFile(referenceFile);
        gc.setGatk(gatk);
        gc.setOffline(true);
        gc.setTmpDir(outputDir);
        gc.setGatkSGEMemory(1);
        gc.setGatkSGEThreads(1);
        gc.setGatkCallReference(true);
        gc.setgATKVariantCaller(GATKVariantCallers.HaplotypeCaller);

        File foundRawSNPCalls = new File(outputDir, "foundSNPCalls_raw.vcf");
        try {

            SAMFileReader in = new SAMFileReader(tmpRealignBamFile);
            List<File> inputFiles = new ArrayList<File>();
            inputFiles.add(tmpRealignBamFile);
            
            
            
            
            for (SAMSequenceRecord chromosome : in.getFileHeader().getSequenceDictionary().getSequences()) {
                
                File rawVCFChromFile = new File(outputDir, FilenameUtils.getBaseName(tmpRealignBamFile.getName()) +"_"+chromosome.getSequenceName()+ "_raw.vcf");
                GATKCallRawVariantsJob gATKCallRawVariantsJob = new GATKCallRawVariantsJob(inputFiles, rawVCFChromFile, gc, chromosome);
                gATKCallRawVariantsJob.executeOffline();
                gATKCallRawVariantsJob.waitForOfflineExecution();
                
                
            }
            
            

         
        } catch (IOException ex) {
            Logger.getLogger(GATKCallRawVariantsJobTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(GATKCallRawVariantsJobTest.class.getName()).log(Level.SEVERE, null, ex);
        }


        try {
            Assert.assertEquals(FileUtils.readLines(expectedRawSNPCalls), FileUtils.readLines(foundRawSNPCalls));
        } catch (IOException ex) {
            Logger.getLogger(GATKCallRawVariantsJobTest.class.getName()).log(Level.SEVERE, null, ex);
        }





    }
}