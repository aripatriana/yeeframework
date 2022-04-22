package com.yeeframework.automate.workflow;


import java.util.LinkedList;
import java.util.Map;

import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yeeframework.automate.Actionable;
import com.yeeframework.automate.ContextLoader;
import com.yeeframework.automate.DBConnection;
import com.yeeframework.automate.DriverManager;
import com.yeeframework.automate.FormActionable;
import com.yeeframework.automate.Menu;
import com.yeeframework.automate.MenuAwareness;
import com.yeeframework.automate.MultipleFormActionable;
import com.yeeframework.automate.Retention;
import com.yeeframework.automate.WebElementWrapper;
import com.yeeframework.automate.WebExchange;
import com.yeeframework.automate.actionable.ModalSuccessAction;
import com.yeeframework.automate.actionable.OpenFormAction;
import com.yeeframework.automate.actionable.OpenMenuAction;
import com.yeeframework.automate.actionable.OpenSubMenuAction;
import com.yeeframework.automate.exception.FailedTransactionException;
import com.yeeframework.automate.exception.ModalFailedException;
import com.yeeframework.automate.form.ManagedFormAction;
import com.yeeframework.automate.form.ManagedMultipleFormAction;
import com.yeeframework.automate.report.ReportManager;
import com.yeeframework.automate.report.ReportMonitor;


/**
 * Main workflow
 * 
 * @author ari.patriana
 *
 */
public abstract class Workflow {

	Logger log = LoggerFactory.getLogger(Workflow.class);
	protected WebExchange webExchange;
	protected LinkedList<Actionable> actionableForLoop;
	protected boolean activeLoop = false;
	protected Menu activeMenu;
	protected final int MAX_RETRY_LOAD_PAGE = 3;
	protected boolean scopedAction = false;
	int scopedActionIndex = 0;
	
	public Workflow(WebExchange webExchange) {
		this.webExchange = webExchange;
	}
	
	public WebExchange getWebExchange() {
		return webExchange;
	}
	
	public Menu getActiveMenu() {
		return activeMenu;
	}
	
	public void scopedAction() {
		this.scopedAction = true;
		this.scopedActionIndex = 0;
	}
	
	public void resetScopedAction() {
		this.scopedAction = false;
	}
	
	public Workflow openPage(String url) {
		log.info("Open Page " + url);
		
		DriverManager.getDefaultDriver().get(url);
		return this;
	}
	
	public void setActiveMenu(Menu activeMenu) {
		this.activeMenu = activeMenu;
		webExchange.put("active_module_id", activeMenu.getModuleId());
		webExchange.put("active_menu_id", activeMenu.getId());
	}

	public Workflow openMenu(Menu menu) {
		OpenMenuAction menuAction = new OpenMenuAction(null, menu.getMenu());
		OpenSubMenuAction subMenuAction = new OpenSubMenuAction(menuAction, menu.getSubMenu(), menu.getMenuId());
		OpenFormAction formAction = new OpenFormAction((menu.getSubMenu() != null ? subMenuAction : menuAction), menu.getMenuId(), menu.getForm());
		((MenuAwareness) formAction).setMenu(menu);
		activeMenu = menu;
		
		if (!activeLoop) {
			menuAction.submit(webExchange);
			subMenuAction.submit(webExchange);
			formAction.submit(webExchange);	
		} else {
			actionableForLoop.add(menuAction);
			actionableForLoop.add(subMenuAction);
			actionableForLoop.add(formAction);			
		}

		return this;
	}
	
	public Workflow actionMajor(Actionable actionable) {
		try {
			if (ContextLoader.isPersistentSerializable(actionable)) {
				if (ContextLoader.isLocalVariable(actionable)) {
					ContextLoader.setObjectLocal(actionable);
					executeSafeActionable(actionable);
				} else {
					ContextLoader.setObject(actionable);
					executeSafeActionable(actionable);	
				}
			} else {
				ContextLoader.setObject(actionable);
				executeSafeActionable(actionable);
			}
		} catch (FailedTransactionException e) {
			log.error("Failed for transaction ", e);
		} catch (ModalFailedException e) {
			log.error("Modal failed ", e);
		}
		return this;
	}
	
