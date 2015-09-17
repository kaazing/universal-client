/**
 * Created by romans on 9/15/15.
 */
var clientFunction=function(logInformation){
    var queueName="client" + Math.floor(Math.random() * 1000000);
    var routingKey="broadcastkey";
    function getRandomInt(min, max) {
        return Math.floor(Math.random() * (max - min)) + min;
    }
    var messageIdCounter = getRandomInt(1, 100000);

    var appId = (function () {
        /**! http://stackoverflow.com/a/2117523/377392 */
        var fmt = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx';
        var ret=fmt.replace(/[xy]/g, function (c) {
            var r = Math.random() * 16 | 0, v = c === 'x' ? r : (r & 0x3 | 0x8);
            return v.toString(16);
        });
        return ret;
    })();

    var initialized=false;

    var AmqpClient = {};

    var loggerFunction=null;
    var messageReceivedFunc=null;
    var amqpClient=null;
    var publishChannel=null;
    var consumeChannel=null;

    var topicPub=null;
    var topicSub=null;
    var noLocalFlag=false;
    var user=null;

    var publishChannelOpenHandler=function(){
        logInformation("INFO","OPENED: Publish Channel");

        publishChannel.declareExchange({exchange: topicPub, type: "fanout"});

        // Listen for these requests to return
        publishChannel.addEventListener("declareexchange", function() {
            logInformation("INFO","EXCHANGE DECLARED: " + topicPub);
        });

        publishChannel.addEventListener("error", function(e) {
            logInformation("ERROR","CHANNEL ERROR: Publish Channel - " + e.message, "ERROR");
        });

        publishChannel.addEventListener("close", function() {
            logInformation("INFO","CHANNEL CLOSED: Publish Channel");
        });
        initialized=true;
    }

    var consumeChannelOpenHandler=function(){
        logInformation("INFO","OPENED: Consume Channel");

        consumeChannel.addEventListener("declarequeue", function() {
            logInformation("INFO","QUEUE DECLARED: " + queueName);
        });

        consumeChannel.addEventListener("bindqueue", function() {
            logInformation("INFO","QUEUE BOUND: " + topicSub+ " - " + queueName);
        });

        consumeChannel.addEventListener("consume", function() {
            logInformation("INFO","CONSUME FROM QUEUE: " + queueName);
        });

        consumeChannel.addEventListener("flow", function(e) {
            logInformation("INFO","FLOW: " + (e.args.active ? "ON" : "OFF"));
        });

        consumeChannel.addEventListener("close", function() {
            logInformation("INFO","CHANNEL CLOSED: Consume Channel");
        });

        consumeChannel.addEventListener("message", function(message) {
            var body = null;

            // Check how the payload was packaged since older browsers like IE7 don't
            // support ArrayBuffer. In those cases, a Kaazing ByteBuffer was used instead.
            if (typeof(ArrayBuffer) === "undefined") {
                body = message.getBodyAsByteBuffer().getString(Charset.UTF8);
            }
            else {
                body = arrayBufferToString(message.getBodyAsArrayBuffer())
            }
            logInformation("DEBUG","Received from the wire "+body);
            try{
                body=angular.fromJson(body);
            }
            catch(e){
                logInformation("Received object is not JSON");
            }
            messageReceivedFunc(body);
        });

        // The default value for noAck is true. Passing a false value for 'noAck' in
        // the AmqpChannel.consumeBasic() function means there should be be explicit
        // acknowledgement when the message is received. If set to true, then no
        // explicit acknowledgement is required when the message is received.
        consumeChannel.declareQueue({queue: queueName})
            .bindQueue({queue: queueName, exchange: topicSub, routingKey: routingKey })
            .consumeBasic({queue: queueName, consumerTag: appId, noAck: true, noLocal:noLocalFlag });
    }

    var openHandler=function(){
        logInformation("INFO","CONNECTED!!!");

        logInformation("INFO","OPEN: Publish Channel");
        publishChannel = amqpClient.openChannel(publishChannelOpenHandler);

        logInformation("OPEN: Consume Channel");
        consumeChannel = amqpClient.openChannel(consumeChannelOpenHandler);
    }
    // Convert a string to an ArrayBuffer.
    //
    var stringToArrayBuffer = function(str) {
        var buf = new ArrayBuffer(str.length);
        var bufView = new Uint8Array(buf);
        for (var i=0, strLen=str.length; i<strLen; i++) {
            bufView[i] = str.charCodeAt(i);
        }
        return buf;
    }

    // Convert an ArrayBuffer to a string.
    //
    var arrayBufferToString = function(buf) {
        return String.fromCharCode.apply(null, new Uint8Array(buf));
    }
    AmqpClient.connect=function(url,username, password, topicP, topicS, noLocal, messageDestinationFuncHandle){
        topicPub=topicP;
        topicSub=topicS;
        user=username;
        messageReceivedFunc=messageDestinationFuncHandle;
        noLocalFlag=noLocal;
        var amqpClientFactory = new AmqpClientFactory();
        var webSocketFactory = new WebSocketFactory();
        amqpClientFactory.setWebSocketFactory(webSocketFactory);
        amqpClient = amqpClientFactory.createAmqpClient();
        amqpClient.addEventListener("close", function() {
            logInformation("INFO","Connection closed.");
        });

        amqpClient.addEventListener("error", function(e) {
            logInformation("ERROR","Connection error! "+ e.message);
        });
        var credentials = {username: username, password: password};
        var options = {
            url: url,
            virtualHost: "/",
            credentials: credentials
        };
        amqpClient.connect(options, openHandler);
    }

    AmqpClient.sendMessage=function(msg){
        if (typeof msg ==="object"){
            msg=angular.toJson(msg);
        }
        var body = null;
        if (typeof(ArrayBuffer) === "undefined") {
            body = new ByteBuffer();
            body.putString(msg, Charset.UTF8);
            body.flip();
        }
        else {
            body = stringToArrayBuffer(msg);
        }
        var props = new AmqpProperties();
        props.setContentType("text/plain");
        props.setContentEncoding("UTF-8");
        props.setDeliveryMode("1");
        props.setMessageId((messageIdCounter++).toString());
        props.setPriority("6");
        props.setTimestamp(new Date());
        props.setUserId(user);

        publishChannel.publishBasic({body: body, properties: props, exchange: topicPub, routingKey: routingKey});
    }

    return AmqpClient;
};
