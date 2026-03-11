import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SeekerDashboardComponent } from './seeker-dashboard';

describe('SeekerDashboardComponent', () => {
    let component: SeekerDashboardComponent;
    let fixture: ComponentFixture<SeekerDashboardComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [SeekerDashboardComponent, HttpClientTestingModule, RouterTestingModule]
        })
            .compileComponents();

        fixture = TestBed.createComponent(SeekerDashboardComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});