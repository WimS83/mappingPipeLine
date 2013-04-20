/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bwa_picard_gatk_pipeline.fileWrappers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author root
 */
public class CsFastaFilePair {

    private File csFastaFile;
    private File qualFile;
      
    private String baseName;
    
    private long recordNr;

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
        }
    }

    public void setCsFastaFile(File csFastaFile) {
        this.csFastaFile = csFastaFile;
    }
    
    

    public void checkCsFastaAndQualContainEqualAmountOfRecords() throws IOException {
        
        long csFastaRecords = countRecordOpeningsInFile(csFastaFile);
        long qualRecords = countRecordOpeningsInFile(qualFile);
        
        if(csFastaRecords != qualRecords)
        {
            throw new IOException("csFasta file " + csFastaFile.getPath() + " and qual " + qualFile.getPath() +" file do not contain same amount of records. Csfasta contains " +csFastaRecords +  " and qual file contains  "+ qualRecords );
        }
        
        recordNr = csFastaRecords;
       
    }
    
    public long countRecordOpeningsInFile(File file)
    {
        long recordCounter = new Long(0);
        Character recordOpeningCharacter = '>';   
        
        try {
            
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                if(line.charAt(0) == recordOpeningCharacter)
                {
                    recordCounter++;                }
            }
            br.close();
        } catch (Exception e) {//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }
        
        return recordCounter;       
    }
    
    
    public FastQFile convertToFastQFile(File outputDir, File csFastaToFastqConverter) throws IOException, InterruptedException
    {
        checkCsFastaAndQualContainEqualAmountOfRecords();
        
        
        List<String> commands = new ArrayList<String>();
        commands.add(csFastaToFastqConverter.getPath());
        commands.add("-f");
        commands.add(csFastaFile.getPath());        
        commands.add("-q");
        commands.add(qualFile.getPath()); 
        commands.add("-e");
        commands.add(baseName);
                
        
        ProcessBuilder processBuilder = new ProcessBuilder(commands);
        processBuilder.directory(outputDir);   
        Process proces = processBuilder.start();
        proces.waitFor();
        
        File fastqFile = new File(outputDir, "p1."+baseName+ ".fastq");
        
        if(!fastqFile.exists()){ throw new IOException("Cannot find fastq file " + fastqFile.getPath());} 
        
        FastQFile fastQFileWrapper = new FastQFile(fastqFile);        
        fastQFileWrapper.countNumberOfrecords();
        
        if(fastQFileWrapper.getRecordNr() != recordNr)
        {
            throw new IOException("Not same amount of records in csfasta and fastq file. Csfasta = "+recordNr +" fastq = "+fastQFileWrapper.getRecordNr());
        }
        
        
        return fastQFileWrapper; 
    }
    
    

    

    public long getRecordNr() {
        return recordNr;
    }
    
    
    
    
    
    
}