	public Workflow action(Actionable actionable) {
		if (!activeLoop) {
			try {
				if (ContextLoader.isPersistentSerializable(actionable)) {
					if (ContextLoader.isLocalVariable(actionable)) {
						ContextLoader.setObjectLocal(actionable);
						executeSafeActionable(actionable);
					} else {
						ContextLoader.setObject(actionable);
						executeSafeActionable(actionable);	
					}
				} else {
					ContextLoader.setObject(actionable);
					executeSafeActionable(actionable);
				}
			} catch (FailedTransactionException e) {
				log.error("Failed for transaction ", e);
			} catch (ModalFailedException e) {
				log.error("Modal failed ", e);
			}
		} else {
			if (scopedAction) {
				if (scopedActionIndex == 0) {
					ManagedFormAction scoped = null;
					if (actionable instanceof FormActionable) {
						scoped = new ManagedFormAction(actionable.getClass());
					} else if (actionable instanceof MultipleFormActionable) {
						scoped = new ManagedMultipleFormAction(actionable.getClass());
					}
					
					if (scoped != null) {
						scoped.addActionable(actionable);
						actionableForLoop.add(scoped);						
					} else {
						log.warn("Managed Action is missing for " + actionable);
					}
				} else {
					Actionable act = actionableForLoop.getLast();
					if (act != null)
						((ManagedFormAction) act).addActionable(actionable);
					else 
						log.warn("Managed Action is missing for " + actionable);
				}
				scopedActionIndex++;
			} else {
				actionableForLoop.add(actionable);	
			}
		}
		return this;
	}
	
	public Workflow load(Retention retention) {
		webExchange.setRetention(true);
		retention.perform(webExchange);
		return this;
	}
	
	public Workflow loop() {
		if (!webExchange.isRetention())
			throw new RuntimeException("Retention not initialized");
		actionableForLoop = new LinkedList<Actionable>();
		activeLoop = true;
		return this;
	}
	
	public abstract Workflow endLoop() throws Exception;
	
	public void executeActionableNoSession(Actionable actionable, Map<String, Object> metadata) throws Exception {
		if (isPersistentSerializable(actionable)) {
			// execute map serializable
			if (isLocalVariable(actionable)) {
				ContextLoader.setObjectLocal(actionable);
				executeSafeActionable(actionable);
			} else {
				ContextLoader.setObjectWithCustom(actionable, metadata);
				executeSafeActionable(actionable);
			}
		} else {
			// execute common action
			ContextLoader.setObject(actionable);
			executeSafeActionable(actionable);
		}
	}
	
	private boolean isPersistentSerializable(Object object) {
		if (object instanceof ManagedFormAction) {
			return ContextLoader.isPersistentSerializable(((ManagedFormAction) object).getInheritClass());
		}
		return ContextLoader.isPersistentSerializable(object);
	}
	
	private boolean isLocalVariable(Object object) {
		if (object instanceof ManagedFormAction) {
			return ContextLoader.isLocalVariable(((ManagedFormAction) object).getInheritClass());
		}
		return ContextLoader.isLocalVariable(object);
	}
	
