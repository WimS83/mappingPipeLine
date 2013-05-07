/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.picard.sam;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMFileWriterFactory;
import net.sf.samtools.SAMRecord;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author wim
 */
public class PicardMarkDuplicates {
    
    
    public File markDuplicates(File bamFile, File tmpDir) throws IOException {

        List<File> inputBamFileList = new ArrayList<File>(0);
        inputBamFileList.add(bamFile);
        
        File outputDir = bamFile.getParentFile();
        String baseName = FilenameUtils.getBaseName(bamFile.getName());
        
        SAMFileReader.setDefaultValidationStringency(SAMFileReader.ValidationStringency.LENIENT); 
        SAMFileWriterFactory.setDefaultCreateIndexWhileWriting(true);
       
        
        File duplicatesMarkedbam = new File(outputDir, baseName+ "_dedup"+".bam");
        File duplicatesMarkedbamMetrics = new File(outputDir, baseName+ "_dedup"+".metrics");
       
        MarkDuplicates markDuplicates = new MarkDuplicates();
        markDuplicates.INPUT = inputBamFileList;
        markDuplicates.OUTPUT = duplicatesMarkedbam; 
        List<File> tmpDirList = new ArrayList<File>(0);
        tmpDirList.add(tmpDir);
        markDuplicates.TMP_DIR = tmpDirList;
        markDuplicates.METRICS_FILE = duplicatesMarkedbamMetrics;
        
        markDuplicates.PROGRAM_GROUP_COMMAND_LINE = "Picard MarkDuplicates";
        markDuplicates.PROGRAM_GROUP_NAME = "Picard MarkDuplicates";
        markDuplicates.PROGRAM_GROUP_VERSION = "Picard MarkDuplicates";
        markDuplicates.PROGRAM_RECORD_ID = "Picard MarkDuplicates";
        
        
              
       
        if(markDuplicates.doWork() != 0 )
        {
            throw new IOException("Could not mark duplicates in bam file using Picard " + bamFile.toString() );
        }       
        
        return duplicatesMarkedbam;   

    }
    
}
