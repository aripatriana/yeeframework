package com.yeeframework.automate.keyword;

import org.springframework.beans.factory.annotation.Value;

import com.yeeframework.automate.Keyword;
import com.yeeframework.automate.actionable.AssertQueryAction;
import com.yeeframework.automate.entry.QueryEntry;
import com.yeeframework.automate.exception.ScriptInvalidException;
import com.yeeframework.automate.reader.QueryReader;
import com.yeeframework.automate.reader.TemplateReader;
import com.yeeframework.automate.workflow.Workflow;
import com.yeeframework.automate.workflow.WorkflowConfig;
import com.yeeframework.automate.workflow.WorkflowEntry;

public class AssertAggregate implements Keyword {

	@Value("active_scen")
	private String testCase;
		
	@Override
	public String script() {
		return com.yeeframework.automate.script.Keywords.ASSERT_AGGREGATE;
	}
	
	@Override
	public void run(WorkflowConfig wc, WorkflowEntry we, Workflow workflow) throws Exception {
		try {			
			TemplateReader tr = new TemplateReader(wc.getWorkflowQuery(testCase, we.getVariable()));
			QueryReader qr = new QueryReader(tr.read().toString());
			QueryEntry qe = qr.read();
			AssertQueryAction actionable = new AssertQueryAction(qe);
			workflow.action(actionable);
		} catch (ScriptInvalidException e) {
			throw e;
		}
	}
}
