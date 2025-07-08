package com.example.githubsearch.service;

import com.example.githubsearch.model.GitHubRepo;
import com.example.githubsearch.model.GitHubSearchResponse;
import com.example.githubsearch.model.PullRequestDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GitHubSearchService {

    @Value("${github.api-url}")
    private String githubApiUrl;

    @Value("${github.token}")
    private String githubToken;

    @Value("${github.per-page}")
    private int perPage;

    private final WebClient webClient = WebClient.builder()
            .baseUrl(githubApiUrl)
            .defaultHeader("Authorization", "Bearer " + githubToken)
            .build();

    public GitHubSearchResponse searchRepos(String org, String query, int page) {
        String finalQuery = "org:" + org + " " + query;

        GitHubSearchResponse response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search/repositories")
                        .queryParam("q", finalQuery)
                        .queryParam("page", page)
                        .queryParam("per_page", perPage)
                        .build())
                .retrieve()
                .bodyToMono(GitHubSearchResponse.class)
                .block();

        List<GitHubRepo> enrichedRepos = Flux.fromIterable(response.getItems())
                .flatMap(repo -> findMatchingPRs(repo, query))
                .collectList()
                .block();

        response.setItems(enrichedRepos);
        return response;
    }

    public Flux<GitHubRepo> findMatchingPRs(GitHubRepo repo, String keyword) {
        String owner = repo.getHtml_url().split("/")[3];
        repo.setOwner(owner);

        return webClient.get()
                .uri("/repos/" + owner + "/" + repo.getName() + "/pulls")
                .retrieve()
                .bodyToFlux(Object.class)
                .flatMap(pr -> {
                    int prNumber = ((Number) ((java.util.LinkedHashMap) pr).get("number")).intValue();
                    String prTitle = (String) ((java.util.LinkedHashMap) pr).get("title");
                    String prUrl = (String) ((java.util.LinkedHashMap) pr).get("html_url");

                    return webClient.get()
                            .uri("/repos/" + owner + "/" + repo.getName() + "/pulls/" + prNumber + "/files")
                            .retrieve()
                            .bodyToFlux(Object.class)
                            .collectList()
                            .flatMap(files -> {
                                boolean matches = files.stream().anyMatch(f -> {
                                    java.util.LinkedHashMap file = (java.util.LinkedHashMap) f;
                                    String filename = (String) file.get("filename");
                                    String patch = (String) file.get("patch");
                                    return filename.contains(keyword) || (patch != null && patch.contains(keyword));
                                });
                                if (!matches) return Flux.empty();

                                return webClient.get()
                                        .uri("/repos/" + owner + "/" + repo.getName() + "/pulls/" + prNumber + "/commits")
                                        .retrieve()
                                        .bodyToFlux(Object.class)
                                        .collectList()
                                        .map(commits -> {
                                            java.util.LinkedHashMap lastCommit = (java.util.LinkedHashMap) commits.get(commits.size() - 1);
                                            java.util.LinkedHashMap commit = (java.util.LinkedHashMap) lastCommit.get("commit");
                                            java.util.LinkedHashMap author = (java.util.LinkedHashMap) commit.get("author");
                                            String lastCommitUser = (String) author.get("name");

                                            PullRequestDetails prDetails = new PullRequestDetails();
                                            prDetails.setTitle(prTitle);
                                            prDetails.setUrl(prUrl);
                                            prDetails.setLastCommitUser(lastCommitUser);
                                            prDetails.setRepoOwner(owner);

                                            return prDetails;
                                        });
                            });
                })
                .collectList()
                .map(prs -> {
                    repo.setPullRequests(prs);
                    return repo;
                });
    }

    public List<GitHubRepo> fetchAllRepos(String org, String query) {
        int page = 1;
        GitHubSearchResponse firstPage = searchRepos(org, query, page);
        int totalCount = firstPage.getTotal_count();
        int totalPages = (int) Math.ceil((double) totalCount / perPage);

        List<GitHubRepo> allRepos = firstPage.getItems();

        while (++page <= totalPages) {
            GitHubSearchResponse nextPage = searchRepos(org, query, page);
            allRepos.addAll(nextPage.getItems());
        }

        return allRepos;
    }
}
