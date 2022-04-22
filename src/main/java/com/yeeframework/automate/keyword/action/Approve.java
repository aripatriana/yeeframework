package com.yeeframework.automate.keyword.action;

import com.yeeframework.automate.Action;
import com.yeeframework.automate.ModalType;
import com.yeeframework.automate.util.ReflectionUtils;
import com.yeeframework.automate.workflow.Workflow;
import com.yeeframework.automate.workflow.WorkflowEntry;

public class Approve implements Action {

	@Override
	public String script() {
		return com.yeeframework.automate.script.Actions.APPROVE;
	}

	@Override
	public void run(Object handler, WorkflowEntry we, Workflow workflow) throws Exception {
		ReflectionUtils.invokeMethod(handler, com.yeeframework.automate.script.Actions.APPROVE, new Class[] {Workflow.class, ModalType.class}, new Object[] {workflow, ModalType.MAIN});
	}

}
