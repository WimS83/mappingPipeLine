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
public class GATKCallRawVariantsJob extends Job {
    
    
    private String sgeName;   
    private File realignedBam;   
    private File rawVCF;
    
    
    private GlobalConfiguration gc;

    public GATKCallRawVariantsJob(File realignedBam, File rawVCF, GlobalConfiguration gc ) throws IOException {
        super(FilenameUtils.removeExtension(realignedBam.getAbsolutePath()) + "_callRawVariants.sh");     
        this.realignedBam = realignedBam;
        this.rawVCF = rawVCF;
        this.gc = gc;
        
        
        addCommands();        
        
        sgeName = "callRawVariants_"+realignedBam.getName();
        close();
        
    }
    
    @Override    
    public String getSGEName() {
        return sgeName;
    }

    private void addCommands() throws IOException {
        
        String baseName = FilenameUtils.getBaseName(realignedBam.getAbsolutePath()); 
        File logFile = new File(realignedBam.getParentFile(), baseName + "_callRawVariants.log");  
        File tmpDir = new File("/tmp", baseName);           
               
        File localVCF = new File(tmpDir, rawVCF.getName());
        File localVCFMetrics = new File("/tmp", FilenameUtils.removeExtension(baseName)+".metrics");       
        
        String appendAlloutputToLog = " >> "+ logFile.getAbsolutePath() + " 2>&1";
        
        //add sge hostname and date information to log
        addCommand("uname -n >> " + logFile.getAbsolutePath());
        addCommand("\n");
        addCommand("date >> " + logFile.getAbsolutePath());
        addCommand("\n");
        
        //create a tmp dir
        addCommand("mkdir " + tmpDir + appendAlloutputToLog);
        addCommand("\n");
        
        String callReference = "";
        if(gc.getGatkCallReference())
        {
            callReference = " -out_mode EMIT_ALL_CONFIDENT_SITES ";
        }        
           
        //call the raw variants 
        addCommand("java "+
                    " -Xmx"+gc.getGatkSGEMemory()+"G"+  
                    " -jar "+gc.getGatk().getAbsolutePath() +
                    " -T UnifiedGenotyper -A AlleleBalance -A Coverage -stand_call_conf 30.0 -stand_emit_conf 10 "+
                    " -R "+gc.getReferenceFile().getAbsolutePath()+
                    " -nt "+ gc.getGatkSGEThreads() +
                    " -I "+realignedBam.getAbsolutePath()+
                    " -o "+localVCF.getAbsolutePath()+
                    " -metrics "+localVCFMetrics.getAbsolutePath()+
                    " -slod "+
                    callReference +                          
                    appendAlloutputToLog);
        addCommand("\n");
        
        //copy the resutls back
        addCommand("cp "+localVCF.getAbsolutePath() +" " + rawVCF.getParentFile().getAbsolutePath() + appendAlloutputToLog);
        addCommand("cp "+localVCFMetrics.getAbsolutePath() +" " + rawVCF.getParentFile().getAbsolutePath()+ appendAlloutputToLog);
        addCommand("\n");
        
        
        //remove the tmp dir from the sge host
        addCommand("rm -rf " + tmpDir.getAbsolutePath() + appendAlloutputToLog);
        addCommand("\n");
        addCommand("echo finished " + appendAlloutputToLog);
        addCommand("date  " + appendAlloutputToLog);
        
    }    
    
}
