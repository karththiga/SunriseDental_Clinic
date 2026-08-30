import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/** Business layer for schedule date validation and retrieval. */
public class ClinicScheduleService {
    private final ClinicScheduleRepository repository;

    public ClinicScheduleService() {
        this(new ClinicScheduleRepository());
    }

    ClinicScheduleService(ClinicScheduleRepository repository) {
        this.repository = repository;
    }

    public List<ScheduleEntry> getSchedule(LocalDate date) throws SQLException {
        if (date == null) {
            throw new IllegalArgumentException("A schedule date is required.");
        }
        LocalDate today = LocalDate.now();
        if (date.isBefore(today.minusDays(30)) || date.isAfter(today.plusYears(1))) {
            throw new IllegalArgumentException("Date must be within the supported schedule range.");
        }
        return repository.findByDate(date);
    }
}
