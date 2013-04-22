
package bwa_picard_gatk_pipeline.sge;

import bwa_picard_gatk_pipeline.exceptions.JobFaillureException;
import org.ggf.drmaa.DrmaaException;
import org.ggf.drmaa.JobTemplate;
import org.ggf.drmaa.Session;
import org.ggf.drmaa.SessionFactory;

/**
 * The JobController regulates all interactions with the SGE master via a Session object.
 * @author Jetse
 */
public class JobController{
	
	private static JobController instance = null;
	
	private Session session;
	private JobTemplate sgeJob;

	/**
	 * Method for retrieving the instance, so only one instance exists at the same time
	 * @return the instance of a jobController, a new JobController is created if none exist yet.
	 */
	public static JobController getInstance() throws DrmaaException{
		if(instance == null){
			instance =  new JobController();
		}
		return instance;
	}
	
	/**
	 * The constructor of the jobController can be called only once. This method creates a new session, and makes sure the session is closed afterwards.
	 * @throws DrmaaException When the session can not be initalized or the jobTemplate can not be created.
	 */
	private JobController() throws DrmaaException{
		session = SessionFactory.getFactory().getSession();
		session.init("");
		sgeJob = session.createJobTemplate();
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				try {
					session.exit();
					session.deleteJobTemplate(sgeJob);
				} catch (DrmaaException e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	/**
	 * The method submitJobs submits an array of jobs
	 * @param jobsToSubmit the jobs to submit to the cluster
	 * @throws DrmaaException When submitting to the cluster fails
	 */
//	public void submitJobs(Job[] jobsToSubmit) throws DrmaaException{
//		ArrayList<String> ids = new ArrayList<String>();
//		for(Job jobToSubmit: jobsToSubmit){
//			String ID = submitJob(jobToSubmit);
//			jobToSubmit.setSgeID(ID);
//			ids.add(ID);
//		}
//	}
	
	/**
	 * The method isFinished checks whether a job is finished, returns true if finished, false if not. 
	 * This method is protected so it can only called from a Job, which will call this always with his newest ID, even after resubmit.
	 * @param id The id of the job
	 * @return Whether the job is finished or not
	 * @throws DrmaaException When retrieving the status fails
	 * @throws JobFaillureException When a job is failed.
	 */
	protected boolean isFinished(String id) throws DrmaaException, JobFaillureException{
		int status = session.getJobProgramStatus(id);
		switch(status){
		case Session.UNDETERMINED:
			throw new JobFaillureException("Job status cannot be determined");
		case Session.QUEUED_ACTIVE:
			return false;
		case Session.SYSTEM_ON_HOLD:
			return false;
		case Session.USER_ON_HOLD:
			return false;
		case Session.USER_SYSTEM_ON_HOLD:
			return false;
		case Session.RUNNING:
			return false;
		case Session.SYSTEM_SUSPENDED:
			return false;
		case Session.USER_SUSPENDED:
			return false;
		case Session.USER_SYSTEM_SUSPENDED:
			return false;
		case Session.FAILED:
			throw new JobFaillureException("Job finished, but failed");
		}
		return true;
	}

	/**
	 * The method submitJob submits a single job to the sun grid engine.
	 * @param jobToSubmit The job to submit
	 * @return The ID to access this job at the sun grid engine
	 * @throws DrmaaException When submitting the job fails
	 */
	protected String submitJob(Job jobToSubmit) throws DrmaaException {
		sgeJob.setJobName(jobToSubmit.getSGEName());
		sgeJob.setRemoteCommand(jobToSubmit.getCommand());
		String ID = session.runJob(sgeJob);
                System.out.println("Submitted job with id "+ID);
                
		return ID;		
	}
        
        protected void deleteJob(String sgeID) throws DrmaaException
        {
            session.control(sgeID, Session.TERMINATE);
        
        }
}