/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bwa_picard_gatk_pipeline.sge.gatk.gatkAnnotateSNP.gatkCombineVariants;

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
public class GATKCombineVariants extends Job{
    
    private String sgeName;   
    private List<File> chromosomeVCFFiles;   
    private File concatenatedVCF;    
   
    
    private GlobalConfiguration gc;
    
    
      public GATKCombineVariants(List<File> chromosomeVCFFiles, File concatenatedVCF, GlobalConfiguration gc) throws IOException {
        super(FilenameUtils.removeExtension(chromosomeVCFFiles.get(0).getAbsolutePath()) + "concatenateVCF.sh");     
        this.chromosomeVCFFiles = chromosomeVCFFiles;
        this.concatenatedVCF = concatenatedVCF;
        this.gc = gc;     
        
        addCommands();        
        
        sgeName = "concatVCF"+chromosomeVCFFiles.get(0).getName();
        close();
        
    }
      
    @Override    
    public String getSGEName() {
        return sgeName;
    }

    private void addCommands() throws IOException {
        
        String baseName = FilenameUtils.getBaseName(chromosomeVCFFiles.get(0).getAbsolutePath()); 
        File logFile = new File(chromosomeVCFFiles.get(0).getParentFile(), baseName + "_concatVariants.log");  
        //File tmpDir = new File("/tmp", baseName);           
        
      //  File metricsFile = new File(rawVCF.getParent(), FilenameUtils.getBaseName(rawVCF.getAbsolutePath())+ ".metrics");
               
//        File localVCF = new File(tmpDir, rawVCF.getName());
//        File localVCFMetrics = new File(tmpDir, FilenameUtils.removeExtension(baseName)+".metrics");       
        
        String appendAlloutputToLog = " >> "+ logFile.getAbsolutePath() + " 2>&1";
        
        //add sge hostname and date information to log
        addCommand("uname -n " + appendAlloutputToLog);
        addCommand("\n");
        addCommand("date " + appendAlloutputToLog);
        addCommand("\n");
        

        StringBuilder inputString = new StringBuilder();
        inputString.append(" ");
        
        for(File chromVCF : chromosomeVCFFiles)
        {
            inputString.append("-V ");
            inputString.append(chromVCF.getAbsolutePath());
            inputString.append(" ");
        }
           
        //concat the raw variants 
        addCommand("java "+
                    " -Xmx"+gc.getGatkSGEMemory()+"G"+  
                    " -jar "+gc.getGatk().getAbsolutePath() +
                    " -T CombineVariants "+
                    " -R "+gc.getReferenceFile().getAbsolutePath()+
                    " -o "+concatenatedVCF.getAbsolutePath()+
                    inputString.toString()+
                    " -assumeIdenticalSamples"+
                    appendAlloutputToLog);
        addCommand("\n");             
        
        //remove the tmp dir from the sge host
       // addCommand("rm -rf " + tmpDir.getAbsolutePath() + appendAlloutputToLog);
        addCommand("\n");
        addCommand("echo finished " + appendAlloutputToLog);
        addCommand("date  " + appendAlloutputToLog);
    }
    
}
