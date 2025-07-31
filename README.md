# Customer Chat Demo

A proof-of-concept real-time customer support chat application built with Angular 18 (frontend) and Spring Boot 3 (backend) using WebSocket (STOMP protocol). This system is designed as a customer support module for a car rental platform.

## Features

- **Real-time chat** using WebSocket (STOMP protocol)
- **JWT authentication** for clients and agents
- **Ticket management system** with status tracking
- **Role-based access control** (clients vs agents)
- **PostgreSQL database** with full car rental schema
- **Docker containerization** for easy deployment
- **Complete database schema** including agencies, agents, clients, offers, reservations, and support tickets

## Quick Start with Docker (Recommended)

### Prerequisites
- Docker
- Docker Compose

### Running the Application

**Build and start all services:**
```bash
docker compose up -d --build
```

**Alternative (if you encounter BuildKit issues in WSL):**
```bash
DOCKER_BUILDKIT=0 docker compose up -d --build
```

**Stop all services:**
```bash
docker compose down
```

**View logs:**
```bash
docker compose logs -f [optional: a particular service-name]
```

**Check service status:**
```bash
docker compose ps
```

### Access the Application

After running `docker compose up -d --build`, wait 1-2 minutes for all services to be ready, then access:

- **Frontend (Angular)**: http://localhost (port 80)
- **Backend API**: http://localhost:8080
- **Database**: localhost:5432
- **API Documentation**: http://localhost:8080/swagger-ui/index.html

### Default Users

The application comes with pre-seeded users:

**Client:**
- Email: `client@gmail.com`
- Username: `bob`
- Password: `password`

**Agents:**
- **Marie** (Customer Service Agent)
  - id: `00000000-0000-0000-0000-000000000000`
  - Secret: `12345678`
- **Guillaume** (Customer Service Agent)
  - id: `88888888-8888-8888-8888-888888888888`
  - Secret: `87654321`

## Manual Installation Guide

### Prerequisites

- Java 21+ (check with `java -version`)
- Node.js 18+ (check with `node -v`)
- npm (check with `npm -v`)
- Apache Maven 3.x.x (check with `mvn -v`)
- PostgreSQL 16+ (check with `psql --version`)

### Clone the Repository

```bash
git clone https://github.com/Xinhe-Yu/customer-chat-demo
cd customer-chat-demo
```

### Backend Setup

1. **Navigate to backend directory:**
```bash
cd back
```

2. **Install Dependencies:**
```bash
mvn clean install -DskipTests
```

3. **Configure Environment Variables:**
   - Copy `env_template` to `.env`
   - Edit `.env` with your PostgreSQL credentials:
   ```
   DB_USERNAME=your_postgres_username
   DB_PASSWORD=your_postgres_password
   JWT_KEY=your_jwt_secret_key
   ```

4. **Initialize Database:**
```bash
# Create database
sudo -u postgres psql -c "CREATE DATABASE chat;"

# Apply schema
sudo -u postgres psql -d chat -f src/main/resources/static/schema.sql

# Apply seed data
sudo -u postgres psql -d chat -f src/main/resources/static/seeds.sql
```

5. **Run Backend:**
```bash
mvn spring-boot:run
```

Backend will start on http://localhost:8080

For API documentation, visit: http://localhost:8080/swagger-ui/index.html


### Frontend Setup

1. **Navigate to frontend directory:**
```bash
cd front
```

2. **Install Dependencies:**
```bash
npm install
```

3. **Start Development Server:**
```bash
ng serve
```

Frontend will start on http://localhost:4200

## Technology Stack

**Frontend:**
- Angular 19
- TypeScript
- WebSocket (STOMP.js)
- Angular Material (if used)
- Nginx (for Docker deployment)

**Backend:**
- Spring Boot 3
- Spring Security (JWT)
- Spring WebSocket
- JPA/Hibernate
- PostgreSQL Driver
- Maven

**Database:**
- PostgreSQL 16
- JSONB for flexible data storage

**DevOps:**
- Docker & Docker Compose
- Multi-stage builds
- Health checks

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## License

This project is a proof-of-concept demo for educational purposes.
