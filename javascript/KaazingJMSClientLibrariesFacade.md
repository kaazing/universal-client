# Kaazing Javascript JMS Client Libraries Facade
Kaazing JavaScript JMS Client Libraries Facade simplifies the interaction with Kaazing JavaScript JMS Client libraries that enable the developers to interact with [JMS](http://docs.oracle.com/javaee/6/tutorial/doc/bncdx.html) brokers via WebSockets.

Kaazing JavaScript JMS Client Libraries Facade:
* Implements basic publish-subscribe functionality for JMS to help developers in getting started with their JMS WebSocket projects 
* Provide developers with the reference implementations for using Kaazing JMS JavaScript client libraries

For more information see:
- [Build JavaScript JMS Clients Using Kaazing WebSocket Gateway - JMS Edition](http://kaazing.com/doc/jms/4.0/dev-js/o_dev_js.html)
- [Use the Kaazing WebSocket Gateway JavaScript JMS Client API](http://kaazing.com/doc/jms/4.0/dev-js/p_dev_js_client.html)

## Organization of the library
- **connect** function - connects client to Kaazing WebSocket JMS gateway and on success returns via callback a _connection_ object that will be used to create subscriptions.
- ** subscribe ** method of a _connection_ object that creates publishing endpoint and subscribes to a subscription endpoint. Method returns via callback a _subscription_ object that is used for sending messages.
- **sendMessage** method of a _subscription_ object - sends the message to a publishing endpoint
- **disconnect** function of a _subscription_ object - closes both publishing and subscription.
- **close** method - closes all subscriptions and disconnects client from Kaazing WebSocket AMQP gateway

### **connect** function
Connect function implements the following sequence:

- Create JMS connection factory

```javascript
	var jmsConnectionFactory = new JmsConnectionFactory(url);
```

- Create connection. createConnection function of JmsConnectionFactory takes three parameters: login, password and a callback function that will be called upon completion. Function returns the future that is checked in a callback function for exceptions.

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
	
 Once connection is created, callback function does the following:
	1. Obtains the connection from the connectionFuture that was returned by createConection.
	2. Sets exception listener to handle exceptions.
	3. Creates session using createSession method. Session is created with auto-acknowledgement. 
	4. Starts the connection using start function passing to it a callback function.

- Once connection is started, connection object is returned for the subscription to be created using __subscribe__ method.

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

### **disconnect** function of a subscription object
Function closes producer and consumer that were created during the subscription call.

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

