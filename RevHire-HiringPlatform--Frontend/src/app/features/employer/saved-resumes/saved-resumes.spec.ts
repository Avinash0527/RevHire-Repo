import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SavedResumesComponent } from './saved-resumes';

describe('SavedResumesComponent', () => {
    let component: SavedResumesComponent;
    let fixture: ComponentFixture<SavedResumesComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [SavedResumesComponent, HttpClientTestingModule, RouterTestingModule]
        })
            .compileComponents();

        fixture = TestBed.createComponent(SavedResumesComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});