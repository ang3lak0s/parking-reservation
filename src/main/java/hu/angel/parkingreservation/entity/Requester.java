package hu.angel.parkingreservation.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "requesters")
public class Requester {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    protected Requester() {
        // Required by JPA
    }

    public Requester(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}