import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { signal } from '@angular/core';
import { of } from 'rxjs';
import { AuthService } from '@core/services/auth.service';
import { JwtPayload } from '@core/utils/jwt';
import { MainShell } from './main-shell';

describe('MainShell', () => {
  let fixture: ComponentFixture<MainShell>;

  beforeEach(async () => {
    const user = signal<JwtPayload | null>({
      sub: '1',
      type: 'access',
      email: 'a@b.c',
      username: 'usuario1',
      roles: [],
      jti: 'j',
      iss: 'exptrack-identity',
      iat: 1,
      exp: 9999999999,
    });

    await TestBed.configureTestingModule({
      imports: [MainShell],
      providers: [
        provideRouter([]),
        {
          provide: AuthService,
          useValue: { currentUser: user, logout: () => of({}) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(MainShell);
  });

  it('creates the component', () => {
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('exposes the authenticated user', () => {
    expect(fixture.componentInstance.user()?.username).toBe('usuario1');
  });
});
