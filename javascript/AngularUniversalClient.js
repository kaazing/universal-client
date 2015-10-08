/**
 * Created by romans on 9/15/15.
 */

/**
 * Kaazing Client AngularJS Module that implements Client services.
  */
var KaazingClientService = angular.module('KaazingClientService', [])
/**
 * Universal Client Service that works as a facade that communicates with the gateway based on specified protocol. Script downloads necessary libraries except of JmsClient.js - that script has to be added to the <head> section.
 */
KaazingClientService.factory('AngularUniversalClient', [function () {
    /**
     * Provides communication services with JMS or AMQP server. Created within KaazingClientService AngularJS Module.
     * @class
     * @name AngularUniversalClient
     */
    var AngularUniversalClient = {};
    var client = null;

    /**
     * Connects to Kaazing WebSocket Gateway (AMQP or JMS)
     * @param protocol Specifies protocol that should be used for communications: jms - for communication with Kaazing JMS Gateway, amqp - for communication with Kaazing AMQP Gateway.
     * @param url Connection URL
     * @param username User name to be used to establish connection
     * @param password User password to be used to establish connection
     * @param topicP Name of the publishing endpoint - AMQP exchange used for publishing.
     * @param topicS Name of the subscription endpoint - AMQP exchange used for subscription
     * @param noLocal Flag indicating whether the client wants to receive its own messages (true) or not (false). That flag should be used when publishing and subscription endpoints are the same.
     * @param messageDestinationFuncHandle Function that will be used to process received messages from subscription endpoint in a format: function(messageBody)
     * @param loggerFuncHandle function that is used for logging events in a format of function(severity, message)
     */
    AngularUniversalClient.connect = function (protocol, url, username, password, topicP, topicS, noLocal, messageDestinationFuncHandle, loggerFuncHandle) {
        var logInformation = function (severity, message) {
            if (loggerFuncHandle !== null)
                loggerFuncHandle(severity, message);
            if (severity == "INFO") {
                console.info(message);
            }
            else if (severity == "ERROR") {
                console.error(message);
            }
            else if (severity == "WARN") {
                console.warn(message);
            }
            else
                console.trace(message);
        }
        if (protocol.toLowerCase() === "amqp") {
            requirejs(['js/kaazing/library/WebSocket.js',"js/kaazing/library/AmqpClient.js", "js/kaazing/universal-client/AmqpUniversalClient.js"], function () {
                console.info("Using AMQP protocol!");
                client = amqpClientFunction(logInformation);
                client.connect(url, username, password, topicP, topicS, noLocal, messageDestinationFuncHandle, loggerFuncHandle);
            });
        }
        else if (protocol.toLowerCase() === "jms") {
            requirejs(['js/kaazing/library/WebSocket.js',/*"js/kaazing/library/JmsClient.js"*/, "js/kaazing/universal-client/JMSUniversalClient.js"], function () {
                console.info("Using JMS protocol!");
                client = jmsClientFunction(logInformation);
                client.connect(url, username, password, topicP, topicS, noLocal, messageDestinationFuncHandle, loggerFuncHandle);
            });
        }
        else {
            alert("Unsupported protocol " + protocol);
        }
    }
    /**
     * Sends messages to a publishing endpoint.
     * @param msg Message to be sent. As messages are sent in a text format msg will be converted to JSON if it is not a string.
     */
    AngularUniversalClient.sendMessage = function (msg) {
        client.sendMessage(msg);
    }

    /**
     * Disconnects from Kaazing WebSocket Gateway
     */
    AngularUniversalClient.disconnect=function(){
        client.disconnect();
    }

    return AngularUniversalClient;

}]);
