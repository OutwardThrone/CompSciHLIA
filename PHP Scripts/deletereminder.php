<?php
require "DataBase.php";
$db = new DataBase();
if (isset($_POST['title']) && isset($_POST['description']) && isset($_POST['username'])) {
    if ($db->dbConnect()) {
        if ($db->deleteReminder(addslashes($_POST['username']), addslashes($_POST['title']), addslashes($_POST['description']))) { 
            echo "Deleted Reminder Successfully";
        } else echo "Error deleting reminder";
    } else echo "Error: Database connection";
} else echo "All fields are required";
?>