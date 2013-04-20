/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bwa_picard_gatk_pipeline;

import bwa_picard_gatk_pipeline.sge.CommandLineClass;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.configuration.ConfigurationException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 *
 * @author Wim Spee
 */
public class BWA_PICARD_GATK_PipelineTest {
    
    public BWA_PICARD_GATK_PipelineTest() {
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
     * Test of main method, of class BWA_PICARD_GATK_Pipeline.
     */
    @Test
    public void testReadReadGroup() {
       
       File readGroupFile = new File(getClass().getResource("readGroupTestInputFile.csv").getFile());
       Map<String, ReadGroup> readGroups = null;
       
        try {       
            readGroups = CommandLineClass.readReadGroupsFile(readGroupFile);
            
            String blaat = "blaat";
            
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } catch (ConfigurationException ex) {
            System.out.println(ex.getMessage());
        }
        
        
      
                          
     
        
        
        
        
    }
}