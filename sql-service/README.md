# sql-service

Dockerized MySQL instance shared by the other cardiac-diagnostics services.

## Usage

```bash
cd sql-service
docker compose up -d
```

This starts a MySQL 8.0 container on the `cardiac-net` Docker network, exposed on `localhost:3306`.

## Connection details

- Host: `localhost` (or `cardiac-sql-service` from another container on `cardiac-net`)
- Port: `3306`
- Database: `cardiac_diagnostics`
- User: `cardiac_user`
- Password: `cardiac_pass`
- Root password: `rootpassword`

## Connecting other services

Other services can join the same network to reach the database by container name instead of `localhost`:

```yaml
networks:
  default:
    external:
      name: cardiac-net
```

Then use `cardiac-sql-service:3306` as the DB host.
