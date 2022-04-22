package com.yeeframework.automate;

import com.yeeframework.automate.workflow.Workflow;
import com.yeeframework.automate.workflow.WorkflowEntry;

public interface Action {

	public String script();
	
	public void run(Object handler, WorkflowEntry we, Workflow workflow) throws Exception;
	
}
