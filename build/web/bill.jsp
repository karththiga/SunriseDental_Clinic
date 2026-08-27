<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%
    String username =
            (String) session.getAttribute("username");

    if (username == null) {

        response.sendRedirect(
                "login.jsp"
        );

        return;
    }

    String error =
            (String) request.getAttribute("error");
     Boolean billGenerated =
            (Boolean) request.getAttribute("billGenerated");


   
%>

<!DOCTYPE html>

<html>

<head>

    <meta charset="UTF-8">

    <meta name="viewport"
          content="width=device-width,
          initial-scale=1.0">

    <title>
        Billing - Sunrise Dental Clinic
    </title>


    <style>

        * {
            box-sizing: border-box;
        }


        body {

            margin: 0;

            font-family:
                Arial,
                sans-serif;

            background:
                #f4f7fb;

            color:
                #222;
        }


        .navbar {

            background:
                #1f6feb;

            color:
                white;

            padding:
                18px 40px;

            display:
                flex;

            justify-content:
                space-between;

            align-items:
                center;
        }


        .navbar-title {

            font-size:
                23px;

            font-weight:
                bold;
        }


        .navbar a {

            color:
                white;

            text-decoration:
                none;

            font-weight:
                bold;
        }


        .container {

            max-width:
                800px;

            margin:
                40px auto;

            padding:
                0 20px;
        }


        .billing-box {

            background:
                white;

            padding:
                35px;

            border-radius:
                12px;

            box-shadow:
                0 5px 20px
                rgba(0,0,0,0.08);
        }


        h1 {

            text-align:
                center;

            margin-top:
                0;
        }


        .subtitle {

            text-align:
                center;

            color:
                #666;

            margin-bottom:
                30px;
        }


        .bill-form {

            display:
                flex;

            gap:
                10px;
        }


        .bill-form input {

            flex:
                1;

            padding:
                13px;

            border:
                1px solid #ccc;

            border-radius:
                7px;

            font-size:
                16px;
        }


        .bill-form button {

            padding:
                13px 25px;

            border:
                none;

            border-radius:
                7px;

            background:
                #1f6feb;

            color:
                white;

            font-size:
                16px;

            cursor:
                pointer;
        }


        .error {

            background:
                #ffe5e5;

            color:
                #b00020;

            padding:
                12px;

            margin-top:
                20px;

            text-align:
                center;

            border-radius:
                7px;
        }


        .receipt {

            background:
                white;

            margin-top:
                25px;

            padding:
                35px;

            border-radius:
                12px;

            box-shadow:
                0 5px 20px
                rgba(0,0,0,0.08);
        }


        .receipt-header {

            text-align:
                center;

            border-bottom:
                2px solid #ddd;

            padding-bottom:
                20px;

            margin-bottom:
                20px;
        }


        .receipt-header h2 {

            margin:
                0 0 5px 0;
        }


        .row {

            display:
                flex;

            justify-content:
                space-between;

            padding:
                10px 0;

            border-bottom:
                1px solid #eee;
        }


        .label {

            font-weight:
                bold;
        }


        .total {

            font-size:
                20px;

            font-weight:
                bold;

            margin-top:
                10px;

            border-top:
                2px solid #333;

            padding-top:
                15px;
        }


        .print-btn {

            display:
                block;

            width:
                100%;

            margin-top:
                25px;

            padding:
                13px;

            background:
                #1f6feb;

            border:
                none;

            border-radius:
                7px;

            color:
                white;

            font-size:
                16px;

            cursor:
                pointer;
        }


        .back-link {

            display:
                block;

            text-align:
                center;

            margin-top:
                25px;

            color:
                #1f6feb;

            text-decoration:
                none;

            font-weight:
                bold;
        }


        @media print {

            .navbar,
            .billing-box,
            .print-btn,
            .back-link {

                display:
                    none;
            }


            body {

                background:
                    white;
            }


            .receipt {

                box-shadow:
                    none;

                margin:
                    0;
            }
        }

    </style>

</head>


<body>


<div class="navbar">

    <div class="navbar-title">
        Sunrise Dental Clinic
    </div>

    <a href="dashboard.jsp">
        Dashboard
    </a>

</div>


<div class="container">


    <div class="billing-box">

        <h1>
            Generate Patient Bill
        </h1>

        <p class="subtitle">
            Enter the appointment number
            to calculate the bill.
        </p>


        <form
            class="bill-form"
            action="${pageContext.request.contextPath}/BillServlet"
            method="post">


            <input
                type="text"
                name="appointmentNumber"
                placeholder="Example: APT001"
                required>


            <button type="submit">
                Generate Bill
            </button>


        </form>


        <%
            if (error != null) {
        %>

        <div class="error">
            <%= error %>
        </div>

        <%
            }
        %>

    </div>



    <%
        if (Boolean.TRUE.equals(billGenerated)) {
    %>


    <div class="receipt">


        <div class="receipt-header">

            <h2>
                Sunrise Dental Clinic
            </h2>

            <p>
                Patient Bill / Receipt
            </p>

        </div>


        <div class="row">

            <span class="label">
                Appointment No:
            </span>

            <span>
                <%= request.getAttribute("appointmentNumber") %>
            </span>

        </div>


        <div class="row">

            <span class="label">
                Patient:
            </span>

            <span>
                <%= request.getAttribute("patientName") %>
            </span>

        </div>


        <div class="row">

            <span class="label">
                Contact:
            </span>

            <span>
                <%= request.getAttribute("contactNumber") %>
            </span>

        </div>


        <div class="row">

            <span class="label">
                Dentist:
            </span>

            <span>
                <%= request.getAttribute("dentistName") %>
            </span>

        </div>


        <div class="row">

            <span class="label">
                Treatment:
            </span>

            <span>
                <%= request.getAttribute("treatmentName") %>
            </span>

        </div>


        <div class="row">

            <span class="label">
                Appointment Date:
            </span>

            <span>
                <%= request.getAttribute("appointmentDate") %>
            </span>

        </div>


        <div class="row">

            <span class="label">
                Treatment Cost:
            </span>

            <span>
                Rs.
               <%= String.format(
        "%.2f",
        (Double) request.getAttribute("treatmentCost")
) %>
            </span>

        </div>


        <div class="row">

            <span class="label">
                Consultation Fee:
            </span>

            <span>
                Rs.
               <%= String.format(
        "%.2f",
        (Double) request.getAttribute("consultationFee")
) %>
            </span>

        </div>


        <div class="row total">

            <span>
                Total Amount:
            </span>

            <span>
                Rs.
                <%= String.format(
        "%.2f",
        (Double) request.getAttribute("totalAmount")
) %>
            </span>

        </div>


        <button
            class="print-btn"
            onclick="window.print()">

            Print Receipt

        </button>


    </div>


    <%
        }
    %>


    <a href="dashboard.jsp"
       class="back-link">

        ← Back to Dashboard

    </a>


</div>


</body>

</html>