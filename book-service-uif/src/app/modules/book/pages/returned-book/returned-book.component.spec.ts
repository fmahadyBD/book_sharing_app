import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ReturnedBookComponent } from './returned-book.component';

describe('ReturnedBookComponent', () => {
  let component: ReturnedBookComponent;
  let fixture: ComponentFixture<ReturnedBookComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ReturnedBookComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ReturnedBookComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
