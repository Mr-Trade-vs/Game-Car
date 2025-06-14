package integrative.task.model;

import java.time.LocalDateTime;
import java.util.Comparator;

public class Incident {
    private Priority priority;
    private TypeIncident typeIncident;
    private LocalDateTime generateDateIncident;

    public Incident(Priority priority, TypeIncident typeIncident, LocalDateTime generateDateIncident) {
        this.priority = priority;
        this.typeIncident = typeIncident;
        this.generateDateIncident = generateDateIncident;
    }

    // Getters y setters...
    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public TypeIncident getTypeIncident() {
        return typeIncident;
    }

    public void setTypeIncident(TypeIncident typeIncident) {
        this.typeIncident = typeIncident;
    }

    public LocalDateTime getGenerateDateIncident() {
        return generateDateIncident;
    }

    public void setGenerateDateIncident(LocalDateTime generateDateIncident) {
        this.generateDateIncident = generateDateIncident;
    }

    public static Comparator<Incident> getComparator() {
        return new Comparator<Incident>() {
            @Override
            public int compare(Incident o1, Incident o2) {
                int result = Integer.compare(o2.getPriority().getValue(), o1.getPriority().getValue());
                if (result == 0) {
                    result = o1.getGenerateDateIncident().compareTo(o2.getGenerateDateIncident());
                }
                return result;
            }
        };
    }

    @Override
    public String toString() {
        return "Incident{priority=" + priority + ", type=" + typeIncident +
                ", date=" + generateDateIncident + "}";
    }
}
