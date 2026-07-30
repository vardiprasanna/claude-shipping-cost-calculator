# API Security

**As a platform administrator, I want the shipping API secured with API key authentication, so that only authorised clients can access the service.**

## Rules and Examples

### Rule: Must require a valid, recognized API key via the `X-API-Key` header for every request to a protected endpoint, rejecting missing or unrecognized keys with 401 Unauthorized

| X-API-Key Header | Result |
|---|---|
| (missing) | 401 Unauthorized |
| "not-a-real-key" (unrecognized) | 401 Unauthorized |
| valid USER key | 200 OK |
| valid ADMIN key | 200 OK |

The missing/invalid pair pins authentication as mandatory and independent of role; both valid roles pass this gate identically — role is checked separately (see next rule).

---

### Rule: Must restrict the admin-only endpoint (`GET /api/admin/rates`) to ADMIN-role keys, rejecting an authenticated but under-privileged USER key with 403 Forbidden

- **Example:** The one where a valid ADMIN key requests `GET /api/admin/rates` → 200 OK.
- **Counter-example:** The one where a valid USER key (authenticated, but wrong role) requests `GET /api/admin/rates` → 403 Forbidden — distinct from 401, since the key itself is valid.

---

### Rule: Must exempt Swagger/OpenAPI documentation from authentication

- **Example:** The one where an unauthenticated request to `/swagger-ui.html` → 200 OK, no API key required.
- **Counter-example:** The one where the same unauthenticated request instead targets `/api/shipping/calculate` → 401 Unauthorized, showing the exemption is scoped to docs only, not a blanket bypass.

---

## Resolved decisions (for implementation)

- **Swagger exemption scope:** covers the full Swagger/OpenAPI resource family needed for the UI to render — `/swagger-ui.html`, `/swagger-ui/**`, and `/v3/api-docs/**` — not just the HTML page, since excluding only the page would leave it broken (unable to fetch its own JS/CSS/spec without a key).
- **Admin endpoint:** `GET /api/admin/rates`, per the project's established security design.
- **Roles:** exactly two, `USER` and `ADMIN`. `ADMIN` is a superset — it can do everything `USER` can, plus the admin-only endpoint.
- **Key storage:** keys configured in `application.properties`, out of scope for this spec's business rules.
- **Out of scope:** response body shape for 401/403 errors; key rotation/expiry; rate limiting.
