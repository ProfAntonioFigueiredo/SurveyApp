package mack.doutorado.surveyapp.entities;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "survey_response_id", nullable = false)
    private SurveyResponse surveyResponse;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(nullable = false)
    private String answerText;
}
