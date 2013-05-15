/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bwa_picard_gatk_pipeline.fileWrappers;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Wim Spee
 */
public class CsFastaFilePairTest {

    private static final File tmpDir = new File(System.getProperty("java.io.tmpdir"));
    private static List<FastQChunk> fastQChunks;
    private static CsFastaFilePair csFastaFilePair;
    
    //expected files is based on the first 1000 lines, or 500 csfasta reacord. So the whole fastq contains 500 records, the split 200, 200 and 100
    private static File expectedFastqFile;
    private static File expectedFastqFileChunkaa;
    private static File expectedFastqFileChunkab;
    private static File expectedFastqFileChunkac;
    

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
    public void setUp() throws IOException {

        fastQChunks = new ArrayList<FastQChunk>();

        csFastaFilePair = new CsFastaFilePair();

        File csFastaFile = new File(getClass().getResource("solid0042_20110504_PE_M520newPEkit_Nico_M520_F3_first1000.csfasta").getFile());

        expectedFastqFile = new File(getClass().getResource("p1.solid0042_20110504_PE_M520newPEkit_Nico_M520_F3_first1000.fastq").getFile());
        
        expectedFastqFileChunkaa = new File(getClass().getResource("p1.solid0042_20110504_PE_M520newPEkit_Nico_M520_F3_first1000_chunkaa.fastq").getFile());
        expectedFastqFileChunkab = new File(getClass().getResource("p1.solid0042_20110504_PE_M520newPEkit_Nico_M520_F3_first1000_chunkab.fastq").getFile());
        expectedFastqFileChunkac = new File(getClass().getResource("p1.solid0042_20110504_PE_M520newPEkit_Nico_M520_F3_first1000_chunkac.fastq").getFile());
        
        
        

        csFastaFilePair.setCsFastaFile(csFastaFile);
        csFastaFilePair.lookupQualFile();
    }

    @After
    public void tearDown() {
        if (fastQChunks.isEmpty()) {
            for (FastQChunk fastQChunk : fastQChunks) {
                fastQChunk.getFastqFile().delete();
            }
        }
    }

    @Test
    public void testConvertToFastqFileExist() throws Exception {

        fastQChunks= csFastaFilePair.convertToFastQ(tmpDir, "unitTest", 1000000);
        File fastQFile = fastQChunks.get(0).getFastqFile();

        Assert.assertTrue("Fastq file " + fastQFile.getAbsolutePath() + "does not exist", fastQFile.exists());
    }

    @Test
    public void testConvertToFastqSameAmountOfRecords() throws Exception {
        fastQChunks= csFastaFilePair.convertToFastQ(tmpDir, "unitTest", 1000000);

        Long expectedFastqRecords = new Long(500);
        Long fastqRecords = fastQChunks.get(0).countNumberOfrecords();

        Assert.assertTrue("Fastq file " + fastQChunks.get(0).getFastqFile().getAbsolutePath() + "does not contains expected amount of records. Expected " + expectedFastqRecords.toString() + " found: " + fastqRecords.toString(), fastqRecords.equals(expectedFastqRecords));
    }

    @Test
    public void testConvertToFastqCSValuesMatch() throws Exception {
        fastQChunks= csFastaFilePair.convertToFastQ(tmpDir, "unitTest", 1000000);
        
        Boolean allCSLinesMatch = compareFilesByLine(expectedFastqFile, fastQChunks.get(0).getFastqFile(), 2, 4);
        
        Assert.assertTrue("Not all CS lines match between found and expected fastqFile", allCSLinesMatch);      
    }   
    
    @Test
    public void testConvertToFastqQualValuesMatch() throws Exception 
    {    
        fastQChunks= csFastaFilePair.convertToFastQ(tmpDir, "unitTest", 1000000);
        
        Boolean allCSLinesMatch = compareFilesByLine(expectedFastqFile, fastQChunks.get(0).getFastqFile(), 4, 4);
        
        Assert.assertTrue("Not all qual lines match between found and expected fastqFile", allCSLinesMatch);   
    }
    
