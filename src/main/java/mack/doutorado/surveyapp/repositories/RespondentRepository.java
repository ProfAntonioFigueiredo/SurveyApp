package mack.doutorado.surveyapp.repositories;

import mack.doutorado.surveyapp.entities.Respondent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RespondentRepository extends JpaRepository<Respondent, Long> {
    Optional<Respondent> findByEmail(String email);
}
