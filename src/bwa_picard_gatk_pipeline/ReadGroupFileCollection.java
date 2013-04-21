/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bwa_picard_gatk_pipeline;

import bwa_picard_gatk_pipeline.enums.FileTypeEnum;
import static bwa_picard_gatk_pipeline.enums.FileTypeEnum.CSFASTA;
import static bwa_picard_gatk_pipeline.enums.FileTypeEnum.FASTQ;
import bwa_picard_gatk_pipeline.enums.TagEnum;
import bwa_picard_gatk_pipeline.fileWrappers.CsFastaFilePair;
import bwa_picard_gatk_pipeline.fileWrappers.FastQFile;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

/**
 *
 * @author Wim Spee
 */
public class ReadGroupFileCollection {
    
    private String readGroupId;
    private ReadGroupLogFile log;
    
    private EnumMap<TagEnum, ArrayList<CsFastaFilePair>> CsFastaFilePairsPerTagMap;
    private EnumMap<TagEnum, ArrayList<FastQFile>> fastQFilesPerTagMap;

    public ReadGroupFileCollection(ReadGroupLogFile log, String readGroupId) {
        
        this.log = log;
        this.readGroupId = readGroupId;
        
        CsFastaFilePairsPerTagMap = new EnumMap<TagEnum, ArrayList<CsFastaFilePair>>(TagEnum.class);
        fastQFilesPerTagMap = new EnumMap<TagEnum, ArrayList<FastQFile>>(TagEnum.class);
        
    }
    
    public void addCSFastaFile(String csFastaFilePath, TagEnum tag) {
        CsFastaFilePair csFastaFilePair = new CsFastaFilePair();
        csFastaFilePair.setCsFastaFile(new File(csFastaFilePath));
        csFastaFilePair.setTag(tag);
        try {
            csFastaFilePair.lookupQualFile();
            if (!CsFastaFilePairsPerTagMap.containsKey(tag)) {
                CsFastaFilePairsPerTagMap.put(tag, new ArrayList<CsFastaFilePair>());
            }
            CsFastaFilePairsPerTagMap.get(tag).add(csFastaFilePair);
            log.append("Added csFastaFilePair to read group " + readGroupId);
            log.append(csFastaFilePair.toString());

        } catch (IOException ex) {
            log.append(ex.getMessage());
        }

    }
    
    public void addFastQFile(String filePath, TagEnum tag) {

        File fastqFile = new File(filePath);
        if (!fastqFile.canRead()) {
            log.append("Cannot read fastq file " + fastqFile.getPath());
        } else {
            FastQFile fastQFile = new FastQFile(new File(filePath));
            fastQFile.setTag(tag);

            if (!fastQFilesPerTagMap.containsKey(tag)) {
                fastQFilesPerTagMap.put(tag, new ArrayList<FastQFile>());
            }
            fastQFilesPerTagMap.get(tag).add(fastQFile);
            log.append("Added FastQ file to read group " + readGroupId);
            log.append(fastQFile.toString());
        }
    }
    
    public List<CsFastaFilePair> getCsFastaFilePairs()
    {
        List<CsFastaFilePair> csFastaFilePairs = new ArrayList<CsFastaFilePair>();
        
        for(TagEnum tag: CsFastaFilePairsPerTagMap.keySet())
        {
            csFastaFilePairs.addAll(CsFastaFilePairsPerTagMap.get(tag));
        }        
        
        return csFastaFilePairs;
    
    }
    
     public List<FastQFile> getFastQFiles()
    {
        List<FastQFile> fastQFiles = new ArrayList<FastQFile>();
        
        for(TagEnum tag: fastQFilesPerTagMap.keySet())
        {
            fastQFiles.addAll(fastQFilesPerTagMap.get(tag));
        }        
        
        return fastQFiles;
    
    }
     
      public void addFile(String filePath, FileTypeEnum fileType, TagEnum tag) throws IOException  {
 
        switch (fileType) {
            case CSFASTA:
                addCSFastaFile(filePath, tag);
                break;
            case FASTQ:
                addFastQFile(filePath, tag);
                break;

        }
    }
    
    
    
    
    
    
    
}
