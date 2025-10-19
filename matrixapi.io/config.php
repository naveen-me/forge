<?php
require 'vendor/autoload.php';

$dotenv = Dotenv\Dotenv::createImmutable(__DIR__);
$dotenv->safeLoad();
$db_name = $_ENV['DB_DATABASE'];
$db_user = $_ENV['DB_USERNAME'];
$db_pass = $_ENV['DB_PASSWORD'];
// Using Medoo namespace.
use Medoo\Medoo;

global $db;
$db = new Medoo([
	// [required]
	'type' => 'mysql',
	'host' => 'localhost',
	'database' => $db_name,
	'username' => $db_user,
	'password' => $db_pass,

	// [optional]
// 	'charset' => 'utf8mb4',
// 	'collation' => 'utf8mb4_general_ci',
	'port' => 3306,

	// [optional] Table prefix, all table names will be prefixed as PREFIX_table.
	//'prefix' => 'PREFIX_',


	// [optional]
	// Error mode
	// Error handling strategies when error is occurred.
	// PDO::ERRMODE_SILENT (default) | PDO::ERRMODE_WARNING | PDO::ERRMODE_EXCEPTION
	// Read more from https://www.php.net/manual/en/pdo.error-handling.php.
	'error' => PDO::ERRMODE_SILENT,

	// [optional]
	// The driver_option for connection.
	// Read more from http://www.php.net/manual/en/pdo.setattribute.php.
	'option' => [
		PDO::ATTR_CASE => PDO::CASE_NATURAL
	],

	// [optional] Medoo will execute those commands after connected to the database.
	'command' => [
		'SET SQL_MODE=ANSI_QUOTES'
	]
]);

function uploadImage($file, $path = '/some/folder', $maxSize = 2097152) {
    // Check if the folder exists, if not create it
    if (!is_dir($path)) {
        if (!mkdir($path, 0777, true)) {
            return ['code' => 500, 'message' => 'Failed to create directory'];
        }
    }

    // Validate the file upload
    if (!isset($file) || $file['error'] !== UPLOAD_ERR_OK) {
        return ['code' => 400, 'message' => 'Upload File Size Too Large'];
    }

    // Get file details
    $fileTmpPath = $file['tmp_name'];
    $fileSize = $file['size'];
    $fileType = mime_content_type($fileTmpPath);
    $validImageTypes = ['image/jpeg', 'image/png', 'image/webp'];

    // Validate file type
    if (!in_array($fileType, $validImageTypes)) {
        return ['code' => 400, 'message' => 'Invalid image type'];
    }

    // Generate unique file name
    $fileExt = pathinfo($file['name'], PATHINFO_EXTENSION);
    $uniqueFileName = uniqid('image_', true) . '.' . $fileExt;
    $destinationPath = rtrim($path, '/') . '/' . $uniqueFileName;

    // Check if image needs to be resized
    if ($fileSize > $maxSize) {
        $image = null;
        switch ($fileType) {
            case 'image/jpeg':
                $image = imagecreatefromjpeg($fileTmpPath);
                break;
            case 'image/png':
                $image = imagecreatefrompng($fileTmpPath);
                break;
            case 'image/webp':
                $image = imagecreatefromwebp($fileTmpPath);
                break;
        }

        if ($image === null) {
            return ['code' => 500, 'message' => 'Failed to create image resource'];
        }

        // Compress the image and save it
        $quality = 75; // Adjust as necessary
        $compressed = false;

        switch ($fileType) {
            case 'image/jpeg':
                $compressed = imagejpeg($image, $destinationPath, $quality);
                break;
            case 'image/png':
                $compressed = imagepng($image, $destinationPath, 9 - ($quality / 10));
                break;
            case 'image/webp':
                $compressed = imagewebp($image, $destinationPath, $quality);
                break;
        }

        imagedestroy($image);

        if (!$compressed) {
            return ['code' => 500, 'message' => 'Failed to compress image'];
        }
    } else {
        // Simply move the uploaded file if no resizing is needed
        if (!move_uploaded_file($fileTmpPath, $destinationPath)) {
            return ['code' => 500, 'message' => 'Failed to move uploaded file'];
        }
    }

    return ['code' => 200, 'message' => 'File uploaded successfully', 'fileName' => $uniqueFileName];
}

function createOrUpdateTable(string $tableName, array $columns): string {
    global $db;

    // Ensure default columns are included
    $columns['created_at'] = 'TIMESTAMP DEFAULT CURRENT_TIMESTAMP';
    $columns['updated_at'] = 'TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP';
    $columns['isDeleted'] = 'TINYINT(1) DEFAULT 0';
    $columns['deleted_at'] = 'TIMESTAMP NULL';


    try {
        // Check if the table exists
        $existingTables = $db->query("SHOW TABLES LIKE :table", [
            ':table' => $tableName
        ])->fetchAll();

        if (empty($existingTables)) {
            // Table does not exist, create it
            $columnsSql = [];
            foreach ($columns as $columnName => $attributes) {
                $columnsSql[] = "`$columnName` $attributes";
            }
            $columnsSqlString = implode(", ", $columnsSql);

            $sql = "CREATE TABLE `$tableName` ($columnsSqlString)";
            $db->query($sql);

            return json_encode([
                'code' => 201,
                'message' => "Table `$tableName` created successfully."
            ]);
        } else {
            // Table exists, check and update columns
            $existingColumns = $db->query("SHOW COLUMNS FROM `$tableName`")->fetchAll(PDO::FETCH_COLUMN, 0);

            foreach ($columns as $columnName => $attributes) {
                if (!in_array($columnName, $existingColumns)) {
                    // Add the new column
                    $sql = "ALTER TABLE `$tableName` ADD `$columnName` $attributes";
                    $db->query($sql);
                }
            }

            return json_encode([
                'code' => 200,
                'message' => "Table `$tableName` updated successfully."
            ]);
        }
    } catch (Exception $e) {
        // Handle errors
        return json_encode([
            'code' => 500,
            'message' => "Error: " . $e->getMessage()
        ]);
    }
}