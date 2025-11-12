# Personal Finance Manager - Frontend

A modern, responsive Vue.js 3 frontend application for managing personal finances.

## Tech Stack

- **Vue 3** - Progressive JavaScript framework with Composition API
- **Vite** - Next generation frontend tooling
- **Vue Router** - Official router for Vue.js
- **Pinia** - Intuitive Vue state management
- **Axios** - Promise-based HTTP client
- **Element Plus** - Vue 3 UI component library
- **Chart.js** - JavaScript charting library
- **Vue-ChartJS** - Vue wrapper for Chart.js

## Features

### Authentication
- User login and registration
- JWT token-based authentication
- Protected routes with navigation guards
- Automatic token refresh

### Dashboard
- Financial overview with key metrics
- Monthly income/expense trends chart
- Account balances overview
- Recent transactions list

### Account Management
- Create, edit, and delete accounts
- Support for multiple account types (Bank, Credit Card, Cash)
- Multi-currency support
- Real-time balance tracking

### Transaction Management
- Add, edit, and delete transactions
- Advanced filtering (by account, category, type, date range)
- Pagination support
- Income and expense categorization

### Category Management
- Pre-seeded system categories
- Create custom categories
- Icon and color customization
- Visual category identification

### Budget Management
- Set monthly budgets per category
- Track spending vs budget
- Visual progress indicators
- Budget utilization percentage
- Over-budget warnings

### Reports & Analytics
- Income vs Expense summary
- Category-wise spending breakdown
- Interactive charts and visualizations
- Monthly trend analysis
- Percentage-based insights

### User Profile
- Update profile information
- Change password
- User preferences

## Project Structure

```
frontend/
├── src/
│   ├── assets/           # Static assets
│   ├── components/       # Reusable Vue components
│   ├── router/           # Vue Router configuration
│   ├── services/         # API service modules
│   │   ├── auth.js      # Authentication API
│   │   ├── user.js      # User API
│   │   ├── account.js   # Accounts API
│   │   ├── transaction.js # Transactions API
│   │   ├── category.js  # Categories API
│   │   ├── budget.js    # Budgets API
│   │   └── report.js    # Reports API
│   ├── stores/          # Pinia state stores
│   │   ├── auth.js     # Authentication state
│   │   ├── account.js  # Accounts state
│   │   └── category.js # Categories state
│   ├── utils/          # Utility functions
│   │   └── request.js  # Axios instance & interceptors
│   ├── views/          # Page components
│   │   ├── Login.vue
│   │   ├── Register.vue
│   │   ├── Layout.vue
│   │   ├── Dashboard.vue
│   │   ├── Accounts.vue
│   │   ├── Transactions.vue
│   │   ├── Categories.vue
│   │   ├── Budgets.vue
│   │   ├── Reports.vue
│   │   └── Profile.vue
│   ├── App.vue         # Root component
│   └── main.js         # Application entry point
├── public/             # Public static files
├── index.html          # HTML template
├── vite.config.js      # Vite configuration
├── package.json        # Dependencies
├── Dockerfile          # Docker build configuration
└── nginx.conf          # Nginx server configuration
```

## Development Setup

### Prerequisites
- Node.js 18+
- npm or yarn

### Installation

1. Navigate to the frontend directory:
```bash
cd frontend
```

2. Install dependencies:
```bash
npm install
```

3. Create environment file:
```bash
cp .env.example .env
```

4. Start development server:
```bash
npm run dev
```

The application will be available at http://localhost:5173

### Build for Production

```bash
npm run build
```

The production-ready files will be in the `dist` directory.

### Preview Production Build

```bash
npm run preview
```

## Docker Deployment

The frontend is containerized using Docker with Nginx as the web server.

### Build Docker Image

```bash
docker build -t finance-frontend .
```

### Run Docker Container

```bash
docker run -p 80:80 finance-frontend
```

## API Integration

The frontend communicates with the backend REST API through Axios interceptors that:

- Automatically attach JWT tokens to requests
- Handle authentication errors (401)
- Provide global error handling
- Format request/response data

API base URL can be configured via environment variable:
```
VITE_API_BASE_URL=/api
```

## Routing

The application uses Vue Router with the following routes:

- `/login` - User login page (guest only)
- `/register` - User registration page (guest only)
- `/` - Dashboard (protected)
- `/accounts` - Accounts management (protected)
- `/transactions` - Transactions management (protected)
- `/categories` - Categories management (protected)
- `/budgets` - Budgets management (protected)
- `/reports` - Reports and analytics (protected)
- `/profile` - User profile (protected)

## State Management

Pinia stores manage application state:

- **Auth Store**: User authentication state, login/logout, token management
- **Account Store**: Accounts data and CRUD operations
- **Category Store**: Categories data and CRUD operations

## UI Components

The application uses Element Plus for consistent UI:

- Forms with validation
- Data tables with pagination
- Modal dialogs
- Cards and layouts
- Charts and visualizations
- Notifications and messages
- Date pickers
- Color pickers
- Dropdowns and menus

## Responsive Design

The application is fully responsive and works on:
- Desktop (1200px+)
- Tablet (768px - 1199px)
- Mobile (< 768px)

## Browser Support

- Chrome (latest)
- Firefox (latest)
- Safari (latest)
- Edge (latest)

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `VITE_API_BASE_URL` | Backend API base URL | `/api` |

## Contributing

When contributing to the frontend:

1. Follow Vue 3 Composition API patterns
2. Use Pinia for state management
3. Implement proper error handling
4. Add loading states for async operations
5. Ensure responsive design
6. Write clean, maintainable code
7. Use Element Plus components when possible

## License

MIT License
