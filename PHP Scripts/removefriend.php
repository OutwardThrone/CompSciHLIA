<?php
require "DataBase.php";
$db = new DataBase();
if (isset($_POST['username']) && isset($_POST['friendid'])) {
    if ($db->dbConnect()) {
        if ($db->removeFriend(addslashes($_POST['username']), addslashes($_POST['friendid']))) {
            echo "Successfully removed friend";
        } else echo "Error removing friend";
    } else echo "Error: Database connection";
} else echo "All fields are required";
?>