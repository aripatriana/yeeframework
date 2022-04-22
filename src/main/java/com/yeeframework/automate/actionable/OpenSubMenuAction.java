package com.yeeframework.automate.actionable;

import org.openqa.selenium.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yeeframework.automate.Actionable;
import com.yeeframework.automate.WebElementWrapper;
import com.yeeframework.automate.WebExchange;
import com.yeeframework.automate.util.Sleep;
import com.yeeframework.automate.util.StringUtils;

/**
 * The action for open the menu trees
 * 
 * @author ari.patriana
 *
 */
public class OpenSubMenuAction extends WebElementWrapper implements Actionable {

	Logger log = LoggerFactory.getLogger(OpenSubMenuAction.class);
	OpenMenuAction prevMenu;
	String menuName;
	String menuId;
	int timeout = 3;
	
	public OpenSubMenuAction(OpenMenuAction prevMenu, String menuName, String menuId) {
		this.prevMenu = prevMenu;
		this.menuName = menuName;
		this.menuId = menuId;
	}
	
	public String getMenuId() {
		return menuId;
	}
	
	public String getMenuName() {
		return menuName;
	}
	
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
	
	public int getTimeout() {
		return timeout;
	}
	
	@Override
	public void submit(WebExchange webExchange) {
		if (menuName == null) return;
		Sleep.wait(500);
		log.info("Open Sub Menu " + menuName);
		try {
			findElementByXpath("//ul[@id='" + StringUtils.removeLastChar(menuId, "::") + "']//li//a//span[text()='" + getMenuName() + "']", timeout).click();
		} catch (TimeoutException e) {
			if (prevMenu != null) {
				prevMenu.setTimeout(1);
				prevMenu.submit(webExchange);
				this.submit(webExchange);
			} else {
				getDriver().navigate().refresh();
				submit(webExchange);				
			}
		}
		
	}

}
