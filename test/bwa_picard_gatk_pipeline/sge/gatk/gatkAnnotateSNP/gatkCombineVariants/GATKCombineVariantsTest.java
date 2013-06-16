/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bwa_picard_gatk_pipeline.sge.gatk.gatkAnnotateSNP.gatkCombineVariants;

import bwa_picard_gatk_pipeline.GlobalConfiguration;
import bwa_picard_gatk_pipeline.enums.GATKVariantCallers;
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
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author wim
 */
public class GATKCombineVariantsTest {
    
    private static final File tmpDir = new File(System.getProperty("java.io.tmpdir"));
    private static File outputDir;
    
    public GATKCombineVariantsTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
         Assert.assertTrue("Unable to create " + tmpDir.getAbsolutePath(), tmpDir.exists() || tmpDir.mkdirs());

        outputDir = new File(tmpDir, "gatkConcatVCFJobUnitTest");

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
     * Test of getSGEName method, of class GATKCombineVariants.
     */
    @Test
    public void testGetSGEName() throws IOException {
        
         File expectedRawSNPCalls = new File(getClass().getResource("expectedSNPCalls_raw.vcf").getFile());
         File referenceFile = new File(getClass().getResource("Rnor_chr1_10000000_11000000.fa").getFile());
         
         File concatenatedVCF = new File(outputDir, "concatenated.vcf");
         
        File tmpVCFFile = new File(outputDir, expectedRawSNPCalls.getName());
        try {
            FileUtils.copyFile(expectedRawSNPCalls, tmpVCFFile);           
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
        gc.setgATKVariantCaller(GATKVariantCallers.HaplotypeCaller);
        
        List<File> inputFiles = new ArrayList<File>();
        inputFiles.add(tmpVCFFile);
        inputFiles.add(tmpVCFFile);
        
        
        
        GATKCombineVariants combineVariants = new GATKCombineVariants(inputFiles, concatenatedVCF, gc);
        try {
            combineVariants.executeOffline();
            combineVariants.waitForOfflineExecution();
        } catch (InterruptedException ex) {
            Logger.getLogger(GATKCombineVariantsTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
         
        
        
    }
}