	public void executeActionableWithSession(Actionable actionable) throws Exception {
		if (isPersistentSerializable(actionable)) {
			// execute map serializable
			if (isLocalVariable(actionable)) {
				if (actionable instanceof ManagedMultipleFormAction) {
					try {
						ContextLoader.setObjectLocal(actionable);	
						executeSafeActionable(actionable);
						((WebElementWrapper) actionable).getDriver().navigate().refresh();
						
						ReportMonitor.logDataEntry(getWebExchange().getSessionList(),getWebExchange().get("active_scen").toString(),
								getWebExchange().get("active_workflow").toString(), null, null);
					} catch (FailedTransactionException | IndexOutOfBoundsException e) {
						webExchange.addListFailedSession(webExchange.getSessionList());
						log.info("Transaction is not completed, skipped for further processes");
						log.error("Failed for transaction ", e);
						
						captureFailedWindow(actionable);
						((WebElementWrapper) actionable).getDriver().navigate().refresh();
						
						ReportMonitor.logDataEntry(getWebExchange().getSessionList(),getWebExchange().get("active_scen").toString(),
								getWebExchange().get("active_workflow").toString(), null, null, 
								e.getMessage(), ReportManager.FAILED);
					} catch (ModalFailedException e) {
						log.info("Modal failed, skipped for further processes");
//						webExchange.addListFailedSession(webExchange.getSessionList());
						
						ReportMonitor.logDataEntry(getWebExchange().getSessionList(),getWebExchange().get("active_scen").toString(),
								getWebExchange().get("active_workflow").toString(), null, null, 
								e.getMessage(), ReportManager.PASSED);
					}
				} else {
					int i = 0;
					while(true) {
						String sessionId = webExchange.createSession(i);
						if (!webExchange.isSessionFailed(sessionId)) {
							log.info("Execute data-row index " + i + " with session " + sessionId);
							webExchange.setCurrentSession(sessionId);
							
							Map<String, Object> metadata = null;
							try {	
								metadata = webExchange.getMetaData(getActiveMenu().getModuleId(), i);
								
								if (actionable instanceof ManagedFormAction) {
									ContextLoader.setObjectLocal(actionable);
									((ManagedFormAction) actionable).setMetadata(metadata);
								} else {
									ContextLoader.setObjectLocal(actionable);	
								}
							
								executeSafeActionable(actionable);
								((WebElementWrapper) actionable).getDriver().navigate().refresh();
								
								ReportMonitor.logDataEntry(getWebExchange().getCurrentSession(),getWebExchange().get("active_scen").toString(),
										getWebExchange().get("active_workflow").toString(), getWebExchange().getLocalSystemMap(), metadata);
							} catch (FailedTransactionException | IndexOutOfBoundsException e) {
								log.info("Transaction is not completed, data-index " + i + " with session " + webExchange.getCurrentSession() + " skipped for further processes");
								log.error("Failed for transaction ", e);

								webExchange.addFailedSession(sessionId);
								captureFailedWindow(actionable);
								((WebElementWrapper) actionable).getDriver().navigate().refresh();
								
								ReportMonitor.logDataEntry(getWebExchange().getCurrentSession(),getWebExchange().get("active_scen").toString(),
										getWebExchange().get("active_workflow").toString(), getWebExchange().getLocalSystemMap(),
										metadata, e.getMessage(), ReportManager.FAILED);
							} catch (ModalFailedException e) {
								log.info("Modal failed, data-index " + i + " with session " + webExchange.getCurrentSession() + " skipped for further processes");
//								webExchange.addFailedSession(sessionId);
								
								ReportMonitor.logDataEntry(getWebExchange().getCurrentSession(),getWebExchange().get("active_scen").toString(),
										getWebExchange().get("active_workflow").toString(), getWebExchange().getLocalSystemMap(),
										metadata, e.getMessage(), ReportManager.PASSED);
							}
						}
						i++;
						
						if (webExchange.getSessionList().size() <= i) {
							webExchange.clearCachedSession();
							break;
						}
					}
				}

			} else {
				int i = 0;
				while(true) {
					String sessionId = webExchange.createSession(i);
					if (!webExchange.isSessionFailed(sessionId)) {
						Map<String, Object> metadata = null;
						
						log.info("Execute data-row index " + i + " with session " + sessionId);
					
						try {
							metadata = webExchange.getMetaData(getActiveMenu().getModuleId(),i, true);
							
							if (actionable instanceof ManagedFormAction) {
								ContextLoader.setObject(actionable);
								((ManagedFormAction) actionable).setMetadata(metadata);
							} else {
								ContextLoader.setObjectWithCustom(actionable, metadata);	
							}
							
							executeSafeActionable(actionable);
							((WebElementWrapper) actionable).getDriver().navigate().refresh();
							
							ReportMonitor.logDataEntry(getWebExchange().getCurrentSession(),getWebExchange().get("active_scen").toString(),
									getWebExchange().get("active_workflow").toString(), getWebExchange().getLocalSystemMap(), metadata);
						} catch (FailedTransactionException | IndexOutOfBoundsException e) {
							log.info("Transaction is not completed, data-index " + i + " with session " + webExchange.getCurrentSession() + " skipped for further processes");
							log.error("Failed for transaction ", e);
							
							webExchange.addFailedSession(sessionId);
							captureFailedWindow(actionable);
							((WebElementWrapper) actionable).getDriver().navigate().refresh();
							
							ReportMonitor.logDataEntry(getWebExchange().getCurrentSession(), getWebExchange().get("active_scen").toString(),
									getWebExchange().get("active_workflow").toString(), getWebExchange().getLocalSystemMap(),
									metadata, e.getMessage(), ReportManager.FAILED);
						} catch (ModalFailedException e) {
							log.info("Modal failed, data-index " + i + " with session " + webExchange.getCurrentSession() + " skipped for further processes");
//							webExchange.addFailedSession(sessionId);
							
							ReportMonitor.logDataEntry(getWebExchange().getCurrentSession(), getWebExchange().get("active_scen").toString(),
									getWebExchange().get("active_workflow").toString(), getWebExchange().getLocalSystemMap(),
									metadata, e.getMessage(), ReportManager.PASSED);
						}
					}
					i++;
					
					if (webExchange.getListMetaData(getActiveMenu().getModuleId()).size() <= i) {
						webExchange.clearCachedSession();
						break;
					}
				}
			}
		} else {
			ContextLoader.setObject(actionable);
			executeSafeActionable(actionable);	
		}
	}
	
