package bwa_picard_gatk_pipeline.sge;



import bwa_picard_gatk_pipeline.exceptions.JobFaillureException;
import org.ggf.drmaa.DrmaaException;
import org.ggf.drmaa.JobInfo;
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
					session.deleteJobTemplate(sgeJob);
					session.exit();
				} catch (DrmaaException e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	/**
	 * The method waitFor waits till a job is finished.
	 * This method is protected so it can only called from a Job, which will call this always with his newest ID, even after resubmit.
	 * @param id The id of the job
	 * @return Whether the job is finished or not
	 * @throws DrmaaException When retrieving the status fails
	 * @throws JobFaillureException When a job is failed.
	 */
	protected void waitFor(String id) throws DrmaaException, JobFaillureException{
		JobInfo info = session.wait(id, Session.TIMEOUT_WAIT_FOREVER);
		if (info.wasAborted()) {
			throw new JobFaillureException("Job finished, but failed");
	    } else if (info.hasExited()) {
	    	//optional, easy to implement: print job information, stored in info object.
	    	return;
	    }
		throw new JobFaillureException("Job not aborted either exited...");
	}

	/**
	 * The method submitJob submits a single job to the sun grid engine.
	 * @param jobToSubmit The job to submit
	 * @return The ID to access this job at the sun grid engine
	 * @throws DrmaaException When submitting the job fails
	 */
	protected String submitJob(Job jobToSubmit, String hostName, Integer threads) throws DrmaaException {
		sgeJob.setJobName(jobToSubmit.getSGEName());
		
                
                StringBuilder nativeSpeficicaion = new StringBuilder();
                nativeSpeficicaion.append(" -b no");               
                
                if(hostName != null)
                {
                    nativeSpeficicaion.append(" -l hostname="+hostName );
                }
                if(threads != null)
                {
                    nativeSpeficicaion.append(" -pe threaded "+ threads);
                }
                
                sgeJob.setNativeSpecification(nativeSpeficicaion.toString());                
		sgeJob.setRemoteCommand(jobToSubmit.getAbsolutePath());
		//sgeJob.setErrorPath("fedor12:" + ((TophatJob)jobToSubmit).getFastaFile().getOpts().get(Opts.OUTPUTDIR));
		
		String ID = session.runJob(sgeJob);
		return ID;		
	}
}