/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.picard.sam;

import java.io.File;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author wim
 */
public class PicardBamMergerTest {
    
    public PicardBamMergerTest() {
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
     * Test of mergeBamFilesUsingPicard method, of class PicardBamMerger.
     */
    @Test
    public void testMergeBamFilesUsingPicard() throws Exception {
        
        File dir = new File("/home/wim/Analysis/ratfounders/pipelineTest/testOutput/testOutput/F334/F334_F1/F3");
        String[] extensions = new String[] { "bam" };
        List<File> bamFileList = (List<File>) FileUtils.listFiles(dir, extensions, true);
        
        PicardBamMerger picardBamMerger = new PicardBamMerger();        
        picardBamMerger.mergeBamFilesUsingPicard(bamFileList, new File("/tmp/"));
        
        
        
        
    }
}