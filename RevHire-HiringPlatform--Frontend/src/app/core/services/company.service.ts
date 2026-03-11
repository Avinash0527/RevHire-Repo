import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Company } from '../models/company.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class CompanyService {
    private readonly API_URL = `${environment.apiUrl}/company/register`;

    constructor(private http: HttpClient) { }

    getMyCompanies(): Observable<Company[]> {
        return this.http.get<Company[]>(`${this.API_URL}/companies`);
    }

    getCompany(): Observable<Company> {
        return this.http.get<Company>(this.API_URL);
    }

    updateCompany(company: Company): Observable<Company> {
        return this.http.post<Company>(this.API_URL, company);
    }

    getCompanyById(id: number): Observable<Company> {
        return this.http.get<Company>(`${this.API_URL}/${id}`);
    }

    getDashboardStats(): Observable<any> {
        return this.http.get<any>(`${this.API_URL}/dashboard`);
    }

    updateCompanyById(id: number, company: Company): Observable<Company> {
        return this.http.put<Company>(`${this.API_URL}/${id}`, company);
    }

    deleteCompany(id: number): Observable<any> {
        return this.http.delete(`${this.API_URL}/${id}`, { responseType: 'text' });
    }

    getCompanyJobs(id: number): Observable<any[]> {
        return this.http.get<any[]>(`${this.API_URL}/${id}/jobs`);
    }

    getCompanyEmployees(id: number): Observable<any[]> {
        return this.http.get<any[]>(`${this.API_URL}/${id}/employees`);
    }
}
