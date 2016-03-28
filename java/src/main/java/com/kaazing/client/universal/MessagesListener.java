/**
 * Kaazing Inc., Copyright (C) 2016. All rights reserved.
 */
package com.kaazing.client.universal;

import java.io.Serializable;

/**
 * Listener for the messages received by Universal Client
 */
public interface MessagesListener {
    /**
     * Called when message is received
     * 
     * @param message
     *            body of the message
     */
    public void onMessage(Serializable message);
}
