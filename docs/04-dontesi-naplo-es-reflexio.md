Döntési napló és reflexió – Parkolóhely-foglalási rendszer
1. Bevezetés

A fejlesztés során több olyan technikai döntést kellett meghozni, amelyek meghatározták a rendszer felépítését és működését. A döntések során elsősorban az egyszerű, jól karbantartható és a feladat követelményeinek megfelelő megoldás kialakítása volt a cél.

Az alábbiakban a projekt szempontjából fontosabb döntéseket és az azok mögött álló megfontolásokat foglalom össze.

2. Döntési napló
   2.1. Spring Boot alapú backend alkalmazás

Döntés:
A backend megvalósításához Spring Boot keretrendszert használunk.

Indoklás:
A Spring Boot megfelelő alapot biztosít REST API létrehozásához, az adatbázis-kezeléshez, validációhoz és tranzakciókezeléshez. A Spring Data JPA segítségével az adatbázis-hozzáférés is egyszerűen rétegezhető.

Mérlegelt alternatíva:
Egy egyszerűbb, közvetlen JDBC-alapú megoldás is megvalósítható lett volna, azonban ebben az esetben több adatbázis-kezelési kódot kellett volna kézzel megírni.

Döntés eredménye:
A Spring Boot + Spring Data JPA kombinációval a rendszer rétegei jól elkülöníthetők, miközben a megoldás mérete továbbra is kezelhető marad.

2.2. PostgreSQL használata relációs adatbázisként

Döntés:
A perzisztens adatok tárolására PostgreSQL adatbázist választottunk.

Indoklás:
A projekt adatai természetesen illeszkednek relációs adatmodellbe. A parkolóhelyek, kérelmezők és foglalások között egyértelmű kapcsolatok vannak, amelyek idegen kulcsokkal és adatbázis-szintű megszorításokkal kezelhetők.

A PostgreSQL ezen felül alkalmas arra, hogy az alkalmazás üzleti szabályait kiegészítő integritási feltételeket is biztosítson.

Mérlegelt alternatíva:
Egy beágyazott adatbázis, például H2 használata egyszerűbb fejlesztői környezetet eredményezett volna, azonban a végleges rendszerhez egy különálló, konténerizált relációs adatbázis jobban illeszkedik.

Döntés eredménye:
A PostgreSQL külön Docker konténerben fut, az alkalmazás pedig hálózaton keresztül kapcsolódik hozzá.

2.3. Flyway használata adatbázis-migrációhoz

Döntés:
Az adatbázis sémáját és a kezdő adatokat Flyway migrationök segítségével kezeljük.

Indoklás:
A migrationök lehetővé teszik, hogy az adatbázis létrehozása reprodukálható legyen. Így az alkalmazás futtatásához nem szükséges manuálisan létrehozni a táblákat vagy beszúrni a kezdő adatokat.

A projektben külön migration hozza létre az adatbázis sémáját, majd egy további migration tölti be a kezdő adatokat.

Mérlegelt alternatíva:
A táblák létrehozását kizárólag Hibernate/JPA segítségével is meg lehetett volna oldani, illetve az adatokat külön SQL scriptből lehetett volna betölteni.

Döntés eredménye:
A Flyway használatával az adatbázis verziózott és reprodukálható módon inicializálható.

2.4. Foglalási ütközések kezelése az adatbázis-lekérdezésben

Döntés:
A foglalási ütközéseket közvetlenül az adatbázisban végrehajtott lekérdezéssel ellenőrizzük, nem pedig az összes foglalás alkalmazásoldali betöltésével.

Indoklás:
A foglalási ütközés eldöntéséhez nincs szükség az összes foglalás visszaadására az alkalmazásnak. A repository egy célzott lekérdezéssel azt vizsgálja, hogy létezik-e olyan aktív foglalás, amely átfedésben van a kért időintervallummal.

Az alkalmazás így csak az ellenőrzés eredményét kapja meg.

Mérlegelt alternatíva:
Megoldható lett volna az összes releváns foglalás lekérdezése, majd az ütközések ellenőrzése Java kódban. Ez azonban felesleges adatmozgatással és összetettebb alkalmazásoldali logikával járna.

Döntés eredménye:
A foglalási szabály egyszerűen és célzottan ellenőrizhető.

2.5. Foglalás törlése helyett állapotváltoztatás

Döntés:
A foglalás lemondásakor nem töröljük fizikailag a rekordot, hanem annak állapotát CANCELLED értékre változtatjuk.

Indoklás:
Ezzel megőrizhető a korábbi foglalás adata és annak ténye, hogy a foglalást korábban létrehozták, majd lemondták.

A megoldás lehetővé teszi azt is, hogy a lemondott foglalás már ne akadályozza új foglalás létrehozását.

Mérlegelt alternatíva:
A foglalás tényleges törlése egyszerűbb megoldás lett volna, azonban ebben az esetben elveszne a foglalás korábbi állapota.

Döntés eredménye:
A foglalás életciklusa egyszerű állapotátmenettel kezelhető:

ACTIVE → CANCELLED
2.6. Docker Compose használata az alkalmazás futtatásához

Döntés:
Az alkalmazást és a PostgreSQL adatbázist Docker Compose környezetben futtatjuk.

Indoklás:
A megoldás segítségével a teljes futtatási környezet egységesen reprodukálható. A felhasználónak nem kell külön PostgreSQL szervert telepítenie vagy manuálisan konfigurálnia az alkalmazás adatbázis-kapcsolatát.

A végső megoldásnál különösen fontos szempont volt, hogy a teljes rendszer egyetlen paranccsal elindítható legyen.

Mérlegelt alternatíva:
Az alkalmazás közvetlenül a host gépen, a PostgreSQL pedig külön telepítve is futtatható lett volna, ez azonban több manuális beállítást igényelne.

Döntés eredménye:
A rendszer jelenlegi állapotában Docker Compose segítségével egyetlen paranccsal indítható.

3. A fontosabb döntések összefoglalása
   Döntés	Választott megoldás	Fő szempont
   Backend technológia	Spring Boot	Egyszerű REST és réteges felépítés
   Adatbázis	PostgreSQL	Relációs adatmodell és integritás
   Migráció	Flyway	Reprodukálható adatbázis-inicializálás
   Ütközésvizsgálat	Repository lekérdezés	Célzott adatbázis-művelet
   Lemondás	CANCELLED állapot	Adatok megőrzése
   Futtatási környezet	Docker Compose	Egyszerű és reprodukálható indítás

4. Rövid reflexió

A projekt során az egyik legfontosabb tapasztalat az volt, hogy
egy viszonylag egyszerű üzleti feladatnál is érdemes már a kezdetektől
elkülöníteni az API-, üzleti és perzisztenciafelelősségeket.

A fejlesztés során különösen hasznosnak bizonyult a foglalási ütközések egyértelmű
üzleti szabályként történő kezelése, valamint az adatbázis-migrációk használata.
A Docker Compose bevezetése jelentősen egyszerűsítette a teljes rendszer futtatását,
így a végleges megoldás egyetlen paranccsal elindítható.
A hibakezelés és a validáció külön rétegekben történő kialakítása átláthatóbbá tette az API működését.
Összességében a fejlesztés során szerzett tapasztalatok alapján a réteges felépítés és
az automatizált környezet-inicializálás a későbbi hasonló projektekben is hasznos kiindulópont lenne.