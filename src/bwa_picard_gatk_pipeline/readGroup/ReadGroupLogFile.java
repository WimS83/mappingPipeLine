/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bwa_picard_gatk_pipeline.readGroup;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author Wim Spee
 */
public class ReadGroupLogFile {
    
    
    private BufferedWriter logOut;
    private String readGroupId;

    public ReadGroupLogFile(File outputDir, String readGroupId) {
        
        this.readGroupId = readGroupId; 
        
        try {
            
            DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
            Date date = new Date();
            String dateString= (dateFormat.format(date));
            
            
            File outputFile = new File(outputDir, readGroupId + dateString+".log");
            FileWriter fstream = new FileWriter(outputFile);
            logOut = new BufferedWriter(fstream);
        } catch (IOException ex) {
            System.out.println("Cannot create logfile for read group " + readGroupId);
        }
    }  
   
    
    public void append(String string) {
        try {
            logOut.write(string);
            logOut.write("\n");
            logOut.flush();
            System.out.println(string);
        } catch (IOException ex) {
            System.out.println("Canont write log file for read group " + readGroupId + " : " + ex.getMessage());
        }
    }

    public void close() {
        try {
            logOut.close();
        } catch (IOException ex) {
            System.out.println("Could not close log file: " + ex.getMessage());
        }
    }
    
    
}
