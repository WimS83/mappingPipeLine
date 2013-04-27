/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bwa_picard_gatk_pipeline;

import bwa_picard_gatk_pipeline.enums.TagEnum;
import bwa_picard_gatk_pipeline.enums.TargetEnum;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.picard.sam.PicardBamSorter;
import net.sf.samtools.SAMFileHeader.SortOrder;

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
            tag.startProcessing();        
        }        
        
        if (globalConfiguration.getTargetEnum().getRank() >= TargetEnum.READGROUP_BAM.getRank()) 
        {         
            try {
                mergeTagBams();
            } catch (IOException ex) {
                Logger.getLogger(ReadGroup.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(ReadGroup.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        log.close();
    
    }
    
    private void mergeTagBams() throws IOException, InterruptedException {
        
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
    
    private File mergeF3AndF5Bam() throws IOException, InterruptedException {
        
        File pairedBamFileSortedByQueryName = new File(readGroupOutputDir, id+"_F3_F5_paired.bam");
        
        PicardBamSorter picardBamSorter = new PicardBamSorter();
        
        File F3BamFileSortedByQueryName = picardBamSorter.sortBamFilesUsingPicard(F3Bam, SortOrder.queryname);
        File F5BamFileSortedByQueryName = picardBamSorter.sortBamFilesUsingPicard(F5Bam, SortOrder.queryname);
        
        List<String> commands = new ArrayList<String>();
        commands.add("/usr/local/Picl/picl");
        commands.add("pairedbammaker");        
        commands.add("-ori"); 
        commands.add("ni"); 
        commands.add("-first"); 
        commands.add(F3BamFileSortedByQueryName.getAbsolutePath()); 
        commands.add("-second"); 
        commands.add(F5BamFileSortedByQueryName.getAbsolutePath()); 
        commands.add("-output"); 
        commands.add(pairedBamFileSortedByQueryName.getAbsolutePath());   
        
        ProcessBuilder processBuilder = new ProcessBuilder(commands);
        processBuilder.directory(readGroupOutputDir);   
        Process proces = processBuilder.start();        
        proces.waitFor();
        
        File pairedBamFileSortedByCoordinate = picardBamSorter.sortBamFilesUsingPicard(pairedBamFileSortedByQueryName, SortOrder.coordinate);
        
        return pairedBamFileSortedByCoordinate;
        
    }

    private File mergeF3AndR3Bam() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
