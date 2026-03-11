export type ApplicationStatus = 'APPLIED' | 'REVIEWING' | 'SHORTLISTED' | 'SELECTED' | 'REJECTED' | 'WITHDRAWN';

export interface ApplicationHistoryEntry {
    status: ApplicationStatus;
    date: string;
    note?: string;
}

export interface Application {
    id: number;
    jobId: number;
    jobTitle: string;
    companyName: string;
    jobSeekerId: number;
    jobSeekerName: string;
    jobSeekerEmail: string;
    jobSeekerSkills?: string;
    jobSeekerExperience?: string;
    jobSeekerEducation?: string;
    status: ApplicationStatus;
    withdrawReason?: string;
    appliedAt: string;
    notes?: string;
    jobSeekerProfileImage?: string;
    companyLogo?: string;
}
