package com.yeeframework.automate.keyword;

import com.yeeframework.automate.Keyword;
import com.yeeframework.automate.entry.ArgsEntry;
import com.yeeframework.automate.exception.ScriptInvalidException;
import com.yeeframework.automate.function.ExecuteAction;
import com.yeeframework.automate.reader.ArgsReader;
import com.yeeframework.automate.workflow.Workflow;
import com.yeeframework.automate.workflow.WorkflowConfig;
import com.yeeframework.automate.workflow.WorkflowEntry;

public class Execute implements Keyword {

	@Override
	public String script() {
		return com.yeeframework.automate.script.Keywords.EXECUTE;
	}

	@Override
	public void run(WorkflowConfig wc, WorkflowEntry we, Workflow workflow) throws Exception {
		try {
			ArgsReader ar = new ArgsReader(we.getVariable());
			ArgsEntry ae = ar.read();

			ExecuteAction actionable = new ExecuteAction(wc.getFunction(ae.getFunction()), ae);
			workflow.action(actionable);
		} catch (ScriptInvalidException e) {
			throw e;
		}
	}

}
