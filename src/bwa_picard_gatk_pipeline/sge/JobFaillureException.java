package bwa_picard_gatk_pipeline.sge;

public class JobFaillureException extends Exception{
	private static final long serialVersionUID = 1L;

	public JobFaillureException(String message){
		super(message);
	}
}