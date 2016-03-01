/**
 * Kaazing Inc., Copyright (C) 2016. All rights reserved.
 */
package com.kaazing.client.starter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URI;

import com.kaazing.client.universal.ClientConnection;
import com.kaazing.client.universal.ClientException;
import com.kaazing.client.universal.ErrorsListener;
import com.kaazing.client.universal.MessagesListener;
import com.kaazing.client.universal.UniversalClient;
import com.kaazing.client.universal.UniversalClientFactory;
import com.kaazing.client.universal.UniversalClientProtocol;

/**
 * Simple starter application to illustrate how to communicate with Kaazing Web
 * Gateway
 * 
 * @author romans
 *
 */
public class KaazingJavaStarterApp {
	public static void main(String[] args) throws Exception {
		try (UniversalClient universalClient = UniversalClientFactory.createUniversalClient(UniversalClientProtocol.amqp, // Use
																															// AMQP
				new URI("ws://localhost:8001/amqp"), // Kaazing Gateway URL
				"guest", // Login to use to connect to Kaazing Gateway
				"guest", // Password to use to connect to Kaazing Gateway
				new ErrorsListener() { // Error listener callback - simply print
										// errors

					@Override
					public void onException(ClientException exception) {
						System.err.println("Exception occurred! " + exception.getMessage());
					}
				});) {

			ClientConnection connection = universalClient.connect("test", // publishing
																			// point
					"test", // subscription point
					new MessagesListener() { // Message listener - simply print
												// messages
						@Override
						public void onMessage(Serializable message) {
							System.out.println("Received message: " + message.toString());
						}
					}, false); // We want to receive our own messages - noLocal
								// is false

			System.out.println("Type the message to send or <exit> to stop.");
			BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
			while (true) {
				String text = console.readLine();
				if (text.toLowerCase().equals("<exit>"))
					break;
				// Send as a text
				connection.sendMessage(text);
				// Send as an object
				connection.sendMessage(new TestObjectMessage(text));
			}
		}
	}

	public static class TestObjectMessage implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1721747391179889337L;
		private String message;

		public TestObjectMessage(String message) {
			this.message = message;
		}

		@Override
		public String toString() {
			return "TestObjectMessage[" + this.message + "]";
		}

	}

}
