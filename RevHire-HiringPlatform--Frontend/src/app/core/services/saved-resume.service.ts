import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class SavedResumeService {
    private http = inject(HttpClient);
    private readonly API_URL = `${environment.apiUrl}/employer/saved-resumes`;

    getSavedResumes(): Observable<any[]> {
        return this.http.get<any[]>(this.API_URL);
    }

    saveResume(seekerId: number, jobId: number): Observable<any> {
        const params = new HttpParams().set('jobId', jobId);
        return this.http.post(`${this.API_URL}/${seekerId}`, {}, { params });
    }

    unsaveResume(seekerId: number, jobId: number): Observable<any> {
        const params = new HttpParams().set('jobId', jobId);
        return this.http.delete(`${this.API_URL}/${seekerId}`, { params });
    }

    isResumeSaved(seekerId: number, jobId: number): Observable<boolean> {
        const params = new HttpParams().set('jobId', jobId);
        return this.http.get<boolean>(`${this.API_URL}/${seekerId}/check`, { params });
    }
}
