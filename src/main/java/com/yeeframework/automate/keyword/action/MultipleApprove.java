package com.yeeframework.automate.keyword.action;

import com.yeeframework.automate.Action;
import com.yeeframework.automate.util.ReflectionUtils;
import com.yeeframework.automate.workflow.Workflow;
import com.yeeframework.automate.workflow.WorkflowEntry;

public class MultipleApprove implements Action {

	@Override
	public String script() {
		return com.yeeframework.automate.script.Actions.MULTIPLE_APPROVE;
	}

	@Override
	public void run(Object handler, WorkflowEntry we, Workflow workflow) throws Exception {
		ReflectionUtils.invokeMethod(handler, com.yeeframework.automate.script.Actions.MULTIPLE_APPROVE, Workflow.class, workflow);
	}

}
