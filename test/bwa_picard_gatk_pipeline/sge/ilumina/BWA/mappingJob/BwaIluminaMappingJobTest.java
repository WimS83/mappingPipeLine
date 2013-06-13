/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bwa_picard_gatk_pipeline.sge.ilumina.BWA.mappingJob;

import bwa_picard_gatk_pipeline.GlobalConfiguration;
import bwa_picard_gatk_pipeline.readGroup.ReadGroupIlumina;
import bwa_picard_gatk_pipeline.readGroup.ReadGroupIluminaPE;
import bwa_picard_gatk_pipeline.readGroup.ReadGroupSolidFragment;
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
public class BwaIluminaMappingJobTest {
    
     private static final File tmpDir = new File(System.getProperty("java.io.tmpdir"));
     private static File outputDir;
     
     private ReadGroupIlumina rg;
    
    public BwaIluminaMappingJobTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        
        Assert.assertTrue("Unable to create " + tmpDir.getAbsolutePath(), tmpDir.exists() || tmpDir.mkdirs());

        outputDir = new File(tmpDir, "bwaIluminaUnitTest");

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
        
       File reference = new File(getClass().getResource("Rnor_chr1_10000000_11000000.fa").getFile());
       
       File BWA = new File("/home/sge_share_fedor8/common_scripts/bwa/bwa-0.7.5a/bwa");
       
       if(!BWA.exists()){fail("Cannot find bwa 0.5.9 in location /home/sge_share_fedor8/common_scripts/bwa/bwa-0.7.5a/bwa");}
       
       GlobalConfiguration gc = new GlobalConfiguration();
       gc.setReferenceFile(reference);    
       gc.setBWA(BWA);        
        
       rg = new ReadGroupIluminaPE();
       rg.setGlobalConfiguration(gc);
       rg.setId("SHR_PE1");
       rg.setSample("Sample");
       rg.setDescription("Description");
       rg.setLibrary("library");
        
        
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getSGEName method, of class BwaIluminaMappingJob.
     */
    @Test
    public void mapIluminaPEReads() throws IOException, InterruptedException {
        
        File firstReads = new File(getClass().getResource("ERR111542_1_first100K_records.fastq").getFile());       
        File secondReads = new File(getClass().getResource("ERR111542_2_first100K_records.fastq").getFile()); 
        
        File tmpFirstReadsFile = new File(outputDir, firstReads.getName());     
        File tmpSecondReadsFile = new File(outputDir, secondReads.getName());     
        try {
            FileUtils.copyFile(firstReads, tmpFirstReadsFile);            
            FileUtils.copyFile(secondReads, tmpSecondReadsFile);         
        } catch (IOException ex) {
            Logger.getLogger(BwaSolidMappingJobTest.class.getName()).log(Level.SEVERE, null, ex);
        } 
        
        
        File foundBam = new File(outputDir, "foundFragmentBam.bam");
        
        BwaIluminaMappingJob bwaIluminaMappingJob = new BwaIluminaMappingJob(tmpFirstReadsFile, tmpSecondReadsFile, foundBam, rg);
        bwaIluminaMappingJob.executeOffline();
        
    }
}