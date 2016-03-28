/**
 * Kaazing Inc., Copyright (C) 2016. All rights reserved.
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
