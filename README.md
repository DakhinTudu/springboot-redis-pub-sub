# 🚀 Spring Boot Redis Pub/Sub Demo

A modern, lightweight Spring Boot demonstration application showcasing real-time messaging using **Redis Publish/Subscribe (Pub/Sub)** infrastructure. This project displays how to distribute messages across multiple channels, handle dynamic subscriptions using wildcard pattern matching, and serialize message payloads.

---

## 📐 System Architecture

### Component Diagram
The system follows a typical Pub/Sub publisher-subscriber pattern where publishers and subscribers are decoupled. The Spring Boot application serves as both the REST endpoint provider (Publisher) and the Message Listener container (Subscriber), utilizing **Redis** as the message broker.

```mermaid
graph TD
    classDef client fill:#f9f,stroke:#333,stroke-width:2px;
    classDef SpringBoot fill:#bbf,stroke:#333,stroke-width:2px;
    classDef Redis fill:#fbb,stroke:#333,stroke-width:2px;

    subgraph Client Space
        Client[REST Client / Postman]:::client
    end

    subgraph Spring Boot Application
        Controller[PublisherController]:::SpringBoot
        Template[RedisTemplate]:::SpringBoot
        Container[RedisMessageListenerContainer]:::SpringBoot
        
        SubGeneral[GeneralSubscriber]:::SpringBoot
        SubSports[SportsSubscriber]:::SpringBoot
        SubTech[TechSubscriber]:::SpringBoot
        SubPrivate[PrivateSubscriber]:::SpringBoot
        SubDynamic[DynamicChannelSubscriber]:::SpringBoot
        SubChat[ChatSubscriber]:::SpringBoot
    end

    subgraph Redis Message Broker
        CG[chat.general Topic]:::Redis
        CS[chat.sports Topic]:::Redis
        CT[chat.tech Topic]:::Redis
        CP[chat.private Topic]:::Redis
        CDy[chat.any.* Pattern Topic]:::Redis
        CChat[chat.message Topic]:::Redis
    end

    Client -->|HTTP POST Payload| Controller
    Controller -->|convertAndSend| Template
    Template -->|PUBLISH| CG
    Template -->|PUBLISH| CS
    Template -->|PUBLISH| CT
    Template -->|PUBLISH| CP
    Template -->|PUBLISH| CDy
    Template -->|PUBLISH| CChat

    CG -->|Event Stream| Container
    CS -->|Event Stream| Container
    CT -->|Event Stream| Container
    CP -->|Event Stream| Container
    CDy -->|Wildcard Event Stream| Container
    CChat -->|Event Stream| Container

    Container -->|onMessage| SubGeneral
    Container -->|onMessage| SubSports
    Container -->|onMessage| SubTech
    Container -->|onMessage| SubPrivate
    Container -->|onMessage| SubDynamic
    Container -->|onMessage| SubChat
```

### Layered Architecture
This system is organized logically into five components, decoupling configuration setup from models, control mappings, template-based transmission services, and individual message listener sub-classes.

```mermaid
graph TD
    subgraph "Configuration Layer"
        RC[RedisConfig.java]
    end

    subgraph "Model Layer"
        MR[MessageRequest.java]
        CM[ChannelMessage.java]
    end

    subgraph "Controller Layer"
        PC[PublisherController.java]
    end

    subgraph "Service Layer"
        RT[RedisTemplate]
        LC[RedisMessageListenerContainer]
    end

    subgraph "Subscriber Layer"
        CS[ChatSubscriber]
        GS[GeneralSubscriber]
        SS[SportsSubscriber]
        TS[TechSubscriber]
        PS[PrivateSubscriber]
        DS[DynamicChannelSubscriber]
    end

    RC -->|Creates| RT
    RC -->|Creates| LC
    
    PC -->|Uses| RT
    PC -->|Receives| MR
    PC -->|Receives| CM
    
    RT -->|Sends to| Redis[(Redis Server)]
    
    Redis -->|Delivers to| LC
    LC -->|Routes to| CS
    LC -->|Routes to| GS
    LC -->|Routes to| SS
    LC -->|Routes to| TS
    LC -->|Routes to| PS
    LC -->|Routes to| DS
    
    CS -->|Prints| Console[Console Output]
    GS -->|Prints| Console
    SS -->|Prints| Console
    TS -->|Prints| Console
    PS -->|Prints| Console
    DS -->|Prints| Console

    classDef config fill:#b3e5fc,stroke:#01579b,stroke-width:2px
    classDef model fill:#fff9c4,stroke:#f57f17,stroke-width:2px
    classDef controller fill:#c8e6c9,stroke:#1b5e20,stroke-width:2px
    classDef service fill:#ffccbc,stroke:#bf360c,stroke-width:2px
    classDef subscriber fill:#e1bee7,stroke:#4a148c,stroke-width:2px
    classDef redis fill:#ffab91,stroke:#d84315,stroke-width:2px
    
    class RC config
    class MR,CM model
    class PC controller
    class RT,LC service
    class CS,GS,SS,TS,PS,DS subscriber
    class Redis redis
```

