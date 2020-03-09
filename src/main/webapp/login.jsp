<%--
  Created by IntelliJ IDEA.
  User: Sophia Hu
  Date: 3/8/2020
  Time: 9:46 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<html>
<html lang="en">
<head>
    <link rel="stylesheet" type="text/css" href="css/login.css">
    <link href="https://fonts.googleapis.com/css?family=Open+Sans&display=swap" rel="stylesheet">
    <meta charset="UTF-8">
    <title>Log In Page</title>
</head>
<body>

<div id="header"> <h1> USC SERC Lab  </h1></div>

<form id = "loginBox" onsubmit="return sendData()">

    <h2>Log in to your account</h2>
    <p>
        <!-- TODO: Add a label -->
        <input id="email" name="email" type="text" placeholder="Email">
    </p>

    <p>
        <!-- TODO: Add a label -->
        <input id="password" name="password" type="password" placeholder="Password">
    <div class = "right"> <a href="/reset_password.jsp">Forgot password?</a> </div>
    </p>

    <p>
        <input id="submit" type="button" value="Log In" onclick="sendData();"/>
    </p>
    <div id = "signup" align="center">
        Need an account? <a href="signup.html">Sign up</a>
    </div>

</form>

</body>

<script>
    function sendData(){
        var email = document.querySelector('#email');
        var pw = document.querySelector('#password');

        // Converting JSON data to string
        var data = JSON.stringify({ header: email.value, body: pw.value });
        console.log(data);

        // Creating a XHR object
        var xhr = new XMLHttpRequest();
        xhr.open("POST", "/Login?signOrLog=login", false);

        // Sending data with the request
        xhr.send(data);

        console.log("RESP: " + xhr.response);

        var response = JSON.parse(xhr.response); //Could check and see if request was successful
        console.log(response);
        if(response.header != "LoggedIn") {
            window.location = "/login.html";
        } else {
            sessionStorage.setItem("userID", response.body);
            console.log(sessionStorage.getItem("userID"));

            var isAdmin = '<%= session.getAttribute("isAdmin") %>';
            if (isAdmin) {
                window.location = "/dashboard_s_band.html";
            } else {
                window.location = "/user_dashboards_band.html";
            }
        }
    }
</script>
</html>