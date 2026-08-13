package hu.angel.parkingreservation.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "parking_spaces")

public class ParkingSpace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true)
    private String identifier;

    @Column(nullable = false)
    private boolean active;

    protected ParkingSpace() {
        // Required by JPA
    }

    public ParkingSpace(String identifier, boolean active) {
        this.identifier = identifier;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
