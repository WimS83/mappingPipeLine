/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bwa_picard_gatk_pipeline.sge.gatk.realignJob;

import bwa_picard_gatk_pipeline.GlobalConfiguration;
import bwa_picard_gatk_pipeline.sge.Job;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author Wim Spee
 */
public class GATKRealignIndelsJob extends Job {
    
    private String sgeName;   
    private File dedupBam;   
    private File realignedBam;
    
    private GlobalConfiguration gc;
    

    public GATKRealignIndelsJob(File dedupBam, File realignedBam, GlobalConfiguration gc) throws IOException {        
        
        super(FilenameUtils.removeExtension(dedupBam.getPath()) + "_realignIndels.sh");        
        this.gc = gc;
        this.dedupBam = dedupBam;
        this.realignedBam = realignedBam;
        
        sgeThreads = gc.getGatkSGEThreads();
        
        addCommands();
        
        
        sgeName = "realignIndels_"+dedupBam.getName();
        close();
    }
    
    
    
    
    @Override    
    public String getSGEName() {
        return sgeName;
    }
    
    private void addCommands() throws IOException 
    {
        String baseName = FilenameUtils.getBaseName(dedupBam.getPath()); 
        File logFile = new File(dedupBam.getParentFile(), baseName + "_realign.log");  
       // File tmpDir = new File("/tmp", baseName);          
        
        File realignTargets = new File(FilenameUtils.removeExtension(dedupBam.getPath()) + "_realignTargets.intervals"); 
        
        String appendAlloutputToLog = " >> "+ logFile.getPath() + " 2>&1";
               
        //add sge hostname and date information to log
        addCommand("uname -n >> " + logFile.getPath());
        addCommand("\n");
        addCommand("date >> " + logFile.getPath());
        addCommand("\n");
        
//        //create a tmp dir
//        addCommand("mkdir " + tmpDir);
//        addCommand("\n");
//       

        
        String knownIndels = "";
        if(gc.getKnownIndels()!= null)
        {
            knownIndels = "-known "+gc.getKnownIndels().getPath();
        }        
        
        addCommand( "java "+
                    " -Xmx"+gc.getGatkSGEMemory()+"G"+
                    " -jar "+gc.getGatk().getPath() +
                    " -T RealignerTargetCreator " +
                    " -R "+gc.getReferenceFile().getPath()+
                    " -I "+dedupBam.getPath()+ 
                    " -o "+realignTargets.getPath()+knownIndels +
                    appendAlloutputToLog);
        
        
        addCommand( "java "+
                    " -Xmx"+gc.getGatkSGEMemory()+"G " +
                    " -jar "+gc.getGatk().getPath() +
                    " -T IndelRealigner "+
                    " -R "+gc.getReferenceFile().getPath()+
                    " -I "+dedupBam.getPath()+
                    " -targetIntervals "+realignTargets.getPath() + 
                    " -o "+realignedBam.getPath()+knownIndels +
                    appendAlloutputToLog);
        
        addCommand("\n");
        //remove the tmp dir from the sge host
      //  addCommand("rm -rf " + tmpDir.getPath() + appendAlloutputToLog);
    //    addCommand("\n");
        addCommand("echo finished " + appendAlloutputToLog);
        addCommand("date " + appendAlloutputToLog);
                 
    
    }
    
    
}
