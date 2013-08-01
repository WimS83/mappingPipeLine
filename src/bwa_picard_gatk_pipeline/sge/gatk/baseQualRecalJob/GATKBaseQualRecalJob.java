/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bwa_picard_gatk_pipeline.sge.gatk.baseQualRecalJob;

import bwa_picard_gatk_pipeline.GlobalConfiguration;
import bwa_picard_gatk_pipeline.sge.Job;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author Wim Spee
 */
public class GATKBaseQualRecalJob extends Job {
    
    private String sgeName;    
    private File realignedBam;
    private File bqsrBam;   
    
    private GlobalConfiguration gc;
    

    public GATKBaseQualRecalJob(File realignedBam, File bqsrBam, GlobalConfiguration gc) throws IOException {        
        
        super(FilenameUtils.removeExtension(realignedBam.getPath()) + "_bqsr.sh");        
        this.gc = gc;        
        this.realignedBam = realignedBam;
        this.bqsrBam = bqsrBam;
        
        sgeThreads = gc.getGatkSGEThreads();
        
        addCommands();
        
        
        sgeName = "bqsr"+realignedBam.getName();
        close();
    }
    
    
    
    
    @Override    
    public String getSGEName() {
        return sgeName;
    }
    
    private void addCommands() throws IOException 
    {
        String baseName = FilenameUtils.getBaseName(realignedBam.getPath()); 
        File logFile = new File(realignedBam.getParentFile(), baseName + "_bqsr.log");  
       // File tmpDir = new File("/tmp", baseName);          
        
        File recalibrationReport = new File(FilenameUtils.removeExtension(realignedBam.getPath()) + "_ recalibration_report.grp"); 
        
        String appendAlloutputToLog = " >> "+ logFile.getPath() + " 2>&1";
               
        //add sge hostname and date information to log
        addCommand("uname -n >> " + logFile.getPath());
        addCommand("\n");
        addCommand("date >> " + logFile.getPath());
        addCommand("\n");
        
//        //create a tmp dir
//        addCommand("mkdir " + tmpDir);
//        addCommand("\n");
          StringBuilder knownSitesSB = new StringBuilder();
         
          if(gc.getKnownSNP() != null)
          {
              knownSitesSB.append(" -knownSites ");
              knownSitesSB.append(gc.getKnownSNP().getPath());              
          }
          if(gc.getKnownIndels()!= null)
          {
              knownSitesSB.append(" -knownSites ");
              knownSitesSB.append(gc.getKnownIndels().getPath());              
          }        
//       
        addCommand( "java "+
                    " -Xmx"+gc.getGatkSGEMemory()+"G"+
                    " -jar "+gc.getGatk().getPath() +
                    " -T BaseRecalibrator " +
                    " -R "+gc.getReferenceFile().getPath()+                  
                    " -I "+realignedBam.getPath()+ 
                    " -o "+recalibrationReport.getPath()+   
                    knownSitesSB.toString()+        
                    appendAlloutputToLog);        
        
    
        
        addCommand("\n");
        
       
        
        addCommand( "java "+
                    " -Xmx"+gc.getGatkSGEMemory()+"G"+
                    " -jar "+gc.getGatk().getPath() +
                    " -T PrintReads " +
                    " -R "+gc.getReferenceFile().getPath()+                  
                    " -I "+realignedBam.getPath()+ 
                    " -BQSR "+recalibrationReport.getPath()+
                    " -o "+bqsrBam.getPath()+
                    appendAlloutputToLog);        
        
    
        
        addCommand("\n");
        //remove the tmp dir from the sge host
      //  addCommand("rm -rf " + tmpDir.getPath() + appendAlloutputToLog);
    //    addCommand("\n");
        addCommand("echo finished " + appendAlloutputToLog);
        addCommand("date " + appendAlloutputToLog);
                 
    
    }
    
    
}
