/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bwa_picard_gatk_pipeline.sge.solid.BWA.mappingJob;

import bwa_picard_gatk_pipeline.sge.solid.BWA.mappingJob.BwaSolidMappingJob;
import bwa_picard_gatk_pipeline.GlobalConfiguration;
import bwa_picard_gatk_pipeline.ReadGroup;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.picard.sam.PicardCompareBam;
import net.sf.picard.sam.PicardGetReadCount;
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
public class BwaSolidMappingJobTest {
    
    
    private static final File tmpDir = new File(System.getProperty("java.io.tmpdir"));
    private static File outputDir;
    
    private ReadGroup rg;
    
    
    public BwaSolidMappingJobTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        
       Assert.assertTrue("Unable to create " + tmpDir.getAbsolutePath(), tmpDir.exists() || tmpDir.mkdirs());
       
       outputDir = new File(tmpDir, "bwaMappingJobUnitTest"); 
       
       if(outputDir.exists())
        {
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
       
       File colorSpaceBWA = new File("/usr/local/bwa/0.5.9/bwa");
       
       if(!colorSpaceBWA.exists()){fail("Cannot find bwa 0.5.9 in location /usr/local/bwa/0.5.9/bwa");}
       
       GlobalConfiguration gc = new GlobalConfiguration();
       gc.setReferenceFile(reference);    
       gc.setColorSpaceBWA(colorSpaceBWA);        
        
       rg = new ReadGroup();
       rg.setGlobalConfiguration(gc);
       rg.setId("LE_PE1");
       rg.setSample("Sample");
       rg.setDescription("Description");
       rg.setLibrary("library");
        
    }
    
    @After
    public void tearDown() {
        
       
    }

    /**
     * testF3FastqToBam
     */
    @Test
    public void testF3FastqToBam() {
        
       File fastq_F3 = new File(getClass().getResource("p1.LErat_F3_subset.fastq").getFile());        
       
       File expectedBam = new File(getClass().getResource("F3_expected.bam").getFile()); 
       
       File tmpF3File = new File(outputDir, fastq_F3.getName());     
        try {
            FileUtils.copyFile(fastq_F3, tmpF3File);            
        } catch (IOException ex) {
            Logger.getLogger(BwaSolidMappingJobTest.class.getName()).log(Level.SEVERE, null, ex);
        } 
       
       File foundBam = new File(outputDir, "F3_found.bam");         
       
       BwaSolidMappingJob bwaSolidMappingJob;
        try {
            bwaSolidMappingJob = new BwaSolidMappingJob(tmpF3File, foundBam, rg);
            bwaSolidMappingJob.executeOffline();
            bwaSolidMappingJob.waitForOfflineExecution();
        } catch (IOException ex) {
            Logger.getLogger(BwaSolidMappingJobTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(BwaSolidMappingJobTest.class.getName()).log(Level.SEVERE, null, ex);
        }    
        
        PicardCompareBam picardCompareBam = new PicardCompareBam();
        Boolean equal = picardCompareBam.compareBamFiles(expectedBam, foundBam);
        
        assertTrue("Expected and found solid F3 bam files are not equal", equal);
         
      
      
    }
    
    
    /**
     * testF3FastqToBam
     */
    @Test
    public void testF5FastqToBam() {
        
             
        
       File fastq_F5 = new File(getClass().getResource("p1.LErat_F5_subset.fastq").getFile()); 
       
       File expectedBam = new File(getClass().getResource("F5_expected.bam").getFile());       
     
       File tmpF5File = new File(outputDir, fastq_F5.getName());
        try {        
            FileUtils.copyFile(fastq_F5, tmpF5File);
        } catch (IOException ex) {
            Logger.getLogger(BwaSolidMappingJobTest.class.getName()).log(Level.SEVERE, null, ex);
        }  
       
       File foundBam = new File(outputDir, "F5_found.bam");         
       
       BwaSolidMappingJob bwaSolidMappingJob;
        try {
            bwaSolidMappingJob = new BwaSolidMappingJob(tmpF5File, foundBam, rg);
            bwaSolidMappingJob.executeOffline();
            bwaSolidMappingJob.waitForOfflineExecution();
        } catch (IOException ex) {
            Logger.getLogger(BwaSolidMappingJobTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(BwaSolidMappingJobTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        PicardCompareBam picardCompareBam = new PicardCompareBam();
        Boolean equal = picardCompareBam.compareBamFiles(expectedBam, foundBam);
        
       assertTrue("Expected and found solid F5 bam files are not equal", equal);
      
      
    }
    
     /**
     * testF3FastqToBam
     */
    @Test
    public void testF3_F1_FastqToBam() {
        
       File fastq_F3 = new File("/home/wim/Analysis/BWA_Picard_GATK_pipeline_test/testInput/p1.LErat_F3_subset_F1.fastq");        
       
      // File expectedBam = new File(getClass().getResource("F3_expected.bam").getFile()); 
       
       File tmpF3File = new File(outputDir, fastq_F3.getName());     
        try {
            FileUtils.copyFile(fastq_F3, tmpF3File);            
        } catch (IOException ex) {
            Logger.getLogger(BwaSolidMappingJobTest.class.getName()).log(Level.SEVERE, null, ex);
        } 
       
       File foundBam = new File(outputDir, "F3_F1_found.bam");    
       
        rg.setId("LE_F1");
       
       BwaSolidMappingJob bwaSolidMappingJob;
        try {
            bwaSolidMappingJob = new BwaSolidMappingJob(tmpF3File, foundBam, rg);
            bwaSolidMappingJob.executeOffline();
            bwaSolidMappingJob.waitForOfflineExecution();
        } catch (IOException ex) {
            Logger.getLogger(BwaSolidMappingJobTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(BwaSolidMappingJobTest.class.getName()).log(Level.SEVERE, null, ex);
        }    
        
       // PicardCompareBam picardCompareBam = new PicardCompareBam();
       // Boolean equal = picardCompareBam.compareBamFiles(expectedBam, foundBam);
        
      //  assertTrue("Expected and found solid F3 bam files are not equal", equal);
         
      
      
    }
    
}