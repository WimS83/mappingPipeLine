package bwa_picard_gatk_pipeline;

public class JobFaillureException extends Exception{
	private static final long serialVersionUID = 1L;

	public JobFaillureException(String message){
		super(message);
	}
}