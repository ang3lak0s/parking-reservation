Felhasználói kézikönyv – Parkolóhely-foglalási rendszer
1. A rendszer rövid bemutatása

A Parkolóhely-foglalási rendszer egy REST API alapú alkalmazás, amely parkolóhelyek foglalását teszi lehetővé.

A rendszer segítségével:

új parkolóhely-foglalás hozható létre,
lekérdezhetők egy adott parkolóhely foglalásai,
meglévő foglalás mondható le.

A rendszer használatához nem szükséges közvetlenül az adatbázishoz csatlakozni. Az alkalmazás REST API-n keresztül használható, amelyhez Swagger UI biztosít interaktív felületet.

2. Előfeltételek

A teljes rendszer Docker Compose segítségével futtatható.

Szükséges:

Docker
Docker Compose

A projekt forráskódjának rendelkezésre kell állnia a számítógépen.

A PostgreSQL adatbázist nem szükséges külön telepíteni vagy manuálisan elindítani, mert azt a Docker Compose a rendszer részeként indítja el.

3. A rendszer indítása

Nyissunk terminált a projekt gyökérkönyvtárában, ahol a docker-compose.yml található.

A rendszer indítása:

docker compose up --build

A parancs automatikusan:

felépíti az alkalmazás Docker image-ét,
elindítja a PostgreSQL adatbázist,
elindítja a Spring Boot alkalmazást,
létrehozza / inicializálja az adatbázis sémáját Flyway segítségével,
betölti a kezdő adatokat.

Az alkalmazás akkor használható, amikor a Spring Boot alkalmazás sikeresen elindult.

A rendszer leállítása:

docker compose down

Az adatbázis adatai Docker volume-ban vannak, ezért a normál leállítás nem törli az adatokat.

4. A Swagger felület megnyitása

Az alkalmazás alapértelmezett HTTP portja:

8080

A Swagger UI az alkalmazás elindulása után a Springdoc által biztosított Swagger útvonalon érhető el.

A Swagger felület segítségével az API végpontjai közvetlenül a böngészőből meghívhatók.

A Swagger UI használata során nincs szükség külön API kliensre.

5. A rendszer kezdőállapota

Az alkalmazás első indításakor a Flyway automatikusan létrehozza az adatbázis szükséges tábláit és betölti a kezdő adatokat.

A rendszer kezdetben az alábbi parkolóhelyeket tartalmazza:

Parkolóhely	Állapot
P-001	aktív
P-002	aktív
P-003	aktív
P-004	inaktív

A rendszerhez az alábbi kérelmezők tartoznak:

John Doe
Jane Smith
Test User

A foglalás létrehozásakor ezeknek az adatoknak az azonosítóját kell megadni.

6. Új foglalás létrehozása

A Swagger UI-ban válasszuk ki:

POST /api/reservations

A Try it out lehetőség kiválasztása után adjuk meg a foglalás adatait.

Példa:

{
"parkingSpaceId": 1,
"requesterId": 1,
"startTime": "2026-08-20T10:00:00",
"endTime": "2026-08-20T12:00:00"
}

Ezután a Execute gombbal küldhető el a kérés.

Sikeres foglalás esetén:

201 Created

válasz érkezik.

A válasz tartalmazza a létrehozott foglalás azonosítóját, a megadott adatokat, az ACTIVE állapotot és a létrehozás időpontját.

7. Foglalási ütközés kezelése

Egy parkolóhely ugyanarra az időszakra nem foglalható le többször.

Például ha már létezik:

10:00 ───────── 12:00

foglalás, akkor egy:

11:00 ───────── 13:00

foglalási kérés elutasításra kerül.

A rendszer:

409 Conflict

választ ad.

Ez jelzi, hogy a kért időintervallum ütközik egy meglévő aktív foglalással.

Egymást követő foglalások

A következő eset viszont megengedett:

10:00 ───────── 12:00
12:00 ───────── 14:00

A második foglalás pontosan 12:00-kor kezdődik, amikor az első véget ér, ezért nincs időbeli átfedés.

8. Foglalások lekérdezése

Egy adott parkolóhely foglalásainak megtekintéséhez használjuk:

GET /api/reservations/parking-space/{parkingSpaceId}

Például az 1 azonosítójú parkolóhely esetén:

GET /api/reservations/parking-space/1

A válasz tartalmazza a parkolóhelyhez tartozó foglalásokat.

Például:

[
{
"id": 1,
"parkingSpaceId": 1,
"requesterId": 1,
"startTime": "2026-08-20T10:00:00",
"endTime": "2026-08-20T12:00:00",
"status": "ACTIVE",
"createdAt": "2026-08-13T18:30:00"
}
]

A foglalások kezdési időpont szerint rendezve jelennek meg.

9. Foglalás lemondása

Egy meglévő foglalás lemondásához használjuk:

DELETE /api/reservations/{reservationId}

Például:

DELETE /api/reservations/1

Sikeres lemondás esetén:

204 No Content

válasz érkezik.

A foglalás nem kerül fizikailag törlésre az adatbázisból. Az állapota:

ACTIVE

értékről:

CANCELLED

értékre változik.

Ezért a korábbi foglalás továbbra is megjelenhet a foglalások lekérdezésénél.

10. Lemondott foglalás utáni újrafoglalás

A lemondott foglalás már nem tekintendő aktív foglalásnak az ütközésvizsgálat során.

Ezért például:

1. foglalás
   10:00 ───────── 12:00
   ↓
   CANCELLED

után ugyanarra az időintervallumra új aktív foglalás létrehozható.

Ez lehetővé teszi, hogy egy korábban lemondott időpont ismét felhasználható legyen.

11. Gyakori hibák
    400 Bad Request

A kérés hibás vagy valamely üzleti szabályt sért.

Például:

hiányzó kötelező mező,
hibás időintervallum,
inaktív parkolóhely használata,
már lemondott foglalás újbóli lemondása.
404 Not Found

A megadott erőforrás nem található.

Például:

nem létező parkolóhely,
nem létező kérelmező,
nem létező foglalás.
409 Conflict

A foglalás időpontja ütközik egy már meglévő aktív foglalással.

12. Ajánlott tesztelési folyamat

A rendszer kipróbálásához az alábbi sorrend javasolt.

1. Foglalás létrehozása
   P-001
   John Doe
   10:00–12:00

Elvárt eredmény:

201 Created
2. Ütköző foglalás
   P-001
   Jane Smith
   11:00–13:00

Elvárt eredmény:

409 Conflict
3. Határon érintkező foglalás
   P-001
   Jane Smith
   12:00–14:00

Elvárt eredmény:

201 Created
4. Foglalások lekérdezése
   GET /api/reservations/parking-space/1

Ekkor a két sikeresen létrehozott foglalás látható.

5. Foglalás lemondása
   DELETE /api/reservations/1

Elvárt eredmény:

204 No Content
6. Újrafoglalás

Az eredetileg lefoglalt időszakra ismét létrehozható foglalás, mivel az előző foglalás már CANCELLED állapotú.

13. Rendszer leállítása

A rendszer leállításához a projekt gyökérkönyvtárában:

docker compose down

A PostgreSQL adatbázis adatai a Docker volume-ban maradnak.

Teljesen tiszta adatbázis létrehozásához a volume-ok is eltávolíthatók:

docker compose down -v

Figyelem: ez az adatbázis Docker volume-ban tárolt adatait is törli.

14. Rövid használati összefoglaló
    docker compose up --build
    │
    ▼
    Swagger UI megnyitása
    │
    ▼
    POST /api/reservations
    │
    ▼
    Foglalás létrehozása
    │
    ├──────► GET /api/reservations/parking-space/{id}
    │                    │
    │                    ▼
    │             Foglalások megtekintése
    │
    └──────► DELETE /api/reservations/{id}
    │
    ▼
    Foglalás lemondása

