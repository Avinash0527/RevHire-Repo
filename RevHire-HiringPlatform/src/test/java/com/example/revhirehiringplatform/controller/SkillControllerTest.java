package com.example.revhirehiringplatform.controller;

import com.example.revhirehiringplatform.model.SkillsMaster;
import com.example.revhirehiringplatform.repository.SkillsMasterRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SkillController.class)
@AutoConfigureMockMvc(addFilters = false)
public class SkillControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SkillsMasterRepository skillsMasterRepository;

    @Test
    @WithMockUser
    void testGetAllSkills() throws Exception {
        when(skillsMasterRepository.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/skills"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testCreateSkill() throws Exception {
        when(skillsMasterRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        mockMvc.perform(post("/api/skills")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"skillName\":\"Java\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testGetSkillById() throws Exception {
        SkillsMaster skill = new SkillsMaster();
        skill.setId(1L);
        skill.setSkillName("Java");
        when(skillsMasterRepository.findById(1L)).thenReturn(Optional.of(skill));

        mockMvc.perform(get("/api/skills/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testUpdateSkill() throws Exception {
        SkillsMaster skill = new SkillsMaster();
        skill.setId(1L);
        skill.setSkillName("Java");
        when(skillsMasterRepository.findById(1L)).thenReturn(Optional.of(skill));
        when(skillsMasterRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        mockMvc.perform(put("/api/skills/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"skillName\":\"Spring\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testDeleteSkill() throws Exception {
        doNothing().when(skillsMasterRepository).deleteById(any());

        mockMvc.perform(delete("/api/skills/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void testSearchSkills() throws Exception {
        when(skillsMasterRepository.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/skills/search?name=Java"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testGetPopularSkills() throws Exception {
        when(skillsMasterRepository.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/skills/popular"))
                .andExpect(status().isOk());
    }
}
