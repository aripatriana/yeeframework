package com.yeeframework.automate.workflow;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yeeframework.automate.ConfigLoader;
import com.yeeframework.automate.ContextLoader;
import com.yeeframework.automate.RunTestWorkflow;
import com.yeeframework.automate.report.ReportManager;
import com.yeeframework.automate.report.ReportMonitor;

/**
 * Default implementation of RunTestWorkflow
 * 
 * @author ari.patriana
 *
 */
public class RunTestWorkflowExecutable implements RunTestWorkflow, WorkflowConfigAwareness {

	Logger log = LoggerFactory.getLogger(RunTestWorkflowExecutable.class);
	
	WorkflowConfig workflowConfig;
	
	@Override
	public void setWorkflowConfig(WorkflowConfig workflowConfig) {
		this.workflowConfig = workflowConfig;
	}
	
	@Override
	public void testWorkflow() {
		long startExeDate = System.currentTimeMillis();
		try {
			for (String scen : workflowConfig.getWorkflowScens()) {
				try {
					Workflow workflow = DefaultWorkflow.configure();
					ContextLoader.getWebExchange().put("active_scen", scen);
					ContextLoader.getWebExchange().put("start_time_milis", startExeDate);
					
					WorkflowExecutor executor = new WorkflowExecutor();
					ContextLoader.setObject(executor);
			
					executor.execute(scen, workflow, workflowConfig);
					
					ReportMonitor.completeTestCase(scen);
				} catch (Exception e) {
					log.error("FATAL ERROR ", e);
					
					ReportMonitor.testCaseHalted(scen, e.getMessage());
				}			
			}
		} catch (Exception e) {
			log.error("FATAL ERROR ", e);
		} finally {
			try {
				ReportManager report = new ReportManager(String.valueOf(startExeDate));
				ContextLoader.setObjectLocalWithCustom(report, ConfigLoader.getConfigMap());
				report.createReport();
			} catch (IOException e) {
				log.error("FATAL ERROR ", e);
			}		

			log.info("Finished in "  + (System.currentTimeMillis()-startExeDate)/1000  + " seconds");
		}
	}
}
