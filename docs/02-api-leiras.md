API-leírás – Parkolóhely-foglalási rendszer

1. Az API célja

A rendszer REST API-n keresztül biztosítja a parkolóhely-foglalások kezelését.

Az API három fő műveletet támogat:

új foglalás létrehozása,
egy parkolóhely foglalásainak lekérdezése,
foglalás lemondása.

Az API alapértelmezett portja 8080, ezért helyi futtatás esetén az alap URL:

http://localhost:8080

A végpontok gyökérútvonala:

/api/reservations

2. Foglalás létrehozása
   POST /api/reservations

Új parkolóhely-foglalást hoz létre.

Request

A kérés törzsében JSON formátumban kell megadni a parkolóhelyet, a kérelmezőt és a foglalási időintervallumot.

{
"parkingSpaceId": 1,
"requesterId": 1,
"startTime": "2026-08-20T10:00:00",
"endTime": "2026-08-20T12:00:00"
}
Mezők
Mező	Típus	Kötelező	Leírás
parkingSpaceId	Long	igen	A lefoglalni kívánt parkolóhely azonosítója
requesterId	Long	igen	A foglalást létrehozó kérelmező azonosítója
startTime	LocalDateTime	igen	Foglalás kezdő időpontja
endTime	LocalDateTime	igen	Foglalás záró időpontja

Az időpont formátuma:

yyyy-MM-ddTHH:mm:ss

például:

2026-08-20T10:00:00
Sikeres válasz
201 Created

Sikeres létrehozás esetén a szerver visszaadja az új foglalás adatait.

{
"id": 1,
"parkingSpaceId": 1,
"requesterId": 1,
"startTime": "2026-08-20T10:00:00",
"endTime": "2026-08-20T12:00:00",
"status": "ACTIVE",
"createdAt": "2026-08-13T18:30:00"
}

A createdAt értékét a rendszer a foglalás létrehozásakor állítja elő.

3. Foglalás létrehozásának ellenőrzései

A rendszer a foglalás létrehozása előtt több feltételt ellenőriz.

3.1. Kötelező mezők

Ha a requestből hiányzik valamelyik kötelező mező, a szerver:

400 Bad Request

választ ad.

Például:

{
"parkingSpaceId": 1,
"requesterId": 1
}

esetén a startTime és endTime hiányzik.

A válasz:

{
"timestamp": "2026-08-13T18:30:00",
"status": 400,
"error": "Bad Request",
"validationErrors": {
"startTime": "Start time is required.",
"endTime": "End time is required."
}
}
3.2. Érvényes időintervallum

A kezdési időpontnak korábbinak kell lennie a záró időpontnál.

startTime < endTime

Például:

10:00 → 12:00    ✓
10:00 → 10:00    ✗
12:00 → 10:00    ✗

Érvénytelen időintervallum esetén:

400 Bad Request
{
"timestamp": "2026-08-13T18:30:00",
"status": 400,
"error": "Bad Request",
"message": "Start time must be before end time."
}
3.3. Nem létező parkolóhely

Ha a megadott parkingSpaceId nem létezik:

404 Not Found
{
"timestamp": "2026-08-13T18:30:00",
"status": 404,
"error": "Not Found",
"message": "Parking space not found: 999"
}
3.4. Inaktív parkolóhely

Ha a parkolóhely létezik, de nem aktív, foglalás nem hozható létre.

400 Bad Request
{
"timestamp": "2026-08-13T18:30:00",
"status": 400,
"error": "Bad Request",
"message": "Parking space is not active: 4"
}
3.5. Nem létező kérelmező

Ha a megadott requesterId nem található:

404 Not Found
{
"timestamp": "2026-08-13T18:30:00",
"status": 404,
"error": "Not Found",
"message": "Requester not found: 999"
}
3.6. Foglalási ütközés

Egy parkolóhelyen két aktív foglalás időintervalluma nem fedheti egymást.

Például egy meglévő foglalás:

10:00 ───────────── 12:00

és egy új kérés:

11:00 ───────────── 13:00

ütközik.

Ebben az esetben:

409 Conflict
{
"timestamp": "2026-08-13T18:30:00",
"status": 409,
"error": "Conflict",
"message": "Parking space is already reserved for the requested time range."
}

A csak határponton érintkező foglalások viszont megengedettek:

10:00 ───────── 12:00
12:00 ───────── 14:00

4. Parkolóhely foglalásainak lekérdezése
   GET /api/reservations/parking-space/{parkingSpaceId}

