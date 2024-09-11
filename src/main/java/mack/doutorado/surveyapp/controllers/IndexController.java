package mack.doutorado.surveyapp.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {

    @GetMapping("/")
    public String index() {
        return "index"; // Redireciona para o arquivo index.html
    }

    // Método para a página blank.html (teste do menu lateral)
    @GetMapping("/blank")
    public String showBlankPage() {
        return "blank"; // Nome do arquivo HTML da página de teste
    }
}
