package com.example.githubsearch.model;

import lombok.Data;

import java.util.List;

@Data
public class GitHubSearchResponse {
    private int total_count;
    private boolean incomplete_results;
    private List<GitHubRepo> items;
}
