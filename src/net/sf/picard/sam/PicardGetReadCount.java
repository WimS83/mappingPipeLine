/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.picard.sam;

import java.io.File;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMFileReader.ValidationStringency;
import net.sf.samtools.SAMRecord;

/**
 *
 * @author Wim Spee
 */
public class PicardGetReadCount {

    public Long getReadCount(File bamFile) {

        Long counter = new Long(0);

        final SAMFileReader in = new SAMFileReader(bamFile);
        in.setValidationStringency(ValidationStringency.SILENT);
        for (final SAMRecord read : in) {
            counter++;
        }

        return counter;
    }
}
