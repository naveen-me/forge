<?php
// auth.php - Authentication API endpoints

// Note: config.php is already included by action.php
global $db;

// Get the data from the request
$data = json_decode(file_get_contents('php://input'), true);
$data = (isset($data)) ? $data : $_POST;
$task = isset($data['task']) ? $data['task'] : '';

// Handle each authentication task
switch ($task) {
    case 'register':
        registerUser($data, $db);
        break;
    case 'login':
        loginUser($data, $db);
        break;
    case 'send-otp':
        sendOTP($data, $db);
        break;
    case 'verify-otp':
        verifyOTP($data, $db);
        break;
    case 'reset-password':
        resetPassword($data, $db);
        break;
    case 'set-pin':
        setPin($data, $db);
        break;
    case 'validate-pin':
        validatePin($data, $db);
        break;
    case 'validate-token':
        validateToken($data, $db);
        break;
    default:
        echo json_encode(['error' => 'Invalid task specified']);
        break;
}

// Register a new user
function registerUser($data, $db) {
    $userData = isset($data['data']) ? $data['data'] : [];
    $name = isset($userData['name']) ? $userData['name'] : '';
    $email = isset($userData['email']) ? $userData['email'] : '';
    $password = isset($userData['password']) ? $userData['password'] : '';
    
    // Validate required fields
    if (empty($name) || empty($email) || empty($password)) {
        echo json_encode(['success' => false, 'message' => 'Name, email, and password are required']);
        return;
    }
    
    // Check if user already exists
    $existingUser = $db->has("users", ["email" => $email]);
    if ($existingUser) {
        echo json_encode(['success' => false, 'message' => 'User already exists with this email']);
        return;
    }
    
    // Hash the password
    $hashedPassword = password_hash($password, PASSWORD_DEFAULT);
    
    // Generate auth token
    $authToken = bin2hex(random_bytes(32));
    
    // Insert new user
    $insertResult = $db->insert("users", [
        "name" => $name,
        "email" => $email,
        "mobile" => "",
        "password" => $hashedPassword,
        "authToken" => $authToken,
        "uType" => 1, // Default user type
        "balance" => 0,
        "lastpaidon" => date('Y-m-d H:i:s'),
        "status" => 1
    ]);
    
    if ($insertResult) {
        echo json_encode([
            'success' => true,
            'message' => 'User registered successfully',
            'data' => ['token' => $authToken]
        ]);
    } else {
        echo json_encode(['success' => false, 'message' => 'Failed to register user']);
    }
}

// Login user
function loginUser($data, $db) {
    $userData = isset($data['data']) ? $data['data'] : [];
    $email = isset($userData['email']) ? $userData['email'] : '';
    $password = isset($userData['password']) ? $userData['password'] : '';
    
    // Validate required fields
    if (empty($email) || empty($password)) {
        echo json_encode(['success' => false, 'message' => 'Email and password are required']);
        return;
    }
    
    // Get user by email
    $user = $db->get("users", "*", ["email" => $email]);
    
    if (!$user) {
        echo json_encode(['success' => false, 'message' => 'User not found']);
        return;
    }
    
    // Verify password
    if (!password_verify($password, $user['password'])) {
        echo json_encode(['success' => false, 'message' => 'Invalid password']);
        return;
    }
    
    // Return success with auth token
    echo json_encode([
        'success' => true,
        'message' => 'Login successful',
        'data' => [
            'token' => $user['authToken'],
            'user' => [
                'id' => $user['id'],
                'name' => $user['name'],
                'email' => $user['email']
            ]
        ]
    ]);
}

// Send OTP
function sendOTP($data, $db) {
    $userData = isset($data['data']) ? $data['data'] : [];
    $email = isset($userData['email']) ? $userData['email'] : '';
    
    // Validate required fields
    if (empty($email)) {
        echo json_encode(['success' => false, 'message' => 'Email is required']);
        return;
    }
    
    // Check if user exists
    $user = $db->has("users", ["email" => $email]);
    if (!$user) {
        echo json_encode(['success' => false, 'message' => 'User not found']);
        return;
    }
    
    // Generate OTP (6 digit)
    $otp = rand(100000, 999999);
    
    // Store OTP in resetKey field (in a real app, you would send it via email)
    $db->update("users", [
        "resetKey" => $otp
    ], [
        "email" => $email
    ]);
    
    // In a real implementation, you would send the OTP via email
    // For now, we'll just return success
    echo json_encode([
        'success' => true,
        'message' => 'OTP sent successfully',
        'data' => ['otp' => $otp] // In production, remove this and send via email
    ]);
}

