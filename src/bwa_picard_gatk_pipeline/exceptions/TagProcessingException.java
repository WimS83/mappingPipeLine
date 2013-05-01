/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bwa_picard_gatk_pipeline.exceptions;

/**
 *
 * @author Wim Spee
 */
public class TagProcessingException extends Exception{
    
     private static final long serialVersionUID = 1L;
    
    public TagProcessingException(String message) {
        super(message);        
        
    }
    
}
