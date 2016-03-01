/**
 * Kaazing Inc., Copyright (C) 2016. All rights reserved.
 */
package com.kaazing.client.universal;

import java.io.Serializable;

public interface MessagesListener {
	public void onMessage(Serializable message);
}
