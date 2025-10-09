# Backend README

## Database Migrations

This project uses Sequelize for database migrations. All migrations are stored in the `migrations/` directory.

### Running Migrations

To run pending migrations:

```bash
# From the backend directory
npm run migrate

# Or from the project root
npm run migrate
```

### Rolling Back Migrations

To undo the last migration:

```bash
# From the backend directory
npm run migrate:undo

# To undo all migrations
npm run migrate:undo:all
```

### Creating New Migrations

To create a new migration:

```bash
# From the backend directory
npx sequelize-cli migration:generate --name create-your-table-name
```

### Seeding Data

To seed the database with initial data:

```bash
# From the backend directory
npm run seed
```

To undo all seeders:

```bash
# From the backend directory
npm run seed:undo
```

### Current Database Schema

The application currently has the following tables:

- MediaLibraries: Stores media files with metadata
- Folders: Organizes media files in folders
- Overlays: Stores overlay elements for scenes
- SceneTemplates: Defines scene templates
- Ads: Stores ad files and settings
- AdGroups: Groups ads together
- Schedules: Contains scheduling information
- ScheduleItems: Individual items in schedules
- ScheduleItemOverlays: Links overlays to schedule items
- ScheduleItemCuePoints: Defines cue points in schedule items
- ScheduleItemAdPlacements: Defines where ads are placed in schedule items
- Auths: Stores authentication information
- SystemDefaults: Stores system-level default settings