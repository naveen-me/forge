<?php
include 'config.php';

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
        'name' => 'TEXT NOT NULL',
        'email' => 'TEXT NOT NULL',
        'mobile' => 'TEXT NOT NULL',
        'password' => 'TEXT NOT NULL',
        'authToken' => 'TEXT NOT NULL',
        'uType' => 'INT NOT NULL',
        'balance' => 'DECIMAL(10,0) NOT NULL',
        'lastpaidon' => 'DATETIME NOT NULL',
        'status' => 'INT NOT NULL DEFAULT 1',
        'resetKey' => 'TEXT',
    ]
];



foreach ($tables as $tableName => $columns) {
    createOrUpdateTable($tableName, $columns);
}
?>
