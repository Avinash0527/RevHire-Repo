import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { EmployerDashboardComponent } from './employer-dashboard';

describe('EmployerDashboardComponent', () => {
    let component: EmployerDashboardComponent;
    let fixture: ComponentFixture<EmployerDashboardComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [EmployerDashboardComponent, HttpClientTestingModule, RouterTestingModule]
        })
            .compileComponents();

        fixture = TestBed.createComponent(EmployerDashboardComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});