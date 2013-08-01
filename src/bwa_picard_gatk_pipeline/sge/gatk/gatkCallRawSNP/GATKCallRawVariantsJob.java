/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bwa_picard_gatk_pipeline.sge.gatk.gatkCallRawSNP;

import bwa_picard_gatk_pipeline.GlobalConfiguration;
import bwa_picard_gatk_pipeline.enums.GATKVariantCallers;
import bwa_picard_gatk_pipeline.sge.Job;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import net.sf.samtools.SAMSequenceRecord;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author Wim Spee
 */
public class GATKCallRawVariantsJob extends Job {
    
    
    private String sgeName;   
    private List<File> sampleBams;   
    private String sampleNames;
    
    private File rawVCF;
    private SAMSequenceRecord chromosome;
    private File bedFile;
    
    
    private GlobalConfiguration gc;

    public GATKCallRawVariantsJob(List<File> sampleBams, File rawVCF, GlobalConfiguration gc, SAMSequenceRecord chromosome) throws IOException {
        super(FilenameUtils.removeExtension(rawVCF.getPath()) + "_callRawVariants.sh");     
        this.sampleBams = sampleBams;
        this.rawVCF = rawVCF;
        this.gc = gc;
        this.chromosome = chromosome;
        
        sgeThreads = gc.getGatkSGEThreads();
        
        createBedFile();
        
        addCommands();        
        
        sgeName = "callRawVariants_"+rawVCF.getName();
        close();
        
    }
    
    private void createBedFile() throws FileNotFoundException {
        
        String chromName = chromosome.getSequenceName();
        Integer bedStart = 0;
        Integer bedEnd = chromosome.getSequenceLength()-1;
        
        String rawVCFBaseName = FilenameUtils.getBaseName(rawVCF.getName());
        String bedFileName = rawVCFBaseName+".bed";
        
        bedFile = new File(rawVCF.getParentFile(), bedFileName);
        
        
        
        PrintWriter writer = new PrintWriter(bedFile );
        writer.println(chromName+"\t"+bedStart+"\t"+bedEnd);
        writer.close();
        
        
        
    }
    
    @Override    
    public String getSGEName() {
        return sgeName;
    }

    private void addCommands() throws IOException {
        
        String baseName = FilenameUtils.getBaseName(rawVCF.getPath()); 
        File logFile = new File(rawVCF.getParentFile(), baseName + "_callRawVariants.log");  
        //File tmpDir = new File("/tmp", baseName);           
        
        File metricsFile = new File(rawVCF.getParent(), FilenameUtils.getBaseName(rawVCF.getPath())+ ".metrics");
               
//        File localVCF = new File(tmpDir, rawVCF.getName());
//        File localVCFMetrics = new File(tmpDir, FilenameUtils.removeExtension(baseName)+".metrics");       
        
        String appendAlloutputToLog = " >> "+ logFile.getPath() + " 2>&1";
        
        //add sge hostname and date information to log
        addCommand("uname -n " + appendAlloutputToLog);
        addCommand("\n");
        addCommand("date " + appendAlloutputToLog);
        addCommand("\n");
        
//        //create a tmp dir
//        addCommand("mkdir " + tmpDir + appendAlloutputToLog);
//        addCommand("\n");
        
        String callReference = "";
       
        if(gc.getGatkCallReference())
        {
            callReference = " -out_mode EMIT_ALL_CONFIDENT_SITES ";
        }    
        
        String metrics = "";
        String slodString = "";  
        String multiThread = "";              
        if(gc.getgATKVariantCaller() == GATKVariantCallers.UnifiedGenotyper)
        {
            slodString = " -slod ";
            metrics = " -metrics "+metricsFile.getPath();
            multiThread = " -nt "+ gc.getGatkSGEThreads();
        }             
        
        
       
       StringBuilder inputString = new StringBuilder();
       
       for(File sampleBamFile : sampleBams)
       {
           inputString.append(" -I ");
           inputString.append(sampleBamFile.getPath());
           inputString.append(" ");
       }        
           
        //call the raw variants 
        addCommand("java "+
                    " -Xmx"+gc.getGatkSGEMemory()+"G"+  
                    " -jar "+gc.getGatk().getPath() +
                    " -T "+gc.getgATKVariantCaller()+" -A AlleleBalance -A Coverage -stand_call_conf 30.0 -stand_emit_conf 10 "+
                    " -R "+gc.getReferenceFile().getPath()+                    
                    inputString.toString()+
                    " -o "+rawVCF.getPath()+
                    " -L "+bedFile.getPath()+
                    multiThread+
                    metrics+      
                    slodString +
                    callReference +                          
                    appendAlloutputToLog);
        addCommand("\n");
        
        //copy the resutls back
//        addCommand("cp "+localVCF.getPath() +" " + rawVCF.getParentFile().getPath() + appendAlloutputToLog);
//        addCommand("cp "+localVCFMetrics.getPath() +" " + rawVCF.getParentFile().getPath()+ appendAlloutputToLog);
//        addCommand("\n");
        
        
        //remove the tmp dir from the sge host
       // addCommand("rm -rf " + tmpDir.getPath() + appendAlloutputToLog);
        addCommand("\n");
        addCommand("echo finished " + appendAlloutputToLog);
        addCommand("date  " + appendAlloutputToLog);
        
    }    

   
    
}
