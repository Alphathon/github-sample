package com.example.githubsearch.controller;

import com.example.githubsearch.model.GitHubRepo;
import com.example.githubsearch.model.GitHubSearchResponse;
import com.example.githubsearch.service.GitHubSearchService;
import com.example.githubsearch.util.CsvUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.util.List;

@RestController
@RequestMapping("/api/github")
@RequiredArgsConstructor
public class GitHubSearchController {

    private final GitHubSearchService service;

    @GetMapping("/search")
    public ResponseEntity<?> search(
            @RequestParam String org,
            @RequestParam String query,
            @RequestParam int page
    ) {
        GitHubSearchResponse response = service.searchRepos(org, query, page);
        int totalPages = (int) Math.ceil((double) response.getTotal_count() / 10);

        return ResponseEntity.ok().body(new Object() {
            public final List<GitHubRepo> items = response.getItems();
            public final int totalPages = totalPages;
            public final int totalCount = response.getTotal_count();
        });
    }

    @GetMapping("/download")
    public ResponseEntity<InputStreamResource> download(
            @RequestParam String org,
            @RequestParam String query
    ) {
        List<GitHubRepo> allRepos = service.fetchAllRepos(org, query);
        ByteArrayInputStream byteArrayInputStream = CsvUtil.reposToCsv(allRepos);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=github_search.csv")
                .contentType(MediaType.parseMediaType("application/csv"))
                .body(new InputStreamResource(byteArrayInputStream));
    }
}
