import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ToastComponent } from './toast';

describe('ToastComponent', () => {
    let component: ToastComponent;
    let fixture: ComponentFixture<ToastComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [ToastComponent, HttpClientTestingModule, RouterTestingModule]
        })
            .compileComponents();

        fixture = TestBed.createComponent(ToastComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});