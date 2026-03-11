import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { JobApplicantsComponent } from './job-applicants';

describe('JobApplicantsComponent', () => {
    let component: JobApplicantsComponent;
    let fixture: ComponentFixture<JobApplicantsComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [JobApplicantsComponent, HttpClientTestingModule, RouterTestingModule]
        })
            .compileComponents();

        fixture = TestBed.createComponent(JobApplicantsComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});