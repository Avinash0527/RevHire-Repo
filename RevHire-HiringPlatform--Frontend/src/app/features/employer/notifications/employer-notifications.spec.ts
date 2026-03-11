import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { EmployerNotificationsComponent } from './employer-notifications';

describe('EmployerNotificationsComponent', () => {
    let component: EmployerNotificationsComponent;
    let fixture: ComponentFixture<EmployerNotificationsComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [EmployerNotificationsComponent, HttpClientTestingModule, RouterTestingModule]
        })
            .compileComponents();

        fixture = TestBed.createComponent(EmployerNotificationsComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});