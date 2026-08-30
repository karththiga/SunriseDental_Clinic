#!/usr/bin/env bash
set -euo pipefail

project_dir="$(cd "$(dirname "$0")/.." && pwd)"
output_dir="${TMPDIR:-/tmp}/sunrise-dental-tests"
mkdir -p "$output_dir"

javac -d "$output_dir" \
  "$project_dir/src/java/Appointment.java" \
  "$project_dir/src/java/AppointmentValidationHandler.java" \
  "$project_dir/src/java/RequiredFieldHandler.java" \
  "$project_dir/src/java/PhoneValidationHandler.java" \
  "$project_dir/src/java/BillCharge.java" \
  "$project_dir/src/java/TreatmentCharge.java" \
  "$project_dir/src/java/ChargeDecorator.java" \
  "$project_dir/src/java/ConsultationFeeDecorator.java" \
  "$project_dir/src/java/PasswordUtil.java" \
  "$project_dir/src/java/DummyPaymentGateway.java" \
  "$project_dir/src/java/PaymentSummary.java" \
  "$project_dir/src/java/DBConnection.java" \
  "$project_dir/src/java/ScheduleEntry.java" \
  "$project_dir/src/java/ClinicScheduleRepository.java" \
  "$project_dir/src/java/ClinicScheduleService.java" \
  "$project_dir/test/AutomatedTestSuite.java"

java -ea -cp "$output_dir" AutomatedTestSuite
