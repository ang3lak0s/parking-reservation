Rendszerterv – Parkolóhely-foglalási rendszer
1. A rendszer célja

A projekt célja egy egyszerű parkolóhely-foglalási backend rendszer megvalósítása, amely lehetővé teszi a parkolóhelyekhez kapcsolódó foglalások kezelését.

A rendszer fő funkciói:

parkolóhelyek nyilvántartása,
foglalási kérések létrehozása,
foglalási időintervallumok ellenőrzése,
ütköző foglalások megakadályozása,
egy adott parkolóhely foglalásainak lekérdezése,
meglévő foglalások lemondása.

A rendszer perzisztens adatbázist használ, az adatbázis sémáját és kezdő adatait Flyway migrationök hozzák létre.

A megoldás REST API-n keresztül használható, és a teljes rendszer az adatbázissal együtt Docker Compose segítségével egyetlen paranccsal elindítható.

2. Technológiai környezet

A backend az alábbi technológiákra épül:

Technológia	Szerepe
Java 21	Programozási nyelv
Spring Boot 4.0.7	Backend alkalmazáskeretrendszer
Spring Web MVC	REST API
Spring Data JPA	Adatbázis-hozzáférés
Hibernate	ORM
PostgreSQL 18	Relációs adatbázis
Flyway 12.0.0	Adatbázis-migráció
Spring Validation	Bemeneti adatok validációja
Springdoc OpenAPI	Swagger / OpenAPI dokumentáció
Maven	Build és dependency management
Docker / Docker Compose	Konténerizált futtatás
JUnit / Spring tesztkörnyezet	Tesztelés

A Java verziója 21, a projekt Spring Boot 4.0.7-re épül. A Flyway verzióját explicit módon 12.0.0-ra állítottuk.

3. Architektúra

A rendszer egyszerű, rétegekre bontott backend architektúrát használ.

                    REST kliens
                        │
                        ▼
              ┌───────────────────┐
              │ ReservationController │
              └─────────┬─────────┘
                        │
                        ▼
              ┌───────────────────┐
              │ ReservationService │
              └─────────┬─────────┘
                        │
            ┌───────────┼───────────┐
            ▼           ▼           ▼
       Repository   Repository   Repository
       ParkingSpace Requester    Reservation
            │           │           │
            └───────────┼───────────┘
                        ▼
                 PostgreSQL
Controller réteg

A ReservationController felelős a REST API végpontjaiért. Feladata:

HTTP kérések fogadása,
request DTO-k kezelése,
bemeneti validáció elindítása,
a service réteg meghívása,
megfelelő HTTP státuszkód és response visszaadása.
Service réteg

A ReservationService tartalmazza az alkalmazás fő üzleti logikáját.

Itt történik többek között:

az időintervallum ellenőrzése,
a parkolóhely létezésének ellenőrzése,
az aktív parkolóhely ellenőrzése,
a kérelmező létezésének ellenőrzése,
foglalási ütközés ellenőrzése,
foglalás létrehozása,
foglalás lemondása.
Repository réteg

A repository-k Spring Data JPA segítségével biztosítják az adatbázis elérését.

Három repository található:

ParkingSpaceRepository
RequesterRepository
ReservationRepository

A ReservationRepository egy célzott lekérdezést is tartalmaz a foglalási ütközések ellenőrzésére.

Entity réteg

A perzisztens üzleti objektumokat a következő entity-k reprezentálják:

ParkingSpace
Requester
Reservation

A foglalás állapotát a ReservationStatus enum kezeli:

ACTIVE
CANCELLED

4. Adatmodell

Az adatbázis három fő táblából áll.

parking_spaces
---------------
id PK
code UNIQUE
active


requesters
---------------
id PK
name


reservations
---------------
id PK
parking_space_id FK → parking_spaces.id
requester_id FK → requesters.id
start_time
end_time
status
created_at

A reservations tábla kapcsolódik a parkolóhelyhez és a kérelmezőhöz.

A V1__initial_schema.sql migration adatbázis-szinten is biztosítja, hogy a foglalás kezdési időpontja korábbi legyen a befejezési időpontnál:

CHECK (start_time < end_time)

Ez azért fontos, mert így az üzleti szabály nem kizárólag az alkalmazás Java kódjától függ.

5. Adatbázis inicializálása

Az adatbázis kezelésére Flyway migrationök szolgálnak.

V1 – kezdeti séma

A V1__initial_schema.sql létrehozza:

parking_spaces
requesters
reservations

táblákat, valamint a szükséges elsődleges és idegen kulcsokat, egyedi megszorítást és időintervallumra vonatkozó CHECK constraintet.

V2 – kezdő adatok

A V2__seed_data.sql a rendszer indulásakor tesztelhető kezdőállapotot biztosít.

A kezdeti parkolóhelyek:

Azonosító	Aktív
P-001	igen
P-002	igen
P-003	igen
P-004	nem

A kezdeti kérelmezők:

John Doe
Jane Smith
Test User

Ez megfelel annak a követelménynek, hogy a rendszer induláskor már inicializált és tesztelhető állapotban legyen.

6. Foglalási üzleti logika

A foglalás létrehozása több lépésben történik.

6.1. Időintervallum ellenőrzése

A kezdési időpontnak szigorúan korábbinak kell lennie a befejezési időpontnál.

startTime < endTime

Ezért például:

10:00 → 12:00     ✓
10:00 → 10:00     ✗
14:00 → 12:00     ✗
6.2. Parkolóhely ellenőrzése

A megadott parkolóhelynek léteznie kell.

Ha nem létezik, a rendszer 404 Not Found hibát ad.

A parkolóhelynek továbbá aktívnak kell lennie. Inaktív parkolóhelyre foglalás nem hozható létre.

