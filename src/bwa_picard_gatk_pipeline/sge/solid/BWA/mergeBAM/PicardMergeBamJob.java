/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bwa_picard_gatk_pipeline.sge.solid.BWA.mergeBAM;

import bwa_picard_gatk_pipeline.GlobalConfiguration;
import bwa_picard_gatk_pipeline.sge.Job;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author wim
 */
public class PicardMergeBamJob extends Job {
    
    private String sgeName;  
    private List<File> bamFilesToMerge;
    private File mergedBam;
    private GlobalConfiguration gc;
    
     public PicardMergeBamJob(List<File> bamFilesToMerge, File mergedBam, String hostNameArg, GlobalConfiguration gc) throws IOException {

        super(FilenameUtils.removeExtension(mergedBam.getAbsolutePath()) + "_mergeBam.sh");

        this.bamFilesToMerge = bamFilesToMerge;
        this.mergedBam = mergedBam;     
        this.gc = gc;
        
        hostName = hostNameArg;

        addCommands();

        sgeName = "MergeBam_" + mergedBam.getName();
        close();
    }     
     
    @Override
    public String getSGEName() {
        return sgeName;
    }    
    
    private void addCommands() throws IOException 
    {
        String baseName = FilenameUtils.getBaseName(mergedBam.getName());
        
        File logFile = new File(mergedBam.getParentFile(), baseName + "_PicardMergeBams.log");      
        String appendAlloutputToLog = " >> "+ logFile.getAbsolutePath() + " 2>&1";
        File tmpDir = gc.getTmpDir();
        File picardMergeSam = new File(gc.getPicardDirectory(), "MergeSamFiles.jar");
        
        
        //add sge hostname and date information to log
        addCommand("uname -n " + appendAlloutputToLog);
        addCommand("\n");
        addCommand("date " + appendAlloutputToLog);
        addCommand("\n");
        //create a tmp dir
        addCommand("mkdir " + tmpDir + appendAlloutputToLog);
        addCommand("\n");
        
        StringBuilder inputString = new StringBuilder();
        inputString.append(" ");
        for(File bamFile : bamFilesToMerge)
        {
            inputString.append("I=");
            inputString.append(bamFile.getAbsolutePath());
            inputString.append(" ");
        }
        
        addCommand("java -jar "+picardMergeSam.getAbsolutePath() +inputString.toString() +" O="+ mergedBam.getAbsolutePath() + " TMP_DIR="+tmpDir.getAbsolutePath()+ " VALIDATION_STRINGENCY=LENIENT CREATE_INDEX=true USE_THREADING=true " + appendAlloutputToLog);
        
    }
    
    
}
