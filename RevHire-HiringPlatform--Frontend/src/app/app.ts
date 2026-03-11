import { Component, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { NavbarComponent } from './shared/components/navbar/navbar';
import { QuoteCarouselComponent } from './shared/components/quote-carousel/quote-carousel.component';
import { SidebarComponent } from './shared/components/sidebar/sidebar';
import { FooterComponent } from './shared/components/footer/footer';
import { ToastComponent } from './shared/components/toast/toast';
import { LoadingSpinnerComponent } from './shared/components/loading-spinner/loading-spinner';
import { ChatbotComponent } from './shared/components/chatbot/chatbot.component';
import { LoadingService } from './core/services/loading.service';
import { AuthService } from './core/services/auth.service';
import { ThemeService } from './core/services/theme.service';

@Component({
    selector: 'app-root',
    standalone: true,
    imports: [RouterOutlet, NavbarComponent, QuoteCarouselComponent, SidebarComponent, FooterComponent, ToastComponent, LoadingSpinnerComponent, ChatbotComponent],
    templateUrl: './app.html',
    styleUrl: './app.css'
})
export class AppComponent {
    authService = inject(AuthService);
    themeService = inject(ThemeService);
    loadingService = inject(LoadingService);
}
