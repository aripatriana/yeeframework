package com.yeeframework.automate.keyword;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;

import com.yeeframework.automate.Actionable;
import com.yeeframework.automate.ConfigLoader;
import com.yeeframework.automate.Keyword;
import com.yeeframework.automate.RunTestApplication;
import com.yeeframework.automate.WebExchange;
import com.yeeframework.automate.actionable.LoginFormAction;
import com.yeeframework.automate.actionable.LogoutFormAction;
import com.yeeframework.automate.actionable.ProductSelectorAction;
import com.yeeframework.automate.exception.FailedTransactionException;
import com.yeeframework.automate.exception.ModalFailedException;
import com.yeeframework.automate.util.LoginInfo;
import com.yeeframework.automate.workflow.Workflow;
import com.yeeframework.automate.workflow.WorkflowConfig;
import com.yeeframework.automate.workflow.WorkflowEntry;

public class Relogin implements Keyword {

	@Value("login.url.it")
	private String loginUrl;
	
	@Value("login.url.cm")
	private String loginUrlCm;
	
	@Value("productType")
	private String productType;
	
	@Override
	public String script() {
		return com.yeeframework.automate.script.Keywords.RELOGIN;
	}
	
	
	@Override
	public void run(WorkflowConfig wc, WorkflowEntry we, Workflow workflow) throws Exception {
		String prefix = LoginInfo.parsePrefixVariable(we.getVariable());
		final String loginUrl = ("it".equals(prefix)) ? this.loginUrl : this.loginUrlCm;		
		workflow
			.action(new LogoutFormAction())
			.action(new Actionable() {
				
				@Override
				public void submit(WebExchange webExchange) throws FailedTransactionException, ModalFailedException {
					workflow.openPage(loginUrl);
					
				}
			})
			.action(new LoginFormAction(getLoginInfo(LoginInfo.parseVariable(we.getVariable()))))
			.action(new ProductSelectorAction(productType));
		
	}
	
	public LoginInfo getLoginInfo(String variable) {
		Map<String, Object> loginUser = ConfigLoader.getLoginInfo(variable);
		return new LoginInfo(loginUser.get(variable + "." + RunTestApplication.PREFIX_MEMBER_CODE).toString(), 
				loginUser.get(variable + "." + RunTestApplication.PREFIX_USERNAME).toString(), 
				loginUser.get(variable + "." + RunTestApplication.PREFIX_PASSWORD).toString(), 
				loginUser.get(variable + "." + RunTestApplication.PREFIX_KEYFILE).toString());
	}
}
