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
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMFileReader.ValidationStringency;
import net.sf.samtools.SAMFileWriterFactory;
import org.apache.commons.io.FilenameUtils;



/**
 *
 * @author Wim Spee
 */
public class PicardBamMerger {
    
    
   public File mergeBamFilesUsingPicard(List<File> bamFiles, File mergedBam, File tmpDir) throws IOException 
   {
        SAMFileReader.setDefaultValidationStringency(ValidationStringency.LENIENT);  
        SAMFileWriterFactory.setDefaultCreateIndexWhileWriting(true);
        
        System.out.println("Merging the following "+ bamFiles.size()+ " bam files:"); 
        for(File bamFile : bamFiles)
        {
            System.out.println(bamFile.getPath());
        }      
       
        MergeSamFiles mergeSamFiles = new MergeSamFiles();     
        mergeSamFiles.INPUT = bamFiles;
        mergeSamFiles.OUTPUT = mergedBam;
        mergeSamFiles.USE_THREADING = true;
        List<File> tmpDirList = new ArrayList<File>(0);
        tmpDirList.add(tmpDir);
        mergeSamFiles.TMP_DIR = tmpDirList;
        mergeSamFiles.CREATE_INDEX = true;
        mergeSamFiles.SORT_ORDER = SAMFileHeader.SortOrder.coordinate;    
        
        
        String blaat = "blaat";
      
        if(mergeSamFiles.doWork() != 0 )
        {
            throw new IOException("Could not merge bam files using Picard " + bamFiles.toString());
        }           
        
        return mergedBam;   
   }
    
}
