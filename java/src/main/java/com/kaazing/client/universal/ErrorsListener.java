/**
 * Kaazing Inc., Copyright (C) 2016. All rights reserved.
 */
package com.kaazing.client.universal;

/**
 * Provides implementation for callback to handle the errors
 */
public interface ErrorsListener {
    /**
     * Called by the Universal Client when exception occurs
     * 
     * @param exception
     *            exception reported by the client
     */
    void onException(ClientException exception);
}
