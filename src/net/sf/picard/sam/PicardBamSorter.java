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
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMFileReader.ValidationStringency;
import net.sf.samtools.SAMFileWriterFactory;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author wim
 */
public class PicardBamSorter {
    
    
     public File sortBamFilesUsingPicard(File bamFile, File tmpDir,  SortOrder sortOrder) throws IOException 
   {
        File outputDir = bamFile.getParentFile();
        String baseName = FilenameUtils.getBaseName(bamFile.getName());
        
        File sortedBamFile = new File(outputDir, baseName+ "_sorted"+sortOrder.toString()+".bam");
       
        SAMFileReader.setDefaultValidationStringency(ValidationStringency.LENIENT);  
        SAMFileWriterFactory.setDefaultCreateIndexWhileWriting(true);
        
        SortSam sortBamFiles = new SortSam();
        sortBamFiles.INPUT = bamFile;
        sortBamFiles.OUTPUT = sortedBamFile;        
        sortBamFiles.SORT_ORDER = sortOrder;
        List<File> tmpDirList = new ArrayList<File>(0);
        tmpDirList.add(tmpDir);
        sortBamFiles.TMP_DIR = tmpDirList;
        sortBamFiles.CREATE_INDEX = true;
        sortBamFiles.VALIDATION_STRINGENCY = sortBamFiles.VALIDATION_STRINGENCY.LENIENT;        
       
        if(sortBamFiles.doWork() != 0 )
        {
            throw new IOException("Could not sort bam file using Picard " + bamFile.toString() + " Order to sort in is: "+sortOrder.toString());
        }       
        
        return sortedBamFile;   
   }
    
}
