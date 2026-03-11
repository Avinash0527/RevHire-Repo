package com.example.revhirehiringplatform.controller;

import com.example.revhirehiringplatform.dto.response.SkillResponse;
import com.example.revhirehiringplatform.model.SkillsMaster;
import com.example.revhirehiringplatform.repository.SkillsMasterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/skills")
@RequiredArgsConstructor
public class SkillController {

    private final SkillsMasterRepository skillsMasterRepository;

    @GetMapping
    public ResponseEntity<List<SkillResponse>> getAllSkills() {
        return ResponseEntity.ok(skillsMasterRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList()));
    }

    @PostMapping
    public ResponseEntity<SkillResponse> createSkill(@RequestBody SkillsMaster skill) {
        return ResponseEntity.ok(mapToDto(skillsMasterRepository.save(skill)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SkillResponse> getSkillById(@PathVariable Long id) {
        return skillsMasterRepository.findById(id)
                .map(this::mapToDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<SkillResponse> updateSkill(@PathVariable Long id, @RequestBody SkillsMaster skillDetails) {
        return skillsMasterRepository.findById(id)
                .map(skill -> {
                    skill.setSkillName(skillDetails.getSkillName());
                    return ResponseEntity.ok(mapToDto(skillsMasterRepository.save(skill)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSkill(@PathVariable Long id) {
        skillsMasterRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<SkillResponse>> searchSkills(@RequestParam String name) {
        return ResponseEntity.ok(skillsMasterRepository.findAll().stream()
                .filter(s -> s.getSkillName().toLowerCase().contains(name.toLowerCase()))
                .map(this::mapToDto)
                .collect(Collectors.toList()));
    }

    @GetMapping("/popular")
    public ResponseEntity<List<SkillResponse>> getPopularSkills() {

        return ResponseEntity.ok(skillsMasterRepository.findAll().stream()
                .limit(10)
                .map(this::mapToDto)
                .collect(Collectors.toList()));
    }

    private SkillResponse mapToDto(SkillsMaster entity) {
        SkillResponse dto = new SkillResponse();
        dto.setId(entity.getId());
        dto.setName(entity.getSkillName());
        dto.setLevel("N/A");
        return dto;
    }
}