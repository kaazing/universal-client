/**
 * Kaazing Inc., Copyright (C) 2016. All rights reserved.
 */
package com.kaazing.client.universal;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.sql.Timestamp;

import com.kaazing.net.ws.amqp.AmqpChannel;
import com.kaazing.net.ws.amqp.AmqpProperties;

/**
 * Contains information specific to AMQP subscriptions
 * 
 * @author romans
 *
 */
public class AmqpClientSubscription extends ClientSubscription {

	private AmqpChannel pubChannel;
	private AmqpChannel subChannel;
	private boolean opened = true;
	private String appId;
	private String userId;
	private String pubChannelName;
	private String queueName;

	public AmqpClientSubscription(String subscriptionIdentifier, String appId, String userId, String pubChannelName, String queueName, AmqpChannel pubChannel, AmqpChannel subChannel) {
		super(subscriptionIdentifier);
		this.pubChannel = pubChannel;
		this.subChannel = subChannel;
		this.pubChannelName = pubChannelName;
		this.queueName = queueName;
		this.appId = appId;
		this.userId = userId;
	}

	@Override
	public void sendMessage(Serializable message) throws ClientException {
		AmqpMessageEnvelope messageEnvelope=new AmqpMessageEnvelope(this.appId, message);
		byte[] serializedObject;
		try {
			serializedObject = Utils.serialize(messageEnvelope);
		} catch (IOException e) {
			throw new ClientException("Cannot serialize message " + message + " to send over subscription " + this.getSubscriptionIdentifier(), e);
		}
		ByteBuffer buffer = ByteBuffer.allocate(serializedObject.length);
		buffer.put(serializedObject);
		buffer.flip();

		Timestamp ts = new Timestamp(System.currentTimeMillis());
		AmqpProperties props = new AmqpProperties();
		props.setMessageId("1");
		props.setCorrelationId("4");
		props.setAppId(appId);
		props.setUserId(userId);
		props.setPriority(6);
		props.setDeliveryMode(1);
		props.setTimestamp(ts);

		this.pubChannel.publishBasic(buffer, props, this.pubChannelName, AmqpUniversalClient.ROUTING_KEY, false, false);
		AmqpUniversalClient.LOGGER.debug("Sent message [" + message.toString() + "] to subscription to " + this.getSubscriptionIdentifier());
	}

	@Override
	public void disconnect() throws ClientException {
		if (opened) {
			AmqpUniversalClient.LOGGER.debug("Closing...");
			this.subChannel.deleteQueue(this.queueName, false, false, false);

			this.pubChannel.closeChannel(0, "", 0, 0);
			this.subChannel.closeChannel(0, "", 0, 0);
			opened = false;
		}
	}

}
