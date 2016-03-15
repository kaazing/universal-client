/**
 * Kaazing Inc., Copyright (C) 2016. All rights reserved.
 */
package com.kaazing.client.universal;

import static org.junit.Assert.*;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author romans
 *
 */
public class AmqpUniversalClientTest {
	private AmqpUniversalClient amqpClient;
	private String receivedMessage="";
	private String errorMessage="";
	final CountDownLatch latch=new CountDownLatch(1);

	
	@Before
	public void setUp() throws ClientException, URISyntaxException{
		this.receivedMessage="";
		this.errorMessage="";
		amqpClient=new AmqpUniversalClient(new URI("ws://localhost:8001/amqp"), "guest", "guest", new ErrorsListener() {
			
			@Override
			public void onException(ClientException exception) {
				errorMessage=exception.getMessage();
				latch.countDown();
			}
		});
	}
	
	

	@Test
	public void testString() throws ClientException, InterruptedException {
		
		ClientSubscription connection = amqpClient.subscribe("test", "test", new MessagesListener() {
			
			@Override
			public void onMessage(Serializable message) {
				receivedMessage=message.toString();
				latch.countDown();
				
			}
		}, false);
		
		connection.sendMessage("Test");
		latch.await(5, TimeUnit.SECONDS);
		assertEquals("", errorMessage);
		assertEquals("Test", receivedMessage);
	}
	
	@Test
	public void testObject() throws ClientException, InterruptedException {
		
		ClientSubscription connection = amqpClient.subscribe("test", "test", new MessagesListener() {
			
			@Override
			public void onMessage(Serializable message) {
				receivedMessage=message.toString();
				latch.countDown();
				
			}
		}, false);
		
		connection.sendMessage(new TestObject(1, "Kaazing"));
		latch.await(5, TimeUnit.SECONDS);
		assertEquals("", errorMessage);
		assertEquals("[1, 'Kaazing']", receivedMessage);
	}

	@Test
	public void testNoLocal() throws ClientException, InterruptedException {
		ClientSubscription connection = amqpClient.subscribe("test", "test", new MessagesListener() {
			
			@Override
			public void onMessage(Serializable message) {
				receivedMessage=message.toString();
				latch.countDown();
				
			}
		}, true);
		
		connection.sendMessage(new TestObject(1, "Kaazing"));
		latch.await(1, TimeUnit.SECONDS);
		assertEquals("", errorMessage);
		assertEquals("", receivedMessage);
	}
	
	@After
	public void shutDown() throws Exception{
		amqpClient.close();
	}
	
	public static class TestObject implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = 2627789983264188629L;
		private int a;
		private String b;
		
		public TestObject(int a, String b){
			this.a=a;
			this.b=b;
		}
		
		@Override
		public String toString() {
			return "["+a+", '"+b+"']";
		}
	}
}
