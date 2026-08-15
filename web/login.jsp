<%@page contentType="text/html" pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">

    <meta name="viewport"
          content="width=device-width, initial-scale=1.0">

    <title>Login - Sunrise Dental Clinic</title>

    <style>

        * {
            box-sizing: border-box;
        }

        body {
            margin: 0;
            font-family: Arial, sans-serif;
            background: #f4f7fb;
        }

        .page-container {
            min-height: 100vh;
            display: flex;
            justify-content: center;
            align-items: center;
            padding: 30px;
        }

        .login-box {
            width: 100%;
            max-width: 470px;
            background: white;
            padding: 40px;
            border-radius: 14px;
            box-shadow: 0 8px 25px rgba(0,0,0,0.1);
        }

        .logo {
            text-align: center;
            font-size: 26px;
            font-weight: bold;
            margin-bottom: 10px;
        }

        h1 {
            text-align: center;
            font-size: 32px;
            margin-bottom: 10px;
        }

        .subtitle {
            text-align: center;
            color: #666;
            margin-bottom: 30px;
        }

        .form-group {
            margin-bottom: 20px;
        }

        label {
            display: block;
            margin-bottom: 8px;
            font-weight: bold;
        }

        input {
            width: 100%;
            padding: 13px;
            border: 1px solid #ccc;
            border-radius: 7px;
            font-size: 15px;
        }

        input:focus {
            outline: none;
            border-color: #1f6feb;
        }

        button {
            width: 100%;
            padding: 14px;
            border: none;
            border-radius: 7px;
            background: #1f6feb;
            color: white;
            font-size: 17px;
            cursor: pointer;
        }

        button:hover {
            opacity: 0.9;
        }

        .message {
            padding: 11px;
            border-radius: 7px;
            text-align: center;
            margin-bottom: 20px;
        }

        .error {
            background: #ffe5e5;
            color: #b00020;
        }

        .success {
            background: #e5f7e8;
            color: #176b2c;
        }

        .signup-link {
            text-align: center;
            margin-top: 22px;
        }

        .signup-link a {
            text-decoration: none;
            color: #1f6feb;
            font-weight: bold;
        }

    </style>
</head>

<body>

<div class="page-container">

    <div class="login-box">

        <div class="logo">
            Sunrise Dental Clinic
        </div>

        <h1>Staff Login</h1>

        <p class="subtitle">
            Login to access the clinic management system
        </p>

        <%
            String error = request.getParameter("error");
            String registered = request.getParameter("registered");

            if ("invalid".equals(error)) {
        %>

        <div class="message error">
            Invalid email or password.
        </div>

        <%
            } else if ("required".equals(error)) {
        %>

        <div class="message error">
            Please enter your email and password.
        </div>

        <%
            } else if ("database".equals(error)) {
        %>

        <div class="message error">
            Database error. Please try again.
        </div>

        <%
            }

            if ("true".equals(registered)) {
        %>

        <div class="message success">
            Account created successfully. Please login.
        </div>

        <%
            }
        %>

        <form
            action="${pageContext.request.contextPath}/LoginServlet"
            method="post">

            <div class="form-group">

                <label for="username">
                    Email Address
                </label>

                <input
                    type="email"
                    id="username"
                    name="username"
                    placeholder="Enter email address"
                    required>

            </div>

            <div class="form-group">

                <label for="password">
                    Password
                </label>

                <input
                    type="password"
                    id="password"
                    name="password"
                    placeholder="Enter password"
                    required>

            </div>

            <button type="submit">
                Login
            </button>

        </form>

        <div class="signup-link">
            Don't have an account?
            <a href="signup.jsp">Create Account</a>
        </div>

    </div>

</div>

</body>
</html>