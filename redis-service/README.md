# redis-service

Dockerized Redis instance shared by the other cardiac-diagnostics services as a cache / session store.

## Usage

```bash
cd redis-service
docker compose up -d
```

This starts a Redis 7.2 container on the `cardiac-net` Docker network, exposed on `localhost:6379`.

This is a bare Redis instance only — each service is responsible for the keys/namespaces it needs and should namespace its keys to avoid collisions with other services.

## Connection details

- Host: `localhost` (or `cardiac-redis-service` from another container on `cardiac-net`)
- Port: `6379`
- Password: `rootpassword`

## Connecting other services

Other services can join the same network to reach Redis by container name instead of `localhost`:

```yaml
networks:
  default:
    external:
      name: cardiac-net
```

Then use `cardiac-redis-service:6379` as the Redis host.
