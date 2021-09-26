<?php
require "DataBase.php";
$db = new DataBase();
if (isset($_POST['title']) && isset($_POST['username']) && isset($_POST['description']) && isset($_POST['date']) && isset($_POST['weekly']) && isset($_POST['daily'])) {
    if ($db->dbConnect()) {
        if ($db->reminderUpload("reminders", addslashes($_POST['username']), addslashes($_POST['title']), addslashes($_POST['description']), addslashes($_POST['date']), addslashes($_POST['weekly']), addslashes($_POST['daily']))) { 
            echo "Added Reminder Successfully";
        } else echo "Error adding reminder";
    } else echo "Error: Database connection";
} else echo "All fields are required";
?>