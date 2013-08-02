package bwa_picard_gatk_pipeline.sge;

import bwa_picard_gatk_pipeline.exceptions.JobFaillureException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.ggf.drmaa.DrmaaException;

/**
 * This abstract class represents a job, which can be executed on the Sun Grid
 * Engine.
 *
 * @author Jetse
 */
public abstract class Job extends File {

    private static final long serialVersionUID = 1L;
    private String sgeID = null;
    private int timesSubmitted = 0;
    private int maxSubmits = 5;
    private BufferedWriter out;
    protected String hostName;
    protected Integer sgeThreads;
    
    private Process proces;

    public Job(String filePath) throws IOException {
        super(filePath);
        createShellJobFile();
    }

    /**
     * The method getSGEName gets an unique name of the job, mostly this name
     * contains the name of the executed program. This name is used to remove
     * jobs from the queue.
     *
     * @return The name on the sun grid engine.
     */
    public abstract String getSGEName();

    /**
     * A simple getter to get the sun grid engine ID
     *
     * @return sgeID the ID of this job at the sun grid engine
     */
    public String getSgeID() {
        return sgeID;
    }

    /**
     * The method submit submits a job to the sun grid engine
     *
     * @return
     * @throws DrmaaException
     */
    public final void submit() throws DrmaaException {
        sgeID = JobController.getInstance().submitJob(this, hostName, sgeThreads);
        String hostNameToPrint = "none";        
        String sgeThreadToPrint = "none";      
        if(hostName != null){ hostNameToPrint = hostName; }
        if(sgeThreads != null){ sgeThreadToPrint = sgeThreads.toString(); }
        
        System.out.println("Submitted job " + getSGEName() + "ID: " + sgeID + " hostname: " + hostNameToPrint + " SGE_slots: " + sgeThreadToPrint);
        
        
        
    }

    /**
     * The method reSubmit submits a failed job again, till maxSubmits times.
     *
     * @throws DrmaaException
     * @throws JobFaillureException
     */
    private final void reSubmit() throws DrmaaException, JobFaillureException {
        if (timesSubmitted < maxSubmits) {
            System.err.println("resubmitted " + timesSubmitted + " times: " + getSGEName());
            submit();
            timesSubmitted++;
        }
        throw new JobFaillureException("Job " + getSGEName() + " failed " + maxSubmits + " times");
    }

    /**
     * The method waitFor waits till this job is finished, when the job fails,
     * the job is resubmitted
     *
     * @return whether the job is finished or not
     * @throws DrmaaException when retrieving the status of the jobs fails
     * @throws JobFaillureException When the job failed {maxSubmits} times
     */
    public void waitFor() throws DrmaaException, JobFaillureException {
        try {
            JobController.getInstance().waitFor(sgeID);
        } catch (JobFaillureException e) {
            reSubmit();

        }
    }

    /**
     * This method puts the default commands in the job file.
     *
     * @throws IOException When writing to the shell job file fails.
     */
    private void createShellJobFile() throws IOException {
        FileWriter fstream = new FileWriter(getAbsolutePath());
        out = new BufferedWriter(fstream);
        out.write("#!/bin/sh");
        out.newLine();
        out.write("#$ -S /bin/sh");
        out.newLine();
    }

    protected void addCommand(String command) throws IOException {
        out.write(command);
        out.newLine();
    }

    protected void close() throws IOException {
        out.close();
    }

    public void executeOffline() throws IOException, InterruptedException {

        List<String> commands = new ArrayList<String>();
        commands.add("/bin/sh");
        commands.add(this.getAbsolutePath());

        ProcessBuilder processBuilder = new ProcessBuilder(commands);
        processBuilder.directory(this.getParentFile());
        proces = processBuilder.start();
        
    }
    
    public void waitForOfflineExecution() throws InterruptedException
    {
        proces.waitFor();
    }
}
