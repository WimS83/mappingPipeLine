package bwa_picard_gatk_pipeline.sge;

import bwa_picard_gatk_pipeline.exceptions.JobFaillureException;
import org.ggf.drmaa.DrmaaException;

/**
 * This abstract class represents a job, which can be executed on the Sun Grid Engine.
 * @author Jetse
 */
public abstract class Job{
	private String sgeID = null;
	private int timesSubmitted = 0;
	private int maxSubmits = 5;

	/**
	 * The method getCommand is different for every job, this is the actual command which has to be executed on the node
	 * @return the command to execute on the node.
	 */
	public abstract String getCommand();

	/**
	 * The method getSGEName gets an unique name of the job, mostly this name contains the name of the executed program. 
	 * This name is used to remove jobs from the queue.
	 * @return The name on the sun grid engine.
	 */
	public abstract String getSGEName();

	/**
	 * A simple getter to get the sun grid engine ID
	 * @return sgeID the ID of this job at the sun grid engine
	 */	
	public String getSgeID(){
		return sgeID;
	}

	/**
	 * The method submit submits a job to the sun grid engine
	 * @return 
	 * @throws DrmaaException
	 */
	public final void submit() throws DrmaaException{
		sgeID = JobController.getInstance().submitJob(this);
		
	}
	
	/**
	 * The method reSubmit submits a failed job again.
	 * @throws DrmaaException
	 * @throws JobFaillureException
	 */
	public final void reSubmit() throws DrmaaException,  JobFaillureException{
		if(timesSubmitted < maxSubmits){
			submit();
			timesSubmitted++;
		}
		throw new JobFaillureException("Job " + getSGEName() + " failed "+ maxSubmits +" times");
	}

	/**
	 * The method isFinished returns whether the job is finished or not.
	 * @return whether the job is finished or not
	 * @throws DrmaaException when retrieving the status of the jobs fails
	 * @throws JobFaillureException When the job failed {maxSubmits} times
	 */
	public boolean isFinished() throws DrmaaException, JobFaillureException{
		try{
			return JobController.getInstance().isFinished(sgeID);
		}catch(JobFaillureException e){
			reSubmit();
		}
		return false;
	}
        
        public void deleleteJob() throws DrmaaException
        {            
            JobController.getInstance().deleteJob(sgeID);
        
        }
}
