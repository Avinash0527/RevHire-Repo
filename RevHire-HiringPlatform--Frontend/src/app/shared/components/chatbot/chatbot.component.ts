import { Component, signal, inject, ElementRef, ViewChild, AfterViewChecked, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { OllamaService, ChatMessage } from '../../../core/services/ollama.service';
import { AuthService } from '../../../core/services/auth.service';
import { effect } from '@angular/core';

@Component({
    selector: 'app-chatbot',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './chatbot.component.html',
    styleUrls: ['./chatbot.component.css']
})
export class ChatbotComponent implements OnInit, AfterViewChecked {
    private ollama = inject(OllamaService);
    private auth = inject(AuthService);
    @ViewChild('scrollContainer') private scrollContainer!: ElementRef;

    private readonly STORAGE_KEY = 'revhire_chat_history';

    isOpen = signal(false);
    userInput = signal('');
    loading = signal(false);
    messages = signal<ChatMessage[]>([]);

    constructor() {
        // Sync with session storage and clear on logout
        effect(() => {
            if (!this.auth.isAuthenticated()) {
                sessionStorage.removeItem(this.STORAGE_KEY);
                this.messages.set([
                    { role: 'assistant', content: 'Hello! I am your RevHire AI Assistant. How can I help you today?' }
                ]);
            }
        }, { allowSignalWrites: true });

        // Save to session storage whenever messages change
        effect(() => {
            const msgs = this.messages();
            if (msgs.length > 0) {
                sessionStorage.setItem(this.STORAGE_KEY, JSON.stringify(msgs));
            }
        });
    }

    ngOnInit() {
        const saved = sessionStorage.getItem(this.STORAGE_KEY);
        if (saved) {
            try {
                this.messages.set(JSON.parse(saved));
            } catch {
                this.setDefaultMessage();
            }
        } else {
            this.setDefaultMessage();
        }
    }

    private setDefaultMessage() {
        this.messages.set([
            { role: 'assistant', content: 'Hello! I am your RevHire AI Assistant. How can I help you today?' }
        ]);
    }

    ngAfterViewChecked() {
        if (this.isOpen()) {
            this.scrollToBottom();
        }
    }

    toggleChat() {
        this.isOpen.update(v => !v);
    }

    async sendMessage() {
        const text = this.userInput().trim();
        if (!text || this.loading()) return;

        const userRole = this.auth.currentUser()?.role;

        // Add user message
        this.messages.update(msgs => [...msgs, { role: 'user', content: text }]);
        this.userInput.set('');
        this.loading.set(true);

        this.ollama.chat(this.messages(), userRole).subscribe({
            next: (response) => {
                this.messages.update(msgs => [...msgs, { role: 'assistant', content: response }]);
                this.loading.set(false);
            },
            error: (err) => {
                console.error('Groq Chat Error:', err);
                const errorMsg = err.error?.error?.message || err.message || 'Unknown error. Check console.';
                this.messages.update(msgs => [...msgs, { role: 'assistant', content: `Error communicating with AI: ${errorMsg}` }]);
                this.loading.set(false);
            }
        });
    }

    private scrollToBottom(): void {
        try {
            this.scrollContainer.nativeElement.scrollTop = this.scrollContainer.nativeElement.scrollHeight;
        } catch (err) { }
    }
}
