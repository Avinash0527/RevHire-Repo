import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { UnauthorizedComponent } from './unauthorized';

describe('UnauthorizedComponent', () => {
    let component: UnauthorizedComponent;
    let fixture: ComponentFixture<UnauthorizedComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [UnauthorizedComponent, HttpClientTestingModule, RouterTestingModule]
        })
            .compileComponents();

        fixture = TestBed.createComponent(UnauthorizedComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});