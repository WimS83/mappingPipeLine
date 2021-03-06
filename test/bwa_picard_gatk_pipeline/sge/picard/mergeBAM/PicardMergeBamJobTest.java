/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bwa_picard_gatk_pipeline.sge.picard.mergeBAM;

import bwa_picard_gatk_pipeline.sge.picard.mergeBAM.PicardMergeBamJob;
import bwa_picard_gatk_pipeline.sge.solid.BWA.mappingJob.BwaSolidMappingJobTest;
import bwa_picard_gatk_pipeline.GlobalConfiguration;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.picard.sam.PicardCompareBam;
import net.sf.picard.sam.PicardCompareBamViaCommandLine;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author wim
 */
public class PicardMergeBamJobTest {
    
    private static final File tmpDir = new File(System.getProperty("java.io.tmpdir"));
    private static File outputDir;
    
    public PicardMergeBamJobTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        
           Assert.assertTrue("Unable to create " + tmpDir.getAbsolutePath(), tmpDir.exists() || tmpDir.mkdirs());
       
       outputDir = new File(tmpDir, "mergeBamsJobUnitTest"); 
       
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
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Merge bams Test
     */
    @Test
    public void mergeBamsTest() {
      
        File expectedPairedBamFile = new File(getClass().getResource("expectedPairedBamFile.bam").getFile());
        File fragmentBamFile = new File(getClass().getResource("LE_F1.bam").getFile());
        File expectedMergedBamFile = new File(getClass().getResource("LE_expectedMerged.bam").getFile());
        
        File tmpFFile = new File(outputDir, fragmentBamFile.getName());     
        File tmpPEFile = new File(outputDir, expectedPairedBamFile.getName()); 
        try {
            FileUtils.copyFile(fragmentBamFile, tmpFFile);            
            FileUtils.copyFile(expectedPairedBamFile, tmpPEFile);     
        } catch (IOException ex) {
            Logger.getLogger(BwaSolidMappingJobTest.class.getName()).log(Level.SEVERE, null, ex);
        } 
        
        
        
        File foundMergedBamFile = new File(outputDir, "LE_foundMerged.bam");
        
        GlobalConfiguration gc = new GlobalConfiguration();
        File picardDir = new File("/home/wim/NetBeansProjects/java_libs/picard-tools-1.89/");
        File picardCompareBam = new File(picardDir, "CompareSAMs.jar");
        
        if(!picardCompareBam.canExecute())
        {
            fail("Cannot execute Picard compare bam on location "+ picardCompareBam.getAbsolutePath());
        }
        
        gc.setPicardDirectory(picardDir);
        gc.setOffline(true);
        gc.setTmpDir(tmpDir);        
        
        List<File> bamFiles = new ArrayList<File>();
        bamFiles.add(tmpFFile);
        bamFiles.add(tmpPEFile);
        try {
            File picardMergeSam = new File(gc.getPicardDirectory(), "MergeSamFiles.jar");
            
            PicardMergeBamJob picardMergeBamJob = new PicardMergeBamJob(bamFiles, foundMergedBamFile, null, gc.getTmpDir(), picardMergeSam);
            picardMergeBamJob.executeOffline();
            picardMergeBamJob.waitForOfflineExecution();                  
        } catch (IOException ex) {
            Logger.getLogger(PicardMergeBamJobTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(PicardMergeBamJobTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        PicardCompareBamViaCommandLine picardCompareBamViaCommandLine = new PicardCompareBamViaCommandLine();
        Boolean equal = false;
        try {
            equal = picardCompareBamViaCommandLine.compareBamFiles(expectedMergedBamFile, foundMergedBamFile, picardCompareBam);
        } catch (IOException ex) {
            Logger.getLogger(PicardMergeBamJobTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(PicardMergeBamJobTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        assertTrue("Expected and found merged bam file are not equal", equal);
        
        

        
    }
}