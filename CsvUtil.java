package com.example.githubsearch.util;

import com.example.githubsearch.model.GitHubRepo;
import com.example.githubsearch.model.PullRequestDetails;

import java.io.ByteArrayInputStream;
import java.util.List;

public class CsvUtil {

    public static ByteArrayInputStream reposToCsv(List<GitHubRepo> repos) {
        StringBuilder csvBuilder = new StringBuilder();

        csvBuilder.append("Name,URL,Description,Language,Stars,PR Title,PR URL,Last Commit User,Repo Owner\n");

        for (GitHubRepo repo : repos) {
            if (repo.getPullRequests() == null || repo.getPullRequests().isEmpty()) {
                csvBuilder.append(String.join(",", repo.getName(), repo.getHtml_url(),
                        repo.getDescription() != null ? repo.getDescription().replace(",", " ") : "",
                        repo.getLanguage(), String.valueOf(repo.getStargazers_count()), "", "", "", repo.getOwner()))
                        .append("\n");
            } else {
                for (PullRequestDetails pr : repo.getPullRequests()) {
                    csvBuilder.append(String.join(",", repo.getName(), repo.getHtml_url(),
                            repo.getDescription() != null ? repo.getDescription().replace(",", " ") : "",
                            repo.getLanguage(), String.valueOf(repo.getStargazers_count()),
                            pr.getTitle(), pr.getUrl(), pr.getLastCommitUser(), pr.getRepoOwner()))
                            .append("\n");
                }
            }
        }

        return new ByteArrayInputStream(csvBuilder.toString().getBytes());
    }
}
