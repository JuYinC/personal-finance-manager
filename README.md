# Personal Finance Manager

A comprehensive full-stack application for managing personal finances, built with Vue.js 3, Spring Boot 3.x, PostgreSQL, and Docker.

## Overview

Personal Finance Manager is a full-featured web application that helps users track their income, expenses, budgets, and financial health. The application provides a modern, responsive Vue.js frontend and a secure RESTful API backend with JWT authentication, comprehensive transaction management, and detailed financial reporting.

## Tech Stack

### Frontend
- **Vue 3** - Progressive JavaScript framework
- **Vite** - Next generation frontend tooling
- **Vue Router** - Official router for Vue.js
- **Pinia** - Vue state management
- **Axios** - HTTP client
- **Element Plus** - Vue 3 UI component library
- **Chart.js** - Data visualization
- **Nginx** - Web server (production)

### Backend
- **Java 17** - Programming language
- **Spring Boot 3.2.0** - Application framework
- **Spring Security** - Authentication and authorization
- **Spring Data JPA** - Database access layer
- **PostgreSQL 15** - Relational database
- **Liquibase** - Database migration management
- **JWT (JSON Web Tokens)** - Secure authentication
- **Gradle** - Build tool
- **Lombok** - Boilerplate code reduction
- **SpringDoc OpenAPI 3** - API documentation (Swagger UI)

### DevOps
- **Docker** - Containerization
- **Docker Compose** - Multi-container orchestration

## Features

### Authentication & Security
- User registration and login
- JWT token-based authentication
- BCrypt password encryption
- CORS configuration for frontend integration

### Account Management
- Create and manage multiple accounts (Bank, Credit Card, Cash)
- Real-time balance tracking
- Multi-currency support

### Transaction Management
- Record income and expenses
- Automatic account balance updates
- Soft delete with balance adjustment
- Advanced filtering (by account, category, date range, type)
- Pagination support

### Category Management
- System-provided default categories
- Custom user categories
- Icon and color customization
- Category usage tracking

### Budget Management
- Set monthly budgets per category
- Track budget vs actual spending
- Budget utilization reports

### Financial Reports
- Income vs Expense summary
- Category-wise spending breakdown
- Monthly trends analysis
- Percentage-based insights

## Prerequisites

- **Docker** 20.10+
- **Docker Compose** 2.0+
- **Java 17+** (for local development without Docker)
- **Gradle 8.5+** (for local development without Docker)

## Quick Start with Docker

### 1. Clone the Repository

```bash
git clone <repository-url>
cd personal-finance-manager
```

### 2. Configure Environment Variables

Copy the example environment file and customize it:

```bash
cp .env.example .env
```

Edit `.env` file with your preferred settings:

```env
# Database Configuration
DB_HOST=postgres
DB_PORT=5432
DB_NAME=finance_manager
DB_USER=postgres
DB_PASSWORD=your_secure_password

# JWT Configuration
JWT_SECRET=your-256-bit-secret-key-change-this-in-production
JWT_EXPIRATION=86400000

# Server Configuration
SERVER_PORT=8080
```

**Important:** Change `JWT_SECRET` to a strong, random secret key in production!

### 3. Start the Application

```bash
docker-compose up --build
```

The application will:
1. Build the Vue.js frontend
2. Build the Spring Boot backend
3. Start PostgreSQL database
4. Run Liquibase migrations
5. Seed default categories
6. Start all services

### 4. Access the Application

- **Frontend Web App:** http://localhost
- **Backend API:** http://localhost:8080
- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **API Docs (JSON):** http://localhost:8080/v3/api-docs

### 5. Default Login

After the application starts, register a new account via the web interface at http://localhost

## API Documentation

### Authentication Endpoints

