/**
 * Kaazing Inc., Copyright (C) 2016. All rights reserved.
 */
package com.kaazing.client.universal;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kaazing.gateway.jms.client.JmsConnectionFactory;
import com.kaazing.gateway.jms.client.JmsInitialContext;
import com.kaazing.net.http.HttpRedirectPolicy;
import com.kaazing.net.ws.WebSocketFactory;

/**
 * JMS specific implementation of Universal Client
 * @author romans
 *
 */
public class JMSUniversalClient implements ExceptionListener, UniversalClient {
	private final ErrorsListener errorsListener;
	private final InitialContext jndiInitialContext;
	private final ConnectionFactory connectionFactory;
	private Connection connection;
	private Session session;
	private List<JMSClientSubscription> connections=new ArrayList<>();
	private String url;
	protected static Logger LOGGER=LoggerFactory.getLogger(JMSUniversalClient.class);

	public JMSUniversalClient(URI url, String login, String password, ErrorsListener errorsListener) throws ClientException {
		this.errorsListener = errorsListener;
		this.url=url.toString();

		Properties env = new Properties();
		env.setProperty("java.naming.factory.initial", "com.kaazing.gateway.jms.client.JmsInitialContextFactory");

		try {
			jndiInitialContext = new InitialContext(env);
		} catch (NamingException e1) {
			throw new ClientException("Error creating initial context factory for JMS!", e1);
		}
		env.put(JmsInitialContext.CONNECTION_TIMEOUT, "15000");
		try {
			connectionFactory = (ConnectionFactory) jndiInitialContext.lookup("ConnectionFactory");
		} catch (NamingException e) {
			throw new ClientException("Error locating connection factory for JMS!", e);
		}
		JmsConnectionFactory jmsConnectionFactory = (JmsConnectionFactory) connectionFactory;
		jmsConnectionFactory.setGatewayLocation(url);
		WebSocketFactory webSocketFactory = jmsConnectionFactory.getWebSocketFactory();
		webSocketFactory.setDefaultRedirectPolicy(HttpRedirectPolicy.ALWAYS);
		try {
			connection = connectionFactory.createConnection(login, password);
		} catch (JMSException e) {
			throw new ClientException("Error connecting to gateway with " + url.toString() + ", credentials " + login + "/" + password, e);
		}
		try {
			connection.setExceptionListener(this);
		} catch (JMSException e) {
			throw new ClientException("Error setting exceptions listener. Connection: " + url.toString() + ", credentials " + login + "/" + password, e);
		}
		try {
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		} catch (JMSException e) {
			throw new ClientException("Error creating session. Connection: " + url.toString() + ", credentials " + login + "/" + password, e);
		}
		try {
			connection.start();
		} catch (JMSException e) {
			throw new ClientException("Error starting connection: " + url.toString() + ", credentials " + login + "/" + password, e);
		}
		LOGGER.info("Connected to "+this.url);
	}

	/* (non-Javadoc)
	 * @see com.kaazing.client.universal.UniversalClientProtocolImpl#connect(java.lang.String, java.lang.String, com.kaazing.client.universal.MessagesListener, boolean)
	 */
	@Override
	public ClientSubscription subscribe(String pubTopicName, String subTopicName, MessagesListener messageListener, boolean noLocal) throws ClientException {
		String clientId=null;
		Destination subDestination;
		try {
			subDestination = (Destination) jndiInitialContext.lookup("/topic/" + subTopicName);
		} catch (NamingException e) {
			throw new ClientException("Cannot locate subscription topic " + subTopicName, e);
		}
		MessageConsumer consumer;
		try {
			
			if (noLocal){
				clientId=UUID.randomUUID().toString();
				consumer = session.createConsumer(subDestination, "clientId<>'"+clientId+"'");
			}
			else
				consumer = session.createConsumer(subDestination);
		} catch (JMSException e) {
			throw new ClientException("Cannot create consumer for subscription topic " + subTopicName, e);
		}
		LOGGER.info("Created subscription to "+subTopicName+" for connection to "+this.url);
		try {
			consumer.setMessageListener(new MessageListenerImpl(messageListener, this.errorsListener, subTopicName));
		} catch (JMSException e) {
			throw new ClientException("Cannot create messages listener for subscription topic " + subTopicName, e);

		}
		Destination pubDestination;
		try {
			pubDestination = (Destination) jndiInitialContext.lookup("/topic/" + pubTopicName);
		} catch (NamingException e) {
			throw new ClientException("Cannot locate publishing topic " + pubTopicName, e);
		}
		MessageProducer producer;
		try {
			producer = session.createProducer(pubDestination);
		} catch (JMSException e) {
			throw new ClientException("Cannot create producer for publishing topic " + pubTopicName, e);
		}
		LOGGER.info("Connected to publishing topic "+pubTopicName+" for connection to "+this.url);
		JMSClientSubscription clientConnection=new JMSClientSubscription(clientId, Utils.generateIdentifier(this.url, pubTopicName, subTopicName), this.session, producer, consumer);
		this.connections.add(clientConnection);
		return clientConnection;
	}

	@Override
	public void onException(JMSException exception) {
		LOGGER.error("JMS Exception occurred ", exception);
		this.errorsListener.onException(new ClientException("JMS Exception!", exception));
	}

	private class MessageListenerImpl implements MessageListener {

		private MessagesListener listener;
		private ErrorsListener errorsListener;
		private String destination;

		public MessageListenerImpl(MessagesListener listener, ErrorsListener errorsListener, String destination) {
			this.listener = listener;
			this.errorsListener = errorsListener;
			this.destination = destination;
		}

		@Override
		public void onMessage(Message message) {
			// GenericBytesMessageImpl m;
			if (message instanceof BytesMessage) {
				try {					
					BytesMessage bytesMessage = ((BytesMessage) message);
					long len = bytesMessage.getBodyLength();
					byte b[] = new byte[(int) len];
					bytesMessage.readBytes(b);
					Serializable object;
					try {
						object = Utils.deserialize(b);
						LOGGER.debug("Received message ["+object.toString()+"] on topic "+destination+", connection to "+url);
						this.listener.onMessage(object);
					} catch (ClassNotFoundException | IOException e) {
						this.errorsListener.onException(new ClientException("Cannot deserialize an object from the message received from " + destination, e));
						LOGGER.error("Cannot deserialize an object from the message received from " + destination+" connection to "+url, e);
						return;
					}
					
				} catch (JMSException e) {
					this.errorsListener.onException(new ClientException("Error receiving message from destination " + destination, e));
					LOGGER.error("Error receiving message from destination " + destination+" connection to "+url, e);
				}
			} else {
				this.errorsListener.onException(new ClientException("Received a message of unexpected type " + message.getClass().getName() + " for a destination " + destination));
				LOGGER.error("Received a message of unexpected type " + message.getClass().getName()  + destination+" connection to "+url);
			}

		}

	}

	/* (non-Javadoc)
	 * @see com.kaazing.client.universal.UniversalClientProtocolImpl#close()
	 */
	@Override
	public void close() throws Exception {
		for(JMSClientSubscription connection: this.connections){
			connection.disconnect();
		}
		this.connection.stop();
		this.session.close();
		this.connection.close();
		LOGGER.info("Connection to "+this.url+" is closed.");
	}
}
