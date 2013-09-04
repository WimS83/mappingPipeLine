/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bwa_picard_gatk_pipeline.sge.ilumina.fastqc;

import bwa_picard_gatk_pipeline.sge.Job;
import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author wim
 */
public class FastQCJob extends Job {
    
    private String sgeName;    
    private File fastqFile;
    private File outputDir;
    private File fastQCFile;
    
    
     public FastQCJob(File fastqFile, File outputDir, File fastQCFile) throws IOException {

        super(FilenameUtils.removeExtension(fastqFile.getAbsolutePath()) + "fastQC.sh");

        this.fastqFile = fastqFile;
        this.outputDir = outputDir;     
        this.fastQCFile = fastQCFile;        
        
        addCommands();

        sgeName = "FastQC" + fastqFile.getName();
        close();
    }     
     
    @Override
    public String getSGEName() {
        return sgeName;
    }    
    
    private void addCommands() throws IOException 
    {
        String baseName = FilenameUtils.getBaseName(fastqFile.getName());
        
        File logFile = new File(outputDir, baseName + "_FastQC.log");      
        String appendAlloutputToLog = " >> "+ logFile.getAbsolutePath() + " 2>&1";
        
//        File tmpDir;
//        
//        if(gc.getOffline())
//        {
//            tmpDir = gc.getTmpDir();
//        }
//        else
//        {
//            tmpDir = new File("/tmp/");
//        }    
       
      // File picardMergeSam = new File(gc.getPicardDirectory(), "MergeSamFiles.jar");
        
        
        //add sge hostname and date information to log
        addCommand("uname -n " + appendAlloutputToLog);
        addCommand("\n");
        addCommand("date " + appendAlloutputToLog);
        addCommand("\n");     
        
        addCommand(fastQCFile.getAbsolutePath() +" -o "+outputDir.getAbsolutePath() +" "+fastqFile.getAbsolutePath());
        
    }
    
}