    @Test
    public void testConvertToFastqSameAmountOfRecordsChunked() throws Exception {
        fastQChunks= csFastaFilePair.convertToFastQ(tmpDir, "unitTest", 200);

        Long expectedFastqRecordsChunkaa = new Long(200);
        Long expectedFastqRecordsChunkab = new Long(200);
        Long expectedFastqRecordsChunkac = new Long(100);
        
        Long fastqRecordsChunkAa = fastQChunks.get(0).countNumberOfrecords();
        Long fastqRecordsChunkAb = fastQChunks.get(1).countNumberOfrecords();
        Long fastqRecordsChunkAc = fastQChunks.get(2).countNumberOfrecords();

        Assert.assertTrue("Fastq chunk " + fastQChunks.get(0).getFastqFile().getAbsolutePath() + "does not contains expected amount of records. Expected " + expectedFastqRecordsChunkaa.toString() + " found: " + fastqRecordsChunkAa.toString(), fastqRecordsChunkAa.equals(expectedFastqRecordsChunkaa));
        Assert.assertTrue("Fastq chunk " + fastQChunks.get(1).getFastqFile().getAbsolutePath() + "does not contains expected amount of records. Expected " + expectedFastqRecordsChunkab.toString() + " found: " + fastqRecordsChunkAb.toString(), fastqRecordsChunkAb.equals(expectedFastqRecordsChunkab));
        Assert.assertTrue("Fastq chunk " + fastQChunks.get(2).getFastqFile().getAbsolutePath() + "does not contains expected amount of records. Expected " + expectedFastqRecordsChunkac.toString() + " found: " + fastqRecordsChunkAc.toString(), fastqRecordsChunkAc.equals(expectedFastqRecordsChunkac));
    }

    @Test
    public void testConvertToFastqCSValuesMatchChunkded() throws Exception {
        fastQChunks= csFastaFilePair.convertToFastQ(tmpDir, "unitTest", 200);
        
        Boolean allCSLinesMatchAA = compareFilesByLine(expectedFastqFileChunkaa, fastQChunks.get(0).getFastqFile(), 2, 4);
        Boolean allCSLinesMatchAB = compareFilesByLine(expectedFastqFileChunkab, fastQChunks.get(1).getFastqFile(), 2, 4);
        Boolean allCSLinesMatchAC = compareFilesByLine(expectedFastqFileChunkac, fastQChunks.get(2).getFastqFile(), 2, 4);
        
        Assert.assertTrue("Not all CS lines match between found and expected fastqFile", allCSLinesMatchAA && allCSLinesMatchAB && allCSLinesMatchAC);      
    }   
    
    @Test
    public void testConvertToFastqQualValuesMatchChunked() throws Exception 
    {    
        fastQChunks= csFastaFilePair.convertToFastQ(tmpDir, "unitTest", 200);
        
        Boolean allCSLinesMatchAA = compareFilesByLine(expectedFastqFileChunkaa, fastQChunks.get(0).getFastqFile(), 4, 4);
        Boolean allCSLinesMatchAB = compareFilesByLine(expectedFastqFileChunkab, fastQChunks.get(1).getFastqFile(), 4, 4);
        Boolean allCSLinesMatchAC = compareFilesByLine(expectedFastqFileChunkac, fastQChunks.get(2).getFastqFile(), 4, 4);
        
        Assert.assertTrue("Not all CS lines match between found and expected fastqFile", allCSLinesMatchAA && allCSLinesMatchAB && allCSLinesMatchAC);      
    }
    
    
    
    
    
    
    
    
    private Boolean compareFilesByLine(File expectedFile, File foundFile, Integer lineNrToCheck, Integer recordLineLenght )
    {
        Boolean linesAllMatch = true;
        
        
        try {
            // Open the file that is the first 
            // command line parameter
            FileInputStream fstreamFound = new FileInputStream(foundFile);
            // Get the object of DataInputStream
            DataInputStream inFound = new DataInputStream(fstreamFound);
            BufferedReader foundReader = new BufferedReader(new InputStreamReader(inFound));
            
            FileInputStream fstreamExpected = new FileInputStream(expectedFile);
            // Get the object of DataInputStream
            DataInputStream inExpected = new DataInputStream(fstreamExpected);
            BufferedReader expectedReader = new BufferedReader(new InputStreamReader(inExpected));
            
            Integer recordCounter = 1;
            Integer lineCounter = 0;
            
            String foundLine;
            //Read File Line By Line
            while ((foundLine = foundReader.readLine()) != null) {
                // Print the content on the console
                lineCounter++;
                String expectedLine = expectedReader.readLine();
                if(lineCounter==lineNrToCheck )
                {
                    if(!foundLine.equals(expectedLine))
                    {
                        linesAllMatch = false;
                    }
                }
                if(lineCounter==recordLineLenght )
                {
                    lineCounter =0;
                    recordCounter++;
                }  
                
            }
            //Close the input stream
            inFound.close();
            inExpected.close();
        } catch (Exception e) {//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }
    
        return linesAllMatch;   
    
    }
    
    
   
    
    
    
    
    
    
    
    
    
    
    
}