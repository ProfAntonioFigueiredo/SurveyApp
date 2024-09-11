package mack.doutorado.surveyapp.controllers;

import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import mack.doutorado.surveyapp.entities.*;
import mack.doutorado.surveyapp.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.*;

@Controller
@RequestMapping("/surveys")
public class SurveyController {

    @Autowired
    private SurveyRepository surveyRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private SurveyResponseRepository surveyResponseRepository;

    @Autowired
    private RespondentRepository respondentRepository;

    @Autowired
    private AnswerRepository answerRepository;

    // 1. Listar todos os surveys
    @GetMapping
    public String getAllSurveys(Model model) {
        List<Survey> surveys = surveyRepository.findAll();
        model.addAttribute("surveys", surveys);
        return "surveys/list"; // Exibe a página de listagem de surveys
    }

    // 1. Exibir formulário para criar um survey (sem questões)
    @GetMapping("/new")
    public String showCreateSurveyForm(Model model) {
        model.addAttribute("survey", new Survey());
        return "surveys/create"; // Página para criar um survey (somente título e descrição)
    }

    // 2. Processar a criação de um survey
    @PostMapping("/new")
    public String createSurvey(@ModelAttribute Survey survey) {
        survey.setCreatedAt(new Date());
        survey.setActive(true);
        surveyRepository.save(survey); // Salva o survey no banco de dados
        return "redirect:/surveys"; // Redireciona para a lista de surveys
    }

    // 3. Exibir detalhes do survey e permitir adicionar questões
    @GetMapping("/{id}")
    public String viewSurveyDetails(@PathVariable("id") Long surveyId, Model model) {
        Optional<Survey> survey = surveyRepository.findById(surveyId);
        if (survey.isPresent()) {
            model.addAttribute("survey", survey.get());
            return "surveys/details"; // Página de detalhes do survey
        } else {
            return "redirect:/surveys"; // Redireciona se o survey não for encontrado
        }
    }

    // 5. Exibir formulário para editar um survey existente
    @GetMapping("/{id}/edit")
    public String showEditSurveyForm(@PathVariable Long id, Model model) {
        Optional<Survey> survey = surveyRepository.findById(id);
        if (survey.isPresent()) {
            model.addAttribute("survey", survey.get());
            return "surveys/edit"; // Exibe o formulário de edição de survey
        } else {
            return "redirect:/surveys"; // Redireciona para a lista de surveys se o ID não for encontrado
        }
    }

    // 6. Atualizar um survey existente
    @PostMapping("/{id}")
    public String updateSurvey(@PathVariable Long id, @ModelAttribute Survey surveyDetails) {
        Optional<Survey> survey = surveyRepository.findById(id);
        if (survey.isPresent()) {
            Survey existingSurvey = survey.get();
            existingSurvey.setTitle(surveyDetails.getTitle());
            existingSurvey.setDescription(surveyDetails.getDescription());
            existingSurvey.setActive(surveyDetails.isActive());
            surveyRepository.save(existingSurvey);
        }
        return "redirect:/surveys"; // Redireciona para a lista de surveys após a atualização
    }

    // 7. Excluir um survey
    @PostMapping("/{id}/delete")
    public String deleteSurvey(@PathVariable Long id) {
        surveyRepository.deleteById(id);
        return "redirect:/surveys"; // Redireciona para a lista de surveys após a exclusão
    }

