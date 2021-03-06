/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bwa_picard_gatk_pipeline.enums;

/**
 *
 * @author Wim Spee
 */
public enum TargetEnum {
    
    FASTQ(1), CHUNKS_BAM(2), TAG_BAM(3), READGROUP_BAM(4), SAMPLE_BAM(5), DEDUP_BAM(6), REALIGN_BAM(7), BQSR_BAM(8), SAMPLE_RAW_VCF(9), SAMPLE_ANNOTATED_VCF(10);
    
    private int rank;

    private TargetEnum(int rank) {
        this.rank = rank;
    }

    public int getRank() {
        return rank;
    }
    
    
    
    
    
}
