package com.yeeframework.automate.keyword;

import com.yeeframework.automate.FileRetention;
import com.yeeframework.automate.Keyword;
import com.yeeframework.automate.exception.XlsSheetStyleException;
import com.yeeframework.automate.reader.MultiLayerXlsFileReader;
import com.yeeframework.automate.workflow.Workflow;
import com.yeeframework.automate.workflow.WorkflowConfig;
import com.yeeframework.automate.workflow.WorkflowEntry;

public class LoadFile implements Keyword {

	@Override
	public String script() {
		return com.yeeframework.automate.script.Keywords.LOAD_FILE;
	}
	
	@Override
	public void run(WorkflowConfig wc, WorkflowEntry we, Workflow workflow) throws Exception {
		try {
			FileRetention retention = new FileRetention(new MultiLayerXlsFileReader(wc.getWorkflowData(we.getVariable())));
			workflow.load(retention);
		} catch (XlsSheetStyleException e) {
			throw new Exception(e);
		}
	}
}
