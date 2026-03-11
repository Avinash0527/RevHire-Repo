import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Resume } from '../models/resume.model';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ResumeService {
    private readonly API_URL = `${environment.apiUrl}/seeker/profile/resume-text`;

    constructor(private http: HttpClient) { }

    getResume(): Observable<Resume> {
        return this.http.get<Resume>(this.API_URL);
    }

    saveResume(resume: Resume): Observable<Resume> {
        return this.http.post<Resume>(this.API_URL, resume);
    }
}
