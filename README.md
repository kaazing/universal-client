# Kaazing Universal Clients

Welcome to the Kaazing WebSocket Universal Clients Repository!

This repository contains the code of the Kaazing Open Source WebSocket Universal Client as well as the *under the hood* explanations with the details how the protocol/technology clients are created.

## Introduction
Creating real-time application requires the use of a publish/subscribe message bus to push messages to the clients. Such message bus may be a JMS server; another popular choice is the AMQP protocol. The WebSocket gateway, connected to a message bus, enables these messages to be delivered over Web protocols (HTTP/HTTPS).

The goal of these libraries is to provide developers with a simple, *universal* WebSocket client library that is called exactly the same way regardles of the underlying message protocol and/or technology. This should simplify the development of websocket client applications communicating with JMS or AMQP editions of the Kaazing gateway


The Kaazing Universal Clients provide the facade for Kaazing specific protocol/technology libraries; these clients:
* Implement basic publish-subscribe functionality to help developers in getting started with their WebSocket projects
* Provide developers with reference technology/protocol specific implementations

## Available Universal Clients
(This list will grow as clients are added)
- [Universal Clients for JavaScript applications][1]

## Organization of the Universal Clients
Regardless of the client technology, all the clients organized as shown on the following diagram

![][image-1]

[1]:	https://github.com/kaazing/universal-client/tree/develop/javascript "Universal Clients for JavaScript applications"

[image-1]:	images/UniversalClients.png "Universal Clients"
