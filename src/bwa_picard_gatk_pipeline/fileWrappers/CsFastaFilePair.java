/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bwa_picard_gatk_pipeline.fileWrappers;

import bwa_picard_gatk_pipeline.enums.PlatformEnum;
import bwa_picard_gatk_pipeline.fileWrappers.FastQChunk;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author root
 */
public class CsFastaFilePair {

    private String path;
    private File csFastaFile;
    private File qualFile;
    private String baseName;
    private Long recordNr;
    private File outputDir;
    private Integer chunkCounter;
    private List<FastQChunk> fastQChunks;
    private FastQChunk currentChunk;
    
    //File readers and writers
    private BufferedReader csFastaReader;
    private BufferedReader qualReader;
    private BufferedWriter out;
    
    private String readNameMask;

    public void lookupCsFastaFile() throws IOException {
        csFastaFile = new File(path);
        if (!csFastaFile.canRead()) {
            throw new IOException("Cannot read csfasta file " + path);
        }
    }

    public void lookupQualFile() throws IOException {

        if (!csFastaFile.exists()) {
            throw new IOException("csFastaFile does not exist " + csFastaFile.getPath());
        }

        File parentDir = csFastaFile.getParentFile();
        baseName = FilenameUtils.getBaseName(csFastaFile.getPath());
        File baseNamePlusQual = new File(parentDir, baseName + ".qual");
        File baseNamePlus_QVQual = new File(parentDir, baseName + "_QV.qual");

        if (baseNamePlusQual.exists()) {
            qualFile = baseNamePlusQual;
        }

        if (baseNamePlus_QVQual.exists()) {
            qualFile = baseNamePlus_QVQual;
        }

        if (qualFile == null) {
            throw new IOException("qual file does not exit in the location " + baseNamePlusQual.getPath() + " or " + baseNamePlus_QVQual.getPath());
        } else {
            System.out.println("Found qual file " + qualFile.getPath());
        }

    }

    public void setCsFastaFile(File csFastaFile) {
        this.csFastaFile = csFastaFile;
    }


    public List<FastQChunk> convertToFastQ(File outputDir, String readGroupId, Integer chunkSize) throws FileNotFoundException, IOException {

        
        fastQChunks = new ArrayList<FastQChunk>();
        this.outputDir = outputDir;
        recordNr = new Long(0);
        chunkCounter = 0;
        Integer chunkRecordCounter = 0;

        //initalize the readers and writer
        initializeReaders();
        setWriterToNextChunk();  

        skipheader();
        
        CSFastaEntry cSFastaEntry;
        while((cSFastaEntry = readNextEntry()) != null)
        {
            String seqName =  cSFastaEntry.getDescription().substring(1);
            if(readNameMask != null)
            {
                   seqName = seqName.replace(readNameMask, "");
            }   
            
            seqName = readGroupId+":"+seqName;
            
            FastqEntry fastqEntry  = new FastqEntry();
            fastqEntry.setSeqName(seqName);
            fastqEntry.convertCsValuesFromCsFastaLine(cSFastaEntry.getCsValue());
            fastqEntry.convertQualFromCsFastaQualLine(cSFastaEntry.getQualValue());
            
            
           // FastqEntry fastqEntry = new FastqEntry(seqName, cSFastaEntry.getCsValue(), cSFastaEntry.getQualValue());
            
             if(recordNr % chunkSize == 0 && recordNr != 0)
             {
                closeWriter(chunkRecordCounter);
                chunkRecordCounter = 0;
                setWriterToNextChunk();
             }
            
            
            out.write(fastqEntry.toString());
            recordNr++; 
            chunkRecordCounter++;
        }
        
        csFastaReader.close();
        qualReader.close();  
        
        closeWriter(chunkRecordCounter);           
        
        return fastQChunks;
    }
    
    private void initializeReaders() throws FileNotFoundException
    {
        csFastaReader = new BufferedReader(new FileReader(csFastaFile));
        qualReader = new BufferedReader(new FileReader(qualFile));    
    }  
    
    
    private CSFastaEntry readNextEntry() throws IOException
    {
        CSFastaEntry cSFastaEntry = new CSFastaEntry();
             
        String csFastaDescriptionLine = csFastaReader.readLine();
        String csFastaValue = csFastaReader.readLine();
        String qualDescriptionLine = qualReader.readLine();
        String qualValue = qualReader.readLine();
        
        if(csFastaDescriptionLine == null || csFastaValue == null || qualDescriptionLine == null || qualValue == null )
        {
            return null;
        }     
        
        if(!csFastaDescriptionLine.equalsIgnoreCase(qualDescriptionLine)){throw new IOException("csfasta and qual description line do not match. csfasta description line:\n"+csFastaDescriptionLine+"\n qual description line\n"+qualDescriptionLine);}
        
        cSFastaEntry.setDescription(csFastaDescriptionLine);
        cSFastaEntry.setCsValue(csFastaValue);
        cSFastaEntry.setQualValue(qualValue);
        
        return cSFastaEntry;   
    }
    
    
    
    private void setWriterToNextChunk() {
        
       
        try {
            chunkCounter++;
            File fastqFile = new File(outputDir, baseName + "_chunk"+chunkCounter+".fastq"); 
            currentChunk = new FastQChunk(fastqFile);
            fastQChunks.add(currentChunk); 
            FileWriter fstream = new FileWriter(fastqFile);
             out = new BufferedWriter(fstream);
        } catch (IOException ex) {
            Logger.getLogger(CsFastaFilePair.class.getName()).log(Level.SEVERE, null, ex);
        }
       
    }
    
    private void closeWriter(Integer chunkRecordCounter) {
        currentChunk.setRecordNr(chunkRecordCounter);
        try {
            out.close();
        } catch (IOException ex) {
            Logger.getLogger(CsFastaFilePair.class.getName()).log(Level.SEVERE, null, ex);
        }
    }    
    
    

    public long getRecordNr() {
        return recordNr;
    }

    public File getCsFastaFile() {
        return csFastaFile;
    }

    public File getQualFile() {
        return qualFile;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getReadNameMask() {
        return readNameMask;
    }

    public void setReadNameMask(String readNameMask) {
        this.readNameMask = readNameMask;
    }
    
    

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("csFasta: " + csFastaFile.getPath());
        sb.append("\n");
        sb.append("qual: " + qualFile.getPath());
        sb.append("\n");


        if (recordNr == null) {
            sb.append("recordNr: unknown");
        } else {
            sb.append("recordNr: " + recordNr);
        }
        sb.append("\n");

        return sb.toString();

    }

    private void skipheader() throws IOException {
        
        csFastaReader.mark(8192);
        
        String csFastaLine;
        while((csFastaLine = csFastaReader.readLine()) != null)
        {
            if (csFastaLine.charAt(0) == '#') {
                csFastaReader.mark(8192);
            }
            else
            {
                csFastaReader.reset();
                break;
            }
        }
        
        
        String qualLine;
        while((qualLine = qualReader.readLine()) != null)
        {
            if (qualLine.charAt(0) == '#') {
                qualReader.mark(8192);
            }
            else
            {
                qualReader.reset();
                break;
            }
        }      
    }

    

    
}
