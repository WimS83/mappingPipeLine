/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bwa_picard_gatk_pipeline.fileWrappers;

import java.io.File;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author root
 */
public class FastQFileTest {
    
    public FastQFileTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
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
       
       fastQFileWrapper.splitFastQFile(new Long(500));
       
       List<File> splitFastQfiles = fastQFileWrapper.getSplitFastQFiles();
       
       assertTrue(splitFastQfiles.size() == 4)
        
    }

   
}