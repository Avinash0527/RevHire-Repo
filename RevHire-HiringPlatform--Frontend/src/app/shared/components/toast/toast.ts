import { Component, inject } from '@angular/core';
import { ToastService } from '../../../core/services/toast.service';
import { NgClass } from '@angular/common';

@Component({
    selector: 'app-toast',
    standalone: true,
    imports: [NgClass],
    templateUrl: './toast.html',
    styleUrl: './toast.css'
})
export class ToastComponent {
    toastService = inject(ToastService);
}
