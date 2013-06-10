/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bwa_picard_gatk_pipeline.readGroup;

import bwa_picard_gatk_pipeline.fileWrappers.CsFastaFilePair;
import bwa_picard_gatk_pipeline.fileWrappers.FastQChunk;
import bwa_picard_gatk_pipeline.fileWrappers.FastQFile;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author wim
 */
public abstract class ReadGroupSolid extends ReadGroup{
    
     protected void lookupCsFastaAndQualFiles(List<CsFastaFilePair> csfastaFiles) throws IOException {
        for (CsFastaFilePair csFastaFilePair : csfastaFiles) {
            csFastaFilePair.lookupCsFastaFile();
            csFastaFilePair.lookupQualFile();
        }
    }
     
    protected void convertCSFastaToFastQChunks(List<CsFastaFilePair> csfastaFiles, List<FastQChunk> fastQChunks) throws IOException {

        System.out.println("Converting csFasta to fastq ");
        List<FastQChunk> fastQChunksConverted = new ArrayList<FastQChunk>();

        for (CsFastaFilePair csFastaFilePair : csfastaFiles) {

            fastQChunksConverted.addAll(csFastaFilePair.convertToFastQ(readGroupOutputDir, id, gc.getChunkSize()));
            getLog().append(csFastaFilePair.toString());
        }

        getLog().append("Converted " + csfastaFiles.size() + " csFastaFilePairs to " + fastQChunksConverted.size() + " Fastq chunks");
        fastQChunks.addAll(fastQChunksConverted);
    } 
    
    protected void convertFastQFilesToFastQChunks(List<FastQFile> fastQFiles, List<FastQChunk> fastQChunks ) throws FileNotFoundException, IOException {
        getLog().append("Start splitting fastqFiles");
        List<FastQChunk> fastQChunksConverted = new ArrayList<FastQChunk>();

        for (FastQFile fastQFile : fastQFiles) {
            fastQChunksConverted.addAll(fastQFile.splitFastQFile(gc.getChunkSize(), readGroupOutputDir, id));
            getLog().append(fastQFile.toString());
        }

        getLog().append("Splitted " + fastQFiles.size() + " fastQFiles to " + fastQChunksConverted.size() + " fastq chunks");
        fastQChunks.addAll(fastQChunksConverted);
    }
    
}
