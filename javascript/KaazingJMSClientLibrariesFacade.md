# Kaazing Javascript JMS Client Libraries Facade
The Kaazing JavaScript JMS Client Libraries Facade simplifies the interaction with the Kaazing JavaScript JMS Client libraries. These enable the developers to interact with [JMS](http://docs.oracle.com/javaee/6/tutorial/doc/bncdx.html) brokers via WebSockets.

The Kaazing JavaScript JMS Client Libraries Facade:
* Implements basic publish-subscribe functionality for JMS to help developers in getting started with their JMS WebSocket projects 
* Provides developers with the reference implementations for using Kaazing JMS JavaScript client libraries

For more information see:
- [Build JavaScript JMS Clients Using Kaazing WebSocket Gateway - JMS Edition](http://kaazing.com/doc/jms/4.0/dev-js/o_dev_js.html)
- [Use the Kaazing WebSocket Gateway JavaScript JMS Client API](http://kaazing.com/doc/jms/4.0/dev-js/p_dev_js_client.html)

## Organization of the library
- **connect** function - connects client to Kaazing WebSocket JMS gateway and on success returns via callback a _connection_ object that will be used to create subscriptions.
- **subscribe** method of a _connection_ object that creates publishing endpoint and subscribes to a subscription endpoint. Method returns via callback a _subscription_ object that is used for sending messages.
- **sendMessage** method of a _subscription_ object - sends the message to a publishing endpoint
- **disconnect** function of a _subscription_ object - closes both publishing and subscription.
- **close** method - closes all subscriptions and disconnects client from Kaazing WebSocket AMQP gateway

### **connect** function
The _Connect_ function implements the following sequence:

- Create JMS connection factory

```javascript
	var jmsConnectionFactory = new JmsConnectionFactory(url);
```

- Create connection. The _createConnection_ function of _JmsConnectionFactory_ takes three parameters: _login_, _password_ and a callback function that will be called upon completion. The function returns the future that is checked in a callback function for exceptions.

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
	
Once a connection is created, the callback function does the following:

1. Obtains the connection from the _connectionFuture_ that was returned by _createConection_.
1. Sets an exception listener to handle exceptions.
1. Creates a session using the _createSession_ method. The session is created with auto-acknowledgement. 
1. Starts the connection using the _start_ function passing to it a callback function.

Once a connection is started, a _connection_ object is returned for the subscription to be created using the __subscribe__ method.

### **subscribe** method of connection object
Method executed the following actions:

- Creates publishing topic and producer to send messages

	```javascript
	var pubDest = session.createTopic(topicPub);
	var producer = session.createProducer(dest);
	```
- Creates subscription topic and consumer.
	_In order to prevent client from receiving its own messages consumer may be created with a query that will filter out the messages with the `appId` string property set to this client application ID - a randomly generated GUID._
	Once a consumer is created, the _setMessageListener_ function is used to specify the function to be called when a new message is received.

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
This function creates a text message and sends it. In order to prevent the client from receiving its own messages the `appId` string property may be set to this client application ID - a randomly generated GUID.

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

### **disconnect** function of a subscription object
This function closes the producer and consumer that were created during the subscription call.

```javascript
	this.producer.close(function(){
		this.consumer.close(function(){
		});
	})
```
	    	
### **close** function
Closes all subscriptions (causing closing of their producer and consumer), stops the connection and then closes session and connection in a chain of callbacks.
	
```javascript
	JMSClient.disconnect=function(){
		for(var i=0;i<this.subscriptions.length;i++){
			this.subscriptions[i].close();
		}
	
		... Wait while all the subscriptions are closed...
		
		connection.stop(function(){
				session.close(function () {
					connection.close(function () {

					});
				});
			});
    }

```

