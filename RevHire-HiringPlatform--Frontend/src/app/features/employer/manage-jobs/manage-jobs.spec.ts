import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ManageJobsComponent } from './manage-jobs';

describe('ManageJobsComponent', () => {
    let component: ManageJobsComponent;
    let fixture: ComponentFixture<ManageJobsComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [ManageJobsComponent, HttpClientTestingModule, RouterTestingModule]
        })
            .compileComponents();

        fixture = TestBed.createComponent(ManageJobsComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});