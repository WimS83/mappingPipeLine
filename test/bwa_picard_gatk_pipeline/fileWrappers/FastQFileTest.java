/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bwa_picard_gatk_pipeline.fileWrappers;

import bwa_picard_gatk_pipeline.exceptions.SplitFastQException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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
        FastQFile fastQFileWrapper = new FastQFile(fastqFile);
        assertTrue(fastQFileWrapper.countNumberOfrecords() == 500);
        
    }

    /**
     * Test of splitFastQFile method, of class FastQFile.
     */
    @Test
    public void testSplitFastQFile() {
       File fastqFile = new File(getClass().getResource("p1.solid0042_20110504_PE_M520newPEkit_Nico_M520_F3_first1000.fastq").getFile());  
       FastQFile fastQFileWrapper = new FastQFile(fastqFile);
       
       outputDir = new File(tmpDir, "tmpOutputDir");
       outputDir.mkdir();
       
       fastQFileWrapper.countNumberOfrecords();          
        try {
            fastQFileWrapper.splitFastQFile(new Long(500), outputDir);
        } catch (FileNotFoundException ex) {
            System.out.println(ex.getMessage());
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } catch (SplitFastQException ex) {
            System.out.println(ex.getMessage());
        }
       
       List<FastQFile> splitFastQfiles = fastQFileWrapper.getSplitFastQFiles();
       
       assertTrue(splitFastQfiles.size() == 4);
       
       long fastQrecords = fastQFileWrapper.getRecordNr();
       long splitRecordsNr = fastQFileWrapper.getRecordNrInChunks();
      
       
       assertTrue(fastQrecords == splitRecordsNr);
       
       
        
    }

   
}