/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bwa_picard_gatk_pipeline;

import bwa_picard_gatk_pipeline.fileWrappers.CsFastaFilePair;
import bwa_picard_gatk_pipeline.fileWrappers.FastQFile;
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
 * @author root
 */
public class CsFastaFilePairTest {
    
    
    private static final File tmpDir = new File(System.getProperty("java.io.tmpdir"));
    private static FastQFile fastqFile;
    
    public CsFastaFilePairTest() {
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
        if(fastqFile != null){   fastqFile.getFastqFile().delete(); }
        
    }

    /**
     * Test of SetFilesBasedOnCsFastaFilePath method, of class CsFastaFilePair.
     */
    
    @Test
    public void testcountRecordOpeningsInCsFasta() throws Exception {
        
        File csFastaFile = new File(getClass().getResource("solid0042_20110504_PE_M520newPEkit_Nico_M520_F3_first1000.csfasta").getFile());
        File qualFile = new File(getClass().getResource("solid0042_20110504_PE_M520newPEkit_Nico_M520_F3_first1000_QV.qual").getFile());
        File fastqFile = new File(getClass().getResource("p1.solid0042_20110504_PE_M520newPEkit_Nico_M520_F3_first1000.fastq").getFile());               
        
        CsFastaFilePair csFastaFilePair = new CsFastaFilePair();
        
        long nrCsFastaEntries = csFastaFilePair.countRecordOpeningsInFile(csFastaFile);
        
        assertTrue(nrCsFastaEntries==500);          
        
    }
    
    @Test
    public void testcountRecordOpeningsInQual() throws Exception {
        
        File csFastaFile = new File(getClass().getResource("solid0042_20110504_PE_M520newPEkit_Nico_M520_F3_first1000.csfasta").getFile());
        File qualFile = new File(getClass().getResource("solid0042_20110504_PE_M520newPEkit_Nico_M520_F3_first1000_QV.qual").getFile());
        File fastqFile = new File(getClass().getResource("p1.solid0042_20110504_PE_M520newPEkit_Nico_M520_F3_first1000.fastq").getFile());               
        
        CsFastaFilePair csFastaFilePair = new CsFastaFilePair();
        
        long nrCsFastaEntries = csFastaFilePair.countRecordOpeningsInFile(qualFile);
        
        assertTrue(nrCsFastaEntries==500);  
    }
    
    
    @Test
    public void testConvertToFastq() throws Exception {
        
        CsFastaFilePair csFastaFilePair = new CsFastaFilePair();
        
        File csFastaFile = new File(getClass().getResource("solid0042_20110504_PE_M520newPEkit_Nico_M520_F3_first1000.csfasta").getFile());
        File converter = new File(getClass().getResource("csfastaToFastq").getFile());
        csFastaFilePair.setCsFastaFile(csFastaFile);
        csFastaFilePair.lookupQualFile();        
        
        csFastaFilePair.convertToFastQ(tmpDir);
        
        
        
        
        
    
        
    
    }
    
    
    
}