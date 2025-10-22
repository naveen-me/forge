<?php
require_once __DIR__ . '/../config.php';

$tables = [
    'customers' => [
        'id' => 'INT NOT NULL AUTO_INCREMENT PRIMARY KEY',
        'name' => 'TEXT NOT NULL',
        'mobile' => 'TEXT NOT NULL',
        'mobile2' => 'TEXT',
        'address' => 'TEXT NOT NULL',
        'balance' => 'INT NOT NULL',
        'lastpaidon' => 'DATETIME NOT NULL',
    ],
    'users' => [
        'id' => 'INT NOT NULL AUTO_INCREMENT PRIMARY KEY',
        'name' => 'VARCHAR(255) NOT NULL',
        'email' => 'VARCHAR(255) NOT NULL UNIQUE',
        'mobile' => 'VARCHAR(20)',
        'password' => 'VARCHAR(255) NOT NULL',
        'authToken' => 'VARCHAR(500)',  // Added for JWT token storage
        'company_name' => 'VARCHAR(255)',  // Added for onboarding
        'address' => 'TEXT',  // Added for onboarding
        'gstin' => 'VARCHAR(50)',  // Added for onboarding
        'channel_logo' => 'VARCHAR(255)',  // Added for onboarding
        'onboarding_status' => 'INT DEFAULT 0',  // 0=not started, 1=completed
        'uType' => 'INT NOT NULL DEFAULT 1',
        'status' => 'INT NOT NULL DEFAULT 1',
        'resetKey' => 'VARCHAR(255)',
    ],
    'plans' => [
        'id' => 'INT NOT NULL AUTO_INCREMENT PRIMARY KEY',
        'name' => 'VARCHAR(255) NOT NULL',
        'price' => 'DECIMAL(10, 2) NOT NULL',
        'duration_days' => 'INT NOT NULL',
    ],
    'subscriptions' => [
        'id' => 'INT NOT NULL AUTO_INCREMENT PRIMARY KEY',
        'user_id' => 'INT NOT NULL',
        'plan_id' => 'INT NOT NULL',
        'start_date' => 'DATETIME NOT NULL',
        'end_date' => 'DATETIME NOT NULL',
        'status' => 'VARCHAR(50) NOT NULL',
        'payment_method' => 'VARCHAR(50)',
        'upi_id' => 'VARCHAR(100)',
        'transaction_id' => 'VARCHAR(100)',
        'payment_status' => 'VARCHAR(50) DEFAULT \'pending\'',
        'FOREIGN KEY (user_id)' => 'REFERENCES users(id)',
        'FOREIGN KEY (plan_id)' => 'REFERENCES plans(id)',
    ]
];



foreach ($tables as $tableName => $columns) {
    createOrUpdateTable($tableName, $columns);
}

// Handle the subscriptions table separately to include foreign keys
$subsTableName = 'subscriptions';
$subsColumns = [
    'id' => 'INT NOT NULL AUTO_INCREMENT PRIMARY KEY',
    'user_id' => 'INT NOT NULL',
    'plan_id' => 'INT NOT NULL',
    'start_date' => 'DATETIME NOT NULL',
    'end_date' => 'DATETIME NOT NULL',
    'status' => 'VARCHAR(50) NOT NULL',
    'created_at' => 'TIMESTAMP DEFAULT CURRENT_TIMESTAMP',
    'updated_at' => 'TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP',
    'isDeleted' => 'TINYINT(1) DEFAULT 0',
    'deleted_at' => 'TIMESTAMP NULL'
];

$subsConstraints = [
    'FOREIGN KEY (user_id) REFERENCES users(id)',
    'FOREIGN KEY (plan_id) REFERENCES plans(id)'
];

// Check if the table exists
$tableExists = $db->query("SHOW TABLES LIKE :table", [':table' => $subsTableName])->fetch();

if (!$tableExists) {
    $sql = "CREATE TABLE `$subsTableName` (";
    $columnParts = [];
    foreach ($subsColumns as $name => $type) {
        $columnParts[] = "`$name` $type";
    }
    $sql .= implode(', ', $columnParts);
    $sql .= ", " . implode(', ', $subsConstraints);
    $sql .= ");";

    try {
        $db->query($sql);
        echo "<p>Table `$subsTableName` created successfully with foreign keys.</p>";
    } catch (Exception $e) {
        echo "<p>Error creating table `$subsTableName`: " . $e->getMessage() . "</p>";
    }
}

