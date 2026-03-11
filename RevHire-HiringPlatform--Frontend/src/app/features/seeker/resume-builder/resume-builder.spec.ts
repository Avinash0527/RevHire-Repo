import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ResumeBuilderComponent } from './resume-builder';

describe('ResumeBuilderComponent', () => {
    let component: ResumeBuilderComponent;
    let fixture: ComponentFixture<ResumeBuilderComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [ResumeBuilderComponent, HttpClientTestingModule, RouterTestingModule]
        })
            .compileComponents();

        fixture = TestBed.createComponent(ResumeBuilderComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});