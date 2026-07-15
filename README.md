# Ecomera Product Service

![Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.11-brightgreen?logo=springboot&logoColor=white)
![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2025.0.1-6DB33F?logo=spring&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-336791?logo=postgresql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-7-DC382D?logo=redis&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Ready-2496ED?logo=docker&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-yellow?logo=open-source-initiative&logoColor=white)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=ecomera-product-service&metric=coverage)](https://sonarcloud.io/summary/new_code?id=ecomera-product-service)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=ecomera-product-service&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=ecomera-product-service)

Product Catalog microservice for the Ecomera ecosystem.

---

## Overview

Manages the product catalog for the Ecomera e-commerce platform. Provides CRUD operations, category filtering, search functionality, and inventory tracking with Redis caching for performance.

---

## Tech Stack

- **Spring Boot** 3.5.11
- **Spring Data JPA** - Database persistence
- **PostgreSQL** - Product data storage
- **Redis** - Distributed caching
- **MapStruct** - DTO mapping
- **Spring Cloud Config** - Centralized configuration
- **Eureka Client** - Service registration
- **Springdoc OpenAPI** - API documentation

---

## Running Locally

### Prerequisites
- Java 17+
- Maven 3.6+
- PostgreSQL 16+ (database: `ecomera_product`)
- Redis 7+
- Config Server running on port 8888
- Eureka Server running on port 8761

### Start the Service
```bash
mvn spring-boot:run
```

**Service available at:** `http://localhost:8082`

---

## API Endpoints

| Endpoint | Method | Description | Auth Required |
|----------|--------|-------------|---------------|
| `/api/v1/products` | POST | Create a new product | Yes (ADMIN, MANAGER) |
| `/api/v1/products` | GET | Get all products (paginated) | No |
| `/api/v1/products/{id}` | GET | Get product by ID | No |
| `/api/v1/products/{id}` | PUT | Update a product | Yes (ADMIN, MANAGER) |
| `/api/v1/products/{id}` | DELETE | Delete a product | Yes (ADMIN) |
| `/api/v1/products/search` | GET | Search products by title | No |
| `/api/v1/products/category/{category}` | GET | Get products by category | No |
| `/api/v1/products/price-range` | GET | Get products by price range | No |
| `/actuator/health` | GET | Health check | No |
| `/swagger-ui.html` | GET | OpenAPI documentation | No |

---

## Database Schema

```
product
├── id (UUID, PK)
├── title (VARCHAR, NOT NULL)
├── description (TEXT)
├── image_url (VARCHAR)
├── price (DECIMAL, NOT NULL)
├── stock (INTEGER, NOT NULL)
├── category (ENUM, NOT NULL)
├── created_at
├── created_by
├── updated_at
└── updated_by

category_type (ENUM)
├── ELECTRONICS
├── JEWELRY
├── MENS_CLOTHING
└── WOMENS_CLOTHING
```

---

## Configuration

Configuration fetched from **Config Server** (`product-service.yml`):
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/ecomera_product
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  data:
    redis:
      host: localhost
      port: 6379

server:
  port: 8082
```

---

## Docker Support

### Build Image
```bash
docker build -t ecomera-product-service .
```

### Run Container
```bash
docker run -p 8082:8082 \
  -e CONFIG_SERVER_URL=http://config-server:8888 \
  -e EUREKA_SERVER_URL=http://eureka:8761/eureka/ \
  ecomera-product-service
```

---

## Testing

```bash
# Unit tests
mvn test

# Integration tests with Testcontainers
mvn verify
```

---

## Architecture

```
Client → API Gateway → Product Service (port 8082)
                             ↓
                   PostgreSQL + Redis Cache
                             ↓
                   Config Server (configs)
                             ↓
                   Eureka Server (registration)
```

---

## Features

- **CRUD Operations** - Full create, read, update, delete for products
- **Pagination & Sorting** - Efficient data retrieval for large catalogs
- **Search** - Full-text search by product title
- **Category Filtering** - Filter products by category type
- **Price Range Filtering** - Find products within a price range
- **Redis Caching** - Cached product queries for performance
- **Audit Fields** - Automatic tracking of creation and update timestamps
- **Validation** - Request validation with Jakarta Bean Validation
- **API Documentation** - Auto-generated OpenAPI/Swagger docs

---

## Related Services

**Infrastructure:**
- [Config Server](https://github.com/ecomera-ecosystem/ecomera-config-server) - Centralized configuration
- [Eureka Server](https://github.com/ecomera-ecosystem/ecomera-eureka-service-registry) - Service discovery
- [API Gateway](https://github.com/ecomera-ecosystem/ecomera-api-gateway) - Entry point

**Business Services:**
- [Auth Service](https://github.com/ecomera-ecosystem/ecomera-auth-service) - Authentication & authorization
- [Order Service](https://github.com/ecomera-ecosystem/ecomera-order-service) - Order management
- [Cart Service](https://github.com/ecomera-ecosystem/ecomera-cart-service) - Shopping cart

---

**Status:** Active Development
