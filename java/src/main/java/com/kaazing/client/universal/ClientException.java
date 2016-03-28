/**
 * Kaazing Inc., Copyright (C) 2016. All rights reserved.
 */
package com.kaazing.client.universal;

/**
 * Contains the exception reported by Universal Client
 */
public class ClientException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 3071666188376628792L;

    public ClientException(String text, Throwable t) {
        super(text, t);
    }

    public ClientException(String text) {
        super(text);
    }

    public ClientException(Throwable t) {
        super(t);
    }

}
