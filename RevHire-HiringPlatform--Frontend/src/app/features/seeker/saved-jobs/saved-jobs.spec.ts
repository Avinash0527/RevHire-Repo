import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SavedJobsComponent } from './saved-jobs';

describe('SavedJobsComponent', () => {
    let component: SavedJobsComponent;
    let fixture: ComponentFixture<SavedJobsComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [SavedJobsComponent, HttpClientTestingModule, RouterTestingModule]
        })
            .compileComponents();

        fixture = TestBed.createComponent(SavedJobsComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});