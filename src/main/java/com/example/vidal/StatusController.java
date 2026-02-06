package com.example.vidal;

import com.example.vidal.model.SolutionEntity;
import com.example.vidal.repo.SolutionRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class StatusController {
    private final SolutionRepository repo;

    public StatusController(SolutionRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/")
    public String root() {
        long count = repo.count();
        return "OK - savedSolutions=" + count;
    }

    @GetMapping("/solutions")
    public List<SolutionEntity> solutions() {
        return repo.findAll();
    }
}
