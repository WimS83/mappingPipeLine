/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.picard.sam;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author wim
 */
public class PicardCompareBamViaCommandLine {
    
    
    public Boolean compareBamFiles(File bam1, File bam2, File picardCompareSam) throws IOException, InterruptedException
    {
        
        List<String> command = new ArrayList<String>();
        command.add(picardCompareSam.getAbsolutePath());
        command.add(bam1.getAbsolutePath());
        command.add(bam2.getAbsolutePath());

        ProcessBuilder builder = new ProcessBuilder(command);
        Process process = builder.start();
        //builder.w        
                
        InputStream is = process.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        
        String bamFilesMatchString = "SAM files match.";
        Boolean bamFilesMatch = false;
        
        String line;
        while ((line = br.readLine()) != null) {
            if(line.equalsIgnoreCase(bamFilesMatchString)){bamFilesMatch= true;}            
            System.out.println(line);
        }
        System.out.println("Program terminated!");
        
        return bamFilesMatch;              
    }
    
    
}
