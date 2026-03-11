import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
    selector: 'app-footer',
    standalone: true,
    imports: [RouterLink],
    templateUrl: './footer.html',
    styleUrl: './footer.css'
})
export class FooterComponent {
    auth = inject(AuthService);
}
