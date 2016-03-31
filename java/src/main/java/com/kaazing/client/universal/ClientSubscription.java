/**
 * Copyright 2007-2015, Kaazing Corporation. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.kaazing.client.universal;

import java.io.Serializable;

/**
 * Contains information about subscription
 */
public abstract class ClientSubscription {
    private final String subscriptionIdentifier;

    /**
     * Construct the subscription object. The constructor should be overwritten
     * by the implementation classes.
     * 
     * @param subscriptionIdentifier
     *            Identification of the subscription that is automatically
     *            generated when subscription is created.
     */
    public ClientSubscription(String subscriptionIdentifier) {
        this.subscriptionIdentifier = subscriptionIdentifier;
    }

    /**
     * Sends the message over established subscription to the publishing point
     * 
     * @param message
     *            message to send
     * @throws ClientException
     *             indicates that error occurred
     */
    public abstract void sendMessage(Serializable message) throws ClientException;

    /**
     * Closes both publishing and subscription endpoints
     * 
     * @throws ClientException
     *             indicates that error occurred
     */
    public abstract void disconnect() throws ClientException;

    public String getSubscriptionIdentifier() {
        return subscriptionIdentifier;
    }
}
