# eventHub

## docker

| parameters        | value      |
|-------------------|------------|
| name container    | eventHub   |
| POSTGRES_DB       | eventHubDB |
| POSTGRES_USER     | root       |
| POSTGRES_PASSWORD | root       |
| ports             | 5432:5432  |
| version           | 15         |

### run postgres

```bash
docker run --name eventHub -e POSTGRES_DB=eventHubDB -e POSTGRES_USER=root -e POSTGRES_PASSWORD=root -p 5432:5432 -d postgres:15
```

### executed postgres in container

```bash
docker exec -it eventHub psql -U postgres
```