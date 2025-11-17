# Gemini Project Context: Playout System

This document provides a comprehensive overview of the Playout System project to be used as instructional context for Gemini.

## 1. Project Overview

This is a **local-first desktop playout system** designed for managing and organizing media assets for broadcasting or streaming. The application is built using **Electron** and features a multi-faceted backend architecture.

### Core Technologies & Architecture:

*   **Frontend:** A **Vue.js 3** single-page application built with **Vite**.
    *   **State Management:** Pinia
    *   **Routing:** Vue Router
    *   **UI:** TailwindCSS, with some Bootstrap components.
    *   **Key Libraries:** `vuedraggable` for list ordering, `axios` for HTTP requests, and `tiptap` for rich text editing.

*   **Node.js Backend:** An **Express.js** server that acts as the primary API for the frontend.
    *   **Database:** SQLite, managed via the **Sequelize** ORM.
    *   **Real-time:** Uses **WebSockets (`ws`)** to send real-time updates to the frontend (e.g., after media processing).
    *   **Media Processing:** A background worker (`worker.js`) uses **`fluent-ffmpeg`** to generate thumbnails and extract metadata from video and image files.
    *   **Authentication:** Handles JWT validation.

*   **PHP Backend:** A separate backend that handles sensitive business logic.
    *   **Responsibilities:** User authentication, subscriptions, and payments.
    *   **Database:** Interacts with a database via the **Medoo** framework.
    *   **Communication:** The Node.js backend communicates with the PHP backend via a dedicated `phpApiService`, which sends HMAC-signed requests for security.

### Key Features:

*   **Media Library & Ads:** Two very similar features that provide a file-manager-like interface for organizing local video/image files into groups (folders). They support thumbnail generation, sorting, and moving items.
*   **Overlay Manager:** A system for creating, managing, and grouping graphical overlays (Image, Video, Text). These overlays can be positioned, styled, and ordered. The UI supports drag-and-drop for reordering overlays within groups.
*   **Single Executable Build:** The project is configured to be packaged into a single executable file using Node.js SEA (Single Executable Applications), bundling the Node.js server and the built Vue.js frontend.

## 2. Building and Running

### Environment Setup

1.  **Node.js Backend:** Create a `.env` file in the `server/` directory. Key variables include `PORT`, `DB_FILE`, and `PHP_API_URL`.
2.  **PHP Backend:** The PHP environment needs to be configured separately with its own environment variables for database and JWT secrets.

### Development Commands

*   **Run the Backend Server:**
    ```bash
    cd server
    npm install
    npm run dev
    ```

*   **Run the Frontend App:**
    ```bash
    cd webapp
    npm install
    npm run dev
    ```

*   **Run the PHP Server:**
    *TODO: The method for running the local PHP server is not specified. It likely requires a local server environment like XAMPP, WAMP, or using PHP's built-in server.*

### Production Build

1.  **Build the Vue App:**
    ```bash
    cd webapp
    npm run build
    ```
2.  **Build the Single Executable:**
    ```bash
    cd server
    node ../build/build-sea.js
    ```
    The output will be in the `build/output/` directory.

## 3. Development Conventions

*   **Modular Structure:** The code is organized by feature and responsibility.
    *   **Node.js Backend:** `routes`, `models` (Sequelize), `services`, `controllers`.
    *   **Vue.js Frontend:** `views` (top-level pages), `components`, `stores` (Pinia), `services`.
*   **API Communication:**
    *   For features like `media-library` and `ads`, the frontend uses dedicated service files (`mediaService.js`, `adService.js`) that wrap `axios` calls.
    *   For the `overlays` feature, `axios` is used directly within the Vue component.
*   **State Management:** Feature-specific Pinia stores are used to manage frontend state (e.g., `useAdStore`, `useMediaStore`).
*   **Background Jobs:** Long-running tasks like thumbnail generation are offloaded to a separate worker process (`worker.js`) to avoid blocking the main server thread.
*   **Database:** The schema for each feature (`MediaItem`, `Ad`, `Overlay`) uses a self-referencing `parentId` and an `order` column to implement a hierarchical group structure and maintain custom sorting.
