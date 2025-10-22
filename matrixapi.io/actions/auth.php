<?php
require_once __DIR__ . '/../vendor/autoload.php';
use Firebase\JWT\JWT;

if (!isset($task)) {
    http_response_code(400);
    echo json_encode(['success' => false, 'message' => 'Task not specified for auth action']);
    exit();
}

switch ($task) {
    case 'register':
        handle_register($data, $db);
        break;
    case 'login':
        handle_login($data, $db);
        break;
    case 'verify-token':
        handle_verify_token($data, $db);
        break;
    default:
        http_response_code(400);
        echo json_encode(['success' => false, 'message' => 'Invalid auth task']);
        break;
}

function handle_register($data, $db) {
    $name = $data['name'] ?? null;
    $email = $data['email'] ?? null;
    $password = $data['password'] ?? null;

    if (!$name || !$email || !$password) {
        http_response_code(400);
        echo json_encode(['success' => false, 'message' => 'Name, email, and password are required']);
        return;
    }

    if ($db->has('users', ['email' => $email])) {
        http_response_code(409);
        echo json_encode(['success' => false, 'message' => 'User with this email already exists']);
        return;
    }

    $password_hash = password_hash($password, PASSWORD_BCRYPT);

    $db->insert('users', [
        'name' => $name,
        'email' => $email,
        'password' => $password_hash,
    ]);

    echo json_encode(['success' => true, 'message' => 'User registered successfully']);
}

function handle_login($data, $db) {
    $email = $data['email'] ?? null;
    $password = $data['password'] ?? null;

    if (!$email || !$password) {
        http_response_code(400);
        echo json_encode(['success' => false, 'message' => 'Email and password are required']);
        return;
    }

    $user = $db->get('users', '*', ['email' => $email]);

    if (!$user || !password_verify($password, $user['password'])) {
        http_response_code(401);
        echo json_encode(['success' => false, 'message' => 'Invalid credentials']);
        return;
    }

    $jwt_secret = $_ENV['JWT_SECRET'] ?? null;
    
    if (empty($jwt_secret)) {
        // Generate a temporary token without JWT if secret is not configured
        $jwt = bin2hex(random_bytes(32)); // Generate a random token
        $db->update('users', ['authToken' => $jwt], ['id' => $user['id']]);
        echo json_encode(['success' => true, 'token' => $jwt]);
        return;
    }
    
    $payload = [
        'iss' => "your-app-name",
        'aud' => "your-app-name",
        'iat' => time(),
        'exp' => time() + (60*60*24), // 24 hours
        'data' => [
            'id' => $user['id'],
            'email' => $user['email'],
        ]
    ];

    $jwt = JWT::encode($payload, $jwt_secret, 'HS256');
    
    // Update the user's authToken in the database
    $db->update('users', ['authToken' => $jwt], ['id' => $user['id']]);

    echo json_encode(['success' => true, 'token' => $jwt]);
}

function handle_verify_token($data, $db) {
    $token = $data['token'] ?? null;
    $userIdFromToken = $data['userId'] ?? null;
    $email = $data['email'] ?? null;

    if (!$token) {
        http_response_code(400);
        echo json_encode(['success' => false, 'message' => 'Token is required']);
        return;
    }

    try {
        $jwt_secret = $_ENV['JWT_SECRET'] ?? null;
        
        if (empty($jwt_secret)) {
            // If no JWT secret, try to match against stored authToken
            $user = $db->get('users', ['id', 'email'], [
                'authToken' => $token
            ]);
            
            if ($user && $user['id'] == $userIdFromToken && $user['email'] == $email) {
                echo json_encode(['success' => true, 'message' => 'Token verified']);
                return;
            }
        } else {
            // Decode JWT and verify
            $decoded = JWT::decode($token, new \Firebase\JWT\Key($jwt_secret, 'HS256'));
            $decodedUserId = $decoded->data->id ?? null;
            
            if ($decodedUserId == $userIdFromToken) {
                // Verify user still exists
                $user = $db->get('users', ['id', 'email'], [
                    'id' => $decodedUserId
                ]);
                
                if ($user && $user['email'] == $email) {
                    echo json_encode(['success' => true, 'message' => 'Token verified']);
                    return;
                }
            }
        }
        
        http_response_code(401);
        echo json_encode(['success' => false, 'message' => 'Invalid token']);
    } catch (Exception $e) {
        http_response_code(401);
        echo json_encode(['success' => false, 'message' => 'Invalid token: ' . $e->getMessage()]);
    }
}
