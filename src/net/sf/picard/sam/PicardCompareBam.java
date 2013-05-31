/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.picard.sam;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import net.sf.samtools.SAMFileReader;

/**
 *
 * @author wim
 */
public class PicardCompareBam {
    
    
    public Boolean compareBamFiles(File bam1, File bam2)
    {
        
        List<File> samFiles = new ArrayList<File>();
        samFiles.add(bam1);
        samFiles.add(bam2);
        
        SAMFileReader.setDefaultValidationStringency(SAMFileReader.ValidationStringency.LENIENT);  
        
        CompareSAMs compareSAMs = new CompareSAMs();      
        compareSAMs.samFiles = samFiles;
        compareSAMs.doWork();        
        
        return compareSAMs.areEqual();
    
    
    }
    
}
