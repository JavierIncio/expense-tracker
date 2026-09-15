import { HttpErrorResponse } from '@angular/common/http';
import { httpErrorMessage } from './http-error';

describe('httpErrorMessage', () => {
  it('extracts the message from an ErrorResponse', () => {
    const error = new HttpErrorResponse({
      status: 409,
      error: {
        timestamp: 'x',
        status: 409,
        error: 'Conflict',
        message: 'Already exists',
        path: '/x',
      },
    });
    expect(httpErrorMessage(error, 'fallback')).toBe('Already exists');
  });

  it('uses the fallback if there is no body or message', () => {
    expect(httpErrorMessage(new HttpErrorResponse({ status: 401 }), 'fallback')).toBe('fallback');
    expect(httpErrorMessage('something went wrong', 'fallback')).toBe('fallback');
  });
});
