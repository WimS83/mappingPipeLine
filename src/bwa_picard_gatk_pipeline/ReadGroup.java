/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bwa_picard_gatk_pipeline;

import bwa_picard_gatk_pipeline.enums.TagEnum;
import bwa_picard_gatk_pipeline.enums.TargetEnum;
import bwa_picard_gatk_pipeline.exceptions.JobFaillureException;
import bwa_picard_gatk_pipeline.exceptions.TagProcessingException;
import bwa_picard_gatk_pipeline.sge.PiclPairReadsJob;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FilenameUtils;
import org.ggf.drmaa.DrmaaException;

/**
 *
 * @author Wim Spee
 */
public class ReadGroup {

    private String id;
    private String library;
    private String sample;
    private String description;
    
    private File readGroupOutputDir;
    
    
    
    private ReadGroupLogFile log;
    
    private List<Tag> tags;  
    
    private GlobalConfiguration globalConfiguration;
    
    private File mergedBam;
    private File F3Bam;
    private File F5Bam;
    private File R3Bam;
    
    
    
    public void startProcessing()
    {
        log = new ReadGroupLogFile(readGroupOutputDir, id);
        
        for(Tag tag : tags)
        {
            tag.setReadGroup(this);
            tag.createOutputDir(readGroupOutputDir);
            try {        
                tag.startProcessing();
            } catch (TagProcessingException ex) {
                log.append(ex.getMessage());
            }
        }        
        
        if (globalConfiguration.getTargetEnum().getRank() >= TargetEnum.READGROUP_BAM.getRank()) 
        {         
            try {
                mergeTagBams();
                runQualimap();                
                
            } catch (IOException ex) {
                log.append(ex.getMessage());
            } catch (InterruptedException ex) {
                log.append(ex.getMessage());
            } catch (DrmaaException ex) {
                log.append(ex.getMessage());
            } catch (JobFaillureException ex) {
                log.append(ex.getMessage());
            }
        }
        
        log.close();
    
    }
    
    private void mergeTagBams() throws IOException, InterruptedException, DrmaaException, JobFaillureException {
        
        for(Tag tag : tags)
        {
            if(tag.getName() == TagEnum.F3)
            {
                F3Bam = tag.getMergedBamFile();
            }
            if(tag.getName() == TagEnum.F5)
            {
                F5Bam = tag.getMergedBamFile();
            }
            if(tag.getName() == TagEnum.R3)
            {
                R3Bam = tag.getMergedBamFile();
            }
                
        }
        
        mergedBam = null;
        
        if(F3Bam != null && F5Bam != null)
        {
            mergedBam = mergeF3AndF5Bam();
        }
        else
        {
             if(F3Bam != null && R3Bam != null)
             {
                 mergedBam = mergeF3AndR3Bam();
             
             }
             else
             {
                 mergedBam = F3Bam;
             }        
        }        
    }
    
    private File mergeF3AndF5Bam() throws IOException, InterruptedException, DrmaaException, JobFaillureException {
        
        
        File pairedBamFileSortedByCoordinate = new File(readGroupOutputDir, id+"_F3_F5_paired.bam");
        
        PiclPairReadsJob piclPairReadsJob = new PiclPairReadsJob(F3Bam, F5Bam, pairedBamFileSortedByCoordinate, this, "fedor35");
        
        if(globalConfiguration.getOffline())
        {
            piclPairReadsJob.executeOffline();
        }
        else
        {
            piclPairReadsJob.submit();
            piclPairReadsJob.waitFor();
        }
        
        
        
        
//        PicardBamSorter picardBamSorter = new PicardBamSorter();
//        
//        File F3BamFileSortedByQueryName = picardBamSorter.sortBamFilesUsingPicard(F3Bam, SortOrder.queryname);
//        File F5BamFileSortedByQueryName = picardBamSorter.sortBamFilesUsingPicard(F5Bam, SortOrder.queryname);
//        
//        List<String> commands = new ArrayList<String>();
//        commands.add("/usr/local/Picl/picl");
//        commands.add("pairedbammaker");        
//        commands.add("-ori"); 
//        commands.add("ni"); 
//        commands.add("-first"); 
//        commands.add(F3BamFileSortedByQueryName.getAbsolutePath()); 
//        commands.add("-second"); 
//        commands.add(F5BamFileSortedByQueryName.getAbsolutePath()); 
//        commands.add("-output"); 
//        commands.add(pairedBamFileSortedByQueryName.getAbsolutePath());   
//        
//        ProcessBuilder processBuilder = new ProcessBuilder(commands);
//        processBuilder.directory(readGroupOutputDir);   
//        Process proces = processBuilder.start();        
//        proces.waitFor();
//        
//        File pairedBamFileSortedByCoordinate = picardBamSorter.sortBamFilesUsingPicard(pairedBamFileSortedByQueryName, SortOrder.coordinate);
        
        return pairedBamFileSortedByCoordinate;
        
    }

    private File mergeF3AndR3Bam() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
     private void runQualimap() throws IOException, InterruptedException {
        
        List<String> commands = new ArrayList<String>();
        commands.add(globalConfiguration.getQualiMap().getAbsolutePath()); 
        commands.add("bamqc");
        commands.add("-bam");
        commands.add(mergedBam.getAbsolutePath());
        commands.add("-outdir");
        commands.add(readGroupOutputDir.getAbsolutePath());
        commands.add("-outformat");
        commands.add("PDF");
        
        ProcessBuilder processBuilder = new ProcessBuilder(commands);
        processBuilder.directory(readGroupOutputDir);   
        Process proces = processBuilder.start();        
        proces.waitFor();
         
        File report = new File(readGroupOutputDir, "report.pdf");
        //rename the report file
        String reportRenamed = FilenameUtils.removeExtension(mergedBam.getAbsolutePath()) + ".pdf";
        report.renameTo(new File(reportRenamed));    
    }
    
     

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }    

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }    

    public String getLibrary() {
        return library;
    }

    public String getSample() {
        return sample;
    }

    public void setSample(String sample) {
        this.sample = sample;
    }  
    

    public File getReadGroupOutputDir() {
        return readGroupOutputDir;
    }   

    public ReadGroupLogFile getLog() {
        return log;
    }

    public GlobalConfiguration getGlobalConfiguration() {
        return globalConfiguration;
    }
    
     public void setGlobalConfiguration(GlobalConfiguration globalConfiguration) {
        this.globalConfiguration = globalConfiguration;
    }
    

    public File getMergedBam() {
        return mergedBam;
    }

    void createOutputDir(File sampleOutputDir) {
        readGroupOutputDir = new File(sampleOutputDir, id);
        readGroupOutputDir.mkdir();
        
    }

   

   

    

    

    
    
    
    
    
    
}
