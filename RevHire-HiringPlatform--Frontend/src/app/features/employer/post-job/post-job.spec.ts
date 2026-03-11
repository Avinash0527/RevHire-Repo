import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { PostJobComponent } from './post-job';

describe('PostJobComponent', () => {
    let component: PostJobComponent;
    let fixture: ComponentFixture<PostJobComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [PostJobComponent, HttpClientTestingModule, RouterTestingModule]
        })
            .compileComponents();

        fixture = TestBed.createComponent(PostJobComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});