### Request Processing & Message Routing Flow
The application routes inbound payload packets conditionally based on target channel qualifiers, delegating serialization checks before dumping outputs to standard console logs.

```mermaid
flowchart TD
    Start([Start: HTTP Request]) --> A{Channel Type?}
    
    A -->|Static Channel| B[Send to specific channel]
    A -->|Dynamic Channel| C[Send to chat.* channel]
    A -->|Broadcast| D[Send to ALL channels]
    
    B --> E1[General Channel]
    B --> E2[Sports Channel]
    B --> E3[Tech Channel]
    B --> E4[Private Channel]
    
    C --> F[Dynamic Channel<br/>chat.random]
    D --> G[All Channels]
    
    E1 --> H1[GeneralSubscriber]
    E2 --> H2[SportsSubscriber]
    E3 --> H3[TechSubscriber]
    E4 --> H4[PrivateSubscriber]
    
    C --> F
    F --> H5[DynamicChannelSubscriber]
    G --> H1
    G --> H2
    G --> H3
    G --> H4
    G --> H5
    
    H1 --> I{Message Type?}
    H2 --> I
    H3 --> I
    H4 --> I
    H5 --> I
    
    I -->|String| J1[Print raw string]
    I -->|JSON| J2[Deserialize to ChannelMessage]
    I -->|Object| J3[Print object fields]
    
    J1 --> K[Console Output]
    J2 --> K
    J3 --> K
    
    K --> End([End])

    classDef start fill:#e8f5e9,stroke:#1b5e20,stroke-width:2px
    classDef process fill:#e3f2fd,stroke:#01579b,stroke-width:2px
    classDef channel fill:#fff3e0,stroke:#e65100,stroke-width:2px
    classDef subscriber fill:#f3e5f5,stroke:#4a148c,stroke-width:2px
    classDef output fill:#fce4ec,stroke:#b71c1c,stroke-width:2px
    
    class Start,End start
    class A,B,C,D,I process
    class E1,E2,E3,E4,F,G channel
    class H1,H2,H3,H4,H5 subscriber
    class J1,J2,J3,K output
```

### Message Life Cycle Sequence
The sequence diagram below displays the end-to-end flow of a message published via the REST API to subscribers.

```mermaid
sequenceDiagram
    autonumber
    actor User as REST Client (Postman/cURL)
    participant Ctrl as PublisherController
    participant RT as RedisTemplate
    participant Redis as Redis Server
    participant Cont as RedisMessageListenerContainer
    participant Sub as Subscriber Instance

    User->>Ctrl: POST /publish/general <br/> {"message": "Hello Redis!"}
    Ctrl->>RT: convertAndSend("chat.general", "Hello Redis!")
    RT->>Redis: PUBLISH chat.general "Hello Redis!"
    Note over Redis: Match subscribers of 'chat.general'
    Redis-->>Cont: Push message payload
    Cont->>Sub: onMessage(Message, pattern)
    Note over Sub: Parse message bytes & print to standard output console
```

### Class Diagram
The static code blueprint of the Spring Boot application, detailing annotations, operations, fields, and logical subscriber dependencies.

```mermaid
classDiagram
    class RedisPubSubApplication {
        +main(String[] args)
    }
    
    class RedisConfig {
        +CHANNEL_GENERAL: String
        +CHANNEL_SPORTS: String
        +CHANNEL_TECH: String
        +CHANNEL_PRIVATE: String
        +redisTemplate(RedisConnectionFactory)
        +redisMessageListenerContainer()
    }
    
    class PublisherController {
        -RedisTemplate redisTemplate
        +publishGeneral(MessageRequest)
        +publishSports(MessageRequest)
        +publishTech(MessageRequest)
        +publishPrivate(MessageRequest)
        +publishToAny(String, MessageRequest)
        +publishBroadcast(MessageRequest)
        +publishDetailed(ChannelMessage)
    }
    
    class MessageRequest {
        -String message
        +getMessage()
        +setMessage(String)
    }
    
    class ChannelMessage {
        -String channel
        -String message
        -String sender
        -long timestamp
        +getChannel()
        +setChannel(String)
        +getMessage()
        +setMessage(String)
        +getSender()
        +setSender(String)
        +getTimestamp()
        +setTimestamp(long)
    }
    
    class MessageListener {
        <<interface>>
        +onMessage(Message, byte[])
    }
    
    class ChatSubscriber {
        +onMessage(Message, byte[])
    }
    
    class GeneralSubscriber {
        +onMessage(Message, byte[])
    }
    
    class SportsSubscriber {
        +onMessage(Message, byte[])
    }
    
    class TechSubscriber {
        +onMessage(Message, byte[])
    }
    
    class PrivateSubscriber {
        +onMessage(Message, byte[])
    }
    
    class DynamicChannelSubscriber {
        +onMessage(Message, byte[])
    }
    
    RedisPubSubApplication --> RedisConfig : uses
    RedisPubSubApplication --> PublisherController : uses
    
    PublisherController --> RedisConfig : uses constants
    PublisherController --> MessageRequest : receives
    PublisherController --> ChannelMessage : receives
    
    RedisConfig --> ChatSubscriber : registers
    RedisConfig --> GeneralSubscriber : registers
    RedisConfig --> SportsSubscriber : registers
    RedisConfig --> TechSubscriber : registers
    RedisConfig --> PrivateSubscriber : registers
    RedisConfig --> DynamicChannelSubscriber : registers
    
    ChatSubscriber ..|> MessageListener : implements
    GeneralSubscriber ..|> MessageListener : implements
    SportsSubscriber ..|> MessageListener : implements
    TechSubscriber ..|> MessageListener : implements
    PrivateSubscriber ..|> MessageListener : implements
    DynamicChannelSubscriber ..|> MessageListener : implements
```

