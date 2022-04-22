package com.yeeframework.automate.keyword;

import com.yeeframework.automate.Keyword;
import com.yeeframework.automate.actionable.LogoutFormAction;
import com.yeeframework.automate.workflow.Workflow;
import com.yeeframework.automate.workflow.WorkflowConfig;
import com.yeeframework.automate.workflow.WorkflowEntry;

public class Logout implements Keyword {

	@Override
	public String script() {
		return com.yeeframework.automate.script.Keywords.LOGOUT;
	}
	
	@Override
	public void run(WorkflowConfig wc, WorkflowEntry we, Workflow workflow) throws Exception {
		workflow.action(new LogoutFormAction());
	}
}
