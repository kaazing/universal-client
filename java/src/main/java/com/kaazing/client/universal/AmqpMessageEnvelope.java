/**
 * Kaazing Inc., Copyright (C) 2016. All rights reserved.
 */
package com.kaazing.client.universal;

import java.io.Serializable;

/**
 * Adds the sender ID to the message so it can be used for noLocal filtering
 * when it is not supported by the server
 *
 */
public class AmqpMessageEnvelope implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = -7749481904427909042L;
    private final String clientId;
    private final Serializable data;

    public AmqpMessageEnvelope(String clientId, Serializable data) {
        this.clientId = clientId;
        this.data = data;
    }

    public String getClientId() {
        return clientId;
    }

    public Serializable getData() {
        return data;
    }

    @Override
    public String toString() {
        return "[clientId=" + this.getClientId() + ", data=" + this.getData() + "]";
    }
}
