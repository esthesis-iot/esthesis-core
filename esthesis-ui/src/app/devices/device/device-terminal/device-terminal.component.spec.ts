import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DeviceTerminalComponent } from './device-terminal.component';

describe('DeviceTerminalComponent', () => {
  let component: DeviceTerminalComponent;
  let fixture: ComponentFixture<DeviceTerminalComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DeviceTerminalComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DeviceTerminalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
