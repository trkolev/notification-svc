# Notification Service

A RESTful microservice for sending SMS notifications via Twilio, with comprehensive tracking and management capabilities.

## Table of Contents

- [Tech Stack](#tech-stack)
- [Features](#features)
- [API Endpoints](#api-endpoints)
- [Integrations](#integrations)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [Testing](#testing)
- [Error Handling](#error-handling)

## Tech Stack

### Core Framework
- **Java 17** - Programming language
- **Spring Boot 3.5.7** - Application framework
- **Maven** - Build and dependency management

### Spring Modules
- **Spring Web** - RESTful web services
- **Spring Data JPA** - Database persistence layer
- **Spring Validation** - Input validation
- **Spring Actuator** - Application monitoring and management

### Database
- **MySQL** - Production database
- **H2** - In-memory database for testing

### Third-Party Libraries
- **Twilio SDK 11.0.2** - SMS messaging service integration
- **Lombok** - Reduces boilerplate code
- **Hibernate** - JPA implementation

## Features

### Core Functionalities

1. **SMS Notification Sending**
   - Send SMS messages via Twilio API
   - Automatic status tracking (SUCCEEDED/FAILED)
   - Error handling with detailed exception messages

2. **Notification Management**
   - Query notifications by sender ID
   - Soft delete functionality (logical deletion)
   - Automatic timestamp tracking

3. **Data Persistence**
   - Store notification history in database
   - Track notification status and metadata
   - Support for multiple users/senders

4. **Input Validation**
   - Request body validation using Jakarta Validation
   - Field-level validation with custom error messages
   - Automatic validation error responses

5. **Error Handling**
   - Custom exception handling with `GlobalExceptionHandler`
   - Differentiated HTTP status codes based on error type
   - Structured error response format

6. **Logging**
   - Comprehensive error logging
   - Twilio API error tracking

## API Endpoints

### Base URL
```
http://localhost:8082/api/v1
```

### Send SMS
**POST** `/sms`

Sends an SMS notification to the specified phone number.

**Request Body:**
```json
{
  "phoneNumber": "+359123456789",
  "message": "Your notification message",
  "senderId": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Response (Success):**
- Status: `200 OK`
- Body: `"SMS sent to +359123456789"`

**Response (Error):**
- Status: `400 Bad Request` (Twilio API error) or `500 Internal Server Error` (other errors)
- Body:
```json
{
  "error": "Failed to send SMS to +359123456789: [error details]"
}
```

**Validation Errors:**
- Status: `400 Bad Request`
- Body:
```json
{
  "phoneNumber": "Phone number is required",
  "message": "Message is required",
  "senderId": "Sender ID is required"
}
```

### Get Notifications by Sender
**GET** `/sms?userId={senderId}`

Retrieves all non-deleted notifications for a specific sender.

**Query Parameters:**
- `userId` (UUID, required) - The sender's unique identifier

**Response:**
- Status: `200 OK`
- Body: Array of notification objects
```json
[
  {
    "id": "uuid",
    "message": "Message content",
    "contactInfo": "+359123456789",
    "createdAt": "2024-01-01T12:00:00",
    "status": "SUCCEEDED",
    "userId": "sender-uuid",
    "deleted": false
  }
]
```

### Delete Notifications by Sender
**DELETE** `/sms?userId={senderId}`

Soft deletes all notifications for a specific sender (marks as deleted without physical removal).

**Query Parameters:**
- `userId` (UUID, required) - The sender's unique identifier

**Response:**
- Status: `200 OK`
- Body: Empty

## Integrations

### Twilio SMS Service

The service integrates with **Twilio** for SMS delivery:

- **Service**: Twilio Cloud Communications Platform
- **SDK Version**: 11.0.2
- **Configuration**: Account SID, Auth Token, and Phone Number via application properties
- **Error Handling**: Differentiates between Twilio API errors (400) and other exceptions (500)

**Integration Flow:**
1. Application receives SMS request
2. Creates notification record with initial status
3. Calls Twilio API to send SMS
4. Updates notification status based on result
5. Returns appropriate response or throws exception

### MySQL Database

- **Purpose**: Persistent storage for notification history
- **Schema**: Auto-generated via Hibernate DDL
- **Features**:
  - Automatic database creation if not exists
  - UUID-based primary keys
  - Timestamp tracking
  - Soft delete support

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- MySQL 8.0+ (for production)
- Twilio account with API credentials

### Installation

1. **Clone the repository**
```bash
git clone <repository-url>
cd notification-svc
```

2. **Configure database and Twilio credentials**
   - Edit `src/main/resources/application.properties`
   - Update MySQL connection details
   - Add Twilio credentials

3. **Build the project**
```bash
mvn clean install
```

4. **Run the application**
```bash
mvn spring-boot:run
```

The service will start on port `8082` by default.

## Configuration

### Application Properties

**Database Configuration:**
```properties
spring.datasource.url=jdbc:mysql://host:port/database
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
```

**Twilio Configuration:**
```properties
twilio.account-sid=your_account_sid
twilio.auth-token=your_auth_token
twilio.phone-number=your_twilio_phone_number
```

**Server Configuration:**
```properties
server.port=8082
spring.application.name=notification-svc
```

### Environment Variables

For production, consider using environment variables instead of hardcoded values:

```bash
export TWILIO_ACCOUNT_SID=your_account_sid
export TWILIO_AUTH_TOKEN=your_auth_token
export TWILIO_PHONE_NUMBER=your_phone_number
export SPRING_DATASOURCE_URL=jdbc:mysql://host:port/database
export SPRING_DATASOURCE_USERNAME=username
export SPRING_DATASOURCE_PASSWORD=password
```

## Testing

The project includes comprehensive test coverage:

### Test Types

1. **Unit Tests** (`NotificationServiceUTest`)
   - Service layer unit testing with mocks
   - Tests successful SMS sending
   - Tests error handling scenarios

2. **Integration Tests** (`NotificationServiceITest`)
   - Full application context testing
   - Database integration testing
   - End-to-end workflow validation

3. **API Tests** (`NotificationServiceApiTest`)
   - REST controller testing
   - Request validation testing
   - Exception handler testing

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=NotificationServiceUTest

# Run with test profile
mvn test -Dspring.profiles.active=test
```

### Test Configuration

Tests use H2 in-memory database configured in `application-test.properties`:
- No external database required
- Fast test execution
- Automatic cleanup between tests

## Error Handling

### Exception Types

1. **NotificationException**
   - Custom runtime exception for notification-related errors
   - Contains detailed error messages
   - Preserves original exception as cause

2. **MethodArgumentNotValidException**
   - Automatically handled for validation errors
   - Returns field-level error messages

### Error Response Format

**Validation Errors:**
```json
{
  "fieldName": "Error message"
}
```

**Application Errors:**
```json
{
  "error": "Error description"
}
```

### HTTP Status Codes

- `200 OK` - Successful operation
- `400 Bad Request` - Validation errors or Twilio API errors
- `500 Internal Server Error` - Unexpected application errors

## Project Structure

```
notification-svc/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── org/example/notificationsvc/
│   │   │       ├── configuration/     # Twilio configuration
│   │   │       ├── exception/        # Exception classes and handlers
│   │   │       ├── model/            # Entity models
│   │   │       ├── repository/       # Data access layer
│   │   │       ├── service/          # Business logic
│   │   │       └── web/              # REST controllers and DTOs
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       ├── java/                     # Test classes
│       └── resources/
│           └── application-test.properties
└── pom.xml
```

## License

This project is part of a learning exercise and is provided as-is.

## Support

For issues or questions, please refer to the project repository or contact the development team.

