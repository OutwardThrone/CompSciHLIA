<?php
require "DataBase.php";
$db = new DataBase();
if (isset($_POST['username']) && isset($_POST['password'])) {
    if ($db->dbConnect()) {
        $res = $db->logIn("users", addslashes($_POST['username']), addslashes($_POST['password']));
        if ($res != null) {
            echo "Login Success " . $res;
        } else echo "Username or Password wrong";
    } else echo "Error: Database connection";
} else echo "All fields are required";
?>
