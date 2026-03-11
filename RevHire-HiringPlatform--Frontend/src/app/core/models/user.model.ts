export type UserRole = 'JOB_SEEKER' | 'EMPLOYER' | 'ADMIN';

export interface User {
    id: number;
    name: string;
    email: string;
    role: UserRole;
    avatar?: string;
    companyName?: string; // For employers
    createdAt: string;
    isProfileComplete: boolean;
    phone?: string;
    location?: string;
}

export interface AuthResponse {
    token: string;
    refreshToken: string;
    name: string;
    email: string;
    phone: string;
    role: UserRole;
    id: number;
}

export interface LoginRequest {
    email: string;
    password: string;
}

export interface RegisterRequest {
    name: string;
    email: string;
    password: string;
    role: UserRole;
    phone?: string;
    location?: string;
    employmentStatus?: 'FRESHER' | 'EMPLOYED' | 'UNEMPLOYED';
    companyName?: string;
}

export interface PasswordResetRequest {
    token: string;
    newPassword: string;
}

export interface PasswordUpdateRequest {
    oldPassword: string;
    newPassword: string;
}
