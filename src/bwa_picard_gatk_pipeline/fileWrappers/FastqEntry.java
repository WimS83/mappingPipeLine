/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bwa_picard_gatk_pipeline.fileWrappers;

import java.util.Arrays;
import java.util.HashMap;

/**
 *
 * @author wim
 */
public class FastqEntry {

    private HashMap<Character, Character> csToBWACSMap = null;
    private String seqName = null;
    private String description = "+";
    private String csValues;
    private String qualValues;

    public FastqEntry() {

            
        csToBWACSMap = new HashMap<Character, Character>();
        csToBWACSMap.put('0', 'A');
        csToBWACSMap.put('1', 'C');
        csToBWACSMap.put('2', 'G');
        csToBWACSMap.put('3', 'T');
        csToBWACSMap.put('.', 'N');       

    }

    

    public void convertCsValuesFromCsFastaLine(String csFastaLine) {

        csFastaLine = csFastaLine.substring(2);
        
        StringBuilder csValuesStringBuilder = new StringBuilder();

        for (int i = 0; i < csFastaLine.length(); i++) {
            char c = csFastaLine.charAt(i);
            //Process char
            csValuesStringBuilder.append(csToBWACSMap.get(c));            
        }
        
        csValues = csValuesStringBuilder.toString(); 

    }

    public void convertQualFromCsFastaQualLine(String qualLine) {
        
        String[] splitQualLine = qualLine.split(" ");
        
        String[] splitQualLine2 = Arrays.copyOfRange(splitQualLine, 1, splitQualLine.length);
        
        StringBuilder qualSB = new StringBuilder();
        
        for(String qualValue : splitQualLine2)
        {
            Character charToAppend;
            
            
            int charInteger = new Integer(qualValue);
            charInteger = charInteger + 33;
            charToAppend =  (char) charInteger;
            
            if(qualValue.equalsIgnoreCase("-1"))
            {
                charToAppend ='"';
            }
            if(qualValue.equalsIgnoreCase("0"))
            {
                charToAppend ='"';
            }         
            
            qualSB.append(charToAppend);
        
        }
        
        qualValues = qualSB.toString();        
        
        
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }    
    
    public String getSeqName() {
        return seqName;
    }

    public void setSeqName(String seqName) {
        this.seqName = seqName;
    }

    public String getCsValues() {
        return csValues;
    }

    public void setCsValues(String csValues) {
        this.csValues = csValues;
    }

    public String getQualValues() {
        return qualValues;
    }

    public void setQualValues(String qualValues) {
        this.qualValues = qualValues;
    }   

    @Override
    public String toString() {
        
        StringBuilder sb = new StringBuilder(105);
        sb.append("@");
        sb.append(seqName);
        sb.append("\n");
        sb.append(csValues);
        sb.append("\n");
        sb.append(description);
        sb.append("\n");
        sb.append(qualValues);
        sb.append("\n");
        
        return sb.toString();    
        
        
    }
    
    
    
    
    
}
