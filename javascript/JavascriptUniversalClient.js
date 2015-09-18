/**
 * Created by romans on 9/15/15.
 */

var UniversalClientDef=function(protocol){
    var UniversalClient = {};
    var client = null;
    UniversalClient.connect = function (url, username, password, topicP, topicS, noLocal, messageDestinationFuncHandle, loggerFuncHandle) {
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
                client = clientFunction(logInformation);
                client.connect(url, username, password, topicP, topicS, noLocal, messageDestinationFuncHandle, loggerFuncHandle);
            });
        }
        else if (protocol.toLowerCase() === "jms") {
            requirejs(['js/kaazing/library/WebSocket.js',/*"js/kaazing/library/JmsClient.js"*/, "js/kaazing/universal-client/JMSUniversalClient.js"], function () {
                console.info("Using JMS protocol!");
                client = clientFunction(logInformation);
                client.connect(url, username, password, topicP, topicS, noLocal, messageDestinationFuncHandle, loggerFuncHandle);
            });
        }
        else {
            alert("Unsupported protocol " + protocol);
        }
    }

    UniversalClient.sendMessage = function (msg) {
        client.sendMessage(msg);
    }

    UniversalClient.disconnect=function(){
        client.disconnect();
    }

    return UniversalClient;

};
