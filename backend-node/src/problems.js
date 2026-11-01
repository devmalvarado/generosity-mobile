export class Problem extends Error {
  constructor(status, code, details = []) {
    super(code);
    this.status = status;
    this.code = code;
    this.details = details;
  }
}

export class HttpError extends Error {
  constructor(status, code, details = {}) {
    super(code);
    this.status = status;
    this.code = code;
    this.details = details;
  }
}

export const unauthenticated = () => new HttpError(401, 'unauthenticated');
export const forbidden = () => new HttpError(403, 'forbidden');
export const notFound = () => new HttpError(404, 'not_found');
