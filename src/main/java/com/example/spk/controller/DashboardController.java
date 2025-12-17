package com.example.spk.controller;

import com.example.spk.entity.RankingResult;
import com.example.spk.repository.AuditorRepository;
import com.example.spk.repository.CriteriaRepository;
import com.example.spk.repository.SubCriteriaRepository;
import com.example.spk.repository.UserRepository;
import com.example.spk.service.CalculationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class DashboardController {

    private final UserRepository userRepository;
    private final AuditorRepository auditorRepository;
    private final CriteriaRepository criteriaRepository;
    private final SubCriteriaRepository subCriteriaRepository;
    @Autowired private  CalculationService calculationService;

    public DashboardController(
            UserRepository userRepository,
            AuditorRepository auditorRepository,
            CriteriaRepository criteriaRepository,
            SubCriteriaRepository subCriteriaRepository) {
        this.userRepository = userRepository;
        this.auditorRepository = auditorRepository;
        this.criteriaRepository = criteriaRepository;
        this.subCriteriaRepository = subCriteriaRepository;
    }

    @GetMapping({"/", "dashboard"})
    public String dashboard(Model model, Principal principal) {
        model.addAttribute("username", principal != null ? principal.getName() : "Guest");
        model.addAttribute("auditorCount", auditorRepository.count());
        model.addAttribute("criteriaCount", criteriaRepository.count());
        model.addAttribute("subCriteriaCount", subCriteriaRepository.count());
        model.addAttribute("userCount", userRepository.count());
        model.addAttribute("pageTitle", "Dashboard");
        model.addAttribute("criterias", criteriaRepository.findAll());
        List<RankingResult> rankingResults = calculationService.calculateFinalRanking();

        System.out.println("rankingResults: " + rankingResults.get(0).getAuditor().getName());
        // Kirim hanya 5 atau 10 besar saja untuk dashboard (opsional)
        List<RankingResult> topRankings = rankingResults.stream()
                .limit(5)
                .collect(Collectors.toList());

        model.addAttribute("rankings", topRankings);
        return "dashboard";
    }
}
