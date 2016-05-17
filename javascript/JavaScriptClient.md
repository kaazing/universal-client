# Kaazing JavaScript Universal Client for Javascript
This library is intended to be used with 'plain JavaScript', AngularJS, and ReactJS applications
(see [Kaazing JavaScript Starter Applications](https://github.com/kaazing/javascript.getting.started) and
[Kaazing Examples and Tutorials](https://github.com/kaazing/tutorials)); it provides a JavaScript function that returns
an object that can be used in the client application to interact with Kaazing Gateway.

## Using the Library
### Install the library
#### Using Bower
- Install library with the Bower as specified in the [README][1].
- Add the following scripts to your page:
```html
<script src="bower_components/kaazing-javascript-universal-client/javascript/src/AngularUniversalClient.js"></script>
<script src="bower_components/kaazing-javascript-universal-client/javascript/src/JmsClient.js"></script>

```
**Note:** `JmsClient.js` is not needed when using the AMQP protocol.

#### Using NPM
- Install library with the NPM as specified in the [README][1].
- Add the following scripts to your page:
```html
<script src="node_modules/kaazing-javascript-universal-client/node_modules/kaazing-javascript-jms-client/JmsClient.js"></script>
<script src="node_modules/kaazing-javascript-universal-client/AngularUniversalClientNPM.js"></script>
```
**Note:** Addition of JmsClient.js is not needed when using AMQP protocol.


### Add the library to your application
- Create an instance of the Universal Client Library.
	```javascript
	var client = UniversalClientDef(protocol);
	```
	Where:
	- **protocol**: Specifies the protocol that should be used for communications:
	  - jms - for communication with the Kaazing JMS Gateway
	  - amqp - for communication with the Kaazing AMQP Gateway.

- Establish a connection
	```javascript
	var client = UniversalClientDef(protocol);
	client.connect(connectionInfo, // Connection info
      onError, // callback function to process errors
      function(connection){
        }
	);
	```
	Where:
	- **connectionInfo**: Connection object that includes:
	  - _URL_ (e.g. ws://localhost:8001/amqp or ws://localhost:8001/jms)
	  - _username_(user name to be used to establish connection) and
	  - _password_(user password to be used to establish connection)
    - **onError**: function that is used for error handling in a format of _function(error)_.
	- _callback_ function to receive a connection object once connection is established.
	
	**Note:** If you want to add a logger to log library messages, add the following after creating the client:
	```javascript
	
	var logWebSocketMessage = function (cls, msg) {
 		...
	}
	var client = UniversalClientDef(protocol);
	// Set the logger function
	client.loggerFuncHandle=logWebSocketMessage;

	```
- Subscribe to  topics of interest

```javascript
var client = UniversalClientDef(protocol);
var subscription;
client.connect(
    connectionInfo, // Connection info
    onError, // callback function to process errors
    function(connection){
        connection.subscribe(
            topicP, // Topic to send message
            topicS, // Topic to subscribe to receive messsages
            onMessage, // callback function to process received messages
            noLocal, // noLocal flag set to false - allow receiving your own messages
            function(sub){
                subscription = sub;
            });
        })
````
	
Where:
  - **topicP**: Name of the publishing endpoint - AMQP exchange used for publishing or JMS Topic
  - **topicS**: Name of the subscription endpoint - AMQP exchange used for subscription or JMS Topic
  - **onMessage**: Function that will be used to process received messages from subscription endpoint in a format of _function(message)_
  - **noLocal**: Flag indicating whether the client wants to receive its own messages (true) or not (false). That flag should be used when publishing and subscription endpoints are the same.
  - _callback_ function to receive subscription object
**Note** Multiple subscriptions are allowed within single connection!

	```javascript
	var client = UniversalClientDef(protocol);
    var subscription;
    client.connect(
        connectionInfo, // Connection info
        onError, // callback function to process errors
        function(connection){
            connection.subscribe(
                topicP, // Topic to send message
                topicS, // Topic to subscribe to receive messsages
                onMessage, // callback function to process received messages
                noLocal, // noLocal flag set to false - allow receiving your own messages
                function(sub){
                    subscription = sub;
                });
            })
	```
	- Add disconnect on window close (this example uses JQuery):

    ```javascript
        $(window).unload(function() {
            client.disconnect();
        });
    ```
- To send messages use sendMessage(msg) method of a subscription object
	where _**msg**_ JavaScript object to be sent (as a JSON string). 
	```javascript
	var client=UniversalClientDef(protocol);
	var sendMessage=function(msg){
		// Send message
    	subscription.sendMessage(msg);
	}

	var subscription;
    client.connect(connectionInfo, // Connection info
            onError, // callback function to process errors
            function(connection){
                 connection.subscribe(
                    topicP, // Topic to send message
                    topicS, // Topic to subscribe to receive messages
                    onMessage, // callback function to process received message
                    noLocal, // noLocal flag set to false - allow receiving your own messages
                    function(sub){
                        subscription = sub;
                    });
                }
            );
	```

## Organization of Kaazing JavaScript Universal Client   

![][image-1]

As shown on the diagram above, Kaazing Universal Client works as following:
- Determine Client Library Facade based on the specified protocol
- Download all necessary JavaScript libraries including the needed Client Library Facade using RequireJS.
	- **Notes:**

    - Kaazing AMQP client libraries require Kaazing WebSocket library to be downloaded and instantiated first, to achieve it Universal Client uses the following code:

        For Bower

        ```javascript
        requirejs(['bower_components/kaazing-amqp-0-9-1-client-javascript/javascript/WebSocket.js'],function(){
            requirejs([
              'bower_components/jquery/dist/jquery.js',
              'bower_components/kaazing-amqp-0-9-1-client-javascript/javascript/Amqp-0-9-1.js',
              'bower_components/kaazing-javascript-universal-client/javascript/src/AmqpUniversalClient.js'
              ], function () {
                ...
                });
            });
        ```

        For NPM

        ```javascript
      requirejs(['node_modules/kaazing-javascript-universal-client/node_modules/kaazing-javascript-gateway-client/WebSocket.js'],function(){
            requirejs([
              'node_modules/jquery/dist/jquery.js',
              'node_modules/kaazing-javascript-universal-client/node_modules/kaazing-javascript-amqp-client/AmqpClient.js',
              'node_modules/kaazing-javascript-universal-client/AmqpUniversalClient.js'
              ], function () {
            ...
            });
        });
        ```
    _Bower and NPM implementations differ due to paths of the dependent packages._


   - Due to certain limitations, RequireJS cannot download the Kaazing JMSClient.js library hence it has to be included directly
- Instantiate required Client Facade Library that will interact with necessary Kaazing Javascript Client Libraries
- Pass the data to and from the Kaazing Javascript Client libraries via instantiated Client Facade Library

For more information about Client Facade libraries see
[AMQP Client Libraries Facade][2] and [JMS Client Libraries Facade][3].   

[1]:	README.md
[2]:	KaazingAMQPClientLibrariesFacade.md
[3]:	KaazingJMSClientLibrariesFacade.md
[image-1]:	images/JavascriptUniversalClient.png "Javascript Universal Client"
