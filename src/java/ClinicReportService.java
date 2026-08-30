import java.sql.SQLException;

/** Business layer separating report pages from JDBC implementation details. */
public class ClinicReportService {
    private final ClinicReportRepository repository;

    public ClinicReportService() {
        this(new ClinicReportRepository());
    }

    ClinicReportService(ClinicReportRepository repository) {
        this.repository = repository;
    }

    public ClinicReport generateReport() throws SQLException {
        return repository.loadReport();
    }
}
