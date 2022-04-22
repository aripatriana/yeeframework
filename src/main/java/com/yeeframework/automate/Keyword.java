package com.yeeframework.automate;

import com.yeeframework.automate.workflow.Workflow;
import com.yeeframework.automate.workflow.WorkflowConfig;
import com.yeeframework.automate.workflow.WorkflowEntry;

public interface Keyword {

	public String script();
	
	public void run(WorkflowConfig wc, WorkflowEntry we, Workflow workflow) throws Exception;
}
