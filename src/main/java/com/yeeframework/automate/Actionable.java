package com.yeeframework.automate;

import com.yeeframework.automate.exception.FailedTransactionException;
import com.yeeframework.automate.exception.ModalFailedException;

/**
 * The implementation of logic workflow should be implemented through this class
 * 
 * @author ari.patriana
 *
 */
public interface Actionable {

	public void submit(WebExchange webExchange) throws FailedTransactionException, ModalFailedException;
	
}
