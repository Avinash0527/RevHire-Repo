import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Application, ApplicationStatus } from '../models/application.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ApplicationService {
    private readonly API_URL = `${environment.apiUrl}/applications`;

    constructor(private http: HttpClient) { }

    getMyApplications(): Observable<Application[]> {
        return this.http.get<Application[]>(`${this.API_URL}/my-applications`);
    }

    getApplicationsByEmployer(employerId: number): Observable<Application[]> {
        return this.http.get<Application[]>(`${this.API_URL}/all-for-employer`);
    }

    getApplicationsByJob(jobId: number): Observable<Application[]> {
        return this.http.get<Application[]>(`${this.API_URL}/job/${jobId}`);
    }

    hasApplied(jobId: number): Observable<boolean> {
        return this.http.get<boolean>(`${this.API_URL}/check/${jobId}`);
    }

    applyToJob(jobId: number, coverLetter?: string, resumeFileId?: number): Observable<Application> {
        return this.http.post<Application>(`${this.API_URL}/apply-v2`, { jobId, coverLetter, resumeFileId });
    }

    updateStatus(appId: number, status: ApplicationStatus): Observable<Application> {
        let params = new HttpParams().set('status', status);
        return this.http.put<Application>(`${this.API_URL}/${appId}/status`, null, { params });
    }

    updateBulkStatus(appIds: number[], status: ApplicationStatus): Observable<Application[]> {
        return this.http.put<Application[]>(`${this.API_URL}/bulk-status`, { applicationIds: appIds, status });
    }

    getNotes(appId: number): Observable<any[]> {
        return this.http.get<any[]>(`${this.API_URL}/${appId}/notes`);
    }

    addNote(appId: number, noteText: string): Observable<any> {
        return this.http.post<any>(`${this.API_URL}/${appId}/notes`, noteText);
    }

    updateNote(appId: number, noteId: number, noteText: string): Observable<any> {
        return this.http.put<any>(`${this.API_URL}/${appId}/notes/${noteId}`, noteText);
    }

    deleteNote(appId: number, noteId: number): Observable<any> {
        return this.http.delete(`${this.API_URL}/${appId}/notes/${noteId}`);
    }

    getStatusHistory(appId: number): Observable<any[]> {
        return this.http.get<any[]>(`${this.API_URL}/${appId}/history`);
    }

    withdrawApplication(appId: number, reason?: string): Observable<Application> {
        let params = new HttpParams();
        if (reason) params = params.set('reason', reason);
        return this.http.put<Application>(`${this.API_URL}/${appId}/withdraw`, null, { params });
    }

    searchApplicantsForJob(jobId: number, name?: string, skill?: string, experience?: string, education?: string, appliedAfter?: string, status?: string): Observable<Application[]> {
        let params = new HttpParams();
        if (name) params = params.set('name', name);
        if (skill) params = params.set('skill', skill);
        if (experience) params = params.set('experience', experience);
        if (education) params = params.set('education', education);
        if (appliedAfter) params = params.set('appliedAfter', appliedAfter);
        if (status) params = params.set('status', status);
        return this.http.get<Application[]>(`${this.API_URL}/job/${jobId}/search`, { params });
    }

    deleteApplication(appId: number): Observable<any> {
        return this.http.delete(`${this.API_URL}/${appId}`, { responseType: 'text' });
    }

    getApplicationSummary(jobId: number): Observable<any> {
        return this.http.get<any>(`${this.API_URL}/job/${jobId}/summary`);
    }

    getApplicationById(appId: number): Observable<Application> {
        return this.http.get<Application>(`${this.API_URL}/${appId}`);
    }

    downloadResume(appId: number): Observable<Blob> {
        return this.http.get(`${this.API_URL}/${appId}/resume/download`, { responseType: 'blob' });
    }
}
