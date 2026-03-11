import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { JobCardComponent } from './job-card';

describe('JobCardComponent', () => {
    let component: JobCardComponent;
    let fixture: ComponentFixture<JobCardComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [JobCardComponent, HttpClientTestingModule, RouterTestingModule]
        }).compileComponents();

        fixture = TestBed.createComponent(JobCardComponent);
        component = fixture.componentInstance;
        component.job = {
            id: 1,
            title: 'Test Job',
            companyName: 'Company A',
            jobType: 'FULL_TIME',
            description: 'Test description',
            experienceYears: 2,
            location: 'Remote',
            salary: '$100k',
            skills: ['Angular']
        } as any;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});