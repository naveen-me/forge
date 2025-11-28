# Project Audit Report

## Introduction

This report provides a comprehensive analysis of the playout scheduling application, covering the Node.js backend, the Vue.js frontend, and the build process. The goal of this audit is to identify strengths, weaknesses, and provide actionable recommendations to improve the application's performance, stability, and maintainability to a production-grade standard.

## Backend (`server`) Analysis

The backend is a well-structured Express.js application with a clear separation of concerns. It handles the core business logic, database interactions, and real-time communication with the frontend and OBS.

### Strengths

*   **Modular Structure:** The use of `controllers`, `services`, `routes`, and `models` directories creates a clean and maintainable codebase.
*   **ORM:** The use of Sequelize as an ORM simplifies database interactions and provides a level of abstraction from the underlying SQLite database.
*   **Real-time Functionality:** The WebSocket implementation in `taskService.js` is a solid foundation for real-time features.

### Weaknesses

*   **Hardcoded `userId`:** The `taskService.js` file contains a hardcoded `userId: 1`, which is a critical flaw for a multi-user application.
*   **In-memory Cache:** The in-memory cache in `auth.js` is not persistent, meaning it will be cleared on every server restart, potentially leading to performance issues.
*   **Database Synchronization:** The use of `sequelize.sync({ alter: true })` in `index.js` is not a suitable solution for production environments. It can lead to data loss and other issues.
*   **Inefficient OBS Connection:** The OBS WebSocket connection is re-established on every command, which is inefficient and can lead to performance issues.
*   **Basic Job Queue:** The in-memory job queue in `taskService.js` is not robust and could lead to lost jobs if the server restarts.

### Recommendations

*   **Implement Proper User Authentication for WebSockets:** The WebSocket connection should be authenticated to identify the user and use the correct OBS settings.
*   **Use a Persistent Cache:** Replace the in-memory cache with a persistent solution like Redis for better performance and reliability.
*   **Implement Database Migrations:** Use Sequelize's migration feature to manage database schema changes in a safe and controlled manner.
*   **Maintain a Persistent OBS Connection:** Establish a single, persistent OBS WebSocket connection per user to improve performance.
*   **Use a Robust Job Queue:** Replace the in-memory job queue with a more robust solution like BullMQ or RabbitMQ to ensure job persistence.

## Frontend (`webapp`) Analysis

The frontend is a modern Vue.js application with a reactive UI and a well-defined state management system.

### Strengths

*   **Component-Based Architecture:** The use of Vue components promotes reusability and maintainability.
*   **State Management:** The use of Pinia for state management provides a centralized and predictable way to manage the application's state.
*   **Routing:** The use of Vue Router with `beforeEach` guards for authentication and subscription checks is a robust solution.

### Weaknesses

*   **Large Components:** Some components, like `MediaLibrary.vue`, are very large and handle too much logic, making them difficult to maintain and test.
*   **Hardcoded URLs:** The `getThumbnailUrl` function in `MediaLibrary.vue` contains a hardcoded `localhost:3001` URL, which will not work in a production environment.
*   **Stale Cached Data:** The subscription data is cached in `localStorage` for 12 hours, which could lead to stale data if the user's subscription status changes.

### Recommendations

*   **Refactor Large Components:** Break down large components into smaller, more focused components to improve maintainability.
*   **Use Relative URLs or Environment Variables:** Replace hardcoded URLs with relative paths or environment variables to ensure the application works in different environments.
*   **Implement a Cache Invalidation Strategy:** Implement a mechanism to invalidate the `localStorage` cache when the user's subscription status changes.

## Build and Deployment Analysis

The build process uses Vite for the frontend and Node.js's experimental SEA feature to create a single executable for the backend.

### Strengths

*   **Modern Frontend Build Tool:** Vite provides a fast and efficient development experience for the frontend.
*   **Single Executable:** The SEA feature simplifies the deployment process by creating a single, self-contained executable.

### Weaknesses

*   **Experimental Feature:** The SEA feature is still experimental and may not be suitable for all production environments.
*   **Platform-Specific Tools:** The build script relies on `signtool`, which is a Windows-specific tool.

### Recommendations

*   **Evaluate the Stability of SEA:** Thoroughly test the SEA feature to ensure it's stable enough for your production environment. Consider alternatives like `pkg` or Electron if necessary.
*   **Use Cross-Platform Build Tools:** If the application needs to be built on different platforms, replace platform-specific tools like `signtool` with cross-platform alternatives.

## Performance Bottleneck: `node-file-dialog`

The slowness of the file dialog is likely due to the inherent nature of the `node-file-dialog` library, which is a wrapper around native OS dialogs.

### Recommendations

*   **Consider Alternatives:** For a more integrated and potentially faster experience, consider using a framework like Electron, which has its own file dialog API. If you stick with a pure Node.js backend, you may be limited to the performance of the libraries that wrap native dialogs.

## Conclusion & Prioritized Next Steps

The application is a solid foundation for a playout scheduling system, but there are several key areas that need to be addressed to make it production-grade. The following is a prioritized list of next steps:

1.  **Address Critical Backend Issues:**
    *   Fix the hardcoded `userId` in `taskService.js`.
    *   Implement a proper database migration strategy.
    *   Improve the OBS WebSocket connection handling.

2.  **Improve Frontend Robustness:**
    *   Remove hardcoded URLs.
    *   Implement a cache invalidation strategy for subscription data.
    *   Begin refactoring large components.

3.  **Stabilize the Build Process:**
    *   Evaluate the stability of the SEA feature and explore alternatives if necessary.

4.  **Long-Term Improvements:**
    *   Implement a robust, persistent job queue.
    *   Replace the in-memory cache with a persistent solution.
    *   Investigate alternatives to `node-file-dialog`.
