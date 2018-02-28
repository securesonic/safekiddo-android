#SafeKiddo Android README

* * *

## Wersjonowanie

Wersja aplikacji dla systemu 2.x+ rozpoczyna się od numeru 10xxx
Wersja dla systemy 4.x+ rozpoczyna sie od numeru 14xxx
Przykład (wersja aplikacji numer 58): 
- system 2.3: 10000058
- system 3.2: 10000058
- system 4.0: 14000058
- system 4.4: 14000058
- itd...

## Użyte biblioteki

### BugSense v3.6.1
Raportowanie błędów aplikacji, wysyła info z logiem błędów (web + mail)

[https://www.bugsense.com/](https://www.bugsense.com/)

### querybuilder v1
Własna biblioteka do prostych składań zapytań sqlite. Nie potrzeba licencji ;-)

### console v2
Własna biblioteka do wyświetlania informacji na konsoli (zapis do pliku lub bazy + logcat). Nie potrzeba licencji ;-)

### Styled Dialogs
Własna biblioteka do wyświetlania okien dialogowych (DialogFragment) + stylizacja. Nie potrzeba licencji ;-)

### gson v2.3
obsługa JSONa

[strona z projektem](https://code.google.com/p/google-gson/)

[Licencja - Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0)

### OkHttp v2.0.0
wykonywanie żądań http/https

[strona z projektem](http://square.github.io/okhttp/)

[Licencja - Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0)

### Okio v1.0.1
Biblioteka IO na potrzeby OkHttp 2.x

[strona z projektem](https://github.com/square/okio)

[Licencja - Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0)

### StickyListHeaders v2.5.2
przyklejane nagłówki list

[strona z projektem](https://github.com/emilsjolander/StickyListHeaders)

[Licencja - Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0)

### SystemBarTint v1.0.3
przezroczyste paski nawigacji i pasek statusu dla android 4.4+

[strona z projektem](https://github.com/jgilfelt/SystemBarTint)

[Licencja - Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0)

### Spring Framework v4.1.0.RC1
Części kodu odpowiedzialne za enkodowanie adresu URL (klasa UriUtils)

[strona z projektem](http://projects.spring.io/spring-framework/) |
[strona z projektem GitHub](https://github.com/spring-projects/spring-framework/releases)

[Licencja - Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0)

### SmoothProgressBar v0.5.1
ProgressBar - indeterminate

[strona z projektem](https://github.com/castorflex/SmoothProgressBar) 

[Licencja - Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0)

### LittleProxy v1.0.0-beta8 
[https://github.com/adamfisk/LittleProxy](https://github.com/adamfisk/LittleProxy)

Server proxy. Trochę rzeczy jest przerobionych z oryginalnego projektu (dostosowanie do androida), wywalona została obsługa protokołu UDP
Niby wersja beta ale działa całkiem nieźle ;-)

[Licencja - Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0)


#### Aby LittleProxy chodziło wymagane jest kilka dodatkowych bibliotek

1. commons-lang
	* wersja: 3.3
	* licencaj: [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0)
	* [strona projektu](http://commons.apache.org/proper/commons-lang/)
1. log4j
	* wersja: 1.2.17
	* licencaj: [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0)
	* [strona projektu](http://logging.apache.org/log4j/1.2/)
1. slf4j (slf4j-api, slf4j-simple)
	* wersja: 1.7.2
	* licencja: [MIT license](http://www.slf4j.org/license.html)
	* [strona projektu](http://www.slf4j.org/)
1. netty (all)
	* wersja: 4.0.23.Final
	* licencaj: [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0)
	* [strona projektu](http://netty.io/)
1. commons validator 
	* wersja: 1.4.0
	* licencaj: [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0)
	* [strona projektu](http://commons.apache.org/proper/commons-validator/index.html)
1. jzlib
	* wersja: 1.1.3
	* licencaj: [GNU LGPL](http://www.jcraft.com/jzlib/LICENSE.txt)
	* [strona projektu](http://www.jcraft.com/jzlib/)

### Biblioteki Googla wraz z SDK androia
1. google play services (rev. 20.0.0)
1. android support v7 appcompat (rev. 21.0.0)
1. android support v4 (rev. 21.0.0)

Wszystko na licencji [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0)

