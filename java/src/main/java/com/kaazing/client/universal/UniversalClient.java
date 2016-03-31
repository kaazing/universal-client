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

/**
 * Interface for Kaazing protocol independent universal client
 */
public interface UniversalClient extends AutoCloseable {
    /**
     * Connects client to pub/sub endpoints
     * 
     * @param pubTopicName
     *            name of publishing topic
     * @param subTopicName
     *            name of subscription topic
     * @param messageListener
     *            callback to receive messages
     * @param noLocal
     *            if true client will not receive its own messages (applicable
     *            only when pub and sub points are the same)
     * @return subscription information
     * @throws ClientException
     *             indicates that error occurred
     */
    public ClientSubscription subscribe(String pubTopicName, String subTopicName, MessagesListener messageListener, boolean noLocal) throws ClientException;
}