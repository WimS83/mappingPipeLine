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

    public void checkCsFastaAndQualContainEqualAmountOfRecords() throws IOException {

        System.out.println("Checking csFasta and qual contain equal amount of records");
        long csFastaRecords = countRecordOpeningsInFile(csFastaFile);
        long qualRecords = countRecordOpeningsInFile(qualFile);

        if (csFastaRecords != qualRecords) {
            throw new IOException("csFasta file " + csFastaFile.getPath() + " and qual " + qualFile.getPath() + " file do not contain same amount of records. Csfasta contains " + csFastaRecords + " and qual file contains  " + qualRecords);
        }

        recordNr = csFastaRecords;
        System.out.println("Contains same amount of records " + toString());

    }

    public long countRecordOpeningsInFile(File file) {
        long recordCounter = new Long(0);
        Character recordOpeningCharacter = '>';

        try {

            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.charAt(0) == recordOpeningCharacter) {
                    recordCounter++;
                }
            }
            br.close();
        } catch (Exception e) {//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }

        return recordCounter;
    }

//    public FastQFile convertToFastQFile(File outputDir, File csFastaToFastqConverter) throws IOException, InterruptedException {
//        checkCsFastaAndQualContainEqualAmountOfRecords();
//
//
//        List<String> commands = new ArrayList<String>();
//        commands.add(csFastaToFastqConverter.getPath());
//        commands.add("-f");
//        commands.add(csFastaFile.getPath());
//        commands.add("-q");
//        commands.add(qualFile.getPath());
//        commands.add("-e");
//        commands.add(baseName);
//
//
//        ProcessBuilder processBuilder = new ProcessBuilder(commands);
//        processBuilder.directory(outputDir);
//        Process proces = processBuilder.start();
//        proces.waitFor();
//
//        System.out.println("working directory = " + outputDir.getPath());
//        System.out.println("Converting csFasta file " + csFastaFile.getPath());
//
//
//
//        File fastqFile = new File(outputDir, "p1." + baseName + ".fastq");
//
//        if (!fastqFile.exists()) {
//            throw new IOException("Cannot find fastq file " + fastqFile.getPath());
//        }
//
//        FastQFile fastQFileWrapper = new FastQFile(fastqFile);
//        fastQFileWrapper.countNumberOfrecords();
//
//        if (fastQFileWrapper.getRecordNr() != recordNr) {
//            throw new IOException("Not same amount of records in csfasta and fastq file. Csfasta = " + recordNr + " fastq = " + fastQFileWrapper.getFastqFile().getPath() + " = " + fastQFileWrapper.getRecordNr());
//        }
//
//
//        return fastQFileWrapper;
//    }

    public FastQFile convertToFastQ(File outputDir, String readGroupId) throws FileNotFoundException, IOException {

        File fastqFile = new File(outputDir, baseName + ".fastq");
        
        // Create file 
        FileWriter fstream = new FileWriter(fastqFile);
        BufferedWriter out = new BufferedWriter(fstream);

        BufferedReader csFastaReader = new BufferedReader(new FileReader(csFastaFile));
        BufferedReader qualReader = new BufferedReader(new FileReader(qualFile));
        String csFastaLine;


        Long csFastLineCounter = new Long(0);
        Long fastqRecordCounter = new Long(0);


        Long diveder = new Long(2);

        while ((csFastaLine = csFastaReader.readLine()) != null) {
            String qualLine = qualReader.readLine();
            
            if(qualLine == null){throw new IOException("Cannot find qual entry in "+ qualFile.getAbsolutePath() +" at line "+csFastLineCounter);}
            if (csFastaLine.charAt(0) == '#') {
                continue;
            }
            csFastLineCounter++;

            if (csFastLineCounter % diveder == 0) {
                fastqRecordCounter++;
                String seqName = readGroupId+":"+fastqRecordCounter;
                FastqEntry fastqEntry = new FastqEntry(seqName, csFastaLine, qualLine);
                
                out.write(fastqEntry.toString());
            }
        }
        
        csFastaReader.close();
        qualReader.close();        
        out.close();
        
        FastQFile fastQFileWrapper = new FastQFile(fastqFile);
        
        return fastQFileWrapper;


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