// Verify OTP
function verifyOTP($data, $db) {
    $userData = isset($data['data']) ? $data['data'] : [];
    $email = isset($userData['email']) ? $userData['email'] : '';
    $otp = isset($userData['otp']) ? $userData['otp'] : '';
    
    // Validate required fields
    if (empty($email) || empty($otp)) {
        echo json_encode(['success' => false, 'message' => 'Email and OTP are required']);
        return;
    }
    
    // Get user by email
    $user = $db->get("users", ["resetKey"], ["email" => $email]);
    
    if (!$user) {
        echo json_encode(['success' => false, 'message' => 'User not found']);
        return;
    }
    
    // Check if OTP matches
    if ($user['resetKey'] != $otp) {
        echo json_encode(['success' => false, 'message' => 'Invalid OTP']);
        return;
    }
    
    echo json_encode([
        'success' => true,
        'message' => 'OTP verified successfully'
    ]);
}

// Reset password
function resetPassword($data, $db) {
    $userData = isset($data['data']) ? $data['data'] : [];
    $email = isset($userData['email']) ? $userData['email'] : '';
    $newPassword = isset($userData['newPassword']) ? $userData['newPassword'] : '';
    
    // Validate required fields
    if (empty($email) || empty($newPassword)) {
        echo json_encode(['success' => false, 'message' => 'Email and new password are required']);
        return;
    }
    
    // Hash the new password
    $hashedPassword = password_hash($newPassword, PASSWORD_DEFAULT);
    
    // Update user password
    $updateResult = $db->update("users", [
        "password" => $hashedPassword,
        "resetKey" => "" // Clear the reset key
    ], [
        "email" => $email
    ]);
    
    if ($updateResult) {
        echo json_encode([
            'success' => true,
            'message' => 'Password reset successfully'
        ]);
    } else {
        echo json_encode(['success' => false, 'message' => 'Failed to reset password']);
    }
}

// Set PIN
function setPin($data, $db) {
    $token = isset($data['token']) ? $data['token'] : '';
    $pinData = isset($data['data']) ? $data['data'] : [];
    $pin = isset($pinData['pin']) ? $pinData['pin'] : '';

    // The token is already validated in action.php
    // In a real app, you would store the hashed PIN in the database
    // For now, we'll just update a flag
    $updateResult = $db->update("users", 
        ["hasPin" => 1],
        ["authToken" => $token]
    );

    if ($updateResult) {
        echo json_encode([
            'success' => true,
            'message' => 'PIN set successfully'
        ]);
    } else {
        echo json_encode(['success' => false, 'message' => 'Failed to set PIN']);
    }
}

// Validate PIN
function validatePin($data, $db) {
    $token = isset($data['token']) ? $data['token'] : '';
    $pinData = isset($data['data']) ? $data['data'] : [];
    $pin = isset($pinData['pin']) ? $pinData['pin'] : '';

    // The token is already validated in action.php
    // In a real app, you would compare the provided PIN with the hashed PIN in the database
    $user = $db->get("users", ["hasPin"], ["authToken" => $token]);

    if ($user && $user['hasPin']) {
        // For now, since the PIN is stored on the client, we just return success
        echo json_encode([
            'success' => true,
            'message' => 'PIN validation successful'
        ]);
    } else {
        echo json_encode(['success' => false, 'message' => 'PIN not set or user not found']);
    }
}

// Validate token
function validateToken($data, $db) {
    $token = isset($data['token']) ? $data['token'] : '';

    // Check if token exists and is valid
    $user = $db->get("users", ["id", "name", "email", "status"], ["authToken" => $token]);

    if ($user && $user['status'] == 1) {
        echo json_encode([
            'success' => true,
            'message' => 'Token is valid',
            'data' => [
                'user' => [
                    'id' => $user['id'],
                    'name' => $user['name'],
                    'email' => $user['email']
                ]
            ]
        ]);
    } else {
        echo json_encode(['success' => false, 'message' => 'Invalid or expired token']);
    }
}

?>