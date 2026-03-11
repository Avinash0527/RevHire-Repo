package com.example.revhirehiringplatform.controller;



import com.example.revhirehiringplatform.dto.response.JobPostResponse;
import com.example.revhirehiringplatform.dto.response.JobSeekerProfileResponse;
import com.example.revhirehiringplatform.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/jobs")
    public ResponseEntity<List<JobPostResponse>> searchJobs(@RequestParam String keyword) {
        return ResponseEntity.ok(searchService.searchJobs(keyword));
    }

    @GetMapping("/seekers")
    public ResponseEntity<List<JobSeekerProfileResponse>> searchSeekers(@RequestParam String keyword) {
        return ResponseEntity.ok(searchService.searchSeekers(keyword));
    }
}
