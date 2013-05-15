/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bwa_picard_gatk_pipeline.sge;

import java.io.File;
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
    
    
    public BwaSolidMappingJobTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        
        Assert.assertTrue("Unable to create " + tmpDir.getAbsolutePath(), tmpDir.exists() || tmpDir.mkdirs());
        
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
        
        if(outputDir != null)
        {
            outputDir.delete();
        }
    }

    /**
     * Test of getSGEName method, of class BwaSolidMappingJob.
     */
    @Test
    public void testFastqChunkToBam() {
        
       File fastqChunk = new File(getClass().getResource("solid0042_20101110_PE_F344RatStrain_III_Nico_F344_rat_founder_first10000_F3_chunk1.fastq").getFile());  
       
       File outputDir = new File(tmpDir, "bwaMappingJobUnitTest");
       outputDir.mkdir();
       
       File bam = new File(outputDir, "test.bam");
       
//       
//       BwaSolidMappingJob bwaSolidMappingJob = new BwaSolidMappingJob(fastqChunk, bam, null);
//       
//       bwaSolidMappingJob.e
      
    }
}