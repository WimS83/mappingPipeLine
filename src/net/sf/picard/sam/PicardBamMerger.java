/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.picard.sam;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMFileReader.ValidationStringency;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author Wim Spee
 */
public class PicardBamMerger {
    
    
   public File mergeBamFilesUsingPicard(List<File> bamFiles) throws IOException 
   {
        File outputDir = bamFiles.get(0).getParentFile();
        String baseName = FilenameUtils.getBaseName(bamFiles.get(0).getName());
        
        File mergedBamFile = new File(outputDir, baseName+ "_merged.bam");
       
        MergeSamFiles mergeSamFiles = new MergeSamFiles();
        mergeSamFiles.INPUT = bamFiles;
        mergeSamFiles.OUTPUT = mergedBamFile;
        mergeSamFiles.USE_THREADING = true;
        List<File> tmpDir = new ArrayList<File>(0);
        tmpDir.add(outputDir);
        mergeSamFiles.TMP_DIR = tmpDir;
        mergeSamFiles.CREATE_INDEX = true;
        mergeSamFiles.SORT_ORDER = SAMFileHeader.SortOrder.coordinate; 
        mergeSamFiles.VALIDATION_STRINGENCY = mergeSamFiles.VALIDATION_STRINGENCY.LENIENT;
        if(mergeSamFiles.doWork() != 0 )
        {
            throw new IOException("Could not merge bam files using Picard " + bamFiles.toString());
        }       
        
        return mergedBamFile;   
   }
    
}
