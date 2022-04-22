package com.yeeframework.automate.workflow;

import org.apache.log4j.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yeeframework.automate.ContextLoader;
import com.yeeframework.automate.DriverManager;
import com.yeeframework.automate.Keyword;
import com.yeeframework.automate.actionable.LogoutFormAction;
import com.yeeframework.automate.report.ReportMonitor;

/**
 * The execution of workflow comes from here
 * 
 * @author ari.patriana
 *
 */
public class WorkflowExecutor {

	Logger log = LoggerFactory.getLogger(WorkflowExecutor.class);
	
	public void execute(String scen, Workflow workflow, WorkflowConfig config) throws Exception {
		for (String workflowKey : config.getWorkflowKey(scen)) {
	        MDC.put("testcase", workflowKey);
	        
			try {
				log.info("Execute workflow " + workflowKey);
				ContextLoader.getWebExchange().put("active_workflow", workflowKey);
				if (config.getWorkflowModule(workflowKey) != null)
					ContextLoader.getWebExchange().setModules(config.getWorkflowModule(workflowKey));
				
				for (WorkflowEntry entry : config.getWorkflowEntries(workflowKey)) {
					Keyword keyword = config.getKeyword(entry.getKeyword());
					ContextLoader.setObject(keyword);
					
					keyword.run(config, entry, workflow);
				}
				
				ReportMonitor.completeScen(workflowKey);
			} catch (Exception e) {
				e.printStackTrace();
				log.error(e.toString());
				// scenario halted caused by exception
				ReportMonitor.scenHalted(scen, workflowKey, e.getMessage());
			} finally {
				// if exception occured in any state of the workflow, must be ensured to logout the system
				 if (workflow.getWebExchange().get("token") != null) {
					 try {
						 workflow.actionMajor(new LogoutFormAction());	 
					 } catch (Exception e) {
						 // if exception keeps stubborn then close driver
						 DriverManager.close();
					 }
				 }
					
				 MDC.remove("testcase");
			}
		}
	}
	
}

