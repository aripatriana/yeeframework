package com.yeeframework.automate.workflow;

/**
 * Used to inject workflow config
 * 
 * @author ari.patriana
 *
 */
public interface WorkflowConfigAwareness {

	public void setWorkflowConfig(WorkflowConfig workflowConfig);
}
