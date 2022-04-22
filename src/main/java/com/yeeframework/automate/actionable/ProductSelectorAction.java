package com.yeeframework.automate.actionable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yeeframework.automate.Actionable;
import com.yeeframework.automate.WebElementWrapper;
import com.yeeframework.automate.WebExchange;
import com.yeeframework.automate.exception.FailedTransactionException;
import com.yeeframework.automate.util.Sleep;

/**
 * The action for select the product choosen
 *  
 * @author ari.patriana
 *
 */
public class ProductSelectorAction extends WebElementWrapper implements Actionable {

	Logger log = LoggerFactory.getLogger(ProductSelectorAction.class);
	
	private String productType;
	
	public ProductSelectorAction(String productType) {
		this.productType = productType;
	}
	
	public void setProductType(String productType) {
		this.productType = productType;
	}
	
	public String getProductType() {
		return productType;
	}
	
	@Override
	public void submit(WebExchange webExchange) throws FailedTransactionException {

		 if (webExchange.get("token") == null)
			 throw new FailedTransactionException("Workflow halted caused by login failed");
		 
		log.info("Open Product " + getProductType());
		
		Sleep.wait(1000);
		findElementByXpath("//div[@class='divProductTypeSelector']/span[@id='project-selector']").click();
		findElementByXpath("//*[contains(@href,'" + getProductType() + "')]").click();
	}

}
