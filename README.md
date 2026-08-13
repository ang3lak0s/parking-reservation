# Parking Reservation

Spring Boot alapú REST API parkolóhely-foglalások kezelésére.

A projekt egy MVP (Minimum Viable Product) backendként készült Java nyelven. Az alkalmazás PostgreSQL adatbázist használ, az adatbázis-séma kezelését Flyway végzi, az ORM réteg pedig JPA/Hibernate. Az API Swagger/OpenAPI felületen keresztül is kipróbálható.

## Technológiák

- Java 21
- Spring Boot
- Spring Web / REST API
- Spring Data JPA
- Hibernate
- PostgreSQL
- Flyway
- Maven
- Docker / Docker Compose
- Swagger / OpenAPI
- JUnit alapú tesztek

## Főbb funkciók

- parkolóhelyek és igénylők kezelése
- parkolóhely-foglalás létrehozása
- foglalások lekérdezése parkolóhely alapján
- foglalás lemondása
- időintervallumok átfedésének ellenőrzése
- átfedő foglalás esetén `409 Conflict` válasz
- egymást pontosan követő, de nem átfedő foglalások engedélyezése
- lemondott foglalás által felszabadított időintervallum újrafoglalhatósága
- DTO-szintű és service-szintű validáció
- adatbázis-szintű időintervallum-ellenőrzés
- egységes hibakezelés

## Projekt felépítése

A projekt rétegezett Spring Boot alkalmazás:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
PostgreSQL
```

Az adatbázis-séma Flyway migrationökön keresztül kerül létrehozásra:

```text
V1__initial_schema.sql
V2__seed_data.sql
```

A Hibernate `ddl-auto: validate` módban működik, így az ORM réteg ellenőrzi, hogy az Entity-k megfelelnek-e a Flyway által létrehozott adatbázis-sémának.

## Előfeltételek

Dockeres futtatáshoz szükséges:

- Docker
- Docker Compose

Lokális IntelliJ / Maven futtatáshoz:

- Java 21
- PostgreSQL

A projekt Maven Wrapper-t tartalmaz, ezért külön Maven telepítése nem szükséges.

## Futtatás Docker Compose segítségével

A projekt gyökérkönyvtárából:

```bash
docker compose up --build
```

Az alkalmazás és a PostgreSQL adatbázis külön Compose service-ként indul.

A Dockeres konfigurációban az alkalmazás a `postgres` service nevet használja adatbázis-hostként:

```text
Spring Boot → postgres:5432 → PostgreSQL
```

A PostgreSQL elindulása után a Spring Boot alkalmazás Flyway migrationöket futtat, majd Hibernate validáció következik.

A leállításhoz:

```bash
docker compose down
```

Ha teljesen tiszta adatbázissal szeretnél újraindulni:

```bash
docker compose down -v
docker compose up --build
```

> A `-v` opció törli a PostgreSQL Docker volume-ját, ezért a következő induláskor a Flyway migrationök újra lefutnak.

## Lokális futtatás IntelliJ IDEA-ból

A projekt IntelliJ IDEA-ból is futtatható.

Az `application.yml` alapértelmezett konfigurációja lokális PostgreSQL esetén a következő:

```text
jdbc:postgresql://localhost:5432/parking
```

A Spring Boot alkalmazás fő belépési pontja:

```text
ParkingReservationApplication
```

## Adatbázis

A kezdeti adatbázis-sémát a következő Flyway migration hozza létre:

```text
src/main/resources/db/migration/V1__initial_schema.sql
```

A seed adatokat a:

```text
src/main/resources/db/migration/V2__seed_data.sql
```

migration tartalmazza.

A seed adatok között szerepelnek például:

### Parkolóhelyek

| ID | Code | Active |
|---:|---|---|
| 1 | P-001 | igen |
| 2 | P-002 | igen |
| 3 | P-003 | igen |
| 4 | P-004 | nem |

### Igénylők

| ID | Név |
|---:|---|
| 1 | John Doe |
| 2 | Jane Smith |
| 3 | Test User |

## Swagger / OpenAPI

Az alkalmazás elindítása után a Swagger UI az alábbi címen érhető el:

```text
http://localhost:8080/swagger-ui.html
```

A dokumentációból közvetlenül kipróbálhatók a REST endpointok.

## API

A projekt fő reservation endpointjai:

### Foglalás létrehozása

```http
POST /api/reservations
```

Példa:

```json
{
  "parkingSpaceId": 1,
  "requesterId": 1,
  "startTime": "2026-08-20T10:00:00",
  "endTime": "2026-08-20T12:00:00"
}
```

Sikeres létrehozás esetén:

```text
201 Created
```

### Foglalások lekérdezése parkolóhely alapján

```http
GET /api/reservations/parking-space/{parkingSpaceId}
```

A foglalások időrendben kérhetők le.

### Foglalás lemondása

```http
DELETE /api/reservations/{reservationId}
```

Sikeres lemondás esetén:

```text
204 No Content
```

A foglalás státusza `ACTIVE` állapotból `CANCELLED` állapotba kerül.

## Átfedő foglalások kezelése

Egy parkolóhelyhez nem hozható létre olyan aktív foglalás, amely időben átfed egy már meglévő aktív foglalással.

Például:

```text
10:00 ───────── 12:00
       11:00 ───────── 13:00
```

Ebben az esetben:

```text
409 Conflict
```

A pontosan határon érintkező foglalás viszont megengedett:

```text
10:00 ───────── 12:00
                  12:00 ───────── 14:00
```

Ez nem számít átfedésnek, ezért létrehozható.

A lemondott foglalások nem blokkolják az adott időintervallumot, így az általuk felszabadított időszak később újra lefoglalható.

## Tesztek

A tesztek Maven Wrapper segítségével futtathatók.

Windows:

```bash
./mvnw.cmd test
```

vagy:

```bash
mvnw.cmd test
```

A projektben unit és controller tesztek is találhatók.

## Konfiguráció

Az alkalmazás adatbázis-konfigurációja környezeti változókkal felülírható:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
```

Docker Compose környezetben ezek az értékek biztosítják, hogy az alkalmazás a `postgres` service-en keresztül érje el az adatbázist.

Lokális futtatáskor a konfiguráció alapértelmezett értékei használhatók.

## Dokumentáció

A projekthez kapcsolódó részletes dokumentáció külön dokumentumokban tartalmazza:

- rendszerterv és architektúra
- API-leírás
- felhasználói kézikönyv
- döntési napló és reflexió
- AI használatának bemutatása
- nyers AI prompt history

## Projekt állapota

Az MVP funkcionálisan elkészült. A REST API, adatbázis-kezelés, Flyway migrationök, validáció, hibakezelés, Swagger/OpenAPI és tesztek elkészültek és a fejlesztés során ellenőrzésre kerültek.

A repository véglegesítése során a dokumentáció, Git commit history és GitHub repository kialakítása tartozik a leadási feladatok közé.
