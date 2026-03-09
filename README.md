# Real-Time Chat App Backend (Spring Boot + WebSocket)
A backend system for a real-time messaging application built with Spring Boot, WebSocket, Redis, and Firebase Cloud Messaging.

## Overview

This project is a backend system for a real-time chat application built using Spring Boot and WebSocket (STOMP).

The goal of this project is to design a chat backend that supports real-time messaging, conversation management, presence tracking, moderation features, and push notifications. 
Users can send and receive messages instantly through WebSocket connections. The system also tracks online presence using Redis and sends push notifications using Firebase Cloud Messaging (FCM).

The project focuses mainly on backend architecture and system design rather than UI.

## Problem Statement

Most basic chat applications only demonstrate simple message exchange between users. However, real-world messaging systems require much more than just sending messages.

A production-ready chat system needs to handle:

- real-time communication
- authentication and security
- online/offline presence tracking
- conversation management
- moderation features (block, ban)
- push notifications
- protection against abuse such as message flooding

This project was created to explore how these components can work together in a backend architecture using Spring Boot.

The goal was to design a system that resembles a simplified version of messaging platforms like WhatsApp or Discord from a backend perspective.

## Tech Stack

Backend:
- Java
- Spring Boot
- Spring WebSocket (STOMP)
- Spring Security
- Spring Data JPA

Database:
- PostgreSQL

Caching / Realtime State:
- Redis

Authentication:
- JWT (JSON Web Token)

Push Notifications:
- Firebase Cloud Messaging (FCM)

Build Tool:
- Maven

## Features

The backend provides several core features commonly found in modern messaging systems.

### Real-Time Messaging
- WebSocket based communication using STOMP
- instant message delivery between participants

### Authentication
- JWT based authentication
- WebSocket handshake authentication

### Conversation Management
- create private conversations
- manage conversation participants
- retrieve conversation messages

### Presence Tracking
- track online/offline user status
- Redis used as a presence cache

### Moderation System
- block users
- ban users
- prevent unwanted interactions

### Rate Limiting
- protect the system from flooded messages
- applied through filters and interceptors

### Push Notifications
- Firebase Cloud Messaging integration
- send notifications when users are offline

### Read Receipts
- track whether messages were seen by participants or not

## System Architecture

The following diagram shows the high level architecture of the system and how different components interact.

![Architecture](docs/Chat_App_Architecture.png)

The system follows a layered architecture:

Client Layer  
Handles communication through REST APIs and WebSocket connections.

Security Layer  
Responsible for authentication and request protection using JWT and rate limiting.

Controller Layer  
Handles incoming requests and routes them to application services.

Application Layer  
Contains the main business logic including messaging, presence tracking, moderation, and notifications.

Repository Layer  
Handles database interaction using Spring Data JPA.

Infrastructure  
- PostgreSQL for message storage
- Redis for presence tracking
- Firebase for push notifications

## Message Flow

1. A user sends a message through the WebSocket connection.
2. The request passes through the security layer (JWT + rate limiting).
3. The controller receives the message request.
4. MessageService processes and validate the message.
5. The message is stored in PostgreSQL through the repository layer.
6. ChatEventPublisher publishes the event.
7. The message is broadcast to other connected users.
8. If the recipient is offline, NotificationService sends a push notification through Firebase.

## Project Structure

The project follows a layered architecture structure.
src/main/java
```text
configuration  → application and framework configuration  
controller     → REST and WebSocket endpoints  
service        → business logic  
repository     → database access  
model          → entity models  
security       → authentication and security logic  
component      → event publishing and helpers  
exception      → global exception handling  
DTO            → request and response objects
```

## Running the Project

### Requirements

- Java 17+
- PostgreSQL
- Redis
- Firebase project for push notifications

### Steps

1. Clone the repository

git clone [https://github.com/thamizharasan-nemo/RealTimeMessagingApplication-SpringBoot]

2. Configure application.properties

Update database, Redis, and Firebase configuration.

3. Run the application

./mvnw spring-boot:run

or

Run the main Spring Boot application class from your IDE.


## Possible Improvements

Some features that could be added in the future:

- message delivery status (sent / delivered)
- typing indicators
- message search
- message attachments feature
- group chat roles (admin/moderator)
- message pagination
- distributed message queue (Kafka or RabbitMQ)
