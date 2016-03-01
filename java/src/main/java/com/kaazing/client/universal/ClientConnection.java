package com.kaazing.client.universal;

import java.io.Serializable;

public abstract class ClientConnection {
	private final String connectionIdentifier;

	public ClientConnection(String connectionIdentifier){
		this.connectionIdentifier=connectionIdentifier;
	}
	
	public abstract void sendMessage(Serializable message) throws ClientException;
	
	public abstract void disconnect() throws ClientException;

	public String getConnectionIdentifier() {
		return connectionIdentifier;
	}
}
