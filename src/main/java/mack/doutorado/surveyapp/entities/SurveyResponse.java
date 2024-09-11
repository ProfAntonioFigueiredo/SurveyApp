package mack.doutorado.surveyapp.entities;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Entity
@Data
public class SurveyResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "survey_id", nullable = false)
    private Survey survey;

    @ManyToOne
    @JoinColumn(name = "respondent_id", nullable = false)
    private Respondent respondent;

    @Temporal(TemporalType.TIMESTAMP)
    private Date submittedAt;

    @OneToMany(mappedBy = "surveyResponse", cascade = CascadeType.ALL)
    private List<Answer> answers;
}
