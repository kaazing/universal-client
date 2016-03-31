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

import java.io.IOException;
import java.io.Serializable;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;

/**
 * Contains information specific to JMS subscriptions
 */
public class JMSClientSubscription extends ClientSubscription {
    private final MessageProducer producer;
    private final MessageConsumer consumer;
    private boolean opened = true;
    private final Session session;
    private String clientId;

    public JMSClientSubscription(String clientId, String subscriptionIdentifier, Session session, MessageProducer producer, MessageConsumer consumer) {
        super(subscriptionIdentifier);
        this.producer = producer;
        this.consumer = consumer;
        this.session = session;
        this.clientId = clientId;
    }

    @Override
    public void sendMessage(Serializable message) throws ClientException {
        BytesMessage bytesMessage;
        try {
            bytesMessage = session.createBytesMessage();
        } catch (JMSException e) {
            throw new ClientException("Subscription: " + this.getSubscriptionIdentifier() + " - Cannot create a BytesMessage for an object " + message.toString(), e);
        }
        try {
            bytesMessage.writeBytes(Utils.serialize(message));
        } catch (JMSException e) {
            throw new ClientException("Subscription: " + this.getSubscriptionIdentifier() + " - Cannot write bytes to set the payload of a BytesMessage for an object " + message.toString(), e);
        } catch (IOException e) {
            throw new ClientException("Subscription: " + this.getSubscriptionIdentifier() + " - Cannot serialize an object " + message.toString(), e);
        }
        if (clientId != null) {
            try {
                bytesMessage.setStringProperty("clientId", clientId);
            } catch (JMSException e) {
                throw new ClientException("Subscription: " + this.getSubscriptionIdentifier() + " - Cannot set a string property client id to " + clientId + " for an object " + message.toString(), e);
            }
        }
        try {
            producer.send(bytesMessage);
            JMSUniversalClient.LOGGER.debug("Sent message [" + message.toString() + "] to subscription to " + this.getSubscriptionIdentifier());
        } catch (JMSException e) {
            throw new ClientException("Subscription: " + this.getSubscriptionIdentifier() + " - Cannot sent and object message for an object " + message.toString(), e);
        }

    }

    @Override
    public void disconnect() throws ClientException {
        if (opened) {
            try {
                this.producer.close();
            } catch (JMSException e) {
                throw new ClientException("Subscription: " + this.getSubscriptionIdentifier() + " - Error closing producer.", e);
            }
            try {
                this.consumer.close();
            } catch (JMSException e) {
                throw new ClientException("Subscription: " + this.getSubscriptionIdentifier() + " - Error closing consumer.", e);
            }
            opened = false;
        }

    }

}
