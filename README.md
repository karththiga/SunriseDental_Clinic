# Sunrise Dental Clinic

A Jakarta Servlet/JSP and MariaDB appointment, patient, schedule, and billing system created for the CIS6003 Advanced Programming assessment.

## Key features

- Public clinic home page with daily dentist schedules and live remaining-slot counts
- Patient signup/login, direct slot reservation, confirmation receipts, and appointment history
- Role-based Admin, Dentist, Cashier, and Patient dashboards
- Salted PBKDF2 password storage with transparent legacy-account migration
- Global appointment search, update/undo, admin cancellation and simulated refunds
- Unified people management with transactional dentist creation
- Treatment catalogue management
- Billing through Facade and Decorator patterns
- Printable management reports and staff help
- JSON schedule web service at `/api/schedules?date=YYYY-MM-DD`
- Seven documented design patterns and automated Java tests

## Database

For a new installation, import `db/sunrise_dental.sql`. For an existing database, apply `db/payment_features.sql` and then `db/appointment_cancellation_features.sql`. The cancellation migration adds refund audit fields and persistent patient notifications and is safe to run more than once. Connection settings are currently in `src/java/DBConnection.java`.

## Automated tests

```bash
bash scripts/run-tests.sh
```

See `docs/ARCHITECTURE.md`, `docs/TEST_PLAN.md`, `docs/REQUIREMENT_TRACEABILITY.md`, and `docs/uml/` for assessment evidence.
