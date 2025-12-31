# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

PolyHermes is a Polymarket prediction market copy-trading system with automated order copying, multi-account management, real-time WebSocket push notifications, and statistical analysis.

## Development Commands

### Backend (Kotlin/Spring Boot)
```bash
cd backend
./gradlew bootRun                    # Start development server (port 8000)
./gradlew build                      # Build the project
./gradlew test                       # Run tests
./gradlew bootJar                    # Build executable JAR
```

### Frontend (React/TypeScript/Vite)
```bash
cd frontend
npm install                          # Install dependencies
npm run dev                          # Start dev server (port 3000)
npm run build                        # Production build
npm run lint                         # Run ESLint
```

### Docker Deployment
```bash
./deploy.sh                          # Local build deployment
./deploy.sh --use-docker-hub         # Use pre-built Docker Hub image
docker-compose up -d                 # Start services
docker-compose logs -f               # View logs
```

## Architecture

### Backend Structure (`backend/src/main/kotlin/com/wrbug/polymarketbot/`)
- **api/**: Retrofit interfaces for external APIs (Polymarket CLOB, Gamma, Subgraph, GitHub, Ethereum RPC)
- **controller/**: REST controllers - all endpoints use POST method with `ApiResponse<T>` wrapper
- **service/**: Business logic organized by domain:
  - `accounts/`: Account and position management
  - `auth/`: Authentication with JWT
  - `copytrading/`: Core copy-trading logic (configs, leaders, monitor, orders, statistics, templates)
  - `common/`: Shared services (blockchain, rate limiting, WebSocket subscriptions)
- **entity/**: JPA entities (MySQL with Flyway migrations)
- **repository/**: Spring Data JPA repositories
- **websocket/**: WebSocket message handlers for real-time updates

### Frontend Structure (`frontend/src/`)
- **pages/**: Page components (AccountList, LeaderList, CopyTradingList, etc.)
- **components/**: Reusable components including Layout with responsive mobile/desktop support
- **services/**: API client (`api.ts`) and WebSocket service
- **store/**: Zustand state management
- **locales/**: i18n translations (zh-CN, zh-TW, en)
- **utils/**: Utilities including `formatUSDC()` for currency formatting

### Key Data Flow
1. Copy-trading monitors Leader wallets via WebSocket (`UnifiedOnChainWsService`)
2. When Leader trades are detected, orders are filtered and copied to follower accounts
3. Positions and orders are pushed to frontend via WebSocket
4. Frontend subscribes to account-specific WebSocket channels for real-time updates

## Code Conventions

### Backend (Kotlin)
- **No TODO/FIXME comments** - implement fully or add clear limitation notes
- **No mock data** - all API calls must return real data or explicit errors
- Controller methods must NOT use `suspend` - wrap coroutines with `runBlocking`
- Entity IDs: `Long? = null` with `@GeneratedValue`
- Time fields: `Long` millisecond timestamps (not `LocalDateTime`)
- Numeric fields: `BigDecimal` for precision
- Use `ErrorCode` enum with `MessageSource` for i18n error messages
- Use `outcomeIndex` (0/1) instead of "YES"/"NO" strings for side determination
- Configuration: `application.properties` only (no YAML)

### Frontend (TypeScript/React)
- **No `any` type** - use proper TypeScript definitions
- **No hardcoded text** - use `useTranslation()` hook for all UI text
- **Use `formatUSDC()`** for all USDC amount display (from `utils/index.ts`)
- Import utilities from `../utils` (unified export)
- Mobile-first responsive design (breakpoint: 768px)
- Functional components with hooks

### API Convention
- All endpoints use POST method
- Response format: `{ code: 0, data: T, msg: "" }`
- Error codes: 0=success, 1xxx=param errors, 2xxx=auth errors, 3xxx=not found, 4xxx=business logic, 5xxx=server errors

### Platform Scope
- **Polymarket only** (not Kalshi or other platforms)
- Categories: `sports` and `crypto` only

## Key Files
- `docs/zh/copy-trading-requirements.md`: Backend API specification
- `docs/zh/DEVELOPMENT.md`: Development guide
- `.cursor/rules/backend.mdc`: Detailed backend coding standards
- `.cursor/rules/frontend.mdc`: Detailed frontend coding standards
