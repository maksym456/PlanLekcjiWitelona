DOKUMENTACJA PROJEKTOWA PLAN LEKCJI DLA INFORMATYKÓW

Cel projektu

Celem projektu jest stworzenie aplikacji, dzięki której użytkowanie planu lekcji naszej uczelni będzie wygodniejsze, bardziej estetyczne, szybsze i możliwe do odtworzenia offline w każdym przypadku. Niestety plan lekcji naszej uczelni nie jest zbyt wygodny w obsługiwaniu, dlatego aplikacja ta jest realnie użyteczna i pomaga w wygodny i przyjemny sposób szybko sprawdzić plan lekcji, co może przełożyć się na mniej nieporozumień z numerami sali, oszczędność czasu i używanie czegoś bardziej estetycznego.

Dane techniczne projektu:

•	Język programowania: Java

•	Biblioteki: Zbyt dużo, by wymieniać, między innymi biblioteka do aplikacji android, do połączenia z Internetem oraz do zarządzania obiektami json


•	Kompilator: Android Studio

•	Aplikacja do projektowania UI Całej aplikacji: Android Studio


•	System na którym aplikacja była projektowana: Windows 11


•	Wymagania systemowe aplikacji: System android 

Zakres projektu

Projekt obejmuje:

•	Pobieranie danych o planie zajęć z serwera – program, w momencie odświeżania, wykonuje wywołanie API i ściąga kod HTML ze strony z planem lekcji naszej szkoły

•	Parsowanie danych i zapisanie ich na urządzeniu – wykonywane po program, zaraz po pobraniu danych, parsuje je wszystkie do formatu json tworząc wygodną później do odtworzenia strukturę, którą później zapisuje na dysku urządzenia (przy każdym uruchomieniu aplikacja 
korzysta z tego pliku)

•	Wybieranie Grupy – bezpośrednio w aplikacji, można z wysuwanego menu wybrać grupę, która natychmiast po wybraniu odświeży plan oraz zapisze daną grupę w osobnym pliku, dzięki czemu grupa zapisuje się zawsze nawet po zresetowaniu aplikacji.

•	Wczytywanie planu lekcji – najważniejsza funkcja, wyświetlanie i pokazywanie listy zajęć dla danego dnia.

•	Wybieranie dnia w kalendarzu – możliwe jest wybranie konkretnego dnia w kalendarzu klikając na miesiąc, otwiera się typowy adroidowy pop-up kalendarz.

•	Wybieranie dnia tygodnia danego tygodnia jednym z przycisków dni tygodnia – wygodniejszy sposób, bez potrzeby wchodzenia w kalendarz można szybko poruszać się między dniami danego tygodnia.

Architektura projektu

W projekcie zawartych jest wiele plików, pokrótce omówię zawartość każdego z nich:

CustomBaseAdapter.java

Plik ten rozszerza bazowy adapter androida, umożliwiając dynamiczne wyświetlanie listy różnej wielkości

Dateutils.java

Plik ten zawiera w sobie tylko małą funkcję zarządzającą nazwami miesięcy wyświetlanymi na przycisku.

FileUtils.java

Plik ten zajmuje się operacjami na plikach, zapisywaniem grupy do pliku, zapisywaniem zparsowanych danych do pliku, sprawdzaniem czy plik istnieje i najważniejszym, czyli odczytywaniem danych z pliku.

GroupUtils.java

W pliku jest tylko jedna funkcja, zarządzająca nazwami grup do ich wyświetlenia w liście do wyboru.

HtmlParser.java

Plik zarządzający parsowaniem danych używając jednej zmiennej tekstowej zawierającej kod źródłowy html całej strony. Zwraca odpowiednie wartości dni tygodnia używając tekstu ze strony, układa wszystko w wygodny później do odczytania sposób.

listLesson.java

Plik w którym znajduje się funkcja zarządzająca listą lekcji, ustawia widok itd.

NetworkUtils.java

Zarządza pobieraniem całego kodu html ze strony planu zajęć naszej uczelni.

MainActivity.java

W tej funkcji spina się wszystko, najważniejsza funkcja. Są tutaj funkcje do aktualizowania tekstu na przyciskach, do aktualizowania widoku listy zajęć, do aktualizowania nazwy grupy na górze, obsługuje wybór elementów menu, obsługuje wszystkie przyciski w programie i w niej jest metoda zarządzania kalendarzem. 

Dodatkowo, w projekcie znajduje się kilka plików xml, w których znajduje się całe UI


Są nimi:

activity_main.xml

Jest to główny ekran, jest na nim 8 przycisków, lista zajęć oraz pole tekstowe (do wyświetlania informacji o braku zajęć danego dnia). Wszystko jest w tak zwanym „constraint layout”, czyli całe ułożenie wszystkich elementów i ich rozmiary są definiowane „przyczepianiem” do siebie nawzajem ich krawędzi, lub do krawędzi ekranu.

activity_list.xml

Jest to jeden element listy, w każdym elemencie osobno jest miejsce na salę, nauczyciela, nazwę lekcji oraz numer lekcji. Wszystkie te dane aktualizowane są w funkcjach javy.

menu.xml

Zarządza to wysuwanym menu wyboru grup.


Wszystkie te pliki to jedynie część wszystkich plików projektowych, większość z nich zawiera pojedyncze grafiki, lub teksty i zmienne używane w programie, więc nie będę ich opisywał.

Możliwości przyszłych usprawnień:

Rozszerzenie możliwości na inne semestry dostępne na stronie oraz na inne kierunki

Rozszerzenie możliwości na rozpiskę dla prowadzących.

Zamiana kalendarza oraz wysuwanego menu na alternatywne, bardziej nowoczesne, niż standardowe androidowe

Dostęp do bazy danych i korzystanie z api do zdobycia bezpośrednio danych o planie lekcji, znacznie zwiększyłoby to optymalność całego projektu

Możliwość wyboru motywu w aplikacji

Podsumowanie

Ta aplikacja stanowi odpowiedź na potrzebę posiadania wygodniejszego planu lekcji, ze względu na brak jakichkolwiek innych wygodnych ani estetycznych opcji. Jest zrobiona w sposób w miarę łatwy do rozwinięcia i ma duży potencjał do dalszego rozwoju. Mam nadzieję, że okaże się użyteczna nie tylko jako projekt zaliczający przedmiot, ale również jako realnie ułatwiająca życie studenckie ludziom uczęszczającym do  uczelni Collegium Witelona.
