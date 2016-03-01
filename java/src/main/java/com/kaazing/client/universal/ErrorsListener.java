/**
 * Kaazing Inc., Copyright (C) 2016. All rights reserved.
 */
package com.kaazing.client.universal;

public interface ErrorsListener {
	void onException(ClientException exception);
}
