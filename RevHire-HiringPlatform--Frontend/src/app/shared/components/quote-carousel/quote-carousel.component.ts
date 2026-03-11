import { Component } from '@angular/core';

export interface Quote {
  id: number;
  text: string;
}

@Component({
  selector: 'app-quote-carousel',
  standalone: true,
  imports: [],
  templateUrl: './quote-carousel.component.html',
  styleUrl: './quote-carousel.component.css' // changed from styleUrl: './quote-carousel.component.css'
})
export class QuoteCarouselComponent {
  quotes: Quote[] = [
    { id: 1, text: "The future belongs to those who believe in the beauty of their dreams." },
    { id: 2, text: "Your work is going to fill a large part of your life, and the only way to be truly satisfied is to do what you believe is great work." },
    { id: 3, text: "Success is not final, failure is not fatal: it is the courage to continue that counts." },
    { id: 4, text: "The only way to do great work is to love what you do." },
    { id: 5, text: "Believe you can and you're halfway there." },
  ];
}
