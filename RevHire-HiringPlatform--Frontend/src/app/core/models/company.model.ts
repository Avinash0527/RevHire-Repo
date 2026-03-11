export interface Company {
    id: number;
    employerId: number;
    name: string;
    logo?: string;
    description: string;
    website: string;
    industry: string;
    size: string;
    location: string;
    founded: number;
    email: string;
    phone?: string;
    socialLinks?: {
        linkedin?: string;
        twitter?: string;
    };
    userName?: string;
    userEmail?: string;
    userPhone?: string;
}
