package com.yeeframework.automate.keyword;

import com.yeeframework.automate.Keyword;
import com.yeeframework.automate.actionable.DelayAction;
import com.yeeframework.automate.entry.QueryEntry;
import com.yeeframework.automate.exception.ScriptInvalidException;
import com.yeeframework.automate.reader.QueryReader;
import com.yeeframework.automate.workflow.Workflow;
import com.yeeframework.automate.workflow.WorkflowConfig;
import com.yeeframework.automate.workflow.WorkflowEntry;

public class Delay implements Keyword {
	
	@Override
	public String script() {
		return com.yeeframework.automate.script.Keywords.DELAY;
	}
	
	@Override
	public void run(WorkflowConfig wc, WorkflowEntry we, Workflow workflow) throws Exception {
		try {
			workflow
				.action(new DelayAction(Integer.valueOf(we.getVariable())));
		} catch (Exception e) {
			try {
				QueryReader qr = new QueryReader(we.getVariable());
				QueryEntry qe = qr.read();
				workflow
					.action(new DelayAction(qe));
			} catch (ScriptInvalidException e1) {
				throw e1;
			}
		}
	}

}
