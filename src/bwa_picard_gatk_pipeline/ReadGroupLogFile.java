/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bwa_picard_gatk_pipeline;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

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
            File outputFile = new File(outputDir, readGroupId + ".log");
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
