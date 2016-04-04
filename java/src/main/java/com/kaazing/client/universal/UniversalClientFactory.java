/**
 * Copyright 2007-2016, Kaazing Corporation. All rights reserved.
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
