/**
 * Kaazing Inc., Copyright (C) 2016. All rights reserved.
 */
package com.kaazing.client.universal;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kaazing.net.ws.amqp.AmqpChannel;
import com.kaazing.net.ws.amqp.AmqpClient;
import com.kaazing.net.ws.amqp.AmqpClientFactory;
import com.kaazing.net.ws.amqp.ChannelAdapter;
import com.kaazing.net.ws.amqp.ChannelEvent;
import com.kaazing.net.ws.amqp.ConnectionEvent;
import com.kaazing.net.ws.amqp.ConnectionListener;

/**
 * AMQP specific implmentation of Universal client
 * @author romans
 *
 */
public class AmqpUniversalClient implements UniversalClient {
	protected static Logger LOGGER=LoggerFactory.getLogger(AmqpUniversalClient.class);

	protected static final String ROUTING_KEY = "broadcastkey";
	private final AmqpClient amqpClient;
	private final AmqpClientFactory amqpClientFactory;
	private final ErrorsListener errorsListener;
	private final String url;
	private boolean fConnected;
	private boolean fPubOpened;
	private boolean fSubOpened;
	private final List<AmqpClientSubscription> connections = new ArrayList<>();
	private String login;

	public AmqpUniversalClient(URI url, String login, String password, ErrorsListener errorsListener) throws ClientException {
		this.login=login;
		CountDownLatch latch = new CountDownLatch(1);
		this.url = url.toString();
		this.errorsListener = errorsListener;
		amqpClientFactory = AmqpClientFactory.createAmqpClientFactory();
		amqpClient = amqpClientFactory.createAmqpClient();
		fConnected = false;
		amqpClient.addConnectionListener(new ConnectionListener() {

			@Override
			public void onConnectionOpen(ConnectionEvent e) {
				LOGGER.info("Connected to "+url+" message "+e.getMessage());
				fConnected = true;
				latch.countDown();
			}

			@Override
			public void onConnectionError(ConnectionEvent e) {
				LOGGER.error("Connection error to url "+url+"... "+e.getMessage());
				errorsListener.onException(new ClientException("Error connecting to " + url + ": " + e.getMessage()));
				latch.countDown();
			}

			@Override
			public void onConnectionClose(ConnectionEvent e) {
				for (AmqpClientSubscription conn : connections) {
					try {
						conn.disconnect();
					} catch (ClientException e1) {
						errorsListener.onException(new ClientException("Error closing client connection: "+conn.getSubscriptionIdentifier(), e1));
						LOGGER.error("Error closing client connection: "+conn.getSubscriptionIdentifier(), e1);
					}
				}
				LOGGER.info("Closed connection to "+url+".");
			}

			@Override
			public void onConnecting(ConnectionEvent e) {


			}
		});
		amqpClient.connect(this.url, "/", login, password);
		try {
			latch.await(10, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			throw new ClientException("Exception wating for connection latch connecting to " + this.url, e);
		}
		if (!fConnected) {
			throw new ClientException("Connection to " + this.url + " was not established in 10 sec.");
		}

	}

	@Override
	public void close() throws Exception {
		for (AmqpClientSubscription conn : this.connections) {
			conn.disconnect();
		}
		this.amqpClient.disconnect();
		LOGGER.info("Connection to "+this.url+" is closed.");
	}

	@Override
	public ClientSubscription subscribe(String pubTopicName, String subTopicName, MessagesListener messageListener, boolean noLocal) throws ClientException {
		CountDownLatch connectionsLatch = new CountDownLatch(2);
		AmqpChannel pubChannel = this.amqpClient.openChannel();
		fPubOpened = false;
		fSubOpened = false;
		pubChannel.addChannelListener(new ChannelAdapter() {
			@Override
			public void onClose(ChannelEvent e) {

			}

			@Override
			public void onError(final ChannelEvent e) {
				errorsListener.onException(new ClientException("Error creating publishing channel for " + pubTopicName + ": " + e.getMessage()));
				LOGGER.error("Error creating publishing channel "+pubTopicName+" for connection to "+url+": "+e.getMessage());
				connectionsLatch.countDown();
			}

			@Override
			public void onDeclareExchange(ChannelEvent e) {
				LOGGER.info("Created channel "+pubTopicName+" for url "+url);
				fPubOpened = true;
				connectionsLatch.countDown();
			}

			@Override
			public void onOpen(ChannelEvent e) {
				pubChannel.declareExchange(pubTopicName, "fanout", false, false, false, null);				
			}
		});
		String appId="app"+UUID.randomUUID().toString();
		String clientId = UUID.randomUUID().toString();
		String queueName = "client" + clientId;
		AmqpChannel subChannel = this.amqpClient.openChannel();

		subChannel.addChannelListener(new ChannelAdapter() {			
			@Override
			public void onError(final ChannelEvent e) {
				errorsListener.onException(new ClientException("Error creating subscription channel " + subTopicName + ": " + e.getMessage()));
				LOGGER.error("Error creating subscription channel "+subTopicName+" for url "+url+": "+e.getMessage());
				connectionsLatch.countDown();
			}

			@Override
			public void onConsumeBasic(ChannelEvent e) {
				LOGGER.info("Created subscription channel "+subTopicName+" for url "+url);
				fSubOpened = true;
				connectionsLatch.countDown();
			}

			@Override
			public void onMessage(final ChannelEvent e) {
				LOGGER.debug("Received message...");
				byte[] bytes = new byte[e.getBody().remaining()];
				e.getBody().get(bytes);
				try {
					Serializable object=Utils.deserialize(bytes);
					if (!(object instanceof AmqpMessageEnvelope)){
						errorsListener.onException(new ClientException("Received object is not an instance of AmqpMessageEnvelope;  received from " + subTopicName));
						LOGGER.error("Received object is not an instance of AmqpMessageEnvelope;  received from " + subTopicName +" for url"+url);
						return;
					}
					AmqpMessageEnvelope messageEnvelope=(AmqpMessageEnvelope)object;
					if (noLocal && messageEnvelope.getClientId().equals(appId)){
						LOGGER.debug("Received message ["+messageEnvelope.toString()+"] on topic "+subTopicName+", connection to "+url+" is ignored as it came from the same client and noLocal is set!");
						return;
					}
					LOGGER.debug("Received message ["+messageEnvelope.getData().toString()+"] on topic "+subTopicName+", connection to "+url);
					messageListener.onMessage(messageEnvelope.getData());
				} catch (ClassNotFoundException | IOException e1) {
					errorsListener.onException(new ClientException("Cannot deserialize an object from the message received from " + subTopicName, e1));
					LOGGER.error("Cannot deserialize an object from the message received from " + subTopicName +" for url"+url);
					return;
				}
			}

			@Override
			public void onOpen(ChannelEvent e) {
				subChannel.declareQueue(queueName, false, false, false, false, false, null)
					.bindQueue(queueName, subTopicName, ROUTING_KEY, false, null)
					.consumeBasic(queueName, clientId, noLocal, false, false, false, null);
			}
		});

		try {
			connectionsLatch.await(10, TimeUnit.SECONDS);
		} catch (InterruptedException e1) {
			throw new ClientException("Exception wating for connection latch creating channels for [" + pubChannel + "/" + subChannel, e1);
		}

		if (!fPubOpened) {
			throw new ClientException("Publishing channel was not opened in 10 sec during creating channels for [" + pubChannel + "/" + subChannel);
		}

		if (!fSubOpened) {
			throw new ClientException("Subscribing channel was not opened in 10 sec during creating channels for [" + pubChannel + "/" + subChannel);
		}

		AmqpClientSubscription connection = new AmqpClientSubscription(Utils.generateIdentifier(this.url, pubTopicName, subTopicName), appId, this.login, pubTopicName, queueName, pubChannel, subChannel);
		this.connections.add(connection);

		return connection;
	}

}
