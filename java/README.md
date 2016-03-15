# Universal Clients for Java Applications
The [Kaazing Java WebSocket Universal Clients][2] library uses the same  [Java AMQP Client Libraries Facade][3] and [Java JMS Client Libraries Facade][4] for interaction with Kaazing AMQP and JMS client libraries.
Please, refer to the links above for the details about the details of the usage and implementations.

## Using the Client Libraries
- Using Gradle
```
'com.kaazing.client:universal-client:1.0+'
```
- Using Maven
```
<dependency>
	<groupId>com.kaazing.client</groupId>
	<artifactId>universal-client</artifactId>
	<version>1.0+</version>
</dependency>
```

### Obtaining and Configuring Kaazing Gateway
The Kaazing Universal WebSocket clients depend on the Kaazing WebSocket Gateway (KWG) being installed on one or more servers. KWG supports two protocols, AMQP and JMS. Read [Obtaining and configuring Kaazing Gateways and related Servers](https://github.com/kaazing/universal-client/blob/develop/ObtainingGateways.md) for more information.

### Using the Library
- Create an instance of the Universal Client Library.
	```java
	try (UniversalClient universalClient = UniversalClientFactory.createUniversalClient(UniversalClientProtocol.amqp, // Protocol	
				new URI("ws://localhost:8001/amqp"), // Kaazing Gateway URL
				"guest", // Login to use to connect to Kaazing Gateway
				"guest", // Password to use to connect to Kaazing Gateway
				new ErrorsListener() { // Error listener callback - simply printerrors
					@Override
					public void onException(ClientException exception) {
						System.err.println("Exception occurred! " + exception.getMessage());
					}
				});) {
				
				...
				
	};
	```

- Establish a connection
	```java
	try (UniversalClient universalClient = UniversalClientFactory.createUniversalClient(UniversalClientProtocol.amqp, // Protocol	
				new URI("ws://localhost:8001/amqp"), // Kaazing Gateway URL
				"guest", // Login to use to connect to Kaazing Gateway
				"guest", // Password to use to connect to Kaazing Gateway
				new ErrorsListener() { // Error listener callback - simply printerrors
					@Override
					public void onException(ClientException exception) {
						System.err.println("Exception occurred! " + exception.getMessage());
					}
				});) {
				
			ClientSubscription subscription = universalClient.subscribe("test", // publishing point
					"test", // subscription point
					new MessagesListener() { // Message listener - simply print messages
						@Override
						public void onMessage(Serializable message) {
							System.out.println("Received message: " + message.toString());
						}
				
	};
	```
- Send message
	```java
	try (UniversalClient universalClient = UniversalClientFactory.createUniversalClient(UniversalClientProtocol.amqp, // Protocol	
				new URI("ws://localhost:8001/amqp"), // Kaazing Gateway URL
				"guest", // Login to use to connect to Kaazing Gateway
				"guest", // Password to use to connect to Kaazing Gateway
				new ErrorsListener() { // Error listener callback - simply printerrors
					@Override
					public void onException(ClientException exception) {
						System.err.println("Exception occurred! " + exception.getMessage());
					}
				});) {
				
			ClientSubscription subscription = universalClient.subscribe("test", // publishing point
					"test", // subscription point
					new MessagesListener() { // Message listener - simply print messages
						@Override
						public void onMessage(Serializable message) {
							System.out.println("Received message: " + message.toString());
						}
						
			// Send as a text
			subscription.sendMessage(text);
			// Send as an object
			subscription.sendMessage(new SomeObjectMessage());
									
				
	};
	```
	
## Organization of Kaazing JavaScript Universal Client   

![][image-1]

As shown on the diagram above, Kaazing Universal Client works as following:
- Instantiates required Client Facade Library based on specified protocol that will interact with necessary Kaazing Java Client Libraries
- Pass the data to and from the Kaazing Java Client libraries via instantiated Client Facade Library

For more information about Client Facade libraries see
[AMQP Client Libraries Facade][2] and [JMS Client Libraries Facade][3].   


## Documentation

### AMQP
- [How to Build Java Clients Using Kaazing  WebSocket Gateway](http://developer.kaazing.com/documentation/amqp/4.0/dev-java/o_dev_java.html)
- [Use the Kaazing WebSocket Gateway Java AMQP Client Library](http://developer.kaazing.com/documentation/amqp/4.0/dev-java/p_dev_java_client.html)

### JMS
- [Build Java JMS Clients Using Kaazing WebSocket Gateway - JMS Edition](http://developer.kaazing.com/documentation/jms/4.0/dev-java/o_dev_java.html)
- [Use the Kaazing WebSocket Gateway Java JMS Client API](http://developer.kaazing.com/documentation/jms/4.0/dev-java/p_dev_java_client.html)


[2]:	JavaClient.md "Java library"
[3]:	KaazingAMQPClientLibrariesFacade.md
[4]:	KaazingJMSClientLibrariesFacade.md
[image-1]:	images/JavaUniversalClient.png "Java Universal Client"


