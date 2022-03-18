# Book Phone app
An implementation of test task that allows to book/return phones. 

See the task specification here: [TASK.md](./TASK.md)

# Design goals
* Domain-driven design
* FP approach
* Contract first
# Tech stack
* kotlin
* kotlin-arrow(FP library)
* spring boot(web & dependency injection)
* exposed(data access layer)
# Motivation
* Explore functional programming approach with kotlin & arrow
* Avoid ORM at all costs
* Avoid spring magic - failed on that one because I had run out of time to try out something new
# TODO
see [TODO.md](./TODO.md)

# Run app
## Prerequisites
* JDK 11+
* Docker
## launch dependencies
```bash
docker-compose up -d
```
## apply migration
```bash
./gradlew flywayMigrate
```
## start app
```bash
./gradlew bootRun
```