---

## 🌟 Key Features

* **Multiple Channel System**: Distribute messages to specific silos like General, Sports, Tech, and Private.
* **Pattern-Based Dynamic Subscription**: Subscribe dynamically to channel families using wildcard matching (e.g., `chat.any.*` via `DynamicChannelSubscriber`).
* **Broadcasting Capabilities**: Fan out a single inbound message to multiple channels simultaneously.
* **Payload Serialization**: Utilizes `StringRedisSerializer` and `JacksonJsonRedisSerializer` to handle payload transformation natively.
* **Robust Listener Management**: Handles thread pooling and connection recovery automatically through `RedisMessageListenerContainer`.

---

## 🛠️ Tech Stack & Requirements

* **Language**: Java 17+
* **Framework**: Spring Boot 3.x, Spring Data Redis
* **Message Broker**: Redis (Local Server / WSL / Docker Container)
* **Build System**: Maven
* **Utilities**: Lombok, Jackson

---

## ⚡ Quick Start

### 1. Run Redis Broker
The easiest way to boot up a Redis instance is using **Docker**:
```bash
docker run --name redis-pubsub-demo -p 6379:6379 -d redis
```
Alternatively, start the native service on Windows/WSL or Linux:
```bash
sudo service redis-server start
```

### 2. Configure the Application
Open `src/main/resources/application.properties` (or `application.yml`) to adjust your connection properties:
```properties
spring.data.redis.host=localhost
spring.data.redis.port=6379
```

### 3. Run the Spring Boot App
Navigate to the root directory and run:
```bash
mvn spring-boot:run
```
Or use the Maven Wrapper:
* **Windows**: `mvnw.cmd spring-boot:run`
* **Linux/WSL**: `./mvnw spring-boot:run`

---

## 🔌 API Endpoints & Usage Examples

Here are the endpoints available of the `PublisherController` mapped under `/publish`:

| HTTP Method | Endpoint | Description | Sample Payload |
| :--- | :--- | :--- | :--- |
| **POST** | `/publish/general` | Publish to `chat.general` channel | `{"message": "Hello general group!"}` |
| **POST** | `/publish/sports` | Publish to `chat.sports` channel | `{"message": "Ready for the big game?"}` |
| **POST** | `/publish/tech` | Publish to `chat.tech` channel | `{"message": "New AI models released"}` |
| **POST** | `/publish/private` | Publish to `chat.private` channel | `{"message": "Encrypted workspace message"}` |
| **POST** | `/publish/any/{channel}` | Custom tag routing (matches `chat.any.*`) | `{"message": "Dynamic payload content"}` |
| **POST** | `/publish/broadcast` | Unified fan-out to all core channels | `{"message": "System-wide alert!"}` |
| **POST** | `/publish/detailed` | Structured message detailing sender & topic | `{"channel":"tech","sender":"Antigravity","message":"Publishing payload"}` |

### cURL Snippets

#### 📣 Publish to General Channel
```bash
curl -X POST http://localhost:8080/publish/general \
     -H "Content-type: application/json" \
     -d '{"message": "Hello everyone on the general channel!"}'
```

#### 📬 Publish Custom Topic (Dynamic Matcher)
Channels passed to `/publish/any/{channel}` map internally to `chat.any.{channel}` which matches the `chat.any.*` subscription.
```bash
curl -X POST http://localhost:8080/publish/any/gaming \
     -H "Content-type: application/json" \
     -d '{"message": "New patch notes are live!"}'
```

#### 📡 Broadcast Message
```bash
curl -X POST http://localhost:8080/publish/broadcast \
     -H "Content-type: application/json" \
     -d '{"message": "System-wide database maintenance in 10 minutes."}'
```

---

## 🖥️ Console Output Verification

When you trigger the endpoints above, you will see real-time listener reactions in your local console:

```text
📢 [GENERAL] Message: "Hello everyone on the general channel!"
   Channel: chat.general
   ---

📬 [DYNAMIC] Channel: chat.any.gaming
   Message: "New patch notes are live!"
   ---

📢 [GENERAL] Message: "System-wide database maintenance in 10 minutes."
   Channel: chat.general
   ---
```
