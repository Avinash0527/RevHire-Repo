import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

@Component({
    selector: 'app-info-pages',
    standalone: true,
    imports: [CommonModule, RouterLink],
    templateUrl: './info-pages.html',
    styleUrl: './info-pages.css'
})
export class InfoPagesComponent implements OnInit {
    private route = inject(ActivatedRoute);
    private router = inject(Router);

    activeSection = 'about';

    sections = [
        { id: 'about', label: 'About Us', icon: 'bi-info-circle' },
        { id: 'privacy', label: 'Privacy Policy', icon: 'bi-shield-lock' },
        { id: 'terms', label: 'Terms of Service', icon: 'bi-file-earmark-text' },
        { id: 'support', label: 'Support', icon: 'bi-headset' }
    ];

    ngOnInit(): void {
        this.route.queryParams.subscribe(params => {
            const section = params['section'];
            if (section && this.sections.some(s => s.id === section)) {
                this.activeSection = section;
            } else {
                this.activeSection = 'about';
            }
        });
    }

    setSection(sectionId: string): void {
        this.router.navigate([], {
            relativeTo: this.route,
            queryParams: { section: sectionId },
            queryParamsHandling: 'merge'
        });
    }
}
