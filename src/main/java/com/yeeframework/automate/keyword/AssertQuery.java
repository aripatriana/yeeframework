package com.yeeframework.automate.keyword;

import com.yeeframework.automate.Keyword;
import com.yeeframework.automate.actionable.AssertQueryAction;
import com.yeeframework.automate.entry.QueryEntry;
import com.yeeframework.automate.exception.ScriptInvalidException;
import com.yeeframework.automate.reader.QueryReader;
import com.yeeframework.automate.workflow.Workflow;
import com.yeeframework.automate.workflow.WorkflowConfig;
import com.yeeframework.automate.workflow.WorkflowEntry;

public class AssertQuery implements Keyword {

	@Override
	public String script() {
		return com.yeeframework.automate.script.Keywords.ASSERT_QUERY;
	}

	@Override
	public void run(WorkflowConfig wc, WorkflowEntry we, Workflow workflow) throws Exception {
		try {
			QueryReader qr = new QueryReader(we.getVariable());
			QueryEntry qe = qr.read();
			AssertQueryAction actionable = new AssertQueryAction(qe);
			workflow.action(actionable);
		} catch (ScriptInvalidException e) {
			throw e;
		}
	}

	
	
}
