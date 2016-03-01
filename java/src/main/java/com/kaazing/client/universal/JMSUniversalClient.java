/**
 * Kaazing Inc., Copyright (C) 2016. All rights reserved.
 */
package com.kaazing.client.universal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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

import com.kaazing.gateway.jms.client.JmsConnectionFactory;
import com.kaazing.gateway.jms.client.JmsInitialContext;
import com.kaazing.net.http.HttpRedirectPolicy;
import com.kaazing.net.ws.WebSocketFactory;

/**
 * Universal client implementation for JMS
 *
 */
public class JMSUniversalClient implements ExceptionListener, AutoCloseable {
	private final ErrorsListener errorsListener;
	private final InitialContext jndiInitialContext;
	private final ConnectionFactory connectionFactory;
	private Connection connection;
	private Session session;
	private List<JMSClentConnection> connections=new ArrayList<>();
	//private MessageProducer producer;
	//private MessageConsumer consumer;
	private String url;

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
	}

	public ClientConnection connect(String pubTopicName, String subTopicName, MessagesListener messageListener, boolean noLocal) throws ClientException {
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
		
		try {
			consumer.setMessageListener(new MessageListenerImpl(messageListener, this.errorsListener, subTopicName));
		} catch (JMSException e) {
			throw new ClientException("Cannot create messages listener for subscription topic " + subTopicName, e);

		}
		Destination pubDestination;
		try {
			pubDestination = (Destination) jndiInitialContext.lookup("/topic/" + pubTopicName);
		} catch (NamingException e) {
			throw new ClientException("Cannot locate publicshing topic " + pubTopicName, e);
		}
		MessageProducer producer;
		try {
			producer = session.createProducer(pubDestination);
		} catch (JMSException e) {
			throw new ClientException("Cannot create producer for publishing topic " + pubTopicName, e);
		}
		JMSClentConnection clientConnection=new JMSClentConnection(clientId, "[url="+this.url+", pub="+pubTopicName+", sub="+subTopicName+"]", this.session, producer, consumer);
		this.connections.add(clientConnection);
		return clientConnection;
	}

	@Override
	public void onException(JMSException exception) {
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
						object = Serializer.deserialize(b);
					} catch (ClassNotFoundException | IOException e) {
						this.errorsListener.onException(new ClientException("Cannot deserialize an object from the message received from " + destination, e));
						return;
					}
					this.listener.onMessage(object);
				} catch (JMSException e) {
					this.errorsListener.onException(new ClientException("Error receiving message from destination " + destination, e));

				}
			} else {
				this.errorsListener.onException(new ClientException("Received a message of unexpected type " + message.getClass().getName() + " for a destination " + destination));
			}

		}

	}

	@Override
	public void close() throws Exception {
		for(JMSClentConnection connection: this.connections){
			connection.disconnect();
		}
		this.connection.stop();
		this.session.close();
		this.connection.close();
	}

	public static class Serializer {

		public static byte[] serialize(Serializable obj) throws IOException {
			try (ByteArrayOutputStream b = new ByteArrayOutputStream()) {
				try (ObjectOutputStream o = new ObjectOutputStream(b)) {
					o.writeObject(obj);
				}
				return b.toByteArray();
			}
		}

		public static Serializable deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
			try (ByteArrayInputStream b = new ByteArrayInputStream(bytes)) {
				try (ObjectInputStream o = new ObjectInputStream(b)) {
					return (Serializable) o.readObject();
				}
			}
		}

	}

}
