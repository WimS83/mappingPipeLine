/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bwa_picard_gatk_pipeline.sge;

import bwa_picard_gatk_pipeline.GlobalConfiguration;
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
        
        super(FilenameUtils.removeExtension(dedupBam.getAbsolutePath()) + "_realignIndels.sh");        
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
        String baseName = FilenameUtils.getBaseName(dedupBam.getAbsolutePath()); 
        File logFile = new File(dedupBam.getParentFile(), baseName + "_realign.log");  
        File tmpDir = new File("/tmp", baseName);    
        
        File localdedupBam = new File(tmpDir, dedupBam.getName());
        File remoteIndex = new File(dedupBam.getParentFile(), baseName+".bai");
        File localIndex = new File(localdedupBam.getParentFile(), baseName+".bai");
        
        String appendAlloutputToLog = " >> "+ logFile.getAbsolutePath() + " 2>&1";
        
        
        File realignTargets = new File(FilenameUtils.removeExtension(localdedupBam.getAbsolutePath()) + "_realignTargets.intervals"); 
        
               
        //add sge hostname and date information to log
        addCommand("uname -n  " + appendAlloutputToLog);
        addCommand("\n");
        addCommand("date " + appendAlloutputToLog);
        addCommand("\n");
        
        //create a tmp dir
        addCommand("mkdir " + tmpDir + appendAlloutputToLog);
        addCommand("\n");
        
        //copy the bam to the node
        addCommand("cp " + dedupBam.getAbsolutePath() +" "+localdedupBam.getAbsolutePath() + appendAlloutputToLog);  
        addCommand("cp " + remoteIndex.getAbsolutePath() +" "+localIndex.getAbsolutePath() + appendAlloutputToLog);  
        
        String knownIndels = "";
        if(gc.getKnownIndels()!= null)
        {
            knownIndels = "-known "+gc.getKnownIndels().getAbsolutePath();
        }        
        
        addCommand("java"+
                    " -Xmx"+gc.getGatkSGEMemory()+"G"+
                    " -jar "+gc.getGatk().getAbsolutePath() +
                    " -T RealignerTargetCreator "+
                    " -R "+gc.getReferenceFile().getAbsolutePath()+
                    " -I "+localdedupBam.getAbsolutePath()+
                    " -o "+realignTargets.getAbsolutePath()+knownIndels +
                    appendAlloutputToLog);
        
        addCommand("java"+
                    " -Xmx"+gc.getGatkSGEMemory()+"G"+
                    " -jar "+gc.getGatk().getAbsolutePath() +
                    " -T IndelRealigner "+
                    " -R "+gc.getReferenceFile().getAbsolutePath()+
                    " -I "+localdedupBam.getAbsolutePath()+
                    " -targetIntervals "+realignTargets.getAbsolutePath() + 
                    " -o "+realignedBam.getAbsolutePath()+
                    knownIndels +
                    appendAlloutputToLog);
        
        addCommand("\n");
        //remove the tmp dir from the sge host
        addCommand("rm -rf " + tmpDir.getAbsolutePath() + appendAlloutputToLog);
        addCommand("\n");
        addCommand("echo finished " + appendAlloutputToLog);
        addCommand("date " + appendAlloutputToLog);
                 
    
    }
    
    
}
