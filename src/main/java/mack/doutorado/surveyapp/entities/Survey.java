package mack.doutorado.surveyapp.entities;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.context.annotation.Bean;

import java.util.Date;
import java.util.List;


@Entity
@Data
public class Survey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = true)
    private String description;

    @Column(nullable = false)
    private boolean isActive;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date createdAt;

    @OneToMany(mappedBy = "survey", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Question> questions;
}