#### Register
```http
POST /api/auth/register
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "SecurePass123"
}
```

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "SecurePass123"
}
```

### Protected Endpoints

All other endpoints require JWT token in the Authorization header:

```http
Authorization: Bearer <your-jwt-token>
```

### User Endpoints
- `GET /api/users/me` - Get current user profile
- `PUT /api/users/me` - Update current user profile
- `PUT /api/users/me/password` - Change password

### Account Endpoints
- `GET /api/accounts` - List all accounts
- `POST /api/accounts` - Create new account
- `GET /api/accounts/{id}` - Get account details
- `PUT /api/accounts/{id}` - Update account
- `DELETE /api/accounts/{id}` - Delete account

### Transaction Endpoints
- `GET /api/transactions` - List transactions (with filters)
- `POST /api/transactions` - Create transaction
- `GET /api/transactions/{id}` - Get transaction details
- `PUT /api/transactions/{id}` - Update transaction
- `DELETE /api/transactions/{id}` - Soft delete transaction

### Category Endpoints
- `GET /api/categories` - List all categories
- `POST /api/categories` - Create custom category
- `PUT /api/categories/{id}` - Update custom category
- `DELETE /api/categories/{id}` - Delete custom category

### Budget Endpoints
- `GET /api/budgets?month={month}&year={year}` - List budgets
- `POST /api/budgets` - Create or update budget
- `DELETE /api/budgets/{id}` - Delete budget

### Report Endpoints
- `GET /api/reports/summary?month={month}&year={year}` - Income vs Expense summary
- `GET /api/reports/by-category?startDate={date}&endDate={date}&type={type}` - Category spending
- `GET /api/reports/trends?months={number}` - Monthly trends

For detailed request/response examples, visit the Swagger UI at http://localhost:8080/swagger-ui.html

## Project Structure

```
personal-finance-manager/
├── frontend/                    # Vue.js frontend
│   ├── src/
│   │   ├── assets/             # Static assets
│   │   ├── components/         # Reusable components
│   │   ├── router/             # Vue Router config
│   │   ├── services/           # API services
│   │   ├── stores/             # Pinia stores
│   │   ├── utils/              # Utilities
│   │   ├── views/              # Page components
│   │   │   ├── Login.vue
│   │   │   ├── Register.vue
│   │   │   ├── Dashboard.vue
│   │   │   ├── Accounts.vue
│   │   │   ├── Transactions.vue
│   │   │   ├── Categories.vue
│   │   │   ├── Budgets.vue
│   │   │   ├── Reports.vue
│   │   │   └── Profile.vue
│   │   ├── App.vue
│   │   └── main.js
│   ├── Dockerfile
│   ├── nginx.conf
│   ├── vite.config.js
│   └── package.json
│
├── backend/                     # Spring Boot backend
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/finance/manager/
│   │   │   │   ├── config/          # Configuration classes
│   │   │   │   ├── controller/      # REST controllers
│   │   │   │   ├── dto/             # Data Transfer Objects
│   │   │   │   ├── entity/          # JPA entities
│   │   │   │   ├── repository/      # JPA repositories
│   │   │   │   ├── service/         # Business logic
│   │   │   │   ├── security/        # JWT & security
│   │   │   │   └── exception/       # Exception handling
│   │   │   └── resources/
│   │   │       ├── application.yml
│   │   │       └── db/changelog/    # Liquibase migrations
│   │   └── test/                    # Test files
│   ├── Dockerfile
│   └── build.gradle
│
├── docker-compose.yml           # Docker orchestration
├── .env.example                 # Environment variables template
└── README.md                    # This file
```

## Database Schema

### Users Table
- Stores user account information
- BCrypt hashed passwords
- Unique email constraint

### Accounts Table
- User's financial accounts
- Types: BANK, CREDIT_CARD, CASH
- Tracks real-time balance
- Multi-currency support

### Categories Table
- System categories (pre-seeded)
- User custom categories
- Supports icons and colors

### Transactions Table
- Records all financial transactions
- Soft delete support
- Automatic balance updates
- Foreign key constraints

### Budgets Table
- Monthly budgets per category
- Unique constraint per user/category/month/year
- Budget tracking and reporting

## Development Without Docker

### Prerequisites
- Java 17+
- PostgreSQL 15+
- Gradle 8.5+

### Setup

1. **Install PostgreSQL** and create a database:

```sql
CREATE DATABASE finance_manager;
```

2. **Configure application.yml** or set environment variables:

```bash
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=finance_manager
export DB_USER=postgres
export DB_PASSWORD=postgres
export JWT_SECRET=your-secret-key
```

3. **Build and run the application**:

```bash
cd backend
./gradlew clean build
./gradlew bootRun
```

## Running Tests

```bash
cd backend
./gradlew test
```

## Security Considerations

### Production Checklist
- [ ] Change `JWT_SECRET` to a strong, random 256-bit key
- [ ] Use strong database passwords
- [ ] Enable HTTPS/TLS
- [ ] Configure proper CORS origins
- [ ] Set up rate limiting
- [ ] Enable database connection pooling
- [ ] Configure proper logging levels
- [ ] Use environment-specific configurations
- [ ] Set up monitoring and alerting
- [ ] Regular security updates

### JWT Token Security
- Tokens expire after 24 hours (configurable)
- Tokens are signed with HS256 algorithm
- Passwords are hashed with BCrypt (strength 10)

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_HOST` | Database host | postgres |
| `DB_PORT` | Database port | 5432 |
| `DB_NAME` | Database name | finance_manager |
| `DB_USER` | Database user | postgres |
| `DB_PASSWORD` | Database password | postgres |
| `JWT_SECRET` | JWT signing key | (must be set) |
| `JWT_EXPIRATION` | Token expiration (ms) | 86400000 (24h) |
| `SERVER_PORT` | Application port | 8080 |
| `CORS_ALLOWED_ORIGINS` | Allowed CORS origins | http://localhost:5173,http://localhost:3000 |

## Troubleshooting

### Port Already in Use
If port 8080 or 5432 is already in use, change the ports in `docker-compose.yml` or stop the conflicting service.

### Database Connection Issues
Ensure PostgreSQL is running and credentials in `.env` are correct.

### Liquibase Migration Failures
Check database permissions and ensure no manual schema changes conflict with migrations.

## Screenshots

### Dashboard
Modern dashboard with financial overview, charts, and recent transactions.

### Transactions
Comprehensive transaction management with advanced filtering and pagination.

### Budgets
Visual budget tracking with progress indicators and over-budget warnings.

### Reports
Interactive charts and detailed financial analytics.

## Future Enhancements

- [ ] Recurring transactions
- [ ] File attachments for transactions
- [ ] Multi-user support (family accounts)
- [ ] Budget alerts and notifications
- [ ] Export to CSV/Excel
- [ ] Import from bank statements
- [ ] Investment tracking
- [ ] Bill reminders
- [ ] Financial goals
- [ ] Mobile app
- [ ] Advanced data visualization
- [ ] Machine learning insights
- [ ] Email notifications
- [ ] Dark mode
- [ ] Multi-language support

## Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License.

## Support

For issues, questions, or contributions, please create an issue in the GitHub repository.

## Authors

Finance Manager Team

---

**Built with ❤️ using Spring Boot 3.x**
