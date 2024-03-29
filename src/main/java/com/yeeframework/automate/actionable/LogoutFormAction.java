package com.yeeframework.automate.actionable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yeeframework.automate.Actionable;
import com.yeeframework.automate.WebElementWrapper;
import com.yeeframework.automate.WebExchange;
import com.yeeframework.automate.util.Sleep;

/**
 * The action for logout
 * 
 * @author ari.patriana
 *
 */
public class LogoutFormAction extends WebElementWrapper implements Actionable {
	Logger log = LoggerFactory.getLogger(LogoutFormAction.class);
	
	@Override
	public void submit(WebExchange webExchange) {
		if (webExchange.get("token") == null) 
			return;
		
		log.info("Logout");
		
		Sleep.wait(1000);
		
		findElementByXpath("//a[contains(@title,'Sign Out')]").click();
		
		webExchange.remove("username");
		webExchange.remove("memberCode");
		webExchange.remove("password");
		webExchange.remove("token");
		
		log.info("Logout success");
	}
}