	public void executeSafeActionable(Actionable actionable) throws FailedTransactionException, ModalFailedException {
		int retry = 1;
		try {
			actionable.submit(webExchange);
		} catch (StaleElementReferenceException | ElementNotInteractableException | TimeoutException  | NoSuchElementException | IllegalArgumentException e) {
			retryWhenException(actionable, ++retry);
		}
	}
	
	private void retryWhenException(Actionable actionable, int retry) throws FailedTransactionException, ModalFailedException {
		try {
			log.info("Something happened, be calm! we still loving you!");

			((WebElementWrapper) actionable).getDriver().navigate().refresh();
			actionable.submit(webExchange);
		} catch (StaleElementReferenceException | ElementNotInteractableException | TimeoutException  | NoSuchElementException | IllegalArgumentException e) {			
			if (retry < MAX_RETRY_LOAD_PAGE) {
				retryWhenException(actionable, ++retry);
			} else {
				log.error("Failed for transaction ", e);
				throw new FailedTransactionException("Failed for transaction, " + e.getMessage());
			}	
		}
	}
	
	private void captureFailedWindow(Actionable actionable) {
		try {
			try {
				((WebElementWrapper)actionable).getModalConfirmationId(1);
				((WebElementWrapper)actionable).captureFailedWindow();
			} catch (Exception e) {
				((WebElementWrapper)actionable).captureFailedFullModal(((WebElementWrapper)actionable).getModalId(1));
			}
		} catch (Exception e1) {
			((WebElementWrapper)actionable).captureFailedFullWindow();
		}
	}
		
	public Workflow waitUntil(ModalSuccessAction actionable) {
		if (!activeLoop) {
			try {
				actionable.submit(webExchange);
			} catch (FailedTransactionException e) {
				log.error("Failed for transaction ", e);
			} catch (ModalFailedException e) {
				log.error("Modal failed ", e);
			}
		} else {
			if (scopedAction) {
				if (scopedActionIndex == 0) {
					ManagedFormAction scoped = null;
					if (actionable instanceof FormActionable) {
						scoped = new ManagedFormAction(actionable.getClass());
					} else if (actionable instanceof MultipleFormActionable) {
						scoped = new ManagedMultipleFormAction(actionable.getClass());
					}
					
					if (scoped != null) {
						scoped.addActionable(actionable);
						actionableForLoop.add(scoped);						
					} else {
						log.warn("Managed Action is missing for " + actionable);
					}
				} else {
					Actionable act = actionableForLoop.getLast();
					if (act != null)
						((ManagedFormAction) act).addActionable(actionable);
					else
						log.warn("Managed Action is missing for " + actionable);
				}
			} else {
				actionableForLoop.add(actionable);	
			}
			 
		}
		return this;
	}
	
	public Workflow clearSession() {
		log.info("Clear session");
		DBConnection.close();
		webExchange.clear();
//		Thread.currentThread().interrupt();
		return this;
	}
	

}
