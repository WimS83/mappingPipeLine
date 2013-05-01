/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.picard.sam;

import java.io.File;
import net.sf.samtools.AbstractBAMFileIndex;
import net.sf.samtools.BAMIndexMetaData;
import net.sf.samtools.SAMFileReader;

/**
 *
 * @author Wim Spee
 */
public class PicardBamIndexStats {
    
     
    
    
    
    public Long getReadCount(File bamFile)
    {
    
     
 
        SAMFileReader sam = new SAMFileReader(bamFile,
                                 new File(bamFile.getAbsolutePath() + ".bai"));
 
        AbstractBAMFileIndex index = (AbstractBAMFileIndex) sam.getIndex();
 
        long count = 0;
        for (int i = 0; i < index.getNumberOfReferences(); i++) {
            BAMIndexMetaData meta = index.getMetaData(i);
            count += new Long(meta.getAlignedRecordCount());
        }
        
        return count;
       
        
        
        
        
        
    
    }
    
    
    
    
    
    
    
}
