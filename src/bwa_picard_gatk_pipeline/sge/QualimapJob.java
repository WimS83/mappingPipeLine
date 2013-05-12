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
 * @author wim
 */
public class QualimapJob extends Job {
    
    private String sgeName;   
    private File bam;   
    private File qualimapReport;
    private GlobalConfiguration gc;

    public QualimapJob(File bam, File qualimapReport, GlobalConfiguration gc, String hostNameArg) throws IOException {
        super(FilenameUtils.removeExtension(bam.getAbsolutePath()) + "_qualimap.sh");     
        this.bam = bam;
        this.qualimapReport = qualimapReport;
        this.gc = gc;
        
        sgeName = "qualimap_"+bam.getName();
        
        hostName = hostNameArg;
        
        sgeThreads = gc.getQualimapSGEThreads();
        
        addCommands();
        close();                
        
    }
    
    @Override    
    public String getSGEName() {
        return sgeName;
    }

    private void addCommands() throws IOException {
        
        String baseName = FilenameUtils.getBaseName(bam.getAbsolutePath()); 
        File logFile = new File(bam.getParentFile(), baseName + "_qualimap.log");  
        File tmpDir = new File("/tmp", baseName);
        File localQualimapReport = new File(tmpDir, "report.pdf");
        
        String appendAlloutputToLog = " >> "+ logFile.getAbsolutePath() + " 2>&1";
        
        
        //create a tmp dir
        addCommand("mkdir " + tmpDir + appendAlloutputToLog);
        addCommand("\n");
        
        //call the raw variants 
        addCommand(gc.getQualiMap().getAbsolutePath() +
                    " bamqc "+
                    " -bam "+bam.getAbsolutePath()+
                    " -outdir "+ tmpDir.getAbsolutePath() +
                    " -outformat PDF"+
                    " -nt "+gc.getQualimapSGEThreads()+
                    " --java-mem-size="+gc.getQualimapSGEMemory()+"G"+
                    appendAlloutputToLog);
        addCommand("\n");
        
        //copy the resutls back
        addCommand("cp "+localQualimapReport.getAbsolutePath() +" " + qualimapReport.getAbsolutePath() + appendAlloutputToLog);
        addCommand("\n");
        
        //remove the tmp dir from the sge host
        addCommand("rm -rf " + tmpDir.getAbsolutePath() + appendAlloutputToLog);
        addCommand("\n");
        addCommand("echo finished " + appendAlloutputToLog);
        addCommand("date " + appendAlloutputToLog);    
    }        
    
}
