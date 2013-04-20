/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bwa_picard_gatk_pipeline.sge;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.ggf.drmaa.DrmaaException;

/**
 *
 * @author root
 */
public class Cleanup implements Runnable{

    List<Job> jobsToWaitFor = null;
    
    
    public Cleanup(List<Job> jobsToWaitFor) {
        this.jobsToWaitFor = jobsToWaitFor;
        
    }
    
    
    
    
    public void run(){
//        try {
//            boolean allJobsFinished = false;
//            while(allJobsFinished == false){
//                Thread.sleep(1000 * 60 * 5);
//                allJobsFinished = true;
//                for(Job job:jobsToWaitFor){
//                    try{
//                        if(job.isFinished() == false){
//                            allJobsFinished = false;
//                            break;
//                        }
//                    }catch (JobFaillureException e) {
//                        writeLogFile(e.getMessage(), Arrays.asList(new String[]{"Check the tophat logs for mistakes in the tophat arguments"}));
//                    }
//                }               
//            }
//
//            Thread.sleep(1000 * 60 * 10);
//            cleanup();
//        } catch (DrmaaException e) {
//            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    private void cleanup() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
