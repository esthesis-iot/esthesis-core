import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DeviceProfileComponent } from './device-profile.component';

describe('DeviceProfileComponent', () => {
  let component: DeviceProfileComponent;
  let fixture: ComponentFixture<DeviceProfileComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DeviceProfileComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DeviceProfileComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
