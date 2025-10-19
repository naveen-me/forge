# Playout System Boilerplate

This boilerplate provides a solid foundation for building a local-first playout system using Node.js and Vue 3. It includes a backend server, a frontend web application, and scripts to package the entire system into a single executable file.

## Features

-   **Backend**: Node.js with Express, SQLite, and WebSocket support.
-   **Frontend**: Vue 3 with Vite, Pinia, and Bootstrap.
-   **Build**: Single executable creation using Node.js SEA (Single Executable Applications).
-   **Local-First**: All data is stored locally in a SQLite database.
-   **Real-time Updates**: WebSocket for live communication between the frontend, backend, and other services like OBS.

## Project Structure

```
/
├── build/
│   ├── build-sea.js        # Script to build the single executable
│   └── sea.config.json     # SEA configuration
├── server/
│   ├── src/
│   │   ├── controllers/    # Route handlers
│   │   ├── db/             # Database setup
│   │   ├── routes/         # API routes
│   │   └── services/       # Business logic (OBS, external APIs)
│   ├── .env                # Environment variables
│   ├── index.js            # Server entry point
│   └── package.json
├── webapp/
│   ├── public/             # Static assets
│   ├── src/                # Vue app source
│   ├── index.html
│   ├── package.json
│   └── vite.config.js
└── README.md
```

## Local Development

### 1. Backend Server

```bash
cd server
npm install
npm run dev
```

The server will run on the port specified in `server/.env` (default: 4000).

### 2. Frontend Web Application

```bash
cd webapp
npm install
npm run dev
```

The Vue development server will run on `http://localhost:5173`.

## Building for Production

To build the entire application and create the single executable, follow these steps:

### 1. Build the Vue App

This command bundles the frontend application into the `webapp/dist` directory.

```bash
cd webapp
npm run build
```

### 2. Build the Single Executable

This command packages the Node.js server and the built Vue app into a single `.exe` file.

```bash
cd server
node ../build/build-sea.js
```

The output `playout.exe` will be located in the `build/output/` directory.

## Configuration

### Environment Variables

Create a `.env` file in the `server/` directory to configure the application:

```
PORT=4000
API_BASE_URL=http://yourapi.com
DB_FILE=playout.db
```

### OBS and API Credentials

Place your credentials for OBS WebSocket or other third-party APIs in `server/.env`. Access them within the services located in `server/src/services/`.

-   **OBS WebSocket**: Modify `server/src/services/obsService.js`.
-   **External APIs**: Modify `server/src/services/apiService.js`.

## Extending the Application

### Adding New API Endpoints

1.  **Create a controller**: Add a new function in `server/src/controllers/`.
2.  **Define a route**: Add a new route in `server/src/routes/api.js` and link it to the controller function.

### Adding New Vue Components

1.  **Create a component**: Add a new `.vue` file in `webapp/src/components/`.
2.  **Add a route**: If it's a new page, add a route in `webapp/src/router/index.js`.
3.  **Use Pinia for state**: For global state management, extend the store in `webapp/src/store/index.js`.
