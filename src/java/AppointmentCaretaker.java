import java.io.Serializable;

public class AppointmentCaretaker implements Serializable {

    private AppointmentMemento memento;

    public void save(
            AppointmentMemento memento) {

        this.memento = memento;
    }

    public AppointmentMemento getSavedState() {
        return memento;
    }

    public boolean hasSavedState() {
        return memento != null;
    }

    public void clear() {
        memento = null;
    }
}