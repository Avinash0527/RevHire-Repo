package com.example.revhirehiringplatform.service;

import com.example.revhirehiringplatform.dto.request.CompanyRequest;
import com.example.revhirehiringplatform.dto.response.CompanyResponse;
import com.example.revhirehiringplatform.model.Company;
import com.example.revhirehiringplatform.model.EmployerProfile;
import com.example.revhirehiringplatform.model.User;
import com.example.revhirehiringplatform.repository.CompanyRepository;
import com.example.revhirehiringplatform.repository.EmployerProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyService {

    private static final String COMPANY_NOT_FOUND = "Company not found";

    private final CompanyRepository companyRepository;
    private final EmployerProfileRepository employerProfileRepository;
    private final com.example.revhirehiringplatform.repository.UserRepository userRepository;
    private final com.example.revhirehiringplatform.repository.JobPostRepository jobPostRepository;

    @Transactional
    public CompanyResponse createOrUpdateCompanyProfile(CompanyRequest companyDto, User user) {
        log.info("Creating/Updating company: {} for user: {}", companyDto.getName(), user.getEmail());

        Company company;
        if (companyDto.getId() != null) {
            company = companyRepository.findById(companyDto.getId())
                    .orElseThrow(() -> new RuntimeException(COMPANY_NOT_FOUND));

            if (company.getCreatedBy() != null && !company.getCreatedBy().getId().equals(user.getId())) {
                throw new RuntimeException("Unauthorized to update this company");
            }
        } else {

            java.util.List<Company> existingCompanies = companyRepository.findByCreatedByOrderByNameAsc(user);
            if (!existingCompanies.isEmpty()) {
                throw new RuntimeException("You can only have one company profile");
            }
            company = new Company();
            company.setCreatedBy(user);
        }

        // Update user personal details
        if (companyDto.getUserName() != null && !companyDto.getUserName().isBlank()) {
            user.setName(companyDto.getUserName());
        }
        if (companyDto.getUserPhone() != null && !companyDto.getUserPhone().isBlank()) {
            user.setPhone(companyDto.getUserPhone());
        }
        userRepository.save(user);

        company.setName(companyDto.getName());
        company.setDescription(companyDto.getDescription());
        company.setWebsite(companyDto.getWebsite());
        company.setLocation(companyDto.getLocation());
        company.setIndustry(companyDto.getIndustry());
        company.setSize(companyDto.getSize() != null ? companyDto.getSize() : "");
        if (companyDto.getLogo() != null) {
            company.setLogo(companyDto.getLogo());
        }

        company = companyRepository.save(company);

        Optional<EmployerProfile> profileOpt = employerProfileRepository.findByUserId(user.getId());
        if (profileOpt.isEmpty()) {
            EmployerProfile newProfile = new EmployerProfile();
            newProfile.setUser(user);
            newProfile.setCompany(company);
            newProfile.setDesignation("HR / Admin");
            employerProfileRepository.save(newProfile);
        }

        return mapToDto(company);
    }

    public java.util.List<Company> getCompaniesForUser(User user) {
        return companyRepository.findByCreatedByOrderByNameAsc(user);
    }

    public CompanyResponse getCompanyProfile(User user) {
        Company company = employerProfileRepository.findByUserId(user.getId())
                .map(EmployerProfile::getCompany)
                .orElse(null);
        return company != null ? mapToDto(company) : null;
    }

    public CompanyResponse getCompanyById(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(COMPANY_NOT_FOUND));
        return mapToDto(company);
    }

    @Transactional
    public void deleteCompany(Long id, User user) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(COMPANY_NOT_FOUND));

        if (company.getCreatedBy() != null && !company.getCreatedBy().getId().equals(user.getId())) {
            throw new IllegalStateException("Unauthorized to delete this company");
        }

        if (!jobPostRepository.findByCompanyId(id).isEmpty()) {
            throw new IllegalStateException(
                    "Cannot delete company with active job posts. Please delete the jobs first.");
        }

        java.util.List<EmployerProfile> profiles = employerProfileRepository.findByCompanyId(id);
        employerProfileRepository.deleteAll(profiles);

        companyRepository.delete(company);
        log.info("Company deleted: {} by user: {}", id, user.getEmail());
    }

    private CompanyResponse mapToDto(Company company) {
        CompanyResponse dto = new CompanyResponse();
        dto.setId(company.getId());
        dto.setName(company.getName());
        dto.setDescription(company.getDescription());
        dto.setWebsite(company.getWebsite());
        dto.setLocation(company.getLocation());
        dto.setIndustry(company.getIndustry());
        dto.setSize(company.getSize());
        dto.setLogo(company.getLogo());

        User user = company.getCreatedBy();
        if (user != null) {
            dto.setUserName(user.getName());
            dto.setUserEmail(user.getEmail());
            dto.setUserPhone(user.getPhone());
        }
        return dto;
    }
}