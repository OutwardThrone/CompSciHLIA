<?php
require "DataBase.php";
$db = new DataBase();
if (isset($_POST['title']) && isset($_POST['description']) && isset($_POST['completed']) && isset($_POST['username'])) {
    if ($db->dbConnect()) {
        if ($db->updateCompleted(addslashes($_POST['username']), addslashes($_POST['title']), addslashes($_POST['description']), addslashes($_POST['completed']))) {
            echo "Successfully updated";
        } else echo "Error updating reminder";
    } else echo "Error: Database connection";
} else echo "All fields are required";
?>