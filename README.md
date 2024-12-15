# Distributed Ticket Management System

## Overview
A distributed ticket management system that demonstrates various communication protocols (UDP, AMQP, REST, gRPC) in a client-server architecture. The system allows users to create, view, update, and search support tickets through a Java Swing GUI.

## Features
- Multiple communication protocol support (UDP, AMQP, REST, gRPC)
- Ticket CRUD operations
- Search functionality
- Status management (NEW, ACCEPTED, REJECTED, CLOSED)
- Java Swing-based GUI
- Distributed architecture

# Technologies used

<div align="center">

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-FF6600?style=for-the-badge&logo=rabbitmq&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![gRPC](https://img.shields.io/badge/gRPC-244c5a?style=for-the-badge&logo=grpc&logoColor=white)

</div>



## Prerequisites
- Java 11 or higher
- Gradle 7.3
- RabbitMQ (for AMQP implementation)
- Protobuf compiler (for gRPC implementation)

### Installing Prerequisites
1. **Java**:
   ```bash
   # Check Java version
   java -version
   ```

2. **RabbitMQ** (for AMQP):
   ```bash
   # Using Docker
   docker run -d --rm --name rabbitmq \
   -p 5672:5672 -p 15672:15672 \
   rabbitmq:3.10.2-management
   ```
   Access RabbitMQ management: http://localhost:15672/
   - Username: guest
   - Password: guest

3. **Protobuf** (for gRPC):
   ```bash
   # macOS
   brew install protobuf

   # Ubuntu
   sudo apt-get install protobuf-compiler
   ```

## Project Structure

```
src/
├── client/                 # Client implementation
│   └── src/main/java/
│       └── de/uniba/rz/
│           ├── app/       # Protocol implementations
│           └── ui/        # GUI components
│
├── server/                 # Server implementation
│   └── src/main/java/
│       └── de/uniba/rz/
│           └── backend/   # Server components
│
└── shared/                 # Shared models and interfaces
    └── src/main/java/
        └── de/uniba/rz/
            └── entities/  # Data models
```

## Building the Project

```
# Clone the repository
git clone [repository-url]
cd [project-directory]

# Build the project
./gradlew clean build
```

# Running the Application

1. Start the Server

   ```
   # UDP Server (port 5000)
   ./gradlew :server:run --args="udp 5000"

    # AMQP Server
    ./gradlew :server:run --args="amqp routing_key"

    # gRPC Server (port 5000)
    ./gradlew :server:run --args="grpc 5000"

   # REST Server (port 8080)
   ./gradlew :server:run --args="rest 8080"
   ```


2. Start the Client
```
# Local implementation (no server needed)
./gradlew :client:run --args="local"

# UDP implementation
./gradlew :client:run --args="udp localhost 5000"

# AMQP implementation
./gradlew :client:run --args="amqp localhost routing_key"

# gRPC implementation
./gradlew :client:run --args="grpc localhost 5000"

# REST implementation
./gradlew :client:run --args="rest"
```



## Usage Guide

### Creating a Ticket
1. Launch the client application
2. Click "New Ticket"
3. Fill in the required fields:
   - Reporter
   - Topic
   - Description
   - Type
   - Priority
4. Click "Create"

### Managing Tickets
- **View Tickets**: All tickets are displayed in the main table
- **Search**: Use the search field to filter tickets
- **Update Status**: Select a ticket and use the action buttons:
  - Accept
  - Reject
  - Close

## Protocol Details

### UDP Implementation
- Connectionless communication
- Best for simple, fast requests
- No guaranteed delivery
- Port: 5000 (default)

### AMQP Implementation
- Uses RabbitMQ message broker
- Reliable message delivery
- Asynchronous communication
- Default port: 5672

### REST Implementation
- HTTP-based communication
- RESTful endpoints
- JSON data format
- Default port: 8080

### gRPC Implementation
- High-performance RPC
- Protocol buffer serialization
- Streaming support
- Port: 5000 (default)

