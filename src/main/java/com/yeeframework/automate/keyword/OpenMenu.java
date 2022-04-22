package com.yeeframework.automate.keyword;

import com.yeeframework.automate.Action;
import com.yeeframework.automate.ContextLoader;
import com.yeeframework.automate.Keyword;
import com.yeeframework.automate.util.ReflectionUtils;
import com.yeeframework.automate.workflow.Workflow;
import com.yeeframework.automate.workflow.WorkflowConfig;
import com.yeeframework.automate.workflow.WorkflowEntry;

public class OpenMenu implements Keyword {

	@Override
	public String script() {
		return com.yeeframework.automate.script.Keywords.OPEN_MENU;
	}
	
	@Override
	public void run(WorkflowConfig wc, WorkflowEntry we, Workflow workflow) throws Exception {
		Class<?> clazz = wc.getHandler(we.getVariable());
		
		Object handler = ReflectionUtils.instanceObject(clazz);
		ContextLoader.setObject(handler);
	
		workflow.openMenu(wc.getMenu(we.getVariable()));
	
		workflow.scopedAction();
		
		Action action = wc.getAction(we.getActionType());
		if (action != null) {
			action.run(handler, we, workflow);
		} else {
			throw new Exception("Action is not found");
		}
		
		workflow.resetScopedAction();
	}
	
	
}
