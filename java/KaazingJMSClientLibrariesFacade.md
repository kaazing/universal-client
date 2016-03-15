# Kaazing Java JMS Client Libraries Facade
Kaazing Java JMS Client Libraries Facade simplifies the interaction with Kaazing Java JMS Client libraries that enable the developers to interact with [JMS](http://docs.oracle.com/javaee/6/tutorial/doc/bncdx.html) brokers via WebSockets.

Kaazing Java JMS Client Libraries Facade:
* Implements basic publish-subscribe functionality for JMS to help developers in getting started with their JMS WebSocket projects 
* Provide developers with the reference implementations for using Kaazing JMS Java client libraries

For more information see:
- [Build Java JMS Clients Using Kaazing WebSocket Gateway - JMS Edition](http://developer.kaazing.com/documentation/jms/4.0/dev-java/o_dev_java.html)
- [Use the Kaazing WebSocket Gateway Java JMS Client API](http://developer.kaazing.com/documentation/jms/4.0/dev-java/p_dev_java_client.html)

## Organization of the library
UniversalClientFactory's create 
<<<<<<< HEAD
Library contains JMSUniversalClient class (implementing UniversalClient interface) that is created by UniversalClientFactory's _createUniversalClient_ method. JMSUniversalClient object provides the following functionality:
- **constructor** of JMSUniversalClient - connects client to Kaazing WebSocket JMS gateway 
- **subscribe** method of JMSUniversalClient - subscribes to publishing and subscription endpoints and returns an instance of ClientSubscription object. 
- **sendMessage** method of ClientSubscription - sends the instances of Serialized Objects to the publishing endpoint
- **disconnect** method of ClientSubscription - closes subscription to publishing and subscription endpoints for this subscription 
- **close** method of JMSUniversalClient - closes all subscriptions and connections

### **constructor** of JMSUniversalClient
Constructor implements the following sequence:

- Locate JMS connection factory using JNDI:

```java
	Properties env = new Properties();
	env.setProperty("java.naming.factory.initial", "com.kaazing.gateway.jms.client.JmsInitialContextFactory");

	try {
		jndiInitialContext = new InitialContext(env);
	} catch (NamingException e1) {
		throw new ClientException("Error creating initial context factory for JMS!", e1);
	}
	env.put(JmsInitialContext.CONNECTION_TIMEOUT, "15000");
	try {
		connectionFactory = (ConnectionFactory) jndiInitialContext.lookup("ConnectionFactory");
	} catch (NamingException e) {
		throw new ClientException("Error locating connection factory for JMS!", e);
	}
	JmsConnectionFactory jmsConnectionFactory = (JmsConnectionFactory) connectionFactory;
```

- Create connection. To create connection: 
	- Set the gateway URL connection for JMS connection factory.
	- Create WebSocket factory
	- Create connection passing login and password:
```java
	...
	jmsConnectionFactory.setGatewayLocation(url);
	WebSocketFactory webSocketFactory = jmsConnectionFactory.getWebSocketFactory();
	webSocketFactory.setDefaultRedirectPolicy(HttpRedirectPolicy.ALWAYS);
	try {
		connection = connectionFactory.createConnection(login, password);
	} catch (JMSException e) {
		throw new ClientException("Error connecting to gateway with " + url.toString() + ", credentials " + login + "/" + password, e);
	}
	...
``` 	
- Register the ErrorsListener object that is passed to the constructor with the connection to be a listener for any errors and exceptions:
```java
	...
	try {
		connection.setExceptionListener(this);
	} catch (JMSException e) {
		throw new ClientException("Error setting exceptions listener. Connection: " + url.toString() + ", credentials " + login + "/" + password, e);
	} 
	...  
```
	
- Create __session__ in auto-acknowledgement mode. In this mode session automatically acknowledges a client's receipt of a message either when the session has successfully returned from a call to receive or when the message listener the session has called to process the message successfully returns. 
```java
	...
	try {
		session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
	} catch (JMSException e) {
		throw new ClientException("Error creating session. Connection: " + url.toString() + ", credentials " + login + "/" + password, e);
	}
	...	
```
- Start the connection
```java
	...
	try {
		connection.start();
	} 
	catch (JMSException e) {
		throw new ClientException("Error starting connection: " + url.toString() + ", credentials " + login + "/" + password, e);
	}
	...
```

Once object is successfully constructed it is ready to create subscriptions. 


### **subscribe** method of of JMSUniversalClient object
Method executed the following actions:

- Creates subscription destination

```java
	...
	Destination subDestination;
	try {
		subDestination = (Destination) jndiInitialContext.lookup("/topic/" + subTopicName);
	} catch (NamingException e) {
		throw new ClientException("Cannot locate subscription topic " + subTopicName, e);
	}
	...
```
- Creates message Consumer.
	_In order to prevent client from receiving its own messages consumer may be created with the query that will filter out the messages with the 'appId' string property set to this client application ID - a randomly generated GUID._
```java
	...
	MessageConsumer consumer;
	try {
		if (noLocal){
			clientId=UUID.randomUUID().toString();
			consumer = session.createConsumer(subDestination, "clientId<>'"+clientId+"'");
		}
		else
			consumer = session.createConsumer(subDestination);
	} catch (JMSException e) {
		throw new ClientException("Cannot create consumer for subscription topic " + subTopicName, e);
	}
	...
```
- Registers an instance of ... passed to the method to be a message listener.
```java
	...
	try 
	{
		consumer.setMessageListener(new MessageListenerImpl(messageListener, this.errorsListener, subTopicName));
	} catch (JMSException e) {
		throw new ClientException("Cannot create messages listener for subscription topic " + subTopicName, e);
	}
	...
``` 
	
	We use MessageLinstenerImpl wrapper class that implements MessageListener to convert the ByteMessage object received from the wire to an instance of Serializable object that was sent.
```java
	...
	if (message instanceof BytesMessage) {
		try {					
			BytesMessage bytesMessage = ((BytesMessage) message);
			long len = bytesMessage.getBodyLength();
			byte b[] = new byte[(int) len];
			bytesMessage.readBytes(b);
			Serializable object;
			try {
				object = Utils.deserialize(b);
				LOGGER.debug("Received message ["+object.toString()+"] on topic "+destination+", connection to "+url);
				this.listener.onMessage(object);
			} catch (ClassNotFoundException | IOException e) {
				this.errorsListener.onException(new ClientException("Cannot deserialize an object from the message received from " + destination, e));
				LOGGER.error("Cannot deserialize an object from the message received from " + destination+" connection to "+url, e);
				return;
			}
		} catch (JMSException e) {
			this.errorsListener.onException(new ClientException("Error receiving message from destination " + destination, e));
			LOGGER.error("Error receiving message from destination " + destination+" connection to "+url, e);
		}
	} else {
		this.errorsListener.onException(new ClientException("Received a message of unexpected type " + message.getClass().getName() + " for a destination " + destination));
		LOGGER.error("Received a message of unexpected type " + message.getClass().getName()  + destination+" connection to "+url);
	}
	...
```
		
- Create publishing destination and producer

```java
	...
	Destination pubDestination;
	try {
		pubDestination = (Destination) jndiInitialContext.lookup("/topic/" + pubTopicName);
	} catch (NamingException e) {
		throw new ClientException("Cannot locate publishing topic " + pubTopicName, e);
	}			
	MessageProducer producer;
	try {
		producer = session.createProducer(pubDestination);
	} catch (JMSException e) {
		throw new ClientException("Cannot create producer for publishing topic " + pubTopicName, e);
	}
	...
```
	
	Session, producer and consumer are stored in an JMSClientSubscription object (subclass of ClientSubscription object) for future use. Created instance of ClientSubscription object is registered with JMSUniversalClient.
	   
### **sendMessage** method of JMSClientSubscription object
Function creates a binary message and sends it using the following steps.
	- Creates bytes message and serializes Java object to it.
	```java
		BytesMessage bytesMessage;
		try {
			bytesMessage = session.createBytesMessage();
		} catch (JMSException e) {
			throw new ClientException("Subscription: "+this.getSubscriptionIdentifier()+" - Cannot create a BytesMessage for an object " + message.toString(), e);
		}
		try {
			bytesMessage.writeBytes(Utils.serialize(message));
		} catch (JMSException e) {
			throw new ClientException("Subscription: "+this.getSubscriptionIdentifier()+" - Cannot write bytes to set the payload of a BytesMessage for an object " + message.toString(), e);
		} catch (IOException e) {
			throw new ClientException("Subscription: "+this.getSubscriptionIdentifier()+" - Cannot serialize an object " + message.toString(), e);
		}
	```
	- _If needed_, sets string property clientId. This property will be used to ignore receiving your own messages which may be applicable if publishing and subscription endpoints are the same.  
	```java
		...
		if (clientId!=null){
			try {
				bytesMessage.setStringProperty("clientId", clientId);
			} catch (JMSException e) {
				throw new ClientException("Subscription: "+this.getSubscriptionIdentifier()+" - Cannot set a string property client id to "+clientId+" for an object " + message.toString(), e);
			}
		}
		...
	```
	- Send message to the publishing endpoing
	```java
		...
		try {
			producer.send(bytesMessage);
		} catch (JMSException e) {
			throw new ClientException("Subscription: "+this.getSubscriptionIdentifier()+" - Cannot sent and object message for an object " + message.toString(), e);
		}
		...
	```
	
### **disconnect** method of JMSClientSubscription object
Closes producer and consumer.		
	
```java
	try {
		this.producer.close();
	} catch (JMSException e) {
		throw new ClientException("Subscription: "+this.getSubscriptionIdentifier()+" - Error closing producer.",e);
	}
	try {
		this.consumer.close();
	} catch (JMSException e) {
		throw new ClientException("Subscription: "+this.getSubscriptionIdentifier()+" - Error closing consumer.",e);
	}
``` 	

**close** method of JMSUniversalClient
Disconnects all opened subscriptions, closes session and connection.

```java
	for(JMSClientSubscription connection: this.connections){
			connection.disconnect();
		}
	this.connection.stop();
	this.session.close();
	this.connection.close();
```
=======
Library contains JMSUniversalClient class (implementing UniversalClient interface) that is created by UniversalClientFactory's _createUniversalClient_ method. JMSClient objects provides the following functionality:
- **constructor** connects client to Kaazing WebSocket JMS gateway 
- **subscribe** function - subscribes to publishing and subscription endpoints and returns an instance of ClientSubscription object.
- **close** function - close all subscriptions and connections

### **constructor**
Constructor implements the following sequence:

1. Create JMS connection factory

```javascript
	var jmsConnectionFactory = new JmsConnectionFactory(url);
```

2. Create connection. createConnection function of JmsConnectionFactory takes three parameters: login, password and a callback function that will be called upon completion. Function returns the future that is checked in a callback function for exceptions.

```javascript
	var connectionFuture = jmsConnectionFactory.createConnection(username, password, function () {
		if (!connectionFuture.exception) {
			try {
				connection = connectionFuture.getValue();
				connection.setExceptionListener(handleException);
		
				session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		
				connection.start(function () {
					 var connectionObject=createConnectionObject(session, JMSClient);
                     connectedFunctionHandle(connectionObject);
				});
			}
			catch (e) {
				handleException(e);
			}
		}
		else {
			handleException(connectionFuture.exception);
		}
	})
```
	
3. Once connection is created, callback function does the following:
	1. Obtains the connection from the connectionFuture that was returned by createConection.
	2. Sets exception listener to handle exceptions.
	3. Creates session using createSession method. Session is created with auto-acknowledgement. 
	4. Starts the connection using start function passing to it a callback function.

4. Once connection is started, connection object is returned for the subscription to be created using __subscribe__ method.

### **subscribe** method of connection object
Method executed the following actions:

- Creates publishing topic and producer to send messages

	```javascript
	var pubDest = session.createTopic(topicPub);
	var producer = session.createProducer(dest);
	```
- Creates subscription topic and consumer.
	_In order to prevent client from receiving its own messages consumer may be created with the query that will filter out the messages with the 'appId' string property set to this client application ID - a randomly generated GUID._
	Once consumer is created, setMessageListener function is used to specify the function to be called when new message is received.

	```javascript
	var subDest = session.createTopic(topicSub);			
	if (noLocalFlag)
		consumer = session.createConsumer(dest, "appId<>'" + appId + "'");
	else
		consumer = session.createConsumer(dest);
		consumer.setMessageListener(function (message) {
		... obtain the message body ...			

		rcvFunction(body);
	});
	```
	
- Creates subscription object, adds it to the array of opened subscriptions and returns it via callback.
	   
### **sendMessage** function of a subscription object	
Function creates text message and sends it. In order to prevent client from receiving its own messages 'appId' string property may be set to this client application ID - a randomly generated GUID.

```javascript
	sendMessage:function(msg){
		var textMsg = session.createTextMessage(msg);
		if (noLocalFlag)
			textMsg.setStringProperty("appId", appId);
		try {
			var future = producer.send(textMsg, function () {
			if (future.exception) {
				handleException(future.exception);
			};	
		});
		} catch (e) {
			handleException(e);
		}
	}
``` 	

### **close** function of a subscription object
Function closes producer and consumer that were created during the subscription call.

```javascript
	this.producer.close(function(){
		this.consumer.close(function(){
		});
	})
```
	    	
### **disconnect** function
Closes all subscriptions (causing closing of their producer and consumer), session and connection in a chain of callbacks.
	
```javascript
	JMSClient.disconnect=function(){
		for(var i=0;i<this.subscriptions.length;i++){
			this.subscriptions[i].close();
		}
	
		... Wait while all the subscriptions are closed...
		
		session.close(function () {
			connection.close(function () {

			});
		});
    }

```

>>>>>>> kaazing/develop
