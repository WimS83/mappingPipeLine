/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bwa_picard_gatk_pipeline.sge.solid.BWA.dedupJob;

import bwa_picard_gatk_pipeline.GlobalConfiguration;
import bwa_picard_gatk_pipeline.sge.Job;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author wim
 */
public class PicardDedupBamJob extends Job{
    
    
    private String sgeName;      
    private File mergedBam;
    private File dedupBam;
    private GlobalConfiguration gc;    
    
    public PicardDedupBamJob(File mergedBam, File dedupBam,  GlobalConfiguration gc) throws IOException {
        super(FilenameUtils.removeExtension(mergedBam.getAbsolutePath()) + ".sh");
        

        this.mergedBam = mergedBam;
        this.dedupBam = dedupBam;
        this.gc = gc;

        addCommands();

        sgeName = "dedup_" + mergedBam.getName();
        close();
    }
    
    
    
    @Override
    public String getSGEName() {
        return sgeName;
    }

    private void addCommands() throws IOException {
        
        String baseName = FilenameUtils.getBaseName(dedupBam.getName());
        
        File logFile = new File(mergedBam.getParentFile(), baseName + "_PicardDedupBam.log");    
        File metricsFile = new File(mergedBam.getParentFile(), baseName + ".metrics");
        
        
        String appendAlloutputToLog = " >> "+ logFile.getAbsolutePath() + " 2>&1";
        
        File tmpDir;
        
        if(gc.getOffline())
        {
            tmpDir = gc.getTmpDir();
        }
        else
        {
            tmpDir = new File("/tmp/");
        }    
       
        File picardMarkDuplicates = new File(gc.getPicardDirectory(), "MarkDuplicates.jar");        
        
        //add sge hostname and date information to log
        addCommand("uname -n " + appendAlloutputToLog);
        addCommand("\n");
        addCommand("date " + appendAlloutputToLog);
        addCommand("\n");
        //create a tmp dir
        addCommand("mkdir " + tmpDir + appendAlloutputToLog);
        addCommand("\n");
        
      
        
        addCommand( " java -jar "+picardMarkDuplicates.getAbsolutePath() +
                    " I="+mergedBam.getAbsolutePath() +
                    " O="+dedupBam.getAbsolutePath() +
                    " M="+ metricsFile.getAbsolutePath() + 
                    " TMP_DIR="+tmpDir.getAbsolutePath()+
                    " VALIDATION_STRINGENCY=LENIENT CREATE_INDEX=true " + appendAlloutputToLog);
        
        
    }
    
    
    
    
    
    
    
}
