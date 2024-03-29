package com.yeeframework.automate.function.arg;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.jcraft.jsch.ChannelExec;
import com.yeeframework.automate.Actionable;
import com.yeeframework.automate.ContextLoader;
import com.yeeframework.automate.WebExchange;
import com.yeeframework.automate.exception.FailedTransactionException;
import com.yeeframework.automate.exception.ModalFailedException;
import com.yeeframework.automate.util.PortForwarding;

public class ExecuteCmdAction implements Actionable  {

	Logger log = LoggerFactory.getLogger(ExecuteCmdAction.class);
	
	@Value(value = "gateway.address.localPort")
	private String gatewayLocalPort;
	
	@Value(value = "command")
	private String command;
	
	public ExecuteCmdAction() {
	}
	
	@Override
	public void submit(WebExchange webExchange) throws FailedTransactionException, ModalFailedException {
		PortForwarding tunnel = new PortForwarding(webExchange);
		try {
			
			tunnel.connect();
			ChannelExec channel = null;
			
			try {
				channel = (ChannelExec) tunnel.getSession().openChannel("exec");
				channel.setCommand(command);
	
				channel.connect();
			} finally {
				if (channel != null)
					channel.disconnect();
			}
			
		} catch (Exception e) {
			log.error("Exception ", e);
		} finally {
			tunnel.disconnect();
		}
	}
	
	public static void main(String[] args) throws FailedTransactionException, ModalFailedException {
		WebExchange webExchange = new WebExchange();
	
		webExchange.put("command", "curl -X POST http://192.168.2.71:8180/eclears-app-eventhandling/sendResponse  -H 'cache-control: no-cache'   -H 'content-type: application/json'  -H 'postman-token: b714303a-3862-67c8-0970-912279132bda'  -d '{\"eventId\":\"Settlement Repo Event\",\"filter\":\"Instruction_Type_ID=9028\"}'");
		webExchange.put("gateway.address.username","sigma");
		webExchange.put("gateway.address.password", "sigma123");
		webExchange.put("gateway.address.localPort", "8007");
		webExchange.put("gateway.address.remotePort", "8000");
		webExchange.put("gateway.address.remoteHost", "192.168.2.68");
		webExchange.put("gateway.address.host", "10.10.105.68");
		webExchange.put("timeout.msg.checkPosition", "300");
		webExchange.put("timeout.msg.checkFinalState", "300");
		
		webExchange.put("simple.datasource.url","jdbc:oracle:thin:@10.10.105.41:1521:fasdb");
		webExchange.put("simple.datasource.username","EAEPME");
		webExchange.put("simple.datasource.password","EAEPME");
		webExchange.put("simple.datasource.driverClassName","oracle.jdbc.driver.OracleDriver");
		
		ExecuteCmdAction act = new ExecuteCmdAction();
		ContextLoader.setWebExchange(webExchange);
		ContextLoader.setObject(act);

		
		act.submit(webExchange);

	}

}
