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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * 
 * Provides implementation of commonly used utilities
 */
public class Utils {
    /**
     * Serializes java object to bytes
     * 
     * @param obj
     *            Object to serialize
     * @return byte array containing serialized objects
     * @throws IOException
     *             indicates that object cannot be serialized
     */
    public static byte[] serialize(Serializable obj) throws IOException {
        try (ByteArrayOutputStream b = new ByteArrayOutputStream()) {
            try (ObjectOutputStream o = new ObjectOutputStream(b)) {
                o.writeObject(obj);
            }
            return b.toByteArray();
        }
    }

    /**
     * Deserializes the object from byte array
     * 
     * @param bytes
     *            byte array containing the object
     * @return Deserialized object
     * @throws IOException
     *             indicates that deserialization cannot be performed due to IO
     *             error
     * @throws ClassNotFoundException
     *             indicates that deserialization cannot be performed due to the
     *             abscence of suitable class
     */
    public static Serializable deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream b = new ByteArrayInputStream(bytes)) {
            try (ObjectInputStream o = new ObjectInputStream(b)) {
                return (Serializable) o.readObject();
            }
        }
    }

    public static String generateIdentifier(String url, String pubTopicName, String subTopicName) {
        return "[url=" + url + ", pub=" + pubTopicName + ", sub=" + subTopicName + "]";
    }
}
