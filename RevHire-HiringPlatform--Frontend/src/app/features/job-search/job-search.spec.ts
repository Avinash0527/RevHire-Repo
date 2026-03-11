import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { JobSearchComponent } from './job-search';

describe('JobSearchComponent', () => {
    let component: JobSearchComponent;
    let fixture: ComponentFixture<JobSearchComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [JobSearchComponent, HttpClientTestingModule, RouterTestingModule]
        })
            .compileComponents();

        fixture = TestBed.createComponent(JobSearchComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});