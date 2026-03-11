import { Component, inject } from '@angular/core';
import { LoadingService } from '../../../core/services/loading.service';

@Component({
    selector: 'app-loading-spinner',
    standalone: true,
    templateUrl: './loading-spinner.html',
    styleUrl: './loading-spinner.css'
})
export class LoadingSpinnerComponent {
    loading = inject(LoadingService);
}
