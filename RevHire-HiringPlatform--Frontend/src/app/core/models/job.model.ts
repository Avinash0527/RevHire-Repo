export type JobType = 'FULL_TIME' | 'PART_TIME' | 'CONTRACT' | 'REMOTE';
export type JobCategory = 'TECHNOLOGY' | 'FINANCE' | 'HEALTHCARE' | 'DESIGN' | 'MARKETING' | 'SALES' | 'OPERATIONS' | 'EDUCATION' | 'OTHER';

export interface Job {
    id: number;
    title: string;
    description: string;
    requirements: string;
    responsibilities: string;
    location: string;
    salary: string;
    jobType: string;
    experienceYears: number;
    postedDate: string;
    deadline: string;
    companyId: number;
    companyName: string;
    education?: string;
    openings?: number;
    category?: string;
    status: string;
    applicantCount?: number;
    skills?: string;
    benefits?: string[];
    companyLogo?: string;
}

export interface JobFilter {
    keyword?: string;
    location?: string;
    category?: JobCategory;
    types?: JobType[];
    experienceLevel?: number; // Backend expects Integer experience (years)
    salaryMin?: number;
    salaryMax?: number;
}
