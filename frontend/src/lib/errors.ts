export class ApiErrorResponse extends Error {
  public status: number;
  public code: string;
  public details?: Record<string, unknown>;

  constructor(message: string, status = 500, code = "INTERNAL_ERROR", details?: Record<string, unknown>) {
    super(message);
    this.name = "ApiErrorResponse";
    this.status = status;
    this.code = code;
    this.details = details;
  }
}

export function parseApiError(error: unknown): ApiErrorResponse {
  if (error instanceof ApiErrorResponse) {
    return error;
  }
  if (error instanceof Error) {
    return new ApiErrorResponse(error.message, 500, "CLIENT_ERROR");
  }
  return new ApiErrorResponse("An unexpected network error occurred", 500, "UNKNOWN_ERROR");
}
