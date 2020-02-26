<%--
  Created by IntelliJ IDEA.
  User: mackenziemcclung
  Date: 2/18/20
  Time: 6:58 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
         pageEncoding="ISO-8859-1"%>
<!--The line below is commented out as it throws errors; but it needs to be fixed-->
<!--"%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %"-->
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <title>Reset Password</title>
</head>
<body>
    <!--The line below is commented out as it throws errors; but it needs to be fixed-->
    <!--jsp:directive.include file="header.jsp" /-->

    <div align="center">
        <h2>Reset Your Password</h2>
        <p>
            Please enter your login email, we'll send a new random password to your inbox:
        </p>

        <form id="resetForm" onsubmit="return sendEmail()">

            <div>Email:</div>
            <input type="text" name="email" id="email" size="20">
            <input type="button" value="Submit" onclick="sendEmail();"/>

        </form>
    </div>
    <!--The line below is commented out as it throws errors; but it needs to be fixed-->
    <!--jsp:directive.include file="footer.jsp" /-->
<script>
    function sendEmail() {
        var email = document.querySelector('#email');
        console.log(email.value);

        var data = JSON.stringify({ header: email.value, body: "" });

        // send email to backend POST method
        var xhr = new XMLHttpRequest();
        xhr.open("POST", "/ForgotPassword?email="+email.value,false);
        xhr.send(data);

    }

    // $(document).ready(function() {
    //     $("#resetForm").validate({
    //         rules: {
    //             email: {
    //                 required: true,
    //                 email: true
    //             }
    //         },
    //
    //         messages: {
    //             email: {
    //                 required: "Please enter email",
    //                 email: "Please enter a valid email address"
    //             }
    //         }
    //     });
    //
    // });
</script>

</body>
</html>

