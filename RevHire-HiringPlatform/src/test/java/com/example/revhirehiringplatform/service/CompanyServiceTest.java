package com.example.revhirehiringplatform.service;


import com.example.revhirehiringplatform.dto.request.CompanyRequest;
import com.example.revhirehiringplatform.dto.response.CompanyResponse;
import com.example.revhirehiringplatform.model.Company;
import com.example.revhirehiringplatform.model.EmployerProfile;
import com.example.revhirehiringplatform.model.User;
import com.example.revhirehiringplatform.repository.CompanyRepository;
import com.example.revhirehiringplatform.repository.EmployerProfileRepository;
import com.example.revhirehiringplatform.repository.JobPostRepository;
import com.example.revhirehiringplatform.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CompanyServiceTest {

    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private EmployerProfileRepository employerProfileRepository;
    @Mock
    private JobPostRepository jobPostRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CompanyService companyService;

    private User user;
    private Company company;
    private CompanyRequest companyRequest;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("employer@test.com");

        company = new Company();
        company.setId(10L);
        company.setName("Test Company");
        company.setCreatedBy(user);

        companyRequest = new CompanyRequest();
        companyRequest.setName("Updated Company");
        companyRequest.setDescription("New description");
    }

    @Test
    void testCreateCompany_Success() {
        when(companyRepository.findByCreatedByOrderByNameAsc(user)).thenReturn(new ArrayList<>());
        when(companyRepository.save(any(Company.class))).thenReturn(company);
        when(employerProfileRepository.findByUserId(user.getId())).thenReturn(Optional.empty());
        when(userRepository.save(user)).thenReturn(user);

        CompanyResponse response = companyService.createOrUpdateCompanyProfile(companyRequest, user);

        assertNotNull(response);
        verify(companyRepository).save(any(Company.class));
        verify(employerProfileRepository).save(any(EmployerProfile.class));
    }

    @Test
    void testCreateCompany_AlreadyExists() {
        List<Company> existing = List.of(company);
        when(companyRepository.findByCreatedByOrderByNameAsc(user)).thenReturn(existing);

        assertThrows(RuntimeException.class, () -> companyService.createOrUpdateCompanyProfile(companyRequest, user));
    }

    @Test
    void testUpdateCompany_Success() {
        companyRequest.setId(10L);
        when(companyRepository.findById(10L)).thenReturn(Optional.of(company));
        when(companyRepository.save(any(Company.class))).thenReturn(company);
        when(userRepository.save(user)).thenReturn(user);

        CompanyResponse response = companyService.createOrUpdateCompanyProfile(companyRequest, user);

        assertNotNull(response);
        assertEquals("Updated Company", response.getName());
    }

    @Test
    void testDeleteCompany_WithJobs() {
        when(companyRepository.findById(10L)).thenReturn(Optional.of(company));
        when(jobPostRepository.findByCompanyId(10L)).thenReturn(List.of(new com.example.revhirehiringplatform.model.JobPost()));

        assertThrows(IllegalStateException.class, () -> companyService.deleteCompany(10L, user));
    }
}
