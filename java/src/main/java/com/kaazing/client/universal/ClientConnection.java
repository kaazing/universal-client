/**
 * Kaazing Inc., Copyright (C) 2016. All rights reserved.
 */
package com.kaazing.client.universal;

import java.io.Serializable;

/**
 * Contains information about connection
 * @author romans
 *
 */
public abstract class ClientConnection {
	private final String connectionIdentifier;

	/**
	 * Construct the connection object. The constructor should be overwritten by the implementation classes.
	 * @param connectionIdentifier
	 */
	public ClientConnection(String connectionIdentifier){
		this.connectionIdentifier=connectionIdentifier;
	}
	
	/**
	 * Sends the message over established connection to the publishing point
	 * @param message message to send
	 * @throws ClientException indicates that error occurred
	 */
	public abstract void sendMessage(Serializable message) throws ClientException;
	
	/**
	 * Closes connection to both publishing and subscription endpoints
	 * @throws ClientException
	 */
	public abstract void disconnect() throws ClientException;

	public String getConnectionIdentifier() {
		return connectionIdentifier;
	}
}
