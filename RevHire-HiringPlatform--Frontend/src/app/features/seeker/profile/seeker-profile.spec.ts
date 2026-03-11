import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SeekerProfileComponent } from './seeker-profile';

describe('SeekerProfileComponent', () => {
    let component: SeekerProfileComponent;
    let fixture: ComponentFixture<SeekerProfileComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [SeekerProfileComponent, HttpClientTestingModule, RouterTestingModule]
        })
            .compileComponents();

        fixture = TestBed.createComponent(SeekerProfileComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});