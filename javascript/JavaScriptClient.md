# Kaazing JavaScript Universal Client for Javascript
This library is intended to be used with 'plain JavaScript', AngularJS, ReactJS applications (see [Kaazing JavaScript Starter Applications](https://github.com/kaazing/javascript.getting.started) and [Kaazing Examples and Tutorials](https://github.com/kaazing/tutorials)); it provides JavaScript function that returns an object that can be used in the client application to interact with Kaazing Gateway.

## Using the Library
### Install the library
#### Using Bower
- Install library with the Bower as specified in a [README document][1].
- Add the following to the **\<head\>** section of your page:  
	```html
	
	<head>  
	....    
	<script src="bower_components/kaazing-javascript-universal-client/javascript/src/AngularUniversalClient.js"></script>
	<script src="bower_components/kaazing-javascript-universal-client/javascript/src/JmsClient.js"></script>
	....  
	</head>

	```
<font color='red'> **Note:** </font> Addition of JmsClient.js is not needed when using AMQP protocol.

#### Using NPM
- Install library with the NPM as specified in a [README document][1].
- Add the following to the **\<head\>** section of your page:  
	```html
	
	<head>  
	....    
    <script src="node_modules/kaazing-javascript-universal-client/node_modules/kaazing-javascript-jms-client/JmsClient.js"></script>
    <script src="node_modules/kaazing-javascript-universal-client/AngularUniversalClientNPM.js"></script>
	....  
	</head>
	```
<font color='red'> **Note:** </font> Addition of JmsClient.js is not needed when using AMQP protocol.


### Add the library to your application
- Create an instance of the Universal Client Library.
	```javascript
	...

	var client=UniversalClientDef(protocol);
	...

	```
	Where:
	- **protocol**: Specifies protocol that should be used for communications: jms - for communication with Kaazing JMS Gateway, amqp - for communication with Kaazing AMQP Gateway.

- Establish a connection
	```javascript
	...

	var client=UniversalClientDef(protocol);
	...

	$(document).ready(function () {
		client.connect(connectionInfo, // Connection info
				onError, // callback function to process errors
				function(connection){
			...
		}

	}

	```
	Where:
	- **coonectionInfo**: Connection information that contains _URL_ (e.g. ws://localhost:8001/amqp or ws://localhost:8001/jms), _username_(user name to be used to establish connection) and _password_(user password to be used to establish connection)
	- **onError**: function that is used for error handling in a format of _function(error)_. 
	- _callback function_ to receive a connection object once connection is established.
	
	**Note:** If you want to add a logger to log library messages, add the following after creating the client:
	```javascript
	
	var logWebSocketMessage = function (cls, msg) {
 		...
	}
	var client=UniversalClientDef(protocol);
	// Set the logger function
	client.loggerFuncHandle=logWebSocketMessage;

	```
- Subscribe to the topics of interest
	```javascript
	...

	var client=UniversalClientDef(protocol);
	...

	$(document).ready(function () {
		var subscription;
		client.connect(connectionInfo, // Connection info
				onError, // callback function to process errors
				function(connection){
					connection.subscribe(topicP, // Topic to send message
										 topicS, // Topic to subscribe to receive messsages
										 onMessage, // callback function to process received messages
										 noLocal, // noLocal flag set to false - allow receiving your own messages
						function(subscription){
							subscription=subscr;
						});
					}		
				}

	}
	
	```	
	
	Where:
	- **topicP**: Name of the publishing endpoint - AMQP exchange used for publishing or JMS Topic
	- **topicS**: Name of the subscription endpoint - AMQP exchange used for subscription or JMS Topic
	- **noLocal**: Flag indicating whether the client wants to receive its own messages (true) or not (false). That flag should be used when publishing and subscription endpoints are the same.
	- **onMessage**: Function that will be used to process received messages from subscription endpoint in a format of _function(message)_
	- _callback function_ to receive subscription object
	**Note** Multiple subscriptions are allowed within single connection!
- Add disconnect on window close (shown method uses JQuery):
	```javascript
	...

	var client=UniversalClientDef(protocol);
	...

	$(document).ready(function () {
		var subscription;
		client.connect(connectionInfo, // Connection info
				onError, // callback function to process errors
				function(connection){
					connection.subscribe(topicP, // Topic to send message
										 topicS, // Topic to subscribe to receive messsages
										 onMessage, // callback function to process received message
										 noLocal, // noLocal flag set to false - allow receiving your own messages
						function(subscription){
							subscription=subscr;
						});
					}		
				}
		...
		$( window ).unload(function() {
            // Disconnect
            client.disconnect();
        });

	}

	```
- To send messages use sendMessage(msg) method of a subscription object
	where _**msg**_ JavaScript object to be sent (as a JSON string). 
	```javascript
	...

	var client=UniversalClientDef(protocol);
	...

	var sendMessage=function(msg){
		// Send message
    	subscrpiption.sendMessage(msg);
	}


	$(document).ready(function () {
		var subscription;
		client.connect(connectionInfo, // Connection info
				onError, // callback function to process errors
				function(connection){
					connection.subscribe(topicP, // Topic to send message
										 topicS, // Topic to subscribe to receive messsages
										 onMessage, // callback function to process received message
										 noLocal, // noLocal flag set to false - allow receiving your own messages
						function(subscription){
							subscription=subscr;
						});
					}		
				}
		...
		$( window ).unload(function() {
            // Disconnect
            client.disconnect();
        });

	}
	```

## Organization of Kaazing JavaScript Universal Client   

![][image-1]

As shown on the diagram above, Kaazing Universal Client works as following:
- Determine Client Library Facade based on the specified protocol
- Download all necessary JavaScript libraries including the needed Client Library Facade using RequireJS.
	- <font color='red'> **Notes:** </font>

		- Kaazing AMQP client libraries require Kaazing WebSocket library to be downloaded and instantiated first, to achieve it Universal Client uses the following code:

			For Bower

			```javascript
			...
			requirejs(['bower_components/kaazing-amqp-0-9-1-client-javascript/javascript/WebSocket.js'],function(){
				requirejs(['bower_components/jquery/dist/jquery.js','bower_components/kaazing-amqp-0-9-1-client-javascript/javascript/Amqp-0-9-1.js', 'bower_components/kaazing-javascript-universal-client/javascript/src/AmqpUniversalClient.js'], function () {
					...
					});              
				});
			...
			```
			
			For NPM

			```javascript
			...
		  requirejs(['node_modules/kaazing-javascript-universal-client/node_modules/kaazing-javascript-gateway-client/WebSocket.js'],function(){
                requirejs(['node_modules/jquery/dist/jquery.js','node_modules/kaazing-javascript-universal-client/node_modules/kaazing-javascript-amqp-client/AmqpClient.js', 'node_modules/kaazing-javascript-universal-client/AmqpUniversalClient.js'], function () {
                ...
                });
            });			...
			```
		_The reason for different Bower and NPM implementations is the difference in path of the dependent packages installed via one or another._


		- <font color='orange'> Due to certain limitations, RequireJS cannot download Kaazing JMSClient.js library - hence it has to be included in the \<head\> section </font>
- Instantiate required Client Facade Library that will interact with necessary Kaazing Javascript Client Libraries
- Pass the data to and from the Kaazing Javascript Client libraries via instantiated Client Facade Library

For more information about Client Facade libraries see
[AMQP Client Libraries Facade][2] and [JMS Client Libraries Facade][3].   

[1]:	README.md
[2]:	KaazingAMQPClientLibrariesFacade.md
[3]:	KaazingJMSClientLibrariesFacade.md
[image-1]:	images/JavascriptUniversalClient.png "Javascript Universal Client"
