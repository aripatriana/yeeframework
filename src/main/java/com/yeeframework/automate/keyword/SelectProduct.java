package com.yeeframework.automate.keyword;

import com.yeeframework.automate.Keyword;
import com.yeeframework.automate.actionable.ProductSelectorAction;
import com.yeeframework.automate.workflow.Workflow;
import com.yeeframework.automate.workflow.WorkflowConfig;
import com.yeeframework.automate.workflow.WorkflowEntry;

public class SelectProduct implements Keyword {

	@Override
	public String script() {
		return com.yeeframework.automate.script.Keywords.SELECT_PRODUCT;
	}
	
	@Override
	public void run(WorkflowConfig wc, WorkflowEntry we, Workflow workflow) throws Exception {
		workflow.getWebExchange().put("productType", we.getVariable());
		workflow
		.action(new ProductSelectorAction(we.getVariable()));
	}
}
