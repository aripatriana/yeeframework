package com.yeeframework.automate;

import org.openqa.selenium.WebElement;

import com.yeeframework.automate.exception.FailedTransactionException;
import com.yeeframework.automate.exception.ModalFailedException;

/**
 * The needed for callback
 * 
 * @author ari.patriana
 *
 */
public interface Callback  {

	public void callback(WebElement webElement, WebExchange webExchange) throws FailedTransactionException, ModalFailedException;

}
