package com.yeeframework.automate.keyword;

import com.yeeframework.automate.Keyword;
import com.yeeframework.automate.workflow.Workflow;
import com.yeeframework.automate.workflow.WorkflowConfig;
import com.yeeframework.automate.workflow.WorkflowEntry;

public class ClearSession implements Keyword {
	
	@Override
	public String script() {
		return com.yeeframework.automate.script.Keywords.CLEAR_SESSION;
	}
	
	@Override
	public void run(WorkflowConfig wc, WorkflowEntry we, Workflow workflow) throws Exception {
		try {
			workflow.clearSession();
		} catch (Exception e) {
			throw e;
		} finally {
			workflow.clearSession();							
		}
	}
	

}
