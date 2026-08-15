<%@page contentType="text/html" pageEncoding="UTF-8"%>

<!DOCTYPE html>

<html>

<head>

    <meta charset="UTF-8">

    <meta name="viewport"
          content="width=device-width,
          initial-scale=1.0">

    <title>
        Staff Registration - Sunrise Dental Clinic
    </title>

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

        .signup-box {
            width: 100%;
            max-width: 500px;
            background: white;
            padding: 35px;
            border-radius: 12px;
            box-shadow:
                0 8px 25px rgba(0,0,0,0.1);
        }

        .logo {
            text-align: center;
            font-size: 25px;
            font-weight: bold;
            margin-bottom: 8px;
        }

        h1 {
            text-align: center;
            margin-bottom: 8px;
        }

        .subtitle {
            text-align: center;
            color: #666;
            margin-bottom: 25px;
        }

        .form-row {
            display: flex;
            gap: 15px;
        }

        .form-group {
            margin-bottom: 18px;
            width: 100%;
        }

        label {
            display: block;
            margin-bottom: 7px;
            font-weight: bold;
        }

        input {
            width: 100%;
            padding: 12px;
            border: 1px solid #ccc;
            border-radius: 7px;
            font-size: 15px;
        }

        input:focus {
            outline: none;
            border-color: #555;
        }

        button {
            width: 100%;
            padding: 13px;
            border: none;
            border-radius: 7px;
            font-size: 16px;
            cursor: pointer;
            background: #1f6feb;
            color: white;
        }

        button:hover {
            opacity: 0.9;
        }

        .message {
            padding: 10px;
            border-radius: 6px;
            margin-bottom: 18px;
            text-align: center;
        }

        .error {
            background: #ffe5e5;
            color: #b00020;
        }

        .success {
            background: #e5f7e8;
            color: #176b2c;
        }

        .bottom-text {
            text-align: center;
            margin-top: 20px;
            color: #666;
        }

        @media (max-width: 600px) {

            .form-row {
                flex-direction: column;
                gap: 0;
            }
        }

    </style>

</head>

<body>

<div class="page-container">

    <div class="signup-box">

        <div class="logo">
            Sunrise Dental Clinic
        </div>

        <h1>
            Create Staff Account
        </h1>

        <p class="subtitle">
            Register to access the clinic management system
        </p>

        <%
            String error =
                    request.getParameter("error");
             String message =
                    request.getParameter("param");

            String success =
                    request.getParameter("success");

            if ("required".equals(error)) {
        %>

        <div class="message error">
            Please fill in all required fields.
        </div>

        <%
            } else if ("password".equals(error)) {
        %>

        <div class="message error">
            Password and confirm password
            do not match.
        </div>

        <%
            } else if ("length".equals(error)) {
        %>

        <div class="message error">
            Password must contain at least
            6 characters.
        </div>

        <%
            } else if ("exists".equals(error)) {
        %>

        <div class="message error">
            An account already exists
            with this email address.
        </div>

        <%
            } else if ("database".equals(error)) {
        %>

        <div class="message error">
            ${message}
        </div>

        <%
            } else if ("failed".equals(error)) {
        %>

        <div class="message error">
            Unable to create account.
        </div>

        <%
            }

            if ("true".equals(success)) {
        %>

        <div class="message success">
            Account created successfully.
        </div>

        <%
            }
        %>

        <form
            action="${pageContext.request.contextPath}/SignupServlet"
            method="post">

            <div class="form-row">

                <div class="form-group">

                    <label for="firstName">
                        First Name
                    </label>

                    <input
                        type="text"
                        id="firstName"
                        name="firstName"
                        placeholder="Enter first name"
                        required>

                </div>

                <div class="form-group">

                    <label for="lastName">
                        Last Name
                    </label>

                    <input
                        type="text"
                        id="lastName"
                        name="lastName"
                        placeholder="Enter last name"
                        required>

                </div>

            </div>

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

                <label for="phoneNumber">
                    Phone Number
                </label>

                <input
                    type="tel"
                    id="phoneNumber"
                    name="phoneNumber"
                    placeholder="Enter phone number"
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
                    minlength="6"
                    placeholder="Minimum 6 characters"
                    required>

            </div>

            <div class="form-group">

                <label for="confirmPassword">
                    Confirm Password
                </label>

                <input
                    type="password"
                    id="confirmPassword"
                    name="confirmPassword"
                    minlength="6"
                    placeholder="Re-enter password"
                    required>

            </div>

            <button type="submit">
                Create Account
            </button>

        </form>

        <div class="bottom-text">
            Sunrise Dental Clinic Management System
        </div>

    </div>

</div>

</body>

</html>