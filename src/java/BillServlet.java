import java.io.IOException;
import java.sql.SQLException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/BillServlet")
public class BillServlet extends HttpServlet {

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        /*
         * Check login
         */
        HttpSession session =
                request.getSession(false);

        if (session == null
                || session.getAttribute(
                        "username"
                ) == null) {

            response.sendRedirect(
                    "login.jsp"
            );

            return;
        }


        String appointmentNumber =
                request.getParameter(
                        "appointmentNumber"
                );


        if (appointmentNumber == null
                || appointmentNumber
                        .trim()
                        .isEmpty()) {

            request.setAttribute(
                    "error",
                    "Please enter an appointment number."
            );

            request.getRequestDispatcher(
                    "bill.jsp"
            ).forward(
                    request,
                    response
            );

            return;
        }


        try {

            /*
             * FACade Design Pattern
             *
             * The Servlet calls only
             * one simple Facade method.
             */
            BillingFacade billingFacade =
                    new BillingFacade();

            BillResult bill =
                    billingFacade.generateBill(
                            appointmentNumber.trim()
                    );


            if (bill == null) {

                request.setAttribute(
                        "error",
                        "Appointment not found."
                );

            } else {

                request.setAttribute(
            "appointmentNumber",
            bill.getAppointmentNumber()
    );

    request.setAttribute(
            "patientName",
            bill.getPatientName()
    );

    request.setAttribute(
            "contactNumber",
            bill.getContactNumber()
    );

    request.setAttribute(
            "dentistName",
            bill.getDentistName()
    );

    request.setAttribute(
            "treatmentName",
            bill.getTreatmentName()
    );

    request.setAttribute(
            "appointmentDate",
            bill.getAppointmentDate()
    );

    request.setAttribute(
            "appointmentTime",
            bill.getAppointmentTime()
    );

    request.setAttribute(
            "treatmentCost",
            bill.getTreatmentCost()
    );

    request.setAttribute(
            "consultationFee",
            bill.getConsultationFee()
    );

    request.setAttribute(
            "totalAmount",
            bill.getTotalAmount()
    );

    request.setAttribute(
            "billGenerated",
            true
    );
}


        } catch (SQLException e) {

            e.printStackTrace();

            request.setAttribute(
                    "error",
                    "Unable to generate bill."
            );
        }


        request.getRequestDispatcher(
                "bill.jsp"
        ).forward(
                request,
                response
        );
    }
}