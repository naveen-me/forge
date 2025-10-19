<?php

require_once("router.php");

get('/', 'home.php');
get('/upload', 'upload.php');
post('/api/v1/action', 'actions/action.php');
get('/migrate', 'dbms/migrations.php');
any('/404', '404.php');
