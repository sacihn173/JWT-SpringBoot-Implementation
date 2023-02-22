# JWT-SpringBoot-Implementation

Complete Process of authentication using JWT :

1. A register endpoint where user sends all info and receives JWT token.
2. Now the user can use the /authenticate endpoint and send this username and password to receive a new jwt token.
3. Registering also provides JWT token, that is given during signup process.
4. /authenticate also provides JWT token, that is given during login.
5. Now, there is a filter which we place before the Spring Security layer which looks header to see if any token is given with keyword
"Bearer -------token-------", if no token then request is passed to next filter.
6. If token is present, the filter extracts the token and verify it.
7. If the token is verified, it informs the SecurityContextHolder that this user is authenticated.
8. Now every time we request any endpoint, we just send the JWT token in header and we are allowed to access the endpoint.
9. We also store every JWT in database and keep a check on it's state(active or inactive).
10. At a time, a user can have only one JWT active.
11. To logout, make the JWT (with which request is coming) inactive.
