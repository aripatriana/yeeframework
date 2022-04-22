package com.yeeframework.automate;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Value;

import com.yeeframework.automate.entry.QueryEntry;
import com.yeeframework.automate.io.FileIO;
import com.yeeframework.automate.report.ReportManager;
import com.yeeframework.automate.report.ReportMonitor;
import com.yeeframework.automate.report.SnapshotEntry;
import com.yeeframework.automate.util.DateUtils;
import com.yeeframework.automate.util.FindElement;
import com.yeeframework.automate.util.IDUtils;
import com.yeeframework.automate.util.MapUtils;
import com.yeeframework.automate.util.StringUtils;

public class Checkpoint {

	@Value("active_scen")
	private String testcase;
	
	@Value("active_workflow")
	private String scen;
	
	@Value("active_module_id")
	private String moduleId = "order-repo";
	
	@Value("active_menu_id")
	private String menuId = "order-repo";
	
	@Value("{tmp_dir}")
	private String tmpDir;
	
	@Value("{report_dir}")
	private String reportDir;
	
	@Value("start_time_milis")
	private String startTimeMilis;
	
	private String skipLastRow = "true";
	
	public Checkpoint() {
	}
	
	public void takeElements(WebDriver wd, WebExchange we) {
		putToSession(we, mapElements(new FindElement(wd), we.getElements(moduleId)));
	}

	public void takeElements(WebElement wl, WebExchange we) {
		putToSession(we, mapElements(new FindElement(wl), we.getElements(moduleId)));
	}
	
	public Map<String, Object> mapElements(FindElement fe, Map<String, Object> elements) {
		Map<String, Object> values = new LinkedHashMap<String, Object>();
		Map<String, Map<String, List<String>>> resultMap = new LinkedHashMap<String, Map<String,List<String>>>();
		
		for (Entry<String, Object> entry : elements.entrySet()) {
			if (entry.getKey().contains(QueryEntry.SQUARE_BRACKET)) {
				String[] keys = entry.getKey().split("\\"+QueryEntry.SQUARE_BRACKET);
				
				if (keys.length > 1) {
					Map<String, List<String>> map = resultMap.get(keys[0]);
					if (map == null) map = new LinkedHashMap<String, List<String>>();
					List<String> l = new LinkedList<String>();

					List<WebElement> wls = fe.findVisibleElements(entry.getValue().toString().split(";"));
					for (int i=0; i<wls.size(); i++) {
						if (skipLastRow.equals("true") && i==wls.size()-1) 
							break;
						
						values.put(keys[0] +"[" + i + "]" + keys[1], wls.get(i).getText());
						l.add(wls.get(i).getText());
					}
					map.put(keys[1].replace(".", ""), l);
					
					resultMap.put(keys[0], map);
				} else {
					List<String> l = new LinkedList<String>();
					List<WebElement> wls = fe.findVisibleElements(entry.getValue().toString().split(";"));
					for (int i=0; i<wls.size(); i++) {
						if (skipLastRow.equals("true") && i==wls.size()-1) 
							break;
						
						values.put(keys[0] +"[" + i + "]", wls.get(i).getText());
						l.add(wls.get(i).getText());
					}
					values.put(keys[0]+QueryEntry.SQUARE_BRACKET, l);
				}
			} else {
				values.put(entry.getKey(), fe.findVisibleElement(entry.getValue().toString().split(";")).getText());	
			}
		}
		
		if (resultMap.size() > 0) {
			for (Entry<String, Map<String, List<String>>> e : resultMap.entrySet()) {
				values.put(e.getKey()+QueryEntry.SQUARE_BRACKET, MapUtils.transpose(e.getValue()));
			}
		}
		return values;
	}
	
	private void putToSession(WebExchange we, Map<String, Object> values) {
		String filename = getOutputFile();
		we.putToSession(WebExchange.PREFIX_TYPE_ELEMENT, moduleId, values);
		MapUtils.clearMapKey(moduleId + ".", values);
		FileIO.write(filename, values);
		
		ReportMonitor.logSnapshotEntry(testcase, scen, we.getCurrentSession(), 
				SnapshotEntry.SNAPSHOT_AS_CHECKPOINT, values.toString(), filename, ReportManager.PASSED);
	}
	
	private String getOutputFile() {
		String filename = menuId.replace(".", "_") + "_checkpoint_"+ IDUtils.getRandomId() + ".txt";
		return StringUtils.path(reportDir, DateUtils.format(Long.valueOf(startTimeMilis)), testcase, scen.replace(testcase + "_", ""), filename);
	}

}
