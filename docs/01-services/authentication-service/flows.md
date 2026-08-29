# Key Flows

## Registration

1. Guest User submits the registration form (email, password, basic profile fields) from the
   frontend.
2. Request reaches Authentication Service via the Gateway (public route, no token needed).
3. Authentication checks the email isn't already taken, and the password meets the policy.
   - If either check fails, it returns an error immediately — nothing is stored, nothing is
     published.
4. Authentication hashes the password (bcrypt) and stores the new user record.
5. Authentication publishes the registration event to Kafka (user ID + profile fields) —
   fire-and-forget, doesn't wait for a response.
6. Authentication returns success to the client.
7. The frontend sends the user to the login page (they are **not** automatically logged in).

Meanwhile, asynchronously and independently of the above: UserProfile Service consumes the event
and creates the profile record. See [`messaging.md`](./messaging.md).

## Login

1. Registered User submits email + password from the frontend.
2. Request reaches Authentication Service via the Gateway (public route).
3. Authentication looks up the user by email and checks the password against the stored hash.
   - No match (wrong password, or no such email) → generic "invalid credentials" error, same
     message either way.
4. Match → Authentication issues a JWT (`userId`, `email`, 60-minute expiry) and returns it.
5. The frontend stores the token (used on every subsequent request until it expires or the user
   logs out) and treats the user as logged in.

## Logout

1. User clicks logout.
2. Frontend deletes the token from local storage.
3. Nothing is sent to the backend — there's no `/auth/logout` call to make. The user is
   effectively logged out because the frontend no longer has a token to send.
4. Next request to a protected route has no token → Gateway rejects it → frontend redirects to
   login.
