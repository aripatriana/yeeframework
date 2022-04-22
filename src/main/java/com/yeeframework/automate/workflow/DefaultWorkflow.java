package com.yeeframework.automate.workflow;

import java.util.Map;

import com.yeeframework.automate.Actionable;
import com.yeeframework.automate.ConfigLoader;
import com.yeeframework.automate.ContextLoader;
import com.yeeframework.automate.FormActionable;
import com.yeeframework.automate.MenuAwareness;
import com.yeeframework.automate.WebExchange;
import com.yeeframework.automate.report.ReportMonitor;

/**
 * Workflow that supports for session operation
 * 
 * @author ari.patriana
 *
 */
public class DefaultWorkflow extends Workflow {

	
	public DefaultWorkflow(WebExchange webExchange) {
		super(webExchange);
	}
		
	public static Workflow configure() {
		WebExchange webExchange = new WebExchange();
		webExchange.putAll(ConfigLoader.getConfigMap());
		webExchange.addElements(ConfigLoader.getElementMap());
		for (Map<String, Object> login : ConfigLoader.getLoginInfos()) {
			webExchange.putAll(login);			
		}
		
		ContextLoader.setWebExchange(webExchange);
		return new DefaultWorkflow(webExchange);
	}
	
	public Workflow endLoop() throws Exception {
		if (!activeLoop && webExchange.isRetention()) {
			throw new RuntimeException("Loop must be initialized");	
		}
		
		if (!activeLoop)
			return this;
		
		try {
			if (webExchange.getTotalMetaData() > 0 || actionableForLoop.size() > 0) {
				webExchange.initSession(webExchange.getMetaDataSize());

				ReportMonitor.getScenEntry(webExchange.get("active_workflow").toString())
						.setNumOfData(webExchange.getMetaDataSize());
				
				log.info("Total data-row " + webExchange.getTotalMetaData());
				try {
					for (Actionable actionable : actionableForLoop) {
						
						if (actionable instanceof MenuAwareness) {
							setActiveMenu(((MenuAwareness) actionable).getMenu());
						}
						
						if (actionable instanceof FormActionable && webExchange.getMetaDataSize(getActiveMenu().getModuleId()) == 0) {
							log.info("Skip to process action for " + getActiveMenu().getId() +" data size " + webExchange.getMetaDataSize(getActiveMenu().getModuleId()));
						} else {
							// execute actionable if any session active, if all session failed no further process performed
							if (!(webExchange.getSessionList().size() > 0
									&& (webExchange.getSessionList().size() <= webExchange.getFailedSessionList().size()))) {
								executeActionableWithSession(actionable);						
							}							
						}
					}
				} catch (Exception e) { 
					log.info("Transaction interrupted ");
					log.error("ERROR ", e);
					throw e;
				}
			}	
		} finally {
			webExchange.setRetention(Boolean.FALSE);
			webExchange.clearMetaData();
			activeLoop = false;
		}
		
		return this;
	}
	
}
