import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface ChatMessage {
    role: 'user' | 'assistant' | 'system';
    content: string;
}

export interface OllamaResponse {
    model: string;
    created_at: string;
    message: ChatMessage;
    done: boolean;
}

@Injectable({
    providedIn: 'root'
})
export class OllamaService {
    private http = inject(HttpClient);
    private readonly OLLAMA_URL = environment.ollamaUrl;

    private getSystemPrompt(role?: string): string {
        const base = `You are the RevHire AI Assistant, a helpful and professional companion for the RevHire recruitment platform. 
        Your goal is to assist users with their recruitment journey, provide career advice, and help them navigate the platform's features.
        While you should be aware of the user's current role to provide better context, you are a general-purpose assistant and can answer questions about any aspect of the platform or general technical topics (like programming, interview prep, etc.).`;

        if (role === 'JOB_SEEKER') {
            return `${base}
            CURRENT USER ROLE: Job Seeker.
            Primary focus: Job searching, applications, resume building, and interview preparation.
            Note: You can also explain Employer features if asked, to help them understand the hiring process better.`;
        } else if (role === 'EMPLOYER') {
            return `${base}
            CURRENT USER ROLE: Employer.
            Primary focus: Posting jobs, managing candidates, and company profile management.
            Note: You can also explain Job Seeker features if asked, to help them understand the applicant experience.`;
        }

        return `${base} You are here to support both Job Seekers and Employers in making the recruitment process seamless.`;
    }

    chat(messages: ChatMessage[], role?: string): Observable<string> {
        const payload = {
            model: 'tinyllama',
            messages: [
                { role: 'system', content: this.getSystemPrompt(role) },
                ...messages
            ],
            stream: false
        };

        return this.http.post<OllamaResponse>(this.OLLAMA_URL, payload).pipe(
            map(res => res.message.content)
        );
    }
}
