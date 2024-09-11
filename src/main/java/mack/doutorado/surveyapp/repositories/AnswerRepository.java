package mack.doutorado.surveyapp.repositories;

import mack.doutorado.surveyapp.entities.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {
    List<Answer> findBySurveyResponseId(Long surveyResponseId);
}
