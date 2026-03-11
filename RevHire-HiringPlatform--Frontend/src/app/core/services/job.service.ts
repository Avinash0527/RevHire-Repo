import { Injectable, effect, signal } from '@angular/core';
import { Observable, map } from 'rxjs';
import { Job, JobFilter } from '../models/job.model';
import { AuthService } from './auth.service';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class JobService {
    private readonly API_URL = `${environment.apiUrl}/jobs`;
    private _savedJobIds = signal<Set<number>>(new Set());
    savedJobIdsSignal = this._savedJobIds.asReadonly();

    constructor(private http: HttpClient, private auth: AuthService) {
        effect(() => {
            const user = this.auth.currentUser();
            if (user?.role === 'JOB_SEEKER') {
                this.getSavedJobs().subscribe({
                    next: (jobs) => {
                        const newSet = new Set<number>();
                        jobs.forEach(j => newSet.add(j.id));
                        this._savedJobIds.set(newSet);
                    },
                    error: () => this._savedJobIds.set(new Set())
                });
            } else {
                this._savedJobIds.set(new Set());
            }
        }, { allowSignalWrites: true });
    }

    private get currentSavedIds(): Set<number> {
        return this._savedJobIds();
    }

    getAllJobs(): Observable<Job[]> {
        return this.http.get<Job[]>(this.API_URL);
    }

    getJobById(id: number): Observable<Job> {
        return this.http.get<Job>(`${this.API_URL}/${id}`);
    }

    searchJobs(filter: JobFilter): Observable<Job[]> {
        let params = new HttpParams();
        if (filter.keyword) params = params.set('title', filter.keyword);
        if (filter.location) params = params.set('location', filter.location);
        if (filter.category) params = params.set('category', filter.category);

        if (filter.types && filter.types.length > 0) {
            filter.types.forEach(t => {
                params = params.append('jobType', t);
            });
        }

        if (filter.experienceLevel !== undefined) {
            params = params.set('experience', filter.experienceLevel.toString());
        }

        if (filter.salaryMin) {
            params = params.set('salary', filter.salaryMin.toString());
        }

        return this.http.get<Job[]>(this.API_URL, { params });
    }

    getJobsByEmployer(employerId: number): Observable<Job[]> {
        return this.http.get<Job[]>(`${this.API_URL}/my-jobs`);
    }

    getRecommendedJobs(): Observable<Job[]> {
        return this.http.get<Job[]>(`${this.API_URL}/recommendations`);
    }

    getSavedJobs(): Observable<Job[]> {
        return this.http.get<Job[]>(`${environment.apiUrl}/seeker/saved-jobs`);
    }

    isSaved(jobId: number): boolean {
        return this.currentSavedIds.has(jobId);
    }

    toggleSave(jobId: number): Observable<boolean> {
        const url = `${environment.apiUrl}/seeker/saved-jobs/${jobId}`;
        const isCurrentlySaved = this.isSaved(jobId);

        if (isCurrentlySaved) {
            return this.http.delete(url, { responseType: 'text' }).pipe(
                map(() => {
                    this._savedJobIds.update(set => {
                        const newSet = new Set(set);
                        newSet.delete(jobId);
                        return newSet;
                    });
                    return false;
                })
            );
        } else {
            return this.http.post(url, {}, { responseType: 'text' }).pipe(
                map(() => {
                    this._savedJobIds.update(set => {
                        const newSet = new Set(set);
                        newSet.add(jobId);
                        return newSet;
                    });
                    return true;
                })
            );
        }
    }

    postJob(job: Partial<Job>, companyId?: number): Observable<Job> {
        return this.http.post<Job>(this.API_URL, companyId ? { ...job, companyId } : job);
    }

    updateJobStatus(jobId: number, status: string): Observable<void> {
        const params = new HttpParams().set('status', status);
        return this.http.put<void>(`${this.API_URL}/${jobId}/status`, null, { params });
    }

    updateJob(jobId: number, job: Partial<Job>): Observable<Job> {
        return this.http.put<Job>(`${this.API_URL}/${jobId}`, job);
    }

    getJobApplications(jobId: number): Observable<any[]> {
        return this.http.get<any[]>(`${this.API_URL}/${jobId}/applications`);
    }

    getJobSkills(jobId: number): Observable<any[]> {
        return this.http.get<any[]>(`${this.API_URL}/${jobId}/skills`);
    }

    deleteJob(jobId: number): Observable<void> {
        return this.http.delete(`${this.API_URL}/${jobId}`, { responseType: 'text' }).pipe(
            map(() => void 0)
        );
    }
}