Lekérdezi egy adott parkolóhely foglalásait.

Példa
GET /api/reservations/parking-space/1
Sikeres válasz
200 OK
[
{
"id": 1,
"parkingSpaceId": 1,
"requesterId": 1,
"startTime": "2026-08-20T10:00:00",
"endTime": "2026-08-20T12:00:00",
"status": "ACTIVE",
"createdAt": "2026-08-13T18:30:00"
},
{
"id": 2,
"parkingSpaceId": 1,
"requesterId": 2,
"startTime": "2026-08-20T14:00:00",
"endTime": "2026-08-20T16:00:00",
"status": "ACTIVE",
"createdAt": "2026-08-13T18:35:00"
}
]

A foglalások startTime szerint növekvő sorrendben érkeznek.

A lemondott foglalások is szerepelhetnek a válaszban, CANCELLED státusszal.

Nem létező parkolóhely

Ha a megadott parkolóhely nem létezik:

404 Not Found
{
"timestamp": "2026-08-13T18:30:00",
"status": 404,
"error": "Not Found",
"message": "Parking space not found: 999"
}

5. Foglalás lemondása
   DELETE /api/reservations/{reservationId}

Egy meglévő foglalást lemond.

Példa
DELETE /api/reservations/1

A rendszer a foglalást fizikailag nem törli az adatbázisból, hanem CANCELLED állapotba állítja.

Sikeres válasz
204 No Content

Sikeres lemondás esetén a válasz törzse üres.

HTTP/1.1 204 No Content
Nem létező foglalás
404 Not Found
{
"timestamp": "2026-08-13T18:30:00",
"status": 404,
"error": "Not Found",
"message": "Reservation not found: 999"
}
Már lemondott foglalás

Egy már CANCELLED állapotú foglalás ismételt lemondása nem megengedett.

400 Bad Request

A konkrét üzenetet az alkalmazás ReservationAlreadyCancelledException kivételkezelése adja vissza.

6. Foglalás állapotai

A foglalások két állapot egyikében lehetnek:

Állapot	Jelentés
ACTIVE	Aktív foglalás
CANCELLED	Lemondott foglalás

Állapotátmenet:

ACTIVE
│
│ DELETE /api/reservations/{id}
▼
CANCELLED

A CANCELLED állapotú foglalás már nem akadályozza egy új foglalás létrehozását ugyanarra a parkolóhelyre és időintervallumra.


7. Hibaválaszok

Az alkalmazás egységes hibakezelést használ.

A legtöbb üzleti hiba válaszformátuma:

{
"timestamp": "2026-08-13T18:30:00",
"status": 404,
"error": "Not Found",
"message": "Reservation not found: 999"
}

A mezők jelentése:

Mező	Leírás
timestamp	A hiba keletkezésének időpontja
status	HTTP státuszkód
error	A HTTP státuszkód szöveges megnevezése
message	A konkrét hiba oka

A request-validáció hibája ettől eltérő formátumot használ:

{
"timestamp": "2026-08-13T18:30:00",
"status": 400,
"error": "Bad Request",
"validationErrors": {
"parkingSpaceId": "Parking space ID is required."
}
}

8. HTTP státuszkódok összefoglalása
   Státusz	Jelentés
   200 OK	Sikeres lekérdezés
   201 Created	Sikeresen létrehozott foglalás
   204 No Content	Sikeres foglalás-lemondás
   400 Bad Request	Hibás bemenet vagy érvénytelen üzleti művelet
   404 Not Found	A hivatkozott erőforrás nem található
   409 Conflict	Foglalási időintervallum ütközik meglévő aktív foglalással

9. API végpontok összefoglalása
   HTTP	Endpoint	Funkció	Sikeres státusz
   POST	/api/reservations	Új foglalás létrehozása	201
   GET	/api/reservations/parking-space/{parkingSpaceId}	Parkolóhely foglalásainak lekérdezése	200
   DELETE	/api/reservations/{reservationId}	Foglalás lemondása	204

10. Swagger / OpenAPI

Az API Swagger UI-n keresztül is tesztelhető. Az alkalmazás futása után a Springdoc által biztosított Swagger felület használható az endpointok megtekintésére és interaktív meghívására.

A Swagger használata különösen alkalmas a request body-k és a különböző HTTP válaszok ellenőrzésére.

Ez így szerintem jó második dokumentumnak: rövid, de minden tényleges endpointot, requestet, response-t és üzleti hibát lefed.
