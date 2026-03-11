import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MyApplicationsComponent } from './my-applications';

describe('MyApplicationsComponent', () => {
    let component: MyApplicationsComponent;
    let fixture: ComponentFixture<MyApplicationsComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [MyApplicationsComponent, HttpClientTestingModule, RouterTestingModule]
        })
            .compileComponents();

        fixture = TestBed.createComponent(MyApplicationsComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});