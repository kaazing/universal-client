# Kaazing Java AMQP Client Libraries Facade
Kaazing Java AMQP Client Libraries Facade simplifies the interaction with Kaazing Java AMQP Client libraries that enable the developers to interact with [AMQP 0-9-1](https://www.rabbitmq.com/tutorials/amqp-concepts.html) brokers via WebSockets.

Kaazing Java AMQP Client Libraries Facade:
* Implements basic publish-subscribe functionality for AMQP to help developers in getting started with their AMQP WebSocket projects 
* Provide developers with the reference implementations for using Kaazing AMQP Java client libraries

For more information see:
- [Build Java AMQP Clients Using Kaazing WebSocket Gateway](http://kaazing.com/doc/amqp/4.0/dev-java/o_dev_java.html)
- [Use the Kaazing WebSocket Gateway Java AMQP Client API](http://kaazing.com/doc/amqp/4.0/dev-java/p_dev_java_client.html)

## Organization of the library
UniversalClientFactory's create 
Library contains AmqpUniversalClient class (implementing UniversalClient interface) that is created by UniversalClientFactory's _createUniversalClient_ method. AmqpUniversalClient object provides the following functionality:
- **constructor** of AmqpUniversalClient - connects client to Kaazing WebSocket AMQP gateway 
- **subscribe** method of AmqpUniversalClient - subscribes to publishing and subscription endpoints and returns an instance of ClientSubscription object. 
- **sendMessage** method of ClientSubscription - sends the instances of Serialized Objects to the publishing endpoint
- **disconnect** method of ClientSubscription - closes subscription to publishing and subscription endpoints for this subscription 
- **close** method of AmqpUniversalClient - closes all subscriptions and connections


### **constructor** of AmqpUniversalClient
Connect function implements the following sequence:

- Create AMQP client factory and AMQP client

	```java
	amqpClientFactory = AmqpClientFactory.createAmqpClientFactory();
	amqpClient = amqpClientFactory.createAmqpClient();
	```
- Register connection listeners
	```java
	...
	amqpClient.addConnectionListener(new ConnectionListener() {

			@Override
			public void onConnectionOpen(ConnectionEvent e) {
				LOGGER.info("Connected to "+url+" message "+e.getMessage());
				fConnected = true;
				latch.countDown();
			}

			@Override
			public void onConnectionError(ConnectionEvent e) {
				LOGGER.error("Connection error to url "+url+"... "+e.getMessage());
				errorsListener.onException(new ClientException("Error connecting to " + url + ": " + e.getMessage()));
				latch.countDown();
			}

			@Override
			public void onConnectionClose(ConnectionEvent e) {
				for (AmqpClientSubscription conn : connections) {
					try {
						conn.disconnect();
					} catch (ClientException e1) {
						errorsListener.onException(new ClientException("Error closing client connection: "+conn.getSubscriptionIdentifier(), e1));
						LOGGER.error("Error closing client connection: "+conn.getSubscriptionIdentifier(), e1);
					}
				}
				LOGGER.info("Closed connection to "+url+".");
			}

			@Override
			public void onConnecting(ConnectionEvent e) {


			}
		});
	```
	We use onConnectionOpen and onConnectionError listeners to wait until connection is either established or failed; we use the countdown latch to wait for either of these events. 
- Establish connection using provided login and password
	```java
	amqpClient.connect(this.url, "/", login, password);
		try {
			latch.await(10, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			throw new ClientException("Exception wating for connection latch connecting to " + this.url, e);
		}
		if (!fConnected) {
			throw new ClientException("Connection to " + this.url + " was not established in 10 sec.");
		}
	```
	
### **subscribe** method of of AmqpUniversalClient object
Method executed the following actions:
- Opens publishing channel
```java
	AmqpChannel pubChannel = this.amqpClient.openChannel();
```
- Adds publishing channel listeners
```java
	...
	pubChannel.addChannelListener(new ChannelAdapter() {
			@Override
			public void onClose(ChannelEvent e) {

			}

			@Override
			public void onError(final ChannelEvent e) {
				errorsListener.onException(new ClientException("Error creating publishing channel for " + pubTopicName + ": " + e.getMessage()));
				LOGGER.error("Error creating publishing channel "+pubTopicName+" for connection to "+url+": "+e.getMessage());
				connectionsLatch.countDown();
			}

			@Override
			public void onDeclareExchange(ChannelEvent e) {
				LOGGER.info("Created channel "+pubTopicName+" for url "+url);
				fPubOpened = true;
				connectionsLatch.countDown();
			}

			@Override
			public void onOpen(ChannelEvent e) {
				pubChannel.declareExchange(pubTopicName, "fanout", false, false, false, null);				
			}
		});
	...
```
	Opening the channel will trigger _onOpen_ event listener where we declare an exchange. Successful declaring of an exchange will result in triggering of onDeclareExchange event listener and is an indication of a success; onError listener is triggered in an event of any error. We use the countdown latch to wait for either of these events. 
- Opens subscription channel
```java
	...
	AmqpChannel subChannel = this.amqpClient.openChannel();
```
- Registers subscription channel events listeners:
```java
	subChannel.addChannelListener(new ChannelAdapter() {			
			@Override
			public void onError(final ChannelEvent e) {
				errorsListener.onException(new ClientException("Error creating subscription channel " + subTopicName + ": " + e.getMessage()));
				LOGGER.error("Error creating subscription channel "+subTopicName+" for url "+url+": "+e.getMessage());
				connectionsLatch.countDown();
			}

			@Override
			public void onConsumeBasic(ChannelEvent e) {
				LOGGER.info("Created subscription channel "+subTopicName+" for url "+url);
				fSubOpened = true;
				connectionsLatch.countDown();
			}

			@Override
			public void onMessage(final ChannelEvent e) {
				...
				
			}

			@Override
			public void onOpen(ChannelEvent e) {
				subChannel.declareQueue(queueName, false, false, false, false, false, null)
					.bindQueue(queueName, subTopicName, ROUTING_KEY, false, null)
					.consumeBasic(queueName, clientId, noLocal, false, false, false, null);
			}
		});
```
	Once the channel is successfully opened, onOpen event listener will be call where we:
		- declare a new queue 
		- bind the queue to an exchange with 'broadcast' routing key
		- start basic consumer for the queue.
		**Note** For fanout exchanges routing key is not used. For more information about exchanges and routing keys see: [https://www.rabbitmq.com/tutorials/amqp-concepts.html](https://www.rabbitmq.com/tutorials/amqp-concepts.html). 
		
	Once consumer is started, onConsumeBasic event listener is called which is an indication that channel is successfully opened. onError listener is triggered in an event of any error. We use the countdown latch to wait for either of these events.  
	onMessage event listener is called every time we will receive a message from an exchange. In this method we:
		- Allocates the buffer and read the data from the event body that contains serialized object.
		- Deserialize the object and make sure it an instance of our AmqpMessageEnvelope. The reason to use an envelope it contains application identifier that is used for messages filtering for the AMQP brokers that do not support noLocal functionality (functionality that prevents the client to received its own messages when publishing and subscription endpoints are the same).
		- calls onMessage method of MessagesListener object passing it received object. 
```java
	byte[] bytes = new byte[e.getBody().remaining()];
	e.getBody().get(bytes);
	try {
			Serializable object=Utils.deserialize(bytes);
			if (!(object instanceof AmqpMessageEnvelope)){
				errorsListener.onException(new ClientException("Received object is not an instance of AmqpMessageEnvelope;  received from " + subTopicName));
				LOGGER.error("Received object is not an instance of AmqpMessageEnvelope;  received from " + subTopicName +" for url"+url);
				return;
			}
			AmqpMessageEnvelope messageEnvelope=(AmqpMessageEnvelope)object;
			if (noLocal && messageEnvelope.getClientId().equals(appId)){
				LOGGER.debug("Received message ["+messageEnvelope.toString()+"] on topic "+subTopicName+", connection to "+url+" is ignored as it came from the same client and noLocal is set!");
				return;
			}
			LOGGER.debug("Received message ["+messageEnvelope.getData().toString()+"] on topic "+subTopicName+", connection to "+url);
			messageListener.onMessage(messageEnvelope.getData());
	} catch (ClassNotFoundException | IOException e1) {
			errorsListener.onException(new ClientException("Cannot deserialize an object from the message received from " + subTopicName, e1));
			LOGGER.error("Cannot deserialize an object from the message received from " + subTopicName +" for url"+url);
			return;
	}
```				

Once the channels are opened, they are stored in an AmqpClientSubscription object (subclass of ClientSubscription object) for future use. Created instance of ClientSubscription object is registered with AmqpUniversalClient.
			
### **sendMessage** method of AmqpClientSubscription object
Method sets AMQP properties and sends the message to a publishing exchange using specified routing key.   
**Note:** As mentioned earlier, library creates a fanout type of exchange that does not use routing keys; thus library sets the value of the routing key to 'broadcast'.
We use AmqpMessageEnvelope to store application identifier that may be needed for filtering of the message. 
Serialized object is stored in the ByteBuffer that is sent to the channel along with AMQP properties.

```java
	...
	AmqpMessageEnvelope messageEnvelope=new AmqpMessageEnvelope(this.appId, message);
	byte[] serializedObject;
	try {
		serializedObject = Utils.serialize(messageEnvelope);
	} catch (IOException e) {
		throw new ClientException("Cannot serialize message " + message + " to send over subscription " + this.getSubscriptionIdentifier(), e);
	}
	ByteBuffer buffer = ByteBuffer.allocate(serializedObject.length);
	buffer.put(serializedObject);
	buffer.flip();

	Timestamp ts = new Timestamp(System.currentTimeMillis());
	AmqpProperties props = new AmqpProperties();
	props.setMessageId("1");
	props.setCorrelationId("4");
	props.setAppId(appId);
	props.setUserId(userId);
	props.setPriority(6);
	props.setDeliveryMode(1);
	props.setTimestamp(ts);

	this.pubChannel.publishBasic(buffer, props, this.pubChannelName, AmqpUniversalClient.ROUTING_KEY, false, false);
```
		
### **disconnect** method of AmqpClientSubscription object
Deletes the declared subscription queue and closes the channels
```java
	this.subChannel.deleteQueue(this.queueName, false, false, false);

	this.pubChannel.closeChannel(0, "", 0, 0);
	this.subChannel.closeChannel(0, "", 0, 0);
```

### **close** method of AmqpUniversalClient object
Disconnects all opened subscriptions, disconnects Amqp client.
```java
	...
	for (AmqpClientSubscription conn : this.connections) {
		conn.disconnect();
	}
	this.amqpClient.disconnect();
```