    // Método para exportar todos os surveys (já existente)
    @GetMapping("/export/pdf")
    public ResponseEntity<InputStreamResource> exportSurveysToPdf() {
        List<Survey> surveys = surveyRepository.findAll();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(out);
        Document document = new Document(new com.itextpdf.kernel.pdf.PdfDocument(writer));

        document.add(new Paragraph("List of Surveys with Questions").setBold().setFontSize(16));

        for (Survey survey : surveys) {
            addSurveyToDocument(survey, document);
            document.add(new Paragraph(" ")); // Add a space between surveys
        }

        document.close();

        ByteArrayInputStream bis = new ByteArrayInputStream(out.toByteArray());

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename=surveys_with_questions.pdf");

        return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_PDF).body(new InputStreamResource(bis));
    }

    // Novo método para exportar um survey específico
    @GetMapping("/{id}/export/pdf")
    public ResponseEntity<InputStreamResource> exportSurveyToPdf(@PathVariable Long id) {
        Optional<Survey> surveyOptional = surveyRepository.findById(id);
        if (surveyOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Survey survey = surveyOptional.get();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(out);
        Document document = new Document(new com.itextpdf.kernel.pdf.PdfDocument(writer));

        document.add(new Paragraph("Survey Details with Questions").setBold().setFontSize(16));

        addSurveyToDocument(survey, document);

        document.close();

        ByteArrayInputStream bis = new ByteArrayInputStream(out.toByteArray());

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename=survey_" + id + "_with_questions.pdf");

        return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_PDF).body(new InputStreamResource(bis));
    }

    // Método auxiliar para adicionar os detalhes do survey e suas questões ao documento PDF
    private void addSurveyToDocument(Survey survey, Document document) {
        document.add(new Paragraph("Survey ID: " + survey.getId()));
        document.add(new Paragraph("Title: " + survey.getTitle()));
        document.add(new Paragraph("Description: " + survey.getDescription()));
        document.add(new Paragraph("Active: " + (survey.isActive() ? "Yes" : "No")));

        document.add(new Paragraph("Questions:").setBold());

        List<Question> questions = questionRepository.findBySurveyId(survey.getId());
        if (questions.isEmpty()) {
            document.add(new Paragraph("No questions available for this survey."));
        } else {
            Table questionTable = new Table(2);
            questionTable.addHeaderCell(new Cell().add(new Paragraph("Position").setBold()));
            questionTable.addHeaderCell(new Cell().add(new Paragraph("Question Text").setBold()));

            for (Question question : questions) {
                questionTable.addCell(String.valueOf(question.getPosition()));
                questionTable.addCell(question.getQuestionText());
            }

            document.add(questionTable);
        }
    }

    // 4. Compartilhar link para responder o survey
    @GetMapping("/{id}/share")
    public String shareSurveyLink(@PathVariable("id") Long id, Model model) {
        Optional<Survey> survey = surveyRepository.findById(id);
        if (survey.isPresent()) {
            String link = "http://localhost:8080/surveys/" + id + "/respond"; // Exemplo de link
            model.addAttribute("shareLink", link);
            return "surveys/share"; // Página para exibir o link de compartilhamento
        } else {
            return "redirect:/surveys"; // Redireciona se o survey não for encontrado
        }
    }

    // 8. Listar todas as questões de um survey específico
    @GetMapping("/{id}/questions")
    public String getSurveyQuestions(@PathVariable Long id, Model model) {
        Optional<Survey> survey = surveyRepository.findById(id);
        if (survey.isPresent()) {
            model.addAttribute("survey", survey.get());
            List<Question> questions = questionRepository.findBySurveyId(id);
            model.addAttribute("questions", questions);
            return "questions/list"; // Exibe a página de listagem de questões de um survey
        } else {
            return "redirect:/surveys"; // Redireciona para a lista de surveys se o ID não for encontrado
        }
    }

    // 4. Exibir formulário para adicionar questões a um survey existente
    @GetMapping("/{id}/questions/new")
    public String showAddQuestionsForm(@PathVariable("id") Long surveyId, Model model) {
        Optional<Survey> survey = surveyRepository.findById(surveyId);
        if (survey.isPresent()) {
            model.addAttribute("survey", survey.get());
            return "questions/create"; // Página para adicionar perguntas
        } else {
            return "redirect:/surveys"; // Redireciona se o survey não for encontrado
        }
    }

    // 5. Processar a adição de novas questões
    @PostMapping("/{id}/questions/new")
    public String addQuestionsToSurvey(@PathVariable("id") Long surveyId, @RequestParam("questions[]") List<String> questionTexts) {
        Optional<Survey> survey = surveyRepository.findById(surveyId);
        if (survey.isPresent()) {
            Survey surveyEntity = survey.get();
            List<Question> questions = new ArrayList<>();
            for (int i = 0; i < questionTexts.size(); i++) {
                if (!questionTexts.get(i).isEmpty()) {
                    Question question = new Question();
                    question.setSurvey(surveyEntity);
                    question.setQuestionText(questionTexts.get(i));
                    question.setPosition(surveyEntity.getQuestions().size() + i + 1);
                    questions.add(question);
                }
            }
            questionRepository.saveAll(questions); // Salva as novas questões
            return "redirect:/surveys/" + surveyId; // Redireciona para a página de detalhes do survey
        } else {
            return "redirect:/surveys"; // Redireciona se o survey não for encontrado
        }
    }

    // 11. Exibir formulário para editar uma questão existente
    @GetMapping("/{surveyId}/questions/{questionId}/edit")
    public String showEditQuestionForm(@PathVariable Long surveyId, @PathVariable Long questionId, Model model) {
        Optional<Question> question = questionRepository.findById(questionId);
        if (question.isPresent()) {
            model.addAttribute("surveyId", surveyId);
            model.addAttribute("question", question.get());
            return "questions/edit"; // Exibe o formulário de edição de questão
        } else {
            return "redirect:/surveys/" + surveyId + "/questions"; // Redireciona para a lista de questões se o ID não for encontrado
        }
    }

    // 12. Atualizar uma questão existente
    @PostMapping("/{surveyId}/questions/{questionId}")
    public String updateQuestion(@PathVariable Long surveyId, @PathVariable Long questionId, @ModelAttribute Question questionDetails) {
        Optional<Question> question = questionRepository.findById(questionId);
        if (question.isPresent()) {
            Question existingQuestion = question.get();
            existingQuestion.setQuestionText(questionDetails.getQuestionText());
            existingQuestion.setPosition(questionDetails.getPosition());
            questionRepository.save(existingQuestion);
        }
        return "redirect:/surveys/" + surveyId + "/questions"; // Redireciona para a lista de questões após a atualização
    }

    // 13. Excluir uma questão
    @PostMapping("/{surveyId}/questions/{questionId}/delete")
    public String deleteQuestion(@PathVariable Long surveyId, @PathVariable Long questionId) {
        questionRepository.deleteById(questionId);
        return "redirect:/surveys/" + surveyId + "/questions"; // Redireciona para a lista de questões após a exclusão
    }

    // 1. Exibir todas as respostas para um survey específico
    @GetMapping("/{id}/responses")
    public String viewSurveyResponses(@PathVariable("id") Long surveyId, Model model) {
        Optional<Survey> survey = surveyRepository.findById(surveyId);
        if (survey.isPresent()) {
            Survey surveyEntity = survey.get();
            List<SurveyResponse> responses = surveyResponseRepository.findBySurvey(surveyEntity);
            model.addAttribute("survey", surveyEntity);
            model.addAttribute("responses", responses);
            return "surveys/responses"; // Página para exibir as respostas
        } else {
            return "redirect:/surveys"; // Redireciona se o survey não for encontrado
        }
    }

    // Exibir o formulário para responder ao survey
    @GetMapping("/{id}/respond")
    public String showSurveyResponseForm(@PathVariable("id") Long surveyId, Model model) {
        Optional<Survey> survey = surveyRepository.findById(surveyId);
        if (survey.isPresent()) {
            model.addAttribute("survey", survey.get());
            return "surveys/respond"; // Página para responder ao survey
        } else {
            return "redirect:/surveys"; // Redireciona se o survey não for encontrado
        }
    }

    // Processar a resposta ao survey e associar ao Respondent
    @PostMapping("/{id}/respond")
    public String submitSurveyResponse(@PathVariable("id") Long surveyId, @RequestParam("email") String email, @RequestParam Map<String, String> formData) {
        Optional<Survey> survey = surveyRepository.findById(surveyId);
        if (survey.isPresent()) {
            Survey surveyEntity = survey.get();

            // Criar ou buscar o respondente pelo e-mail
            Respondent respondent = respondentRepository.findByEmail(email).orElseGet(() -> {
                Respondent newRespondent = new Respondent();
                newRespondent.setEmail(email);
                return respondentRepository.save(newRespondent);
            });

            // Criar a resposta do survey
            SurveyResponse surveyResponse = new SurveyResponse();
            surveyResponse.setSurvey(surveyEntity);
            surveyResponse.setRespondent(respondent);
            surveyResponse.setSubmittedAt(new Date());
            surveyResponseRepository.save(surveyResponse);

            // Processar e salvar as respostas
            List<Answer> answers = new ArrayList<>();
            for (String key : formData.keySet()) {
                if (key.startsWith("question_")) {
                    Long questionId = Long.parseLong(key.split("_")[1]);
                    Optional<Question> question = questionRepository.findById(questionId);
                    if (question.isPresent()) {
                        Answer answer = new Answer();
                        answer.setQuestion(question.get());
                        answer.setAnswerText(formData.get(key));
                        answer.setSurveyResponse(surveyResponse);
                        answers.add(answer);
                    }
                }
            }
            answerRepository.saveAll(answers); // Salva todas as respostas

            return "redirect:/surveys"; // Redireciona para a listagem de surveys
        } else {
            return "redirect:/surveys"; // Redireciona se o survey não for encontrado
        }
    }
}