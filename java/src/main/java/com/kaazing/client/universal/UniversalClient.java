/**
 * Kaazing Inc., Copyright (C) 2016. All rights reserved.
 */
package com.kaazing.client.universal;

/**
 * Interface for Kaazing protocol independent universal client 
 * @author romans
 *
 */
public interface UniversalClient extends AutoCloseable{
	/**
	 * Connects client to pub/sub endpoints 
	 * @param pubTopicName name of publishing topic
	 * @param subTopicName name of subscription topic
	 * @param messageListener callback to receive messages
	 * @param noLocal if true client will not receive its own messages (applicable only when pub and sub points are the same)
	 * @return connection information 
	 * @throws ClientException indicates that error occurred
	 */
	public ClientSubscription connect(String pubTopicName, String subTopicName, MessagesListener messageListener, boolean noLocal) throws ClientException;
}