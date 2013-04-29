/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.picard.sam;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.sf.samtools.SAMFileHeader.SortOrder;
import net.sf.samtools.SAMFileReader.ValidationStringency;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author wim
 */
public class PicardBamSorter {
    
    
     public File sortBamFilesUsingPicard(File bamFile, SortOrder sortOrder) throws IOException 
   {
        File outputDir = bamFile.getParentFile();
        String baseName = FilenameUtils.getBaseName(bamFile.getName());
        
        File sortedBamFile = new File(outputDir, baseName+ "_sorted"+sortOrder.toString()+".bam");
       
        SortSam sortBamFiles = new SortSam();
        sortBamFiles.INPUT = bamFile;
        sortBamFiles.OUTPUT = sortedBamFile;        
        sortBamFiles.SORT_ORDER = sortOrder;
        List<File> tmpDir = new ArrayList<File>(0);
        tmpDir.add(outputDir);
        sortBamFiles.TMP_DIR = tmpDir;
        sortBamFiles.CREATE_INDEX = true;
        sortBamFiles.VALIDATION_STRINGENCY = sortBamFiles.VALIDATION_STRINGENCY.LENIENT;        
       
        if(sortBamFiles.doWork() != 0 )
        {
            throw new IOException("Could not sort bam file using Picard " + bamFile.toString() + " Order to sort in is: "+sortOrder.toString());
        }       
        
        return sortedBamFile;   
   }
    
}
