# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a **modular enterprise web application template** designed for AI solutions. The project provides a foundation for rapid development of enterprise web applications with up to 100 users. The template is currently in the planning phase with detailed requirements outlined in `prd.txt`.

## Technology Stack

### Frontend
- **Framework**: Next.js 14
- **UI Library**: Material-UI (MUI) with Light/Dark theme system  
- **State Management**: Zustand
- **HTTP Client**: Axios
- **Form Management**: React Hook Form + Zod validation
- **Real-time**: WebSocket for notifications

### Backend  
- **Language**: Java 17 (LTS)
- **Framework**: Spring Boot 3.2.x
- **Security**: Spring Security + JWT + BCrypt
- **Database**: MySQL with JPA/Hibernate
- **Build Tool**: Gradle

### Infrastructure
- **Cloud**: Microsoft Azure
- **Reverse Proxy**: Nginx
- **Environment Management**: Spring Profiles (local/dev/prod)

## Architecture Principles

### 1. Role-Based Access Control (RBAC)
```
User → Role → Permission → Feature
```
- **최고관리자**: Full system access
- **관리자**: User management, system settings  
- **사용자**: Basic functionality
- Database-driven permissions with no code changes required for new permissions

### 2. Modular Design
- Template designed for easy customization per client requirements
- Plugin-style architecture for adding new modules
- Environment switching via single configuration value in `application.yml`

### 3. Security First
- JWT token-based authentication
- Admin approval required for new user registrations
- BCrypt password encryption
- Input validation on both client and server
- CORS policy configuration

## Core Features

### Authentication System
- Master account auto-generation on first run
- JWT-based login/logout with Remember Me option
- Password policy enforcement
- Automatic redirect for unauthorized access

### User Management
- Personal profile pages with image upload
- Admin-only user management interface
- Account activation/deactivation
- Email uniqueness validation

### Enterprise Features
- Personalized dashboard with user-specific metrics
- Real-time notification system via WebSocket
- File upload/download with configurable restrictions
- Theme system (Light/Dark/Auto) with system preference detection

## Environment Configuration

The application uses a single environment switch in `application.yml`:
```yaml
app:
  environment: local  # local/dev/prod
```

This automatically applies all environment-specific configurations including:
- Database connections
- Log levels (DEBUG for local/dev, INFO+ for prod)
- Performance optimizations
- Security settings

## Development Commands

*Note: The actual project has not been implemented yet. When implementation begins, add the specific build, test, and lint commands here.*

Expected commands based on technology stack:
- Frontend: `npm run dev`, `npm run build`, `npm run lint`
- Backend: `./gradlew bootRun`, `./gradlew build`, `./gradlew test`

## Responsive Design Requirements

- **Desktop**: 1920px+ optimization
- **Tablet**: 768px-1919px
- **Mobile**: 767px and below
- Modern, clean design following ChatGPT/AI Studio aesthetic
- Enterprise-friendly color palette with minimal branding

## Development Phases

1. **Phase 1** (4 weeks): Core infrastructure setup
2. **Phase 2** (4 weeks): Authentication and user management  
3. **Phase 3** (3 weeks): Dashboard, notifications, file system
4. **Phase 4** (2 weeks): UI polish, performance optimization

## Performance Targets

- Initial loading: <3 seconds
- Page transitions: <1 second  
- API responses: <500ms
- Concurrent users: 100
- Code coverage: 80%+