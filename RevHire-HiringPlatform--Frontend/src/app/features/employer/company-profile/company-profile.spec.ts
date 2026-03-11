import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CompanyProfileComponent } from './company-profile';

describe('CompanyProfileComponent', () => {
    let component: CompanyProfileComponent;
    let fixture: ComponentFixture<CompanyProfileComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [CompanyProfileComponent, HttpClientTestingModule, RouterTestingModule]
        })
            .compileComponents();

        fixture = TestBed.createComponent(CompanyProfileComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});