6.3. Kérelmező ellenőrzése

A foglaláshoz megadott kérelmezőnek léteznie kell az adatbázisban.

Ismeretlen kérelmező esetén a rendszer 404 Not Found hibát ad.

6.4. Foglalási ütközés vizsgálata

Egy parkolóhelyre csak akkor hozható létre új aktív foglalás, ha annak időintervalluma nem ütközik meglévő aktív foglalással.

Az ütközésvizsgálat a következő logikát használja:

existing.start < requested.end
AND
existing.end > requested.start

Ez azt jelenti, hogy például:

10:00 ───────── 12:00
11:00 ───────── 13:00

ütközésnek számít.

Viszont az egymást csak a határponton érintő foglalások megengedettek:

10:00 ───────── 12:00
12:00 ───────── 14:00

Ez két külön, egymást nem átfedő időintervallum.

A rendszer az ütközést kizárólag ACTIVE állapotú foglalásoknál vizsgálja. Egy lemondott foglalás ezért már nem akadályozza új foglalás létrehozását.

7. Foglalás lemondása

A foglalás törlése helyett a rendszer állapotot változtat.

ACTIVE
  │
  │ cancel
  ▼
CANCELLED

Ez lehetővé teszi a foglalási előzmény megőrzését.

Egy már CANCELLED állapotú foglalás ismételt lemondása nem engedélyezett, ilyenkor a rendszer hibát jelez.

8. Validáció és hibakezelés

A rendszer több szinten végez ellenőrzést.

DTO szint

A CreateReservationRequest @NotNull annotációkkal ellenőrzi, hogy minden szükséges mező szerepel-e a kérésben.

Service szint

A service végzi az üzleti szabályok ellenőrzését:

helyes időintervallum,
parkolóhely létezése,
parkolóhely aktív állapota,
kérelmező létezése,
foglalási ütközés.
Adatbázis szint

Az adatbázis is tartalmaz megszorításokat, például:

CHECK (start_time < end_time)

A hibák egységes kezelését a GlobalExceptionHandler végzi.

A főbb hibakódok:

Hiba	HTTP státusz
Erőforrás nem található	404
Érvénytelen foglalás	400
Már lemondott foglalás újbóli lemondása	400
Foglalási ütközés	409
Kötelező request mező hiánya	400

9. Tranzakciókezelés

A módosító service műveletek tranzakcióban futnak.

A foglalás létrehozása és lemondása @Transactional, míg a parkolóhely foglalásainak lekérdezése @Transactional(readOnly = true).

Ez elkülöníti az olvasási és módosítási műveleteket, és biztosítja, hogy az adatbázis-műveletek megfelelő tranzakciós környezetben történjenek.

10. Docker architektúra

A rendszer két Docker Compose service-ből áll:

┌──────────────────────────┐
│       app container      │
│                          │
│ Spring Boot / Java 21    │
│ port: 8080               │
└──────     ─┬─────────────┘
             │
             │ PostgreSQL
             │
             ▼
┌──────────────────────────┐
│     postgres container   │
│                          │
│ PostgreSQL 18            │
│ port: 5432               │
└──────────────────────────┘

A Spring Boot konténer a PostgreSQL konténert a Compose által biztosított postgres service néven éri el.

Az alkalmazás csak akkor indul, amikor a PostgreSQL healthcheck alapján elérhetővé vált.

A PostgreSQL adatai Docker volume-ban kerülnek tárolásra.

A Dockerfile kétlépcsős buildet használ:

Maven build Java 21 JDK környezetben.
A létrehozott JAR futtatása kisebb Java 21 JRE környezetben.

A teljes rendszer egyetlen Compose paranccsal indítható.

11. Teljesítményre vonatkozó megfontolások

A rendszer a feladat méretéhez igazodó, egyszerű adatkezelési megoldást alkalmaz.

A foglalási ütközés ellenőrzésére nem történik teljes foglalási lista alkalmazásoldali betöltése. A ReservationRepository közvetlenül egy exists... lekérdezéssel ellenőrzi, hogy létezik-e megfelelő aktív, ütköző foglalás.

A parkolóhely foglalásainak lekérdezése szintén közvetlen adatbázis-lekérdezést használ, és kezdési időpont szerint rendezett eredményt ad.

A jelenlegi feladat nem határoz meg konkrét terhelési célszámot, ezért külön skálázási vagy cache-elési megoldás nem került bevezetésre.

12. Tesztelés

A projektben unit, web rétegű és alkalmazáskontextus-teszt is található.

A service tesztek többek között ellenőrzik:

sikeres foglalás létrehozását,
időintervallum-ütközést,
egymást csak határponttal érintő foglalásokat,
lemondott foglalás utáni újrafoglalást,
nem létező parkolóhelyet,
inaktív parkolóhelyet,
nem létező kérelmezőt,
hibás időintervallumot,
foglalás lemondását,
már lemondott foglalás újbóli lemondását.

A controller tesztek ellenőrzik többek között a 409 Conflict és a 400 Bad Request HTTP válaszokat.

Emellett az alkalmazás context betöltését is külön teszt ellenőrzi.

13. Összegzés

A megvalósított rendszer egy rétegekre bontott, Spring Boot alapú REST backend, amely PostgreSQL adatbázisban tárolja a parkolóhelyeket, kérelmezőket és foglalásokat.

A foglalási üzleti szabályok a service rétegben kerülnek kezelésre, míg az adatbázis további integritási megszorításokat biztosít. A Flyway gondoskodik az adatbázis reprodukálható inicializálásáról és a kezdő adatok létrehozásáról.

A rendszer Docker Compose segítségével az adatbázissal együtt, egyetlen paranccsal futtatható.