/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bwa_picard_gatk_pipeline.fileWrappers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author root
 */
public class FastQFileTest {
    
    private static final File tmpDir = new File(System.getProperty("java.io.tmpdir"));
    private static File outputDir;
    
    public FastQFileTest() {
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
     * Test of countNumberOfrecords method, of class FastQFile.
     */
    @Test
    public void testCountNumberOfrecords() {
       
        File fastqFile = new File(getClass().getResource("p1.solid0042_20110504_PE_M520newPEkit_Nico_M520_F3_first1000.fastq").getFile());  
        FastQFile fastQFileWrapper = new FastQFile();
        fastQFileWrapper.setPath(fastqFile.getAbsolutePath());
        
        Long expectedCount = new Long(500);
        Long foundCount = fastQFileWrapper.countNumberOfrecords();
        
        assertTrue("Found number of records does not match expected. Found: "+foundCount +" expedted: "+expectedCount, foundCount.equals(expectedCount));
        
    }

    /**
     * Test of splitFastQFile method, of class FastQFile.
     */
    @Test
    public void testSplitFastQFile() {
       File fastqFile = new File(getClass().getResource("p1.solid0042_20110504_PE_M520newPEkit_Nico_M520_F3_first1000.fastq").getFile());  
       FastQFile fastQFileWrapper = new FastQFile();
       fastQFileWrapper.setPath(fastqFile.getAbsolutePath());
       
       outputDir = new File(tmpDir, "tmpOutputDir");
       outputDir.mkdir();
       
       fastQFileWrapper.countNumberOfrecords();          
        try {
            fastQFileWrapper.splitFastQFile(new Integer(200), outputDir);
        } catch (FileNotFoundException ex) {
            System.out.println(ex.getMessage());
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } 
       
       List<FastQChunk> fastqChunks = fastQFileWrapper.getSplitFastQFiles();
       
       assertTrue("Expected number of chunks is 3, found number of chunks is "+fastqChunks.size() ,fastqChunks.size() == 3);
       
       long fastQrecords = fastQFileWrapper.getRecordNr();
       long splitRecordsNr = fastQFileWrapper.getRecordNrInChunks();
      
       
       assertTrue("Expected fastq records: "+fastQrecords +" , found fastq records: "+splitRecordsNr, fastQrecords == splitRecordsNr);
       
       
        
    }

   
}