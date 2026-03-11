import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AdminService {
    private http = inject(HttpClient);
    private readonly USER_API = `${environment.apiUrl}/users`;
    private readonly LOG_API = `${environment.apiUrl}/audit-logs`;
    private readonly STATS_API = `${environment.apiUrl}/stats`;
    private readonly DASHBOARD_API = `${environment.apiUrl}/dashboard`;
    private readonly EMPLOYER_PROFILE_API = `${environment.apiUrl}/employer-profiles`;
    private readonly SKILL_API = `${environment.apiUrl}/skills`;

    // User Management
    getAllUsers(): Observable<any[]> {
        return this.http.get<any[]>(this.USER_API);
    }

    updateUserStatus(userId: number, enabled: string): Observable<any> {
        const params = new HttpParams().set('enabled', enabled);
        return this.http.put(`${this.USER_API}/${userId}/status`, null, { params });
    }

    deleteUser(userId: number): Observable<void> {
        return this.http.delete<void>(`${this.USER_API}/${userId}`);
    }

    getUserCount(): Observable<number> {
        return this.http.get<number>(`${this.USER_API}/count`);
    }

    // Audit Logs
    getAllAuditLogs(): Observable<any[]> {
        return this.http.get<any[]>(this.LOG_API);
    }

    getAuditLogById(id: number): Observable<any> {
        return this.http.get<any>(`${this.LOG_API}/${id}`);
    }

    getLogsByEntity(type: string): Observable<any[]> {
        return this.http.get<any[]>(`${this.LOG_API}/entity/${type}`);
    }

    getLogsByUser(userId: number): Observable<any[]> {
        return this.http.get<any[]>(`${this.LOG_API}/user/${userId}`);
    }

    // Statistics
    getJobsByLocation(): Observable<any> {
        return this.http.get<any>(`${this.STATS_API}/jobs/by-location`);
    }

    getJobsByType(): Observable<any> {
        return this.http.get<any>(`${this.STATS_API}/jobs/by-type`);
    }

    getApplicationsByStatus(): Observable<any> {
        return this.http.get<any>(`${this.STATS_API}/applications/by-status`);
    }

    getUsersByRole(): Observable<any> {
        return this.http.get<any>(`${this.STATS_API}/users/by-role`);
    }

    getTrendingJobs(): Observable<string[]> {
        return this.http.get<string[]>(`${this.STATS_API}/jobs/trending`);
    }

    getConversionRate(): Observable<number> {
        return this.http.get<number>(`${this.STATS_API}/applications/conversion-rate`);
    }

    // Dashboard Metrics
    getAdminMetrics(): Observable<any> {
        return this.http.get<any>(`${this.DASHBOARD_API}/admin/metrics`);
    }

    getEmployerDashboardMetrics(): Observable<any> {
        return this.http.get<any>(`${this.DASHBOARD_API}/employer/metrics`);
    }

    getSeekerDashboardMetrics(): Observable<any> {
        return this.http.get<any>(`${this.DASHBOARD_API}/seeker/metrics`);
    }

    getSystemHealth(): Observable<any> {
        return this.http.get<any>(`${this.DASHBOARD_API}/system/health`);
    }

    getSystemLogs(): Observable<string[]> {
        return this.http.get<string[]>(`${this.DASHBOARD_API}/system/logs`);
    }

    // Employer Profiles
    getAllEmployerProfiles(): Observable<any[]> {
        return this.http.get<any[]>(this.EMPLOYER_PROFILE_API);
    }

    getEmployerProfileById(id: number): Observable<any> {
        return this.http.get<any>(`${this.EMPLOYER_PROFILE_API}/${id}`);
    }

    getEmployerProfileByUserId(userId: number): Observable<any> {
        return this.http.get<any>(`${this.EMPLOYER_PROFILE_API}/user/${userId}`);
    }

    updateEmployerProfile(id: number, profile: any): Observable<any> {
        return this.http.put(`${this.EMPLOYER_PROFILE_API}/${id}`, profile);
    }

    deleteEmployerProfile(id: number): Observable<any> {
        return this.http.delete(`${this.EMPLOYER_PROFILE_API}/${id}`);
    }

    // Skill Master Data
    getAllSkills(): Observable<any[]> {
        return this.http.get<any[]>(this.SKILL_API);
    }

    getSkillById(id: number): Observable<any> {
        return this.http.get<any>(`${this.SKILL_API}/${id}`);
    }

    createSkill(skill: any): Observable<any> {
        return this.http.post(this.SKILL_API, skill);
    }

    updateSkill(id: number, skill: any): Observable<any> {
        return this.http.put(`${this.SKILL_API}/${id}`, skill);
    }

    deleteSkill(id: number): Observable<any> {
        return this.http.delete(`${this.SKILL_API}/${id}`);
    }

    searchSkills(name: string): Observable<any[]> {
        const params = new HttpParams().set('name', name);
        return this.http.get<any[]>(`${this.SKILL_API}/search`, { params });
    }

    getPopularSkills(): Observable<any[]> {
        return this.http.get<any[]>(`${this.SKILL_API}/popular`);
    }
}
