export interface ResumeExperience {
    id: number;
    title: string;
    company: string;
    startDate: string;
    endDate: string;
    current: boolean;
    description: string;
}

export interface ResumeEducation {
    id: number;
    degree: string;
    institution: string;
    field: string;
    startYear: number;
    endYear: number;
    gpa?: string;
}

export interface ResumeProject {
    id?: number;
    name: string;
    description: string;
    link?: string;
    technologies: string[];
}

export interface ResumeCertification {
    id?: number;
    name: string;
    issuer: string;
    date: string;
}

export interface ResumeSkill {
    name: string;
    level: 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED' | 'EXPERT';
}

export interface Resume {
    id?: number;
    userId: number;
    fullName: string;
    title: string;
    email: string;
    phone: string;
    location: string;
    website?: string;
    linkedin?: string;
    summary: string;
    experiences: ResumeExperience[];
    education: ResumeEducation[];
    skills: ResumeSkill[];
    languages: string[];
    projects: ResumeProject[];
    certifications: ResumeCertification[];
    updatedAt?: string;
}
