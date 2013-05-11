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
public class GATKCallRawVariants extends Job {
    
    
    private String sgeName;   
    private File realignedBam;   
    private File rawVCF;
    private Integer threads;
    
    private GlobalConfiguration gc;

    public GATKCallRawVariants(File realignedBam, File rawVCF, GlobalConfiguration gc, Integer threads) throws IOException {
        super(FilenameUtils.removeExtension(realignedBam.getAbsolutePath()) + "_callRawvariants.sh");     
        this.realignedBam = realignedBam;
        this.rawVCF = rawVCF;
        this.gc = gc;
        this.threads = threads;
        
        addCommands();
        
        
        sgeName = "callRawvariants_"+realignedBam.getName();
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
        
        //create a tmp dir
        addCommand("mkdir " + tmpDir);
        addCommand("\n");
           
        //call the raw variants 
        addCommand("java -jar "+gc.getGatk().getAbsolutePath() +
                    " -T UnifiedGenotyper -A AlleleBalance -A Coverage -stand_call_conf 30.0 -stand_emit_conf 10 "+
                    " -R "+gc.getReferenceFile().getAbsolutePath()+
                    " -nt "+ threads +
                    " -I "+realignedBam.getAbsolutePath()+
                    " -o "+localVCF.getAbsolutePath()+
                    " -metrics "+localVCFMetrics.getAbsolutePath()+
                    " &>> "+logFile.getAbsolutePath());
        addCommand("\n");
        
        //copy the resutls back
        addCommand("cp "+localVCF.getAbsolutePath() +" " + rawVCF.getParentFile().getAbsolutePath());
        addCommand("cp "+localVCFMetrics.getAbsolutePath() +" " + rawVCF.getParentFile().getAbsolutePath());
        addCommand("\n");
        
        
        
        //remove the tmp dir from the sge host
        addCommand("rm -rf " + tmpDir.getAbsolutePath() + " 2>> " + logFile.getAbsolutePath());
        addCommand("\n");
        addCommand("echo finished >> " + logFile.getAbsolutePath());
        addCommand("date  >> " + logFile.getAbsolutePath());
        
    }    
    
}
