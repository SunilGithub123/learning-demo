# oauth2-client-credentials-jwt

Demonstrates the OAuth2 **Client Credentials** grant (RFC 6749 §4.4) end to
end, using signed JWTs as access tokens. This is the standard pattern for
machine-to-machine auth: one backend service calling another with no user in
the loop.

See [`docs/OAUTH2_GUIDE.md`](docs/OAUTH2_GUIDE.md) for a full write-up of the
OAuth2 concepts involved, how this module's classes map to them, and a
production-readiness review (what's hardened, and what a real deployment
still needs beyond this module).

To keep the demo runnable with zero external setup, a single Spring Boot app
plays both sides of the protocol:

- **Authorization server role** — `POST /oauth2/token` authenticates a
  registered client and issues an RSA-signed JWT access token
  (`JwtEncoder` / `NimbusJwtEncoder`), using a fresh in-memory RSA key pair
  generated at startup.
- **Resource server role** — `GET/POST /api/orders` validates the bearer
  JWT (`spring-boot-starter-oauth2-resource-server`) and authorizes requests
  by scope (`SCOPE_orders.read`, `SCOPE_orders.write`) via `@PreAuthorize`.

In a real system these would be two separate deployments (e.g. Keycloak /
Okta / Spring Authorization Server as the AS, and your own API as the RS)
that only share the JWT issuer's public key. Keeping them in one module here
is purely to make the full flow easy to run and test locally.

## Registered demo clients

Defined in `src/main/resources/application.yml`, with plain-text defaults so
the demo runs with zero setup. Every secret is overridable via environment
variable (`DEMO_CLIENT_SECRET`, `READONLY_CLIENT_SECRET`), and whichever
value is supplied is BCrypt-hashed at startup (see `RegisteredClientStore`)
and never compared or stored as plain text afterwards. Don't put a real
secret in this file — override via env var, and in a real deployment source
it from a secrets manager instead.

| client_id | client_secret | scopes |
|---|---|---|
| `demo-client` | `demo-secret` | `orders.read`, `orders.write` |
| `readonly-client` | `readonly-secret` | `orders.read` |

## Endpoints

| Endpoint | Auth | Purpose |
|---|---|---|
| `POST /oauth2/token` | client_id/client_secret | Issue an access token (RFC 6749 §4.4) |
| `GET /oauth2/jwks` | none | Public JWK Set (RFC 7517) for independent token validation |
| `GET/POST /api/orders` | `Bearer` JWT | Sample protected resource, scope-gated |
| `GET /actuator/health` | none | Liveness/readiness probe |

## Running

```bash
mvn -pl oauth2-client-credentials-jwt spring-boot:run
```

The app listens on `http://localhost:8081`.

## Trying it with curl

Request a token:

```bash
curl -s http://localhost:8081/oauth2/token \
  -d grant_type=client_credentials \
  -d client_id=demo-client \
  -d client_secret=demo-secret
```

```json
{"access_token":"eyJ...","token_type":"Bearer","expires_in":300,"scope":"orders.read orders.write"}
```

Call the protected API with the token:

```bash
TOKEN=$(curl -s http://localhost:8081/oauth2/token \
  -d grant_type=client_credentials -d client_id=demo-client -d client_secret=demo-secret \
  | python3 -c 'import sys,json;print(json.load(sys.stdin)["access_token"])')

curl -s http://localhost:8081/api/orders -H "Authorization: Bearer $TOKEN"
```

Without a token, or with a `readonly-client` token on `POST /api/orders`, the
API responds `401` / `403` respectively.

## Tests

`OAuth2ClientCredentialsFlowTests` runs the full flow through the real
servlet filter chain via `MockMvc`: rejecting unauthenticated calls,
rejecting bad client credentials and malformed token requests, rejecting
unsupported grant types and out-of-scope requests, confirming scope-based
authorization (`demo-client` can read and write, `readonly-client` can only
read), and checking that `/oauth2/jwks` and `/actuator/health` are safely
public (no private key or internal detail leakage).

```bash
mvn -pl oauth2-client-credentials-jwt test
```
