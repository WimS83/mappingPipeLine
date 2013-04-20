/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bwa_picard_gatk_pipeline;

import bwa_picard_gatk_pipeline.enums.FileTypeEnum;
import static bwa_picard_gatk_pipeline.enums.FileTypeEnum.CSFASTA;
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
public class ReadGroup {
    
    String readGroupId;
    String library;
    String sample;
    
    File outputDir;
    File logFile;
    
    EnumMap<TagEnum, ArrayList<CsFastaFilePair>> CsFastaFilePairsPerTagMap;
    EnumMap<TagEnum, ArrayList<FastQFile>> fastQFilesPerTagMap;

    public ReadGroup() {
        CsFastaFilePairsPerTagMap = new EnumMap<TagEnum, ArrayList<CsFastaFilePair>>(TagEnum.class);
        fastQFilesPerTagMap = new EnumMap<TagEnum, ArrayList<FastQFile>>(TagEnum.class);
    }      
    
    

    public void setReadGroupId(String readGroupId) {
        this.readGroupId = readGroupId;
    }

    public void setLibrary(String library) {
        this.library = library;
    }

    public void setSample(String sample) {
        this.sample = sample;
    }
    
    public void addCSFastaFile(String csFastaFilePath, TagEnum tag) throws IOException
    {
        CsFastaFilePair csFastaFilePair = new CsFastaFilePair();
        csFastaFilePair.setCsFastaFile(new File(csFastaFilePath));
        csFastaFilePair.lookupQualFile();
        if(!CsFastaFilePairsPerTagMap.containsKey(tag))
        {
            CsFastaFilePairsPerTagMap.put(tag, new ArrayList<CsFastaFilePair>());
        }   
        
        CsFastaFilePairsPerTagMap.get(tag).add(csFastaFilePair);
    }
    
    private void addFastQFile(String filePath, TagEnum tag) {
        FastQFile fastQFile = new FastQFile(new File(filePath));
       
        if(!fastQFilesPerTagMap.containsKey(tag))
        {
            fastQFilesPerTagMap.put(tag, new ArrayList<FastQFile>());
        }   
        
        fastQFilesPerTagMap.get(tag).add(fastQFile);
        
    }

    public void addFile(String filePath, FileTypeEnum fileType, TagEnum tag) throws IOException {
        
        switch(fileType)
        {
            case CSFASTA:
                addCSFastaFile(filePath, tag);
                break;   
            case FASTQ:    
                addFastQFile(filePath, tag);
                break;
        
        }           
    }
    
    
   public void convertCSFastaToFastQ(File csFastaToFastqConverter) throws IOException, InterruptedException
   {
       for(TagEnum tagEnum: CsFastaFilePairsPerTagMap.keySet())
       {
           List<CsFastaFilePair> csFastaFilePairs = CsFastaFilePairsPerTagMap.get(tagEnum);
           
           for(CsFastaFilePair csFastaFilePair :csFastaFilePairs)
           {
               FastQFile fastqFile = csFastaFilePair.convertToFastQFile(outputDir, csFastaToFastqConverter);
               
               if(!fastQFilesPerTagMap.containsKey(tagEnum))
               {
                   fastQFilesPerTagMap.put(tagEnum, new ArrayList<FastQFile>());
               }
               fastQFilesPerTagMap.get(tagEnum).add(fastqFile);               
           } 
       }      
      
   }
   
   public void splitFastQFiles()
   {
       for(TagEnum tagEnum: fastQFilesPerTagMap.keySet())
       {
           for(FastQFile fastQFile : fastQFilesPerTagMap.get(tagEnum))
           {
               fastQFile.splitFastQFile(new Long(500), outputDir);
           
           }
       }        
   }
   
   
   
   

    public void setOutputDir(File outputDir) {
        this.outputDir = outputDir;
        if(!outputDir.exists())
        {
            outputDir.mkdir();
        }
    }

    public String getReadGroupId() {
        return readGroupId;
    }

    public String getLibrary() {
        return library;
    }

    public String getSample() {
        return sample;
    }

    
    
    
   
   
    
    
    
    
    
    
    
    
    
    
    
}
