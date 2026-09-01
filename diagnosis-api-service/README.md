# diagnosis-api-service

The external Diagnosis API (`stackroutenew/diagnosisapi`, a `json-server` instance) that
`cardiac-diagnosis-service` reads from. Not part of this system's own codebase — a pre-built
image provided for the case study — but run and networked the same way as our other infra
pieces so it's a one-command start for anyone working on this repo.

## Usage

```bash
cd diagnosis-api-service
docker compose up -d
```

This starts the container on the `cardiac-net` Docker network, exposed on `localhost:3232`.

## Connection details

- Host: `localhost` (or `cardiac-diagnosis-api` from another container on `cardiac-net`)
- Port: `3232`

## Connecting other services

Other services can join the same network to reach it by container name instead of `localhost`:

```yaml
networks:
  default:
    external:
      name: cardiac-net
```

Then use `cardiac-diagnosis-api:3232` as the host. `cardiac-diagnosis-service`'s
`external.diagnosis-api-url` should point here once it's containerized too.
