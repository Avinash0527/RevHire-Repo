import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { map, tap } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { Job } from '../models/job.model';

export interface SearchResult {
    id: number;
    title: string;
    subtitle?: string;
    type: 'JOB' | 'SEEKER';
    link: string;
}

@Injectable({ providedIn: 'root' })
export class SearchService {
    private http = inject(HttpClient);
    private readonly API_URL = `${environment.apiUrl}/search`;
    private readonly JOBS_API_URL = `${environment.apiUrl}/jobs`;
    private jobsCache: Job[] | null = null;

    searchJobs(keyword: string): Observable<any[]> {
        const q = keyword.trim().toLowerCase();
        if (!q) return of([]);

        return this.getAllJobsCached().pipe(
            map(jobs =>
                jobs.filter(job => {
                    const title = (job.title || '').toLowerCase();
                    const company = (job.companyName || '').toLowerCase();
                    const location = (job.location || '').toLowerCase();
                    return title.includes(q) || company.includes(q) || location.includes(q);
                })
            )
        );
    }

    searchSeekers(keyword: string): Observable<any[]> {
        const params = new HttpParams().set('keyword', keyword);
        return this.http.get<any[]>(`${this.API_URL}/seekers`, { params });
    }

    private getAllJobsCached(): Observable<Job[]> {
        if (this.jobsCache) {
            return of(this.jobsCache);
        }

        return this.http.get<Job[]>(this.JOBS_API_URL).pipe(
            tap(jobs => {
                this.jobsCache = jobs || [];
            })
        );
    }
}
