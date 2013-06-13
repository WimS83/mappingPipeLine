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
    
     protected void lookupCsFastaAndQualFiles(CsFastaFilePair csfastaFilePair) throws IOException {
  
            csfastaFilePair.lookupCsFastaFile();
            csfastaFilePair.lookupQualFile();
        
    }
     
    protected void convertCSFastaToFastQChunks(CsFastaFilePair csFastaFilePair, List<FastQChunk> fastQChunks) throws IOException {

        System.out.println("Converting csFasta to fastq ");
        List<FastQChunk> fastQChunksConverted = new ArrayList<FastQChunk>();

        fastQChunksConverted.addAll(csFastaFilePair.convertToFastQ(readGroupOutputDir, id, gc.getChunkSize()));
        getLog().append(csFastaFilePair.toString());        

        getLog().append("Converted csFastaFilePair "+csFastaFilePair.getPath()+" to " + fastQChunksConverted.size() + " Fastq chunks");
        fastQChunks.addAll(fastQChunksConverted);
    } 
    
    protected void convertFastQFilesToFastQChunks(FastQFile fastQFile, List<FastQChunk> fastQChunks ) throws FileNotFoundException, IOException {
        getLog().append("Start splitting fastqFiles");
        List<FastQChunk> fastQChunksConverted = new ArrayList<FastQChunk>();

        fastQChunksConverted.addAll(fastQFile.splitFastQFile(gc.getChunkSize(), readGroupOutputDir, id));
        getLog().append(fastQFile.toString());
        

        getLog().append("Splitted fastQFile "+fastQFile.getPath()+" to " + fastQChunksConverted.size() + " fastq chunks");
        fastQChunks.addAll(fastQChunksConverted);
    }
    
}
