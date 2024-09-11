package mack.doutorado.surveyapp.repositories;

import mack.doutorado.surveyapp.entities.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findBySurveyId(Long surveyId);
}
