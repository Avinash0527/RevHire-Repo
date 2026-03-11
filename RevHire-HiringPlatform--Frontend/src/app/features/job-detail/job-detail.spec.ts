import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { JobDetailComponent } from './job-detail';

describe('JobDetailComponent', () => {
    let component: JobDetailComponent;
    let fixture: ComponentFixture<JobDetailComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [JobDetailComponent, HttpClientTestingModule, RouterTestingModule]
        })
            .compileComponents();

        fixture = TestBed.createComponent(JobDetailComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});