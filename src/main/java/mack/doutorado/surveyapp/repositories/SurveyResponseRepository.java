package mack.doutorado.surveyapp.repositories;

import mack.doutorado.surveyapp.entities.Survey;
import mack.doutorado.surveyapp.entities.SurveyResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SurveyResponseRepository extends JpaRepository<SurveyResponse, Long> {
    List<SurveyResponse> findBySurveyId(Long surveyId);
    List<SurveyResponse> findByRespondentId(Long respondentId);
    List<SurveyResponse> findBySurvey(Survey surveyEntity);
}
