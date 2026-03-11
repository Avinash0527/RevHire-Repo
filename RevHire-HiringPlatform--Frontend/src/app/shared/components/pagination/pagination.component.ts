import { Component, Output, EventEmitter, computed, input } from '@angular/core';
import { NgClass, NgFor, NgIf } from '@angular/common';

@Component({
  selector: 'app-pagination',
  standalone: true,
  imports: [NgClass, NgFor, NgIf],
  template: `
    <div class="pagination-container" *ngIf="totalPages() > 1">
      <button 
        class="pagination-btn" 
        [disabled]="currentPage() === 1" 
        (click)="changePage(currentPage() - 1)"
        aria-label="Previous page"
      >
        <i class="bi bi-chevron-left"></i>
      </button>

      <div class="pagination-pages">
        <button 
          *ngFor="let page of pages()" 
          class="page-number" 
          [class.active]="page === currentPage()"
          (click)="changePage(page)"
        >
          {{ page }}
        </button>
      </div>

      <button 
        class="pagination-btn" 
        [disabled]="currentPage() === totalPages()" 
        (click)="changePage(currentPage() + 1)"
        aria-label="Next page"
      >
        <i class="bi bi-chevron-right"></i>
      </button>
    </div>
  `,
  styles: [`
    .pagination-container {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 1rem;
      margin-top: 2rem;
      padding: 1rem 0;
    }

    .pagination-pages {
      display: flex;
      gap: 0.5rem;
    }

    .pagination-btn, .page-number {
      width: 40px;
      height: 40px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 0.75rem;
      border: 1px solid var(--color-border);
      background: var(--color-surface);
      color: var(--color-text);
      font-weight: 600;
      cursor: pointer;
      transition: all 0.2s;
    }

    .pagination-btn:hover:not(:disabled), .page-number:hover:not(.active) {
      background: var(--color-bg);
      border-color: var(--color-primary);
      color: var(--color-primary);
    }

    .pagination-btn:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }

    .page-number.active {
      background: var(--color-primary);
      color: white;
      border-color: var(--color-primary);
      box-shadow: 0 4px 12px rgba(79, 70, 229, 0.25);
    }

    @media (max-width: 480px) {
      .pagination-container {
        gap: 0.5rem;
      }
      .pagination-btn, .page-number {
        width: 32px;
        height: 32px;
        font-size: 0.875rem;
      }
    }
  `]
})
export class PaginationComponent {
  totalItems = input(0);
  pageSize = input(10);
  currentPage = input(1);

  @Output() pageChange = new EventEmitter<number>();

  totalPages = computed(() => Math.ceil(this.totalItems() / this.pageSize()));

  pages = computed(() => {
    const total = this.totalPages();
    const pages: number[] = [];
    for (let i = 1; i <= total; i++) {
      pages.push(i);
    }
    return pages;
  });

  changePage(page: number): void {
    if (page >= 1 && page <= this.totalPages()) {
      this.pageChange.emit(page);
    }
  }
}
