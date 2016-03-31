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
