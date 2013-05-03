/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bwa_picard_gatk_pipeline.fileWrappers;

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
    
    
    BufferedWriter out;
    
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

        //initalize the reader
        BufferedReader csFastaReader = new BufferedReader(new FileReader(csFastaFile));
        BufferedReader qualReader = new BufferedReader(new FileReader(qualFile));
        String csFastaLine;

        //initialize the writer
        setWriterToNextChunk();   
        
        //initialize the counter to check if we are on a description or csvalues line
        Long csFastLineCounter = new Long(0);
        Long diveder = new Long(2);        
        
        String seqName = "";        

        while ((csFastaLine = csFastaReader.readLine()) != null) {
            String qualLine = qualReader.readLine();
            
            if(qualLine == null){throw new IOException("Cannot find qual entry in "+ qualFile.getAbsolutePath() +" at line "+csFastLineCounter);}
            if (csFastaLine.charAt(0) == '#') {
                continue;
            }
            csFastLineCounter++;

            //if at the second and last line of a csfasta record
            if (csFastLineCounter % diveder == 0) {                
                
               if(recordNr % chunkSize == 0 && recordNr != 0)
               {
                   closeWriter(chunkRecordCounter);
                   chunkRecordCounter = 0;
                   setWriterToNextChunk();
               }
                
                seqName = readGroupId+":"+seqName;
                
                FastqEntry fastqEntry = new FastqEntry(seqName, csFastaLine, qualLine);
                
                out.write(fastqEntry.toString());
                recordNr++; 
                chunkRecordCounter++;
            }
            //if at the first line of a csfasta record
            else
            {
                //remove the leading > from the seqname
                seqName = csFastaLine.substring(1);
                
                //remove a string (for example F3 or F5-P2) from the seqname to make them identical for later pairing
                if(readNameMask != null)
                {
                    seqName = seqName.replace(readNameMask, "");
                }               
            }            
        }
        
        csFastaReader.close();
        qualReader.close();  
        
        closeWriter(chunkRecordCounter);           
        
        return fastQChunks;


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

    

    
}
