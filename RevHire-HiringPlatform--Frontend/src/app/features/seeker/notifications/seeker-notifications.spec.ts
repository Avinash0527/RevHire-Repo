import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SeekerNotificationsComponent } from './seeker-notifications';

describe('SeekerNotificationsComponent', () => {
    let component: SeekerNotificationsComponent;
    let fixture: ComponentFixture<SeekerNotificationsComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [SeekerNotificationsComponent, HttpClientTestingModule, RouterTestingModule]
        })
            .compileComponents();

        fixture = TestBed.createComponent(SeekerNotificationsComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});