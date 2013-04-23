/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bwa_picard_gatk_pipeline.exceptions;

/**
 *
 * @author root
 */
public class MappingException extends Exception{
    
    private static final long serialVersionUID = 1L;
    
    public MappingException(String message) {
        super(message);        
        
    }
    
}
