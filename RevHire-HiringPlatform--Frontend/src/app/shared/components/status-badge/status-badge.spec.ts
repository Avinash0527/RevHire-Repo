import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { StatusBadgeComponent } from './status-badge';

describe('StatusBadgeComponent', () => {
    let component: StatusBadgeComponent;
    let fixture: ComponentFixture<StatusBadgeComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [StatusBadgeComponent, HttpClientTestingModule, RouterTestingModule]
        })
            .compileComponents();

        fixture = TestBed.createComponent(StatusBadgeComponent);
        component = fixture.componentInstance;
        component.status = 'PENDING' as any;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});