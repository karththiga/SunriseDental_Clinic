import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.UUID;

/**
 * Simulated payment gateway for assessment/demo use only. It validates common
 * card fields and returns an authorization reference without contacting a bank.
 */
public class DummyPaymentGateway {

    public Authorization authorize(String cardholderName, String cardNumber,
            String expiry, String cvv, BigDecimal amount) {
        String name = cardholderName == null ? "" : cardholderName.trim();
        String digits = cardNumber == null ? ""
                : cardNumber.replaceAll("[\\s-]", "");

        if (name.length() < 2) {
            throw new IllegalArgumentException("Enter the cardholder name.");
        }
        if (!digits.matches("\\d{13,19}") || !passesLuhn(digits)) {
            throw new IllegalArgumentException("Enter a valid dummy card number.");
        }
        if (!isFutureExpiry(expiry)) {
            throw new IllegalArgumentException("Enter a valid future expiry date in MM/YY format.");
        }
        if (cvv == null || !cvv.matches("\\d{3,4}")) {
            throw new IllegalArgumentException("Enter a valid 3 or 4 digit CVV.");
        }
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("The payment amount is invalid.");
        }

        String brand = digits.startsWith("4") ? "Visa" : "Card";
        String reference = "PAY-" + UUID.randomUUID().toString()
                .replace("-", "").substring(0, 12).toUpperCase(Locale.ROOT);
        return new Authorization(reference, brand, digits.substring(digits.length() - 4));
    }

    /**
     * Simulates returning a captured payment. The original reference and
     * amount are validated, but no external banking service is contacted.
     */
    public Refund refund(String paymentReference, BigDecimal amount) {
        if (paymentReference == null || paymentReference.isBlank()) {
            throw new IllegalArgumentException("The original payment reference is required.");
        }
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("The refund amount is invalid.");
        }
        String reference = "REF-" + UUID.randomUUID().toString()
                .replace("-", "").substring(0, 12).toUpperCase(Locale.ROOT);
        return new Refund(reference, amount);
    }

    private boolean isFutureExpiry(String value) {
        if (value == null || !value.matches("(0[1-9]|1[0-2])/\\d{2}")) return false;
        try {
            YearMonth expiry = YearMonth.parse(value,
                    DateTimeFormatter.ofPattern("MM/yy"));
            return !expiry.isBefore(YearMonth.now());
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private boolean passesLuhn(String digits) {
        int sum = 0;
        boolean doubleDigit = false;
        for (int i = digits.length() - 1; i >= 0; i--) {
            int value = digits.charAt(i) - '0';
            if (doubleDigit) {
                value *= 2;
                if (value > 9) value -= 9;
            }
            sum += value;
            doubleDigit = !doubleDigit;
        }
        return sum % 10 == 0;
    }

    public static class Authorization {
        private final String reference;
        private final String cardBrand;
        private final String lastFour;

        Authorization(String reference, String cardBrand, String lastFour) {
            this.reference = reference;
            this.cardBrand = cardBrand;
            this.lastFour = lastFour;
        }

        public String getReference() { return reference; }
        public String getCardBrand() { return cardBrand; }
        public String getLastFour() { return lastFour; }
    }

    public static class Refund {
        private final String reference;
        private final BigDecimal amount;

        Refund(String reference, BigDecimal amount) {
            this.reference = reference;
            this.amount = amount;
        }

        public String getReference() { return reference; }
        public BigDecimal getAmount() { return amount; }
    }
}
