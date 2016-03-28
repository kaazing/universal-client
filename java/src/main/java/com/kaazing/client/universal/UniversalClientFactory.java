/**
 * Kaazing Inc., Copyright (C) 2016. All rights reserved.
 */
package com.kaazing.client.universal;

import java.net.URI;

/**
 * Factory to create an instance of Universal Client
 */
public class UniversalClientFactory {
    private UniversalClientFactory() {

    }

    /**
     * Constructs an instance of Universal client specific to protocols
     * 
     * @param protocol
     *            Protocol to use
     * @param url
     *            URL to Kaazing Gateway
     * @param login
     *            Login to use with Kaazing Gateway
     * @param password
     *            Login to use with Kaazing Gateway
     * @param errorsListener
     *            Callback to handle client errors
     * @return instance of Universal Client specific to a selected protocol
     * @throws ClientException
     *             indicates an error
     */
    public static UniversalClient createUniversalClient(UniversalClientProtocol protocol, URI url, String login, String password, ErrorsListener errorsListener) throws ClientException {
        switch (protocol) {
        case amqp:
            return new AmqpUniversalClient(url, login, password, errorsListener);
        case jms:
            return new JMSUniversalClient(url, login, password, errorsListener);
        default:
            throw new ClientException("Unsupported protocol " + protocol.toString());
        }
    }

}
