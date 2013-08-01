/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bwa_picard_gatk_pipeline.sge.gatk.gatkAnnotateSNP;

import bwa_picard_gatk_pipeline.GlobalConfiguration;
import bwa_picard_gatk_pipeline.sge.Job;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author wim
 */
public class GATKAnnotateVariantsJob extends Job{
    
    private String sgeName;   
    private File annotatedVCF;   
    private File rawVCF;
    
    private GlobalConfiguration gc;
    
    public GATKAnnotateVariantsJob(File rawVCF, File annotatedVCF, GlobalConfiguration gc ) throws IOException {
        super(FilenameUtils.removeExtension(rawVCF.getPath()) + "_annotateVariants.sh");     
        this.rawVCF = rawVCF;
        this.annotatedVCF = annotatedVCF;
        this.gc = gc;
        
        addCommands();        
        
        sgeThreads = gc.getGatkSGEThreads();
        
        sgeName = "annotateVariants_"+rawVCF.getName();
        close();        
    }
    
    @Override    
    public String getSGEName() {
        return sgeName;
    }

    private void addCommands() throws IOException {
        
        String baseName = FilenameUtils.getBaseName(rawVCF.getPath()); 
        File logFile = new File(rawVCF.getParentFile(), baseName + "_annotateVariants.log");  
      //  File tmpDir = new File("/tmp", baseName);           
               
       // File localAnnotatedVCF = new File(tmpDir, annotatedVCF.getName());          
        
        String appendAlloutputToLog = " >> "+ logFile.getPath() + " 2>&1";
        
        //add sge hostname and date information to log
        addCommand("uname -n " + appendAlloutputToLog);
        addCommand("\n");
        addCommand("date " + appendAlloutputToLog);
        addCommand("\n");
        
//        //create a tmp dir
//        addCommand("mkdir " + tmpDir + appendAlloutputToLog);
//        addCommand("\n");
        
        String clusterAnnotation = "";
        if(!gc.getGatkCallReference())
        {
            clusterAnnotation = " --clusterWindowSize 10 ";
        }
        
           
        //annotate raw variants 
        addCommand( "java "+
                    " -Xmx"+gc.getGatkSGEMemory()+"G"+  
                    " -jar "+gc.getGatk().getPath() +
                    " -T VariantFiltration "+
                    " -R "+gc.getReferenceFile().getPath()+                  
                    " -V "+rawVCF.getPath()+
                    " -o "+annotatedVCF.getPath()+
                    clusterAnnotation+                    
                    " --filterExpression \"MQ0 >= 4 && ((MQ0 / (1.0 * DP)) > 0.1)\" --filterName \"HARD_TO_VALIDATE\" "+
                    " --filterExpression \"DP < 5 \" --filterName \"LowCoverage\" "+
                    " --filterExpression \"QUAL < 30.0 \" --filterName \"VeryLowQual\" "+
                    " --filterExpression \"QUAL > 30.0 && QUAL < 50.0 \" --filterName \"LowQual\" "+
                    " --filterExpression \"QD < 1.5 \" --filterName \"LowQD\" "+
                    " --filterExpression \"SB > -10.0 \" --filterName \"StrandBias\" "+                              
                    appendAlloutputToLog);
        addCommand("\n");
        
        //copy the resutls back
//        addCommand("cp "+localAnnotatedVCF.getPath() +" " + rawVCF.getParentFile().getPath() + appendAlloutputToLog);        
//        addCommand("\n");
        
        
        //remove the tmp dir from the sge host
        //addCommand("rm -rf " + tmpDir.getPath() + appendAlloutputToLog);
        addCommand("\n");
        addCommand("echo finished " + appendAlloutputToLog);
        addCommand("date  " + appendAlloutputToLog);
        
    }    
     
     
     
    
    
    
    
    
}
