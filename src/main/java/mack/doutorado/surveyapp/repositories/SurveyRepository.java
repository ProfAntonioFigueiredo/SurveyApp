package mack.doutorado.surveyapp.repositories;

import mack.doutorado.surveyapp.entities.Survey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SurveyRepository extends JpaRepository<Survey, Long> {
    // Aqui você pode adicionar métodos personalizados, se necessário
}
