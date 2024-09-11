package mack.doutorado.surveyapp.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
public class Respondent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @OneToMany(mappedBy = "respondent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SurveyResponse> surveyResponses;
}
