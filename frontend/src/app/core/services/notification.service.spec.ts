import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { NotificationService } from './notification.service';

const notificationPage = (content: unknown[], size: number) => ({
  content,
  totalElements: content.length,
  totalPages: 1,
  size,
  number: 0,
  first: true,
  last: true,
  numberOfElements: content.length,
  empty: content.length === 0,
});

describe('NotificationService', () => {
  let service: NotificationService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(NotificationService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('lists notifications with paging and server-side sort', () => {
    service.list().subscribe((page) => expect(page.content.length).toBe(1));

    const req = http.expectOne((r) => r.url.endsWith('/api/notifications'));
    expect(req.request.params.get('page')).toBe('0');
    expect(req.request.params.get('size')).toBe('10');
    expect(req.request.params.get('sort')).toBe('createdAt,desc');

    req.flush(notificationPage([{ id: 'n1', message: 'test', read: false, createdAt: 'x' }], 10));
  });

  it('passes paging parameters when provided', () => {
    service.list(2, 25).subscribe();

    const req = http.expectOne((r) => r.url.endsWith('/api/notifications'));
    expect(req.request.params.get('page')).toBe('2');
    expect(req.request.params.get('size')).toBe('25');

    req.flush(notificationPage([], 25));
  });

  it('marks a notification as read with PATCH', () => {
    service.markAsRead('n1').subscribe();
    http
      .expectOne((r) => r.method === 'PATCH' && r.url.includes('/api/notifications/n1/read'))
      .flush(null);
  });

  it('refresh loads the unread count and the latest notifications', () => {
    service.refresh();

    http.expectOne((r) => r.url.endsWith('/api/notifications/unread-count')).flush({ count: 3 });
    const list = http.expectOne(
      (r) => r.url.endsWith('/api/notifications') && r.params.get('size') === '5',
    );
    expect(list.request.params.get('sort')).toBe('createdAt,desc');
    list.flush(notificationPage([{ id: 'n1', message: 'test', read: false, createdAt: 'x' }], 5));

    expect(service.unreadCount()).toBe(3);
    expect(service.latest().length).toBe(1);
  });

  it('refresh keeps signals empty when requests fail', () => {
    service.refresh();

    http
      .expectOne((r) => r.url.endsWith('/api/notifications/unread-count'))
      .error(new ErrorEvent('network'));
    http
      .expectOne((r) => r.url.endsWith('/api/notifications') && r.params.get('size') === '5')
      .error(new ErrorEvent('network'));

    expect(service.unreadCount()).toBe(0);
    expect(service.latest()).toEqual([]);
  });

  it('reset clears the shared state', () => {
    service.refresh();
    http.expectOne((r) => r.url.endsWith('/api/notifications/unread-count')).flush({ count: 1 });
    http
      .expectOne((r) => r.url.endsWith('/api/notifications') && r.params.get('size') === '5')
      .flush(notificationPage([], 5));

    service.reset();

    expect(service.unreadCount()).toBe(0);
    expect(service.latest()).toEqual([]);
  });
});
