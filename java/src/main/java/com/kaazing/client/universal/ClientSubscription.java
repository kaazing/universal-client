/**
 * Kaazing Inc., Copyright (C) 2016. All rights reserved.
 */
package com.kaazing.client.universal;

import java.io.Serializable;

/**
 * Contains information about subscription
 * @author romans
 *
 */
public abstract class ClientSubscription {
	private final String subscriptionIdentifier;

	/**
	 * Construct the subscription object. The constructor should be overwritten by the implementation classes.
	 * @param subscriptionIdentifier Identification of the subscription that is automatically generated when subscription is created.
	 */
	public ClientSubscription(String subscriptionIdentifier){
		this.subscriptionIdentifier=subscriptionIdentifier;
	}
	
	/**
	 * Sends the message over established subscription to the publishing point
	 * @param message message to send
	 * @throws ClientException indicates that error occurred
	 */
	public abstract void sendMessage(Serializable message) throws ClientException;
	
	/**
	 * Closes both publishing and subscription endpoints
	 * @throws ClientException indicates that error occurred
	 */
	public abstract void disconnect() throws ClientException;

	public String getSubscriptionIdentifier() {
		return subscriptionIdentifier;
	}
}
