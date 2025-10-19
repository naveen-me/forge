<?php
include 'config.php';
include 'constants.php';

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $data = json_decode(file_get_contents('php://input'), true);
    $data = (isset($data)) ? $data : $_POST;
    
    if(isset($data) && isset($data['action'])){
        $action = $data['action'];
        $task = isset($data['task']) ? $data['task'] : '';

        // Public tasks that don't require authentication
        $publicTasks = ['login', 'register', 'send-otp', 'verify-otp', 'reset-password'];

        // Check for token unless it's a public task
        if ($action === 'auth' && !in_array($task, $publicTasks)) {
            $token = isset($data['token']) ? $data['token'] : '';
            $isUser = $db->has("users", ["authToken" => $token]);
            if (!$isUser) {
                http_response_code(401);
                echo json_encode(['success' => false, 'message' => 'Unauthorized']);
                exit();
            }
        }

        // Include the appropriate action file
        if ($action === 'auth') {
            include __DIR__ . '/auth.php';
        } else {
            $target_file = str_replace('action-', '', $action) . '.php';
            $file_path = __DIR__ . '/' . $target_file;

            if (file_exists($file_path)) {
                include $file_path;
            } else {
                http_response_code(404);
                echo json_encode(['success' => false, 'message' => 'Action not found']);
            }
        }
    } else {
        http_response_code(400);
        echo json_encode(['success' => false, 'message' => 'No action specified']);
    }
} else {
    http_response_code(405);
    echo json_encode(['success' => false, 'message' => 'Invalid request method']);
}
?>
