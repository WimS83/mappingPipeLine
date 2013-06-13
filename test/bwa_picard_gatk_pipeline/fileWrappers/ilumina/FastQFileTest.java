/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bwa_picard_gatk_pipeline.fileWrappers.ilumina;

import bwa_picard_gatk_pipeline.fileWrappers.FastQChunk;
import bwa_picard_gatk_pipeline.fileWrappers.FastQFile;
import bwa_picard_gatk_pipeline.sge.solid.BWA.mappingJob.BwaSolidMappingJobTest;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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
public class FastQFileTest {
    
     private static final File tmpDir = new File(System.getProperty("java.io.tmpdir"));
     private static File outputDir;
    
    public FastQFileTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        
        Assert.assertTrue("Unable to create " + tmpDir.getAbsolutePath(), tmpDir.exists() || tmpDir.mkdirs());

        outputDir = new File(tmpDir, "fastqUnitTest");

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
     * Test of splitFastQFile method, of class FastQFile.
     */
    @Test
    public void testSplitFastQFile() throws Exception {
      
        File firstReads = new File(getClass().getResource("ERR111542_1_first1000.fastq").getFile());        
        
        File tmpfirstReads = new File(outputDir, firstReads.getName());  
        try {
            FileUtils.copyFile(firstReads, tmpfirstReads);           
        } catch (IOException ex) {
            Logger.getLogger(BwaSolidMappingJobTest.class.getName()).log(Level.SEVERE, null, ex);
        }         
        
        FastQFile fastQFile = new FastQFile();
        fastQFile.setPath(tmpfirstReads.getAbsolutePath());
        
        
        fastQFile.initializeFastqReader();
        List<FastQChunk> fastQChunks = fastQFile.splitFastQFile(100, outputDir, "ERR111542");
        
        assertTrue("Number of chunks is not equal to 3", fastQChunks.size()==3);
        
        Long readsInChunks = new Long(0);
        
        for(FastQChunk fastQChunk: fastQChunks)
        {
            readsInChunks = readsInChunks + fastQChunk.countNumberOfrecords();
        }
        
         assertTrue("Number of reads is not equal to 250", readsInChunks==250);          
    }
    
   
   

  

}