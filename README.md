# Reactive GraphQL with Spring Boot

> Production-ready GraphQL API with reactive Spring WebFlux, real-time subscriptions over WebSocket, JWT authentication, and comprehensive error handling.

## Overview

A fully reactive GraphQL implementation built with Spring Boot 3 and Spring WebFlux. Features real-time subscriptions, JWT-based security, custom exception handling, and transport-agnostic GraphQL subscriptions over WebSocket for bidirectional communication.

**Key Features:**
- GraphQL queries and mutations with reactive types
- Real-time subscriptions over WebSocket
- JWT authentication and authorization
- Custom GraphQL error handling
- Request header interception
- Reactive security with Spring Security
- GraphiQL interface for testing
- WebSocket connection management
- Type-safe schema definitions


## Architecture
<img width="1555" height="2832" alt="Mermaid Chart - Create complex, visual diagrams with text -2025-10-14-154845" src="https://github.com/user-attachments/assets/c4214885-81db-40fe-92f8-c36d9109fa85" />


## Request Flow
<img width="2806" height="2578" alt="Mermaid Chart - Create complex, visual diagrams with text -2025-10-14-154717" src="https://github.com/user-attachments/assets/eb42cbdc-983d-4fe5-b26a-a89cca1784eb" />


## Tech Stack

| Category | Technologies |
|----------|-------------|
| **Core** | Java 17, Spring Boot 3.1.3 |
| **Reactive** | Spring WebFlux, Project Reactor |
| **GraphQL** | Spring GraphQL, GraphQL Java |
| **Security** | Spring Security (Reactive), JWT (JJWT 0.12.2) |
| **WebSocket** | Spring WebSocket, Reactor Netty |
| **Testing** | Spring GraphQL Test, Reactor Test |


## Getting Started

### Prerequisites
```bash
- Java 17+
- Maven 3.8+
```

### Installation

#### 1. Build Application
```bash
mvn clean install
```

#### 2. Run Application
```bash
mvn spring-boot:run
```

Application starts on `http://localhost:8080`

**GraphiQL UI:** `http://localhost:8080/graphiql?path=/graphql`


### Query Persons

**GraphQL Query:**
```graphql
query {
  persons {
    id
    age
  }
}
```

**cURL:**
```bash
curl -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer " \
  -d '{
    "query": "query { persons { id age } }"
  }'
```

**Response:**
```json
{
  "data": {
    "persons": [
      { "id": "1", "age": 30 },
      { "id": "2", "age": 20 }
    ]
  }
}
```

### Query Person by ID

**GraphQL Query:**
```graphql
query {
  personById(id: "1") {
    id
    age
  }
}
```

**Response:**
```json
{
  "data": {
    "personById": {
      "id": "1",
      "age": 30
    }
  }
}
```

### GraphQL Subscription (WebSocket)

**Subscription Query:**
```graphql
subscription {
  notifyPersonUpdates {
    id
    age
  }
}
```

**Using GraphiQL:**
1. Open `http://localhost:8080/graphiql?path=/graphql`
2. Paste subscription query
3. Click "Execute query"
4. Observe real-time updates every second (5 updates total)

**WebSocket Connection:**
- Protocol: `graphql-transport-ws`
- Endpoint: `ws://localhost:8080/graphql`
- Init timeout: 60 seconds

## Key Features

### GraphQL Schema Definition

**person.graphqls:**
```graphql
schema {
    query: Query
    subscription: Subscription
}

type Query {
    persons: [Person]
    personById(id: String): Person
}

type Subscription {
    notifyPersonUpdates: Person
    notifyUpdates: String
}

type Person {
    id: String
    age: Int
}
```

### Query Controller
```java
@Controller
@RequiredArgsConstructor
public class PersonController {

    private final PersonService personService;

    @QueryMapping
    Flux persons() {
        return personService.getPersonList();
    }

    @QueryMapping
    Mono personById(@Argument String id, 
                             @ContextValue String authorization) {
        return personService.findById(id);
    }

    @SubscriptionMapping
    public Flux notifyPersonUpdates() {
        return Flux.fromStream(
            Stream.generate(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return new Person(UUID.randomUUID().toString().substring(0, 4), 10);
            }).limit(5)
        );
    }
}
```

### Custom Exception Resolver
```java
@Component
public class CustomExceptionResolver extends DataFetcherExceptionResolverAdapter {

    @Override
    protected GraphQLError resolveToSingleError(Throwable ex, 
                                                 DataFetchingEnvironment env) {
        ValidationException validationException = (ValidationException) ex;
        
        return GraphqlErrorBuilder.newError()
            .errorType(ErrorType.ValidationError)
            .message(validationException.getMessage())
            .path(env.getExecutionStepInfo().getPath())
            .location(env.getField().getSourceLocation())
            .build();
    }
}
```

### Request Header Interceptor
```java
@Configuration
class RequestHeaderInterceptor implements WebGraphQlInterceptor {

    @Override
    public Mono intercept(WebGraphQlRequest request, Chain chain) {
        String authHeader = request.getHeaders().getFirst("Authorization");
        
        if (!ObjectUtils.isEmpty(authHeader)) {
            request.configureExecutionInput((executionInput, builder) ->
                builder.graphQLContext(
                    Collections.singletonMap("authorization", authHeader)
                ).build()
            );
        }
        
        return chain.next(request);
    }
}
```

**Authentication Manager:**
```java
@Component
@AllArgsConstructor
public class AuthenticationManager implements ReactiveAuthenticationManager {

    private JWTUtil jwtUtil;

    @Override
    public Mono authenticate(Authentication authentication) {
        String authToken = authentication.getCredentials().toString();
        String username = jwtUtil.getUsernameFromToken(authToken);
        
        return Mono.just(jwtUtil.validateToken(authToken))
            .filter(valid -> valid)
            .switchIfEmpty(Mono.empty())
            .map(valid -> {
                Claims claims = jwtUtil.getAllClaimsFromToken(authToken);
                String roles = claims.get("role", String.class);
                return new UsernamePasswordAuthenticationToken(username, null, null);
            });
    }
}
```

### Security Configuration
```java
@Configuration
@EnableWebFluxSecurity
@AllArgsConstructor
public class WebSecurityConfig {

    private AuthenticationManager authenticationManager;
    private SecurityContextRepository securityContextRepository;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
            .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
            .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .authenticationManager(authenticationManager)
            .securityContextRepository(securityContextRepository)
            .authorizeExchange((authorize) -> authorize
                .pathMatchers(HttpMethod.OPTIONS).permitAll()
                .pathMatchers("/register").permitAll()
                .anyExchange().authenticated()
            )
            .build();
    }
}
```

# Learn More

For comprehensive guides on building reactive GraphQL applications with Spring Boot:

### Complete GraphQL Tutorial Series

**Reactive GraphQL with Spring Boot:**

| # | Topic | Article |
|---|-------|---------|
| 1 | **Error Handling** | [Error Handling in GraphQL with Spring Boot WebFlux](https://blog.stackademic.com/error-handling-in-graphql-with-spring-boot-webflux-840c3adb5dc0?sk=4e662b739cf7bf934baf7ee2d9d00979) |
| 2 | **GraphQL Subscriptions** | [GraphQL Subscriptions - Transport Agnostic Real-Time](https://blog.stackademic.com/graphql-is-transport-agnostic-graphql-subscriptions-bef59addce0e?sk=8e8123748cdcfb1c02d364c398f1a167) |


