# event-manager-platform

## Документация

### Event Manager API

#### [localhost:8080/swagger-ui/](http://localhost:8080/swagger-ui/index.html#/)

### Notification API

#### [localhost:8081/swagger-ui/](http://localhost:8081/swagger-ui/index.html#/)

### Auth API

#### [localhost:8082/swagger-ui](http://localhost:8082/swagger-ui/index.html#/)



## environments

| name              | value                                                                                                        |
|-------------------|--------------------------------------------------------------------------------------------------------------|
| POSTGRES_USER     | root                                                                                                         |
| POSTGRES_PASSWORD | root                                                                                                         |
| JWT_SECRET_KEY    | [Free JWT Secret Key Generator / Secure HS256, HS384, HS512 Keys Online](https://jwtsecretkeygenerator.com/) |
| JWT_LIFETIME      | 86400                                                                                                        |

example environment file (.env)
```env
POSTGRES_USER=root
POSTGRES_PASSWORD=root

JWT_SECRET_KEY=###########################################
JWT_LIFETIME=86400
```

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
docker exec -it eventHub psql -U root eventHubDB
```