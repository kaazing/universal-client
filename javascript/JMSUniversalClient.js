/**
 * Created by romans on 9/15/15.
 */
var KaazingClientService = angular.module('KaazingJMSClientService', [])
KaazingClientService.factory('JMSClient', ['$log', function ($log) {
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

    var JMSClient = {};

    var loggerFunction=null;
    var messageReceivedFunc=null;
    var amqpClient=null;
    var publishChannel=null;
    var consumeChannel=null;

    var logInformation=function(severity, message){
        if (loggerFunction!==null)
            loggerFunction(severity, message);
        if (severity=="INFO"){
            $log.info(message);
        }
        else if (severity=="ERROR"){
            $log.error(message);
        }
        else if (severity=="WARN"){
            $log.warn(message);
        }
        else
            $log.debug(message);

    }

    var topicPub=null;
    var topicSub=null;
    var noLocalFlag=false;
    var user=null;

    function setupSSO(webSocketFactory) {
        /* Respond to authentication challenges with popup login dialog */
        var basicHandler = new BasicChallengeHandler();
        basicHandler.loginHandler = function (callback) {
            popupLoginDialog(callback);
        }
        webSocketFactory.setChallengeHandler(basicHandler);
    }

    var handleException = function (e) {
        logInformation("ERROR","Error! " + e);
    }
    var connection=null;
    var session=null;
    var producer=null;
    var consumer=null;

    var prepareSend = function () {
        var dest = session.createTopic(topicPub);
        producer = session.createProducer(dest);
        logInformation("INFO","Producer is ready! AppID=" + appId);
    }

    var prepareReceive = function (rcvFunction) {
        var dest = session.createTopic(topicSub);
        consumer = session.createConsumer(dest, "appId<>'" + appId + "'");
        consumer.setMessageListener(function (message) {
            var body=message.getText();
            logInformation("DEBUG","Received from the wire "+body);
            try{
                body=angular.fromJson(body);
            }
            catch(e){
                logInformation("Received object is not JSON");
            }
            rcvFunction(body);
        });
        logInformation("Consumer is ready!");
    }

    JMSClient.connect=function(url,username, password, topicP, topicS, noLocal, messageDestinationFuncHandle, loggerFuncHandle){
        topicPub=topicP;
        topicSub=topicS;
        user=username;
        loggerFunction=loggerFuncHandle;
        messageReceivedFunc=messageDestinationFuncHandle;
        noLocalFlag=noLocal;
        logInformation("INFO","CONNECTING TO: " + url);

        var jmsConnectionFactory = new JmsConnectionFactory(url);

        //setup challenge handler
        setupSSO(jmsConnectionFactory.getWebSocketFactory());
        try {
            var connectionFuture =
                jmsConnectionFactory.createConnection(username, password, function () {
                    if (!connectionFuture.exception) {
                        try {
                            connection = connectionFuture.getValue();
                            connection.setExceptionListener(handleException);

                            logInformation("CONNECTED");

                            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

                            connection.start(function () {
                                logInformation("INFO","JMS session created");
                                prepareSend();
                                prepareReceive(messageReceivedFunc);
                            });
                        }
                        catch (e) {
                            handleException(e);
                        }
                    }
                    else {
                        handleException(connectionFuture.exception);
                    }
                });
        }
        catch (e) {
            handleException(e);
        }
    }

    JMSClient.sendMessage=function(msg){
        if (typeof msg ==="object"){
            msg=angular.toJson(msg);
        }

        var textMsg = session.createTextMessage(msg);
        textMsg.setStringProperty("appId", appId);
        try {
            var future = producer.send(textMsg, function () {
                if (future.exception) {
                    handleException(future.exception);
                }
            });
        } catch (e) {
            handleException(e);
        }
        logInformation("sent","Send command " + msg, "sent");
    }

    return JMSClient;

}]);
