package com.yeeframework.automate.actionable;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.yeeframework.automate.Actionable;
import com.yeeframework.automate.DBConnection;
import com.yeeframework.automate.WebExchange;
import com.yeeframework.automate.entry.SetVarEntry;
import com.yeeframework.automate.exception.FailedTransactionException;
import com.yeeframework.automate.exception.ModalFailedException;
import com.yeeframework.automate.report.ReportManager;
import com.yeeframework.automate.report.ReportMonitor;
import com.yeeframework.automate.report.SnapshotEntry;

public class SetVariableAction implements Actionable {

	Logger log = LoggerFactory.getLogger(SetVariableAction.class);
	
	private SetVarEntry set;
	
	@Value("active_scen")
	private String testcase;
	
	@Value("active_workflow")
	private String scen;
	
	public SetVariableAction(SetVarEntry set) {
		this.set = set;
	}
	
	@Override
	public void submit(WebExchange webExchange) throws FailedTransactionException, ModalFailedException {
		if (webExchange.getCountSession() == 0) {
			ReportMonitor.logError(webExchange.get("active_scen").toString(),
					webExchange.get("active_workflow").toString(), "The session is needed when executing the query using a variable, use loadFile()");
			throw new FailedTransactionException("The session is needed when executing the query using a variable, use loadFile()");
		}
		
		// distinct module
		Set<String> module = new HashSet<String>();
		if (set.getQuery() != null) {
			for (String variable : set.getQuery().getVariables()) {
				if (variable.startsWith("@"+WebExchange.PREFIX_TYPE_DATA)) {
					module.add(variable.split("\\.")[1]);
				}
			}
		}
		
	
		for (int i=0; i<webExchange.getCountSession(); i++) {
			try {
				String sessionId = webExchange.createSession(i);
				if (!webExchange.isSessionFailed(sessionId)) {
					webExchange.setCurrentSession(sessionId);
				
					// log data monitor
					for (String m : module) {
						webExchange.setCurrentSession(i);
						Map<String, Object> metadata = webExchange.getMetaData(m, i);
						ReportMonitor.logDataEntry(webExchange.getCurrentSession(),webExchange.get("active_scen").toString(),
								webExchange.get("active_workflow").toString(), null, metadata);
					}
					
					
					if (set.getQuery() != null) {
						try {
							String[] query = set.getParsedQuery(webExchange);
							List<String[]> result = DBConnection.selectSimpleQuery(query[0], set.getQuery().getColumns().toArray(new String[] {}));
							if (result.size() > 1)
								throw new FailedTransactionException("Result more than one row for query " + query[0]);
							
							webExchange.put(set.getVariable(), result.get(0)[0]);
							

							ReportMonitor.logSnapshotEntry(testcase, scen, webExchange.getCurrentSession(), 
									SnapshotEntry.SNAPSHOT_AS_RAWTEXT, set.getVariable()+"="+query[0], null, ReportManager.PASSED);
						} catch (Exception e) {
							throw new FailedTransactionException("Failed execute query " + set.getScript() + " " + e.getMessage());
						}
					} else {
						webExchange.put(set.getVariable(), set.getValue());
						

						ReportMonitor.logSnapshotEntry(testcase, scen, webExchange.getCurrentSession(), 
								SnapshotEntry.SNAPSHOT_AS_RAWTEXT, set.getScript(), null, ReportManager.PASSED);
					}
					
						
				}	
				
			} catch (FailedTransactionException e) {
				webExchange.addFailedSession(webExchange.getCurrentSession());
				log.error("Failed for transaction ", e);
				ReportMonitor.logDataEntry(webExchange.getCurrentSession(),webExchange.get("active_scen").toString(),
						webExchange.get("active_workflow").toString(), null, null, 
						e.getMessage(), ReportManager.FAILED);
			}
		}
		
	}

}
