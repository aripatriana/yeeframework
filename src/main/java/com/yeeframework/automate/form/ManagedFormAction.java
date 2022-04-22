package com.yeeframework.automate.form;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import com.yeeframework.automate.Actionable;
import com.yeeframework.automate.ContextLoader;
import com.yeeframework.automate.FormActionable;
import com.yeeframework.automate.WebElementWrapper;
import com.yeeframework.automate.WebExchange;
import com.yeeframework.automate.exception.FailedTransactionException;
import com.yeeframework.automate.exception.ModalFailedException;

/**
 * The action is managed by the session
 * @author ari.patriana
 *
 */
public class ManagedFormAction extends WebElementWrapper implements FormActionable {

	private LinkedList<Actionable> actionableList = new LinkedList<Actionable>();
	private Class<?> inheritClass;
	private Map<String, Object> metadata = new HashMap<String, Object>();
	
	public ManagedFormAction(Class<?> inheritClass) {
		this.inheritClass = inheritClass;
	}
	
	public Class<?> getInheritClass() {
		return inheritClass;
	}
	
	public void addActionable(Actionable actionable) {
		actionableList.add(actionable);
	}
	
	public void setMetadata(Map<String, Object> metadata) {
		this.metadata = metadata;
	}
	
	@Override
	public void submit(WebExchange webExchange) throws FailedTransactionException, ModalFailedException {
		for (Actionable actionable : actionableList) {
			setObject(actionable, metadata);
			actionable.submit(webExchange);
		}
	}
	
	public void setObject(Actionable actionable, Map<String, Object> metadata) {
		if (ContextLoader.isPersistentSerializable(inheritClass)) {
			// execute map serializable
			if (ContextLoader.isLocalVariable(actionable)) {
				ContextLoader.setObjectLocal(actionable);
			} else {
				ContextLoader.setObjectWithCustom(actionable, metadata);
			}
		} else {		
			ContextLoader.setObject(actionable);
		}
	}
}