// Create additional tables for features and payment
$additionalTables = [
    'features' => [
        'id' => 'INT NOT NULL AUTO_INCREMENT PRIMARY KEY',
        'name' => 'VARCHAR(255) NOT NULL',
        'description' => 'TEXT',
        'price' => 'DECIMAL(10, 2) NOT NULL',
        'created_at' => 'TIMESTAMP DEFAULT CURRENT_TIMESTAMP',
        'updated_at' => 'TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP',
    ],
    'features' => [
        'id' => 'INT NOT NULL AUTO_INCREMENT PRIMARY KEY',
        'name' => 'VARCHAR(255) NOT NULL',
        'description' => 'TEXT',
        'price' => 'DECIMAL(10, 2) NOT NULL',
        'is_active' => 'TINYINT(1) DEFAULT 1',
        'created_at' => 'TIMESTAMP DEFAULT CURRENT_TIMESTAMP',
        'updated_at' => 'TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP',
    ],
    'plan_features' => [
        'id' => 'INT NOT NULL AUTO_INCREMENT PRIMARY KEY',
        'plan_id' => 'INT NOT NULL',
        'feature_id' => 'INT NOT NULL',
        'created_at' => 'TIMESTAMP DEFAULT CURRENT_TIMESTAMP',
    ],
    'user_features' => [
        'id' => 'INT NOT NULL AUTO_INCREMENT PRIMARY KEY',
        'user_id' => 'INT NOT NULL',
        'feature_id' => 'INT NOT NULL',
        'purchase_date' => 'DATE NOT NULL',
        'created_at' => 'TIMESTAMP DEFAULT CURRENT_TIMESTAMP',
    ],
    'payments' => [
        'id' => 'INT NOT NULL AUTO_INCREMENT PRIMARY KEY',
        'user_id' => 'INT NOT NULL',
        'payment_type' => 'VARCHAR(50) NOT NULL', // subscription or feature
        'item_id' => 'INT NOT NULL', // plan_id or feature_id
        'amount' => 'DECIMAL(10, 2) NOT NULL',
        'currency' => 'VARCHAR(3) DEFAULT \'INR\'',
        'payment_method' => 'VARCHAR(50) NOT NULL', // upi
        'transaction_id' => 'VARCHAR(100) UNIQUE',
        'upi_qr_data' => 'TEXT',
        'status' => 'VARCHAR(50) NOT NULL DEFAULT \'pending\'',
        'payment_date' => 'DATETIME NULL',
        'verification_date' => 'DATETIME NULL',
        'expires_at' => 'DATETIME NULL',
        'created_at' => 'TIMESTAMP DEFAULT CURRENT_TIMESTAMP',
        'updated_at' => 'TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP',
    ],
    'payment_verification' => [
        'id' => 'INT NOT NULL AUTO_INCREMENT PRIMARY KEY',
        'payment_id' => 'INT NOT NULL',
        'verification_status' => 'VARCHAR(50) NOT NULL',
        'verification_response' => 'TEXT',
        'verified_by' => 'VARCHAR(50) NOT NULL', // manual or system
        'verified_at' => 'DATETIME NULL',
        'notes' => 'TEXT',
    ],
    'payment_logs' => [
        'id' => 'INT NOT NULL AUTO_INCREMENT PRIMARY KEY',
        'payment_id' => 'INT NOT NULL',
        'action' => 'VARCHAR(100) NOT NULL',
        'details' => 'TEXT',
        'timestamp' => 'TIMESTAMP DEFAULT CURRENT_TIMESTAMP',
    ],
    'system_upi_details' => [
        'id' => 'INT NOT NULL AUTO_INCREMENT PRIMARY KEY',
        'upi_id' => 'VARCHAR(255) UNIQUE NOT NULL',
        'upi_vpa' => 'VARCHAR(255) NOT NULL',
        'display_name' => 'VARCHAR(255)',
        'is_primary' => 'TINYINT(1) DEFAULT 0',
        'is_active' => 'TINYINT(1) DEFAULT 1',
        'created_at' => 'TIMESTAMP DEFAULT CURRENT_TIMESTAMP',
        'updated_at' => 'TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP',
    ]
];

foreach ($additionalTables as $tableName => $columns) {
    createOrUpdateTable($tableName, $columns);
}

function createOrUpdateTable($tableName, $columns) {
    global $db;
    
    // Check if the table exists
    $stmt = $db->query("SHOW TABLES LIKE :table", [':table' => $tableName]);
    $tableExists = $stmt->fetch();
    
    if (!$tableExists) {
        $sql = "CREATE TABLE `$tableName` (";
        $columnParts = [];
        $constraintParts = [];
        
        foreach ($columns as $name => $definition) {
            if (strpos($name, 'FOREIGN KEY') === 0) {
                $constraintParts[] = $definition;
            } else {
                $columnParts[] = "`$name` $definition";
            }
        }
        
        $sql .= implode(', ', $columnParts);
        
        if (!empty($constraintParts)) {
            $sql .= ', ' . implode(', ', $constraintParts);
        }
        
        $sql .= ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
        
        try {
            $db->query($sql);
            echo "<p>Table `$tableName` created successfully.</p>";
        } catch (Exception $e) {
            echo "<p>Error creating table `$tableName`: " . $e->getMessage() . "</p>";
        }
    } else {
        // Table exists, check if we need to add any new columns
        $existingColumns = $db->query("DESCRIBE `$tableName`")->fetchAll();
        $existingColumnNames = array_column($existingColumns, 'Field');
        
        foreach ($columns as $name => $definition) {
            if (strpos($name, 'FOREIGN KEY') !== 0 && !in_array($name, $existingColumnNames)) {
                $sql = "ALTER TABLE `$tableName` ADD COLUMN `$name` $definition";
                try {
                    $db->query($sql);
                    echo "<p>Added column `$name` to table `$tableName`.</p>";
                } catch (Exception $e) {
                    echo "<p>Error adding column `$name` to table `$tableName`: " . $e->getMessage() . "</p>";
                }
            }
        }
    }
}
?>
