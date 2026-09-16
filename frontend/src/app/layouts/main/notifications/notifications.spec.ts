import { HttpErrorResponse } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NotificationService } from '@core/services/notification.service';
import { of, throwError } from 'rxjs';
import { Notifications } from './notifications';

const page = {
  content: [
    { id: 'n1', message: 'Presupuesto excedido', read: false, createdAt: '2026-09-16T10:00:00Z' },
    { id: 'n2', message: 'Transacción creada', read: true, createdAt: '2026-09-15T10:00:00Z' },
  ],
  totalElements: 2,
  totalPages: 1,
  size: 10,
  number: 0,
  first: true,
  last: true,
  numberOfElements: 2,
  empty: false,
};

describe('Notifications', () => {
  let fixture: ComponentFixture<Notifications>;
  let notificationMock: { list: () => unknown; markAsRead: () => unknown; refresh: () => void };

  beforeEach(async () => {
    notificationMock = {
      list: () => of(page),
      markAsRead: () => of(null),
      refresh: () => void 0,
    };

    await TestBed.configureTestingModule({
      imports: [Notifications],
      providers: [{ provide: NotificationService, useValue: notificationMock }],
    }).compileComponents();

    fixture = TestBed.createComponent(Notifications);
  });

  it('creates the component', () => {
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('loads notifications and renders them', () => {
    fixture.detectChanges();

    expect(fixture.componentInstance.page()?.content.length).toBe(2);
    expect(fixture.nativeElement.querySelectorAll('tbody tr').length).toBe(2);
    expect(fixture.nativeElement.textContent).toContain('Presupuesto excedido');
    expect(fixture.nativeElement.textContent).toContain('No leída');
  });

  it('sets an error when the list fails', () => {
    notificationMock.list = () => throwError(() => new HttpErrorResponse({ status: 500 }));
    fixture.detectChanges();

    expect(fixture.componentInstance.error()).toBeTruthy();
    expect(fixture.nativeElement.querySelector('.alert-error')).toBeTruthy();
  });
});
