# nosql-service

Dockerized MongoDB instance shared by the other cardiac-diagnostics services.

## Usage

```bash
cd nosql-service
docker compose up -d
```

This starts a MongoDB 7.0 container on the `cardiac-net` Docker network, exposed on `localhost:27017`.

This is a bare MongoDB instance only — no database is pre-created. Each service is responsible for creating its own database and collections, and should use its own independent database within this instance.

## Connection details

- Host: `localhost` (or `cardiac-nosql-service` from another container on `cardiac-net`)
- Port: `27017`
- Root user: `root`
- Root password: `rootpassword`

## Connecting other services

Other services can join the same network to reach the database by container name instead of `localhost`:

```yaml
networks:
  default:
    external:
      name: cardiac-net
```

Then use `cardiac-nosql-service:27017` as the DB host.
