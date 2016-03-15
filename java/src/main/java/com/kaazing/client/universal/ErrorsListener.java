/**
 * Kaazing Inc., Copyright (C) 2016. All rights reserved.
 */
package com.kaazing.client.universal;

/**
 * Provides implementation for calback to handle the errors
 * @author romans
 *
 */
public interface ErrorsListener {
	/**
	 * Called by the Universal Client when exception occurs
	 * @param exception exception reported by the client
	 */
	void onException(ClientException exception);
}
