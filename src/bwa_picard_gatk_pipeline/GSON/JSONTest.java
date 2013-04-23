/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bwa_picard_gatk_pipeline.GSON;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author root
 */
public class JSONTest {
    
    
     public static void main(String[] args) 
     {
     
         File jsonFile = new File("/home/wim/BWA_Picard_GATK_pipeline_test/readGroupTestCSFasta.json");
        ObjectMapper mapper = new ObjectMapper(); 
         try {
             JSONConfig jsconConfig = mapper.readValue(jsonFile, JSONConfig.class); // 'src' can be File, InputStream, Reader, String
         } catch (IOException ex) {
             Logger.getLogger(JSONTest.class.getName()).log(Level.SEVERE, null, ex);
         }
         
    
        String blaat = "blaat";
     
     }
    
    
    
}
