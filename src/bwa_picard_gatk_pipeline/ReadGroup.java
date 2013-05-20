/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bwa_picard_gatk_pipeline;

import bwa_picard_gatk_pipeline.enums.TagEnum;
import bwa_picard_gatk_pipeline.enums.TargetEnum;
import bwa_picard_gatk_pipeline.exceptions.JobFaillureException;
import bwa_picard_gatk_pipeline.exceptions.TagProcessingException;
import bwa_picard_gatk_pipeline.sge.PiclPairReadsJob;
import bwa_picard_gatk_pipeline.sge.QualimapJob;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FilenameUtils;
import org.ggf.drmaa.DrmaaException;

/**
 *
 * @author Wim Spee
 */
public class ReadGroup {

    private String id;
    private String library;
    private String sample;
    private String description;
    private File readGroupOutputDir;
    private ReadGroupLogFile log;
    private List<Tag> tags;
    private GlobalConfiguration globalConfiguration;
    private File mergedBam;
    private File F3Bam;
    private File F5Bam;
    private File R3Bam;

    public void startProcessing() {
        log = new ReadGroupLogFile(readGroupOutputDir, id);

        initalizeUnsetList();           //initialize the unset list to empty list  

        for (Tag tag : tags) {
            tag.setReadGroup(this);
            tag.createOutputDir(readGroupOutputDir);
            try {
                tag.startProcessing();
            } catch (TagProcessingException ex) {
                log.append(ex.getMessage());
            }
        }

        if (globalConfiguration.getTargetEnum().getRank() >= TargetEnum.READGROUP_BAM.getRank()) {
            try {
                mergeTagBams();
                runQualimap();

            } catch (IOException ex) {
                log.append(ex.getMessage());
            } catch (InterruptedException ex) {
                log.append(ex.getMessage());
            } catch (DrmaaException ex) {
                log.append(ex.getMessage());
            } catch (JobFaillureException ex) {
                log.append(ex.getMessage());
            }
        }

        log.close();

    }

    private void initalizeUnsetList() {
        if (tags == null) {
            tags = new ArrayList<Tag>();
        }
    }

    private void mergeTagBams() throws IOException, InterruptedException, DrmaaException, JobFaillureException {

        if (tags.isEmpty()) {
            return;
        }

        if (tags.size() == 1) {
            mergedBam = tags.get(0).getMergedBamFile();
        } 
        //if there were multiple tags merge them 
        else {
            for (Tag tag : tags) {
                if (tag.getName() == TagEnum.F3) {
                    F3Bam = tag.getMergedBamFile();
                }
                if (tag.getName() == TagEnum.F5) {
                    F5Bam = tag.getMergedBamFile();
                }
                if (tag.getName() == TagEnum.R3) {
                    R3Bam = tag.getMergedBamFile();
                }

            }

            if (F3Bam != null && F5Bam != null) {
                mergedBam = mergeF3AndF5Bam();
            }

            if (F3Bam != null && R3Bam != null) {
                mergedBam = mergeF3AndR3Bam();
            }

        }
    }

    private File mergeF3AndF5Bam() throws IOException, InterruptedException, DrmaaException, JobFaillureException {

        File pairedBamFileSortedByCoordinate = new File(readGroupOutputDir, id + "_F3_F5_paired.bam");

        PiclPairReadsJob piclPairReadsJob = new PiclPairReadsJob(F3Bam, F5Bam, pairedBamFileSortedByCoordinate, this, "fedor35");

        if (globalConfiguration.getOffline()) {
            piclPairReadsJob.executeOffline();
            piclPairReadsJob.waitForOfflineExecution();
        } else {
            piclPairReadsJob.submit();
            piclPairReadsJob.waitFor();
        }

        return pairedBamFileSortedByCoordinate;

    }

    private File mergeF3AndR3Bam() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void runQualimap() throws IOException, InterruptedException, DrmaaException {

        File qualimapReport = new File(readGroupOutputDir, id+"_qualimap.pdf");        
        QualimapJob qualimapJob = new QualimapJob(mergedBam, qualimapReport, globalConfiguration, "fedor8");
        
         qualimapJob.executeOffline(); // temporary always execute oflline, untill I know to execute via SGE on fedor8 or another suitable node             
//        if(globalConfiguration.getOffline())
//        {
//            qualimapJob.executeOffline();
//        }
//        else
//        {
//            qualimapJob.submit();
//        }              
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public String getLibrary() {
        return library;
    }

    public String getSample() {
        return sample;
    }

    public void setSample(String sample) {
        this.sample = sample;
    }

    public File getReadGroupOutputDir() {
        return readGroupOutputDir;
    }

    public ReadGroupLogFile getLog() {
        return log;
    }

    public GlobalConfiguration getGlobalConfiguration() {
        return globalConfiguration;
    }

    public void setGlobalConfiguration(GlobalConfiguration globalConfiguration) {
        this.globalConfiguration = globalConfiguration;
    }

    public File getMergedBam() {
        return mergedBam;
    }

    void createOutputDir(File sampleOutputDir) {
        readGroupOutputDir = new File(sampleOutputDir, id);
        readGroupOutputDir.mkdir();

    }
}
