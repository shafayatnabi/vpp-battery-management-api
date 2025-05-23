# VPP Battery Management API

This project is a Spring Boot application for managing batteries in a Virtual Power Plant (VPP) system. It provides REST APIs for creating, searching, and retrieving batteries with support for pagination and summary statistics.

## Features

- **Create Batteries**: Add new batteries to the system.
- **Create Batteries Asynchronously**: Send battery creation requests to Kafka for asynchronous processing to handle large number of battery creation.
- **Search Batteries**: Search batteries based on postcode and capacity criteria.
- **Retrieve Batteries in Range**: Fetch batteries within a postcode range with pagination.
- **Summary Statistics**: Get total, average, and count of battery capacities.

## Prerequisites

- Java 17 or higher
- Maven or Gradle
- PostgreSQL
- Docker (for running tests with TestContainers)

## Setup

1. Clone the repository:
   ```bash
   git clone https://github.com/shafayatnabi/vpp-battery-management-api.git
   cd vpp-battery-management-api
   ```

2. Configure the database connection by exporting the environment variables:
   ```bash
   export DB_URL=jdbc:postgresql://localhost:5432/vpp
   export DB_USERNAME=your_username
   export DB_PASSWORD=your_password
   ```

3. Build the project:
   ```bash
   ./gradlew build
   ```

4. Run the application:
   ```bash
   ./gradlew bootRun
   ```

## API Endpoints

### 1. Create Batteries
**POST** `/batteries`

- **Request Body**:
  ```json
  [
    {
      "name": "Battery A",
      "postcode": "2000",
      "capacity": 500
    },
    {
      "name": "Battery B",
      "postcode": "2500",
      "capacity": 600
    }
  ]
  ```
- **Response**:
  ```json
  [
    "uuid1",
    "uuid2"
  ]
  ```
### 2. Create Batteries Asynchronously
**POST** `/batteries/async`

- **Description**: Sends battery creation messages to Kafka for asynchronous processing. The batteries will be saved to the database by a Kafka consumer.
- **Request Body**:
  ```json
  [
    {
      "name": "Battery A",
      "postcode": "2000",
      "capacity": 500
    },
    {
      "name": "Battery B",
      "postcode": "2500",
      "capacity": 600
    }
  ]
  ```
- **Response**:
    - **Status Code**: `202 Accepted`
    - **Body**:
      ```text
      Battery creation sent successfully.
      ```
- **Query Parameters**:
    - `page` (optional): Page number (default: `0`).
    - `size` (optional): Page size (default: `10`).

- **Response**:
  ```json
  {
    "batteries": ["Battery A", "Battery B"],
    "totalBatteries": 2,
    "totalCapacity": 1100,
    "averageCapacity": 550.0
  }
  ```
### 3. Get Batteries in Range
**GET** `/batteries`

- **Query Parameters**:
  - `minPostCode` (required): Minimum postcode (e.g., `2000`).
  - `maxPostCode` (required): Maximum postcode (e.g., `3000`).
  - `page` (optional): Page number (default: `0`).
  - `size` (optional): Page size (default: `10`).

- **Response**:
  ```json
  {
    "batteries": ["Battery A", "Battery B"],
    "totalBatteries": 2,
    "totalCapacity": 1100,
    "averageCapacity": 550.0
  }
  ```

### 4. Search Batteries
**POST** `/batteries/search`

- **Request Body** (optional):
  ```json
  {
    "minPostCode": "2000",
    "maxPostCode": "3000",
    "minCapacity": 400,
    "maxCapacity": 700
  }
  ```

## Running Tests

1. Run unit and integration tests:
   ```bash
   ./gradlew test
   ```

2. The project uses **TestContainers** for integration tests with PostgreSQL.

## Local Run

To run the application locally, follow these steps:

1. **Start Dependencies**:
   Use Docker Compose to start PostgreSQL and Kafka:
   ```bash
   docker-compose up -d
   ```

2. **Run the Application**:
   Use Gradle to start the Spring Boot application:
   ```bash
   ./gradlew bootRun
   ```

3. **Verify the Setup**:
    - PostgreSQL should be running on `localhost:5432`.
    - Kafka should be running on `localhost:9092`.

4. **Stop Dependencies**:
   To stop the Docker containers, run:
   ```bash
   docker-compose down
   ```

## OpenAPI Documentation

The OpenAPI specification for the APIs is available in the file:
`/doc/openapi.yaml`

You can use tools like [Swagger Editor](https://editor.swagger.io/) to view and interact with the API documentation.



## Limitation
- Producer and Consumer of Kafka in same service, for scaling consumer or producer, if we decoupled the producer and consumer, we can scale them independently.
- No Authentication or Authorization for API's.
- No rate limiting.
- No monitoring metrics.
- No batch processing support in Kafka producer or consumer.
- No error handling in Kafka producer or consumer.