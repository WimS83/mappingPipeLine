/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bwa_picard_gatk_pipeline;

import bwa_picard_gatk_pipeline.enums.FileTypeEnum;
import bwa_picard_gatk_pipeline.enums.TagEnum;
import bwa_picard_gatk_pipeline.enums.TargetEnum;
import bwa_picard_gatk_pipeline.fileWrappers.CsFastaFilePair;
import bwa_picard_gatk_pipeline.fileWrappers.FastQFile;
import java.io.File;
import java.io.IOException;
import java.util.EnumMap;

/**
 *
 * @author Wim Spee
 */
public class ReadGroup {

    private String id;
    private String library;
    private String sample;
    private File outputDir;
    private ReadGroupLogFile log;
    private EnumMap<TagEnum, TagFileCollection> tagFileCollectionMap;
    private GlobalConfiguration globalConfiguration;
    private File referenceFile;
    private File mergedBam;
    
    

    public ReadGroup(String id, String library, String sample, File referenceFile) {
        this.id = id;
        this.library = library;
        this.sample = sample;
        this.referenceFile = referenceFile;

        tagFileCollectionMap = new EnumMap<TagEnum, TagFileCollection>(TagEnum.class);

        for (TagEnum tagEnum : TagEnum.values()) {
            tagFileCollectionMap.put(tagEnum, new TagFileCollection(tagEnum, this));
        }
        
        

    }
    
    public void startProcessing()
    {
        for(TagFileCollection tagFileCollection : tagFileCollectionMap.values())
        {
            tagFileCollection.startProcessing();        
        }
        
         if (globalConfiguration.getTargetEnum().getRank() >= TargetEnum.READGROUP_BAM.getRank()) {
         
             mergeTagBams();
         }
        
        
        
    
    }
    
    private void mergeTagBams() {
        File F3Bam = tagFileCollectionMap.get(TagEnum.F3).getMergedBamFile();
        File F5Bam = tagFileCollectionMap.get(TagEnum.F5).getMergedBamFile();
        File R3Bam = tagFileCollectionMap.get(TagEnum.R3).getMergedBamFile();
        
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
    
    private File mergeF3AndF5Bam() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private File mergeF3AndR3Bam() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    

    public String getId() {
        return id;
    }

    public String getLibrary() {
        return library;
    }

    public String getSample() {
        return sample;
    }

    public File getReferenceFile() {
        return referenceFile;
    }

    public File getOutputDir() {
        return outputDir;
    }

    public ReadGroupLogFile getLog() {
        return log;
    }

    public GlobalConfiguration getGlobalConfiguration() {
        return globalConfiguration;
    }    
    

    public void addFile(String filePath, FileTypeEnum fileType, TagEnum tag) throws IOException {

        switch (fileType) {
            case CSFASTA:
                CsFastaFilePair csFastaFilePair = tagFileCollectionMap.get(tag).addCSFastaFile(filePath);
                log.append("Added csFastaFilePair to read group " + id);
                log.append(csFastaFilePair.toString());
                break;
            case FASTQ:
                FastQFile fastQFile = tagFileCollectionMap.get(tag).addFastQFile(filePath);
                log.append("Added FastQ file to read group " + id);
                log.append(fastQFile.toString());
                break;
        }
    }

    public File getMergedBam() {
        return mergedBam;
    }

    

    

    
    
    
    
    
    
}
