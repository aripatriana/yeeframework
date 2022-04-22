package com.yeeframework.automate.schedule;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class JobWorkflowExecutor implements Job {

	public JobWorkflowExecutor() {
		// TODO Auto-generated constructor stub
	}
	
	public void execute(JobExecutionContext context)
		      throws JobExecutionException
    {
      System.err.println("Hello!  HelloJob is executing.");
    }
}
