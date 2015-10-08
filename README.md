# Kaazing Universal Clients

Welcome to the Kaazing WebSocket Universal Clients Repository!

This repository contains the code of the Kaazing Open Source WebSocket Universal Client as well as the 'under the hood' explanations with the details how the protocol/technology clients are created.

The goal is to provide developers with a simple, "universal" WebSocket client that can communicate with either the JMS or AMQP editions of the Kaazing WebSocket Gateway without changes. This should simplify the development of websocket client applications.

## Introduction
Kaazing Universal Clients provide the facade for Kaazing specific protocol/technology libraries; these clients:
* Implement basic publish-subscribe functionality to help developers in getting started with their WebSocket projects
* Provide developers with the reference technology/protocol specific implementations

## Available Universal Clients
(This list will grow as clients are added)
- [Universal Clients for JavaScript applications][1]

## Organization of the Universal Clients
Regardless of the client technology, all the clients organized as shown on the following diagram

![][image-1]

[1]:	https://github.com/nemigaservices/universal-client/tree/develop/javascript "Universal Clients for JavaScript applications"

[image-1]:	images/UniversalClients.png "Universal Clients"
