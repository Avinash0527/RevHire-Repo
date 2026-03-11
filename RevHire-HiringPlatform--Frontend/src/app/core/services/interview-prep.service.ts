import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';

export interface InterviewQuestion {
    id: string;
    question: string;
    hint?: string;
    answer?: string;
    difficulty: 'BASIC' | 'MODERATE' | 'ADVANCED';
    show?: boolean;
}

export interface InterviewData {
    roles: { [key: string]: InterviewQuestion[] };
    skills: { [key: string]: InterviewQuestion[] };
    hr: InterviewQuestion[];
}

@Injectable({
    providedIn: 'root'
})
export class InterviewPrepService {
    private http = inject(HttpClient);
    private dataUrl = 'assets/data/interview-questions.json';

    // Fetch all data
    getInterviewData(): Observable<InterviewData> {
        return this.http.get<InterviewData>(this.dataUrl);
    }

    // Fetch roles list
    getAvailableRoles(): Observable<string[]> {
        return this.getInterviewData().pipe(
            map(data => Object.keys(data.roles || {}))
        );
    }

    // Fetch skills list
    getAvailableSkills(): Observable<string[]> {
        return this.getInterviewData().pipe(
            map(data => Object.keys(data.skills || {}))
        );
    }

    // Fetch questions for a specific role
    getQuestionsByRole(role: string): Observable<InterviewQuestion[]> {
        return this.getInterviewData().pipe(
            map(data => data.roles[role] || [])
        );
    }

    // Fetch questions for a specific skill
    getQuestionsBySkill(skill: string): Observable<InterviewQuestion[]> {
        return this.getInterviewData().pipe(
            map(data => data.skills[skill] || [])
        );
    }

    // Fetch HR questions
    getHRQuestions(): Observable<InterviewQuestion[]> {
        return this.getInterviewData().pipe(
            map(data => data.hr || [])
        );
    }
}
