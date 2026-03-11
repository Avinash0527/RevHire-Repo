import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface SeekerProfileRequest {
    email?: string;
    headline?: string;
    summary?: string;
    location?: string;
    phone?: string;
    employmentStatus?: 'FRESHER' | 'EMPLOYED' | 'UNEMPLOYED';
}

export interface ResumeTextRequest {
    skills?: string;
    objective?: string;
    education?: string;
    experience?: string;
    projects?: string;
    certifications?: string;
}

@Injectable({
    providedIn: 'root'
})
export class SeekerService {
    private http = inject(HttpClient);
    private readonly API_URL = `${environment.apiUrl}/seeker/profile`;

    updateProfile(data: SeekerProfileRequest): Observable<any> {
        return this.http.post<any>(this.API_URL, data, {
            headers: { 'Content-Type': 'application/json' }
        });
    }

    updateResumeText(data: ResumeTextRequest): Observable<any> {
        return this.http.post<any>(`${this.API_URL}/resume-text`, data);
    }

    getProfile(): Observable<any> {
        return this.http.get<any>(this.API_URL);
    }

    getProfileById(id: number): Observable<any> {
        return this.http.get<any>(`${this.API_URL}/${id}`);
    }

    downloadResume(id: number): Observable<Blob> {
        return this.http.get(`${this.API_URL}/${id}/resume/download`, { responseType: 'blob' });
    }

    deleteProfile(id: number): Observable<any> {
        return this.http.delete(`${this.API_URL}/${id}`, { responseType: 'text' });
    }

    deleteAccount(): Observable<any> {
        return this.http.delete(`${this.API_URL}/me`, { responseType: 'text' });
    }

    getSeekerApplications(id: number): Observable<any[]> {
        return this.http.get<any[]>(`${this.API_URL}/${id}/applications`);
    }

    getSeekerSkills(id: number): Observable<any[]> {
        return this.http.get<any[]>(`${this.API_URL}/${id}/skills`);
    }

    uploadResume(file: File): Observable<any> {
        const formData = new FormData();
        formData.append('file', file);
        return this.http.post(`${this.API_URL}/resume`, formData);
    }
}
