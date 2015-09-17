/**
 * Created by romans on 9/15/15.
 */
var KaazingClientService = angular.module('KaazingClientService', ['KaazingAmpgClientService','KaazingJMSClientService'])
KaazingClientService.factory('UniversalClient', ['$log','AmqpClient','JMSClient', function ($log, AmqpClient, JMSClient) {
    var UniversalClient = {};
    var client=null;
    UniversalClient.connect=function(protocol, url,username, password, topicP, topicS, noLocal, messageDestinationFuncHandle, loggerFuncHandle){
        if (protocol.toLowerCase()==="amqp"){
            $log.info("Using AMQP protocol!");
            client=AmqpClient;
        }
        else if (protocol.toLowerCase()==="jms") {
            $log.info("Using JMS protocol!");
            client = JMSClient;
        }
        else{
            throw "Unsupported protocol "+protocol;
        }
        client.connect(url,username, password, topicP, topicS, noLocal, messageDestinationFuncHandle, loggerFuncHandle);
    }

    UniversalClient.sendMessage=function(msg){
        client.sendMessage(msg);
    }

    return UniversalClient;

}]);
