<%@page contentType="text/html" pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html lang="en">

<head>

    <meta charset="UTF-8">

    <meta name="viewport"
          content="width=device-width, initial-scale=1.0">

    <title>Login - Hospital Management System</title>

    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/CSS/CSS.css">

</head>

<body class="modern-login-page">

<div class="login-overlay">

    <div class="modern-login-container">

        <!-- =========================
             LEFT SIDE
             ========================= -->

        <div class="login-welcome-section">

            <div class="welcome-content">

               <span class="welcome-badge">
    Hospital Management System
</span>

<h1>
    Better healthcare starts here.
</h1>

<p>
    Sign in to manage patients, doctors,
    appointments and hospital services efficiently.
</p>

               <div class="welcome-feature">
    <span class="feature-icon">✓</span>
    Easy patient management
</div>

<div class="welcome-feature">
    <span class="feature-icon">✓</span>
    Doctor and appointment management
</div>

<div class="welcome-feature">
    <span class="feature-icon">✓</span>
    Secure healthcare information
</div>
                </div>

            </div>

        </div>


        <!-- =========================
             RIGHT SIDE - LOGIN
             ========================= -->

        <div class="modern-login-card">

            <div class="login-brand">
                CARE<span>PLUS</span>
            </div>

            <h2>
                Welcome Back
            </h2>

            <p class="login-description">
                Please enter your details to continue.
            </p>


            <!-- =========================
                 SIGNUP SUCCESS MESSAGE
                 ========================= -->

            <%
                String signupStatus =
                        request.getParameter("signup");

                if ("success".equals(signupStatus)) {
            %>

            <div class="signup-success-message">

                ✓ Account created successfully!

                <br>

                You can now login using your new account.

            </div>

            <%
                }
            %>


            <!-- =========================
                 LOGIN ERROR MESSAGE
                 ========================= -->

            <%
                String error =
                        request.getParameter("error");

                if ("invalid".equals(error)) {
            %>

            <div class="modern-error-message server-error">

                Incorrect email address or password.

            </div>

            <%
                } else if ("database".equals(error)) {
            %>

            <div class="modern-error-message server-error">

                Database connection failed.
                Please try again.

            </div>

            <%
                } else if ("driver".equals(error)) {
            %>

            <div class="modern-error-message server-error">

                Database driver could not be loaded.

            </div>

            <%
                }
            %>


            <!-- =========================
                 LOGIN FORM
                 ========================= -->

            <form
                action="${pageContext.request.contextPath}/LoginServlet"
                method="post"
                onsubmit="return validateLogin()">


                <!-- Email -->

                <div class="modern-form-group">

                    <label for="loginEmail">
                        Email Address
                    </label>

                    <div class="input-wrapper">

                        <span class="input-icon">
                            @
                        </span>

                        <input
                            type="email"
                            id="loginEmail"
                            name="username"
                            placeholder="Enter your email"
                            autocomplete="email"
                            required>

                    </div>

                </div>


                <!-- Password -->

                <div class="modern-form-group">

                    <label for="loginPassword">
                        Password
                    </label>

                    <div class="input-wrapper">

                        <span class="input-icon">
                            🔒
                        </span>

                        <input
                            type="password"
                            id="loginPassword"
                            name="password"
                            placeholder="Enter your password"
                            autocomplete="current-password"
                            required>


                        <button
                            type="button"
                            class="modern-password-toggle"
                            onclick="togglePassword(
                                'loginPassword',
                                this
                            )">

                            Show

                        </button>

                    </div>

                </div>


                <!-- Remember / Forgot -->

                <div class="modern-login-options">

                    <label class="modern-remember">

                        <input
                            type="checkbox"
                            name="remember">

                        <span>
                            Remember me
                        </span>

                    </label>

                    <a href="#"
                       class="forgot-link">

                        Forgot Password?

                    </a>

                </div>


                <!-- JavaScript validation error -->

                <p
                    id="loginError"
                    class="modern-error-message">
                </p>


                <!-- Login Button -->

                <button
                    type="submit"
                    class="modern-login-button">

                    <span>
                        Login to Your Account
                    </span>

                    <span class="button-arrow">
                        →
                    </span>

                </button>

            </form>


            <!-- Divider -->

            <div class="divider">

                <span>
                    or
                </span>

            </div>


            <!-- Sign Up -->

            <p class="modern-signup-text">

                New to Fashion Store?

                <a href="${pageContext.request.contextPath}/signup.jsp">

                    Create an Account

                </a>

            </p>


            <!-- Guest -->

            <a
                href="${pageContext.request.contextPath}/HomePage.jsp"
                class="modern-guest-link">

                Continue as Guest

            </a>


            <p class="login-security-text">

                🔒 Your information is safe and secure with us.

            </p>

        </div>

    </div>

</div>


<script src="${pageContext.request.contextPath}/JS/Jscript.js">
</script>

</body>

</html>