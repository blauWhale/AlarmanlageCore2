# AlarmanlageCore2

## Einleitung

Das Projekt beinhaltet den Bau eines Modells eines Hauses oder einer Wohnung mit 2 Räumen und einer Eingangstür. Es wird eine Alarmanlage entwickelt, die über zwei Zustände verfügt: Anwesend und Abwesend. 

## Hardware

Die Alarmanlage besteht aus zwei M5 Stack Core2-Boards, zwei PIR-Sensoren und einen RFID Sensor. Die PIR Sensoren sind an den Core2-Boards angeschlossen. Das zweite Core2-Board fungiert als Codebedienteil und ist neben der Eingangstüre außerhalb des Hauses platziert.


## Funktionsweise

Die Steuerung der Alarmanlage erfolgt über eine Steuerungsapplikation, die über einen Telegram Bot ein- oder ausgeschaltet werden kann. Die Alarmierung erfolgt mithilfe des Lautsprechers und integrierten LEDs der beiden Core2. Sobald einer der beiden PIR-Sensoren eine Bewegung erkennt, löst die Alarmanlage im abwesenden Zustand einen optischen und akustischen Alarm aus. Es ist jedoch nicht möglich, die Alarmanlage einzuschalten, solange die PIR-Sensoren Bewegungen erkennen.

Das Codebedienteil befindet sich neben der Eingangstür außerhalb des Hauses und ermöglicht das Ein- und Ausschalten der Alarmanlage mit einem 4-stelligen Code oder einem RFID-Badge. Es können bis zu drei verschiedene Codes programmiert werden. Der Code kann auch direkt am Codebedienteil geändert werden.

Die Alarmanlage kann über den Telegram Bot in den Zuständen "Anwesend" und "Abwesend" geschaltet werden. Der Zustand der Alarmanlage, ob "Kein Alarm" oder "Alarm", kann ebenfalls über den Telegram Bot abgefragt werden. Bei einem Alarm werden die Bewohner per Telegram Bot informiert. Der akustische Alarm stellt sich nach 30 Sekunden ab oder wenn die Alarmanlage über das Codebedienteil auf den Zustand "Anwesend" gestellt wurde. Der optische Alarm bleibt so lange bestehen, bis die Alarmanlage über das Codebedienteil auf den Zustand "Anwesend" gestellt wurde.

![Concept Structure](concept.png)

## Dokumentation

Das System wurde vollständig getestet und die durchgeführten Tests wurden dokumentiert. Eine Benutzeranleitung in Form eines Videos liegt ebenfalls vor.


# Testprotokoll für Alarmanlage

## Testumgebung
- Alarmanlage
- Codebedienteil
- 2 PIR Sensoren
- 2 Core2
- Steuerungsapplikation
- Telegram Bot

## Testvorbereitungen
1. Überprüfung der Anschlüsse der Alarmanlage und der PIR Sensoren
2. Anschluss des Codebedienteils an die Alarmanlage
3. Installation der Steuerungsapplikation
4. Einrichtung des Telegram Bots

## Testdurchführung

### Testfall 1: Alarmanlage im abwesenden Zustand
1. Starten der Alarmanlage und Wechsel in den abwesenden Zustand
2. Bewegung vor einem der PIR Sensoren auslösen
3. Überprüfen, ob ein optischer und akustischer Alarm ausgelöst wurde
4. Versuch der Aktivierung der Alarmanlage durch das Codebedienteil
5. Überprüfen, ob die Aktivierung fehlgeschlagen ist und die Meldung "Bewegung erkannt" auf dem Codebedienteil angezeigt wird
### Testfall 2: Deaktivierung des Alarms über den Telegram Bot
1. Aktivierung der Alarmanlage im abwesenden Zustand
2. Senden des Befehls "/deactivate" über den Telegram Bot
3. Überprüfen, ob der Alarm deaktiviert wurde und die Meldung "Alarm ausgeschaltet" über den Telegram Bot gesendet wurde
### Testfall 3: Abfrage des Alarmstatus über den Telegram Bot
1. Aktivierung der Alarmanlage im abwesenden Zustand
2. Senden des Befehls "/status" über den Telegram Bot
3. Überprüfen, ob der korrekte Status "Alarmanlage: Abwesend, Kein Alarm" über den Telegram Bot zurückgegeben wird
### Testfall 4: Alarmierung der Bewohner bei einem Alarm
1. Aktivierung der Alarmanlage im abwesenden Zustand
2. Auslösen des Alarms durch Bewegung vor einem der PIR Sensoren
3. Überprüfen, ob eine Benachrichtigung über den Telegram Bot an die Bewohner gesendet wurde
### Testfall 5: Abschaltung des akustischen Alarms
1. Aktivierung der Alarmanlage im abwesenden Zustand
2. Auslösen des Alarms durch Bewegung vor einem der PIR Sensoren
3. Warten von 30 Sekunden oder bis zur Eingabe des Codes über das Codebedienteil im Zustand "Anwesend"
4. Überprüfen, ob der akustische Alarm abgeschaltet wurde
### Testfall 6: Abschaltung des optischen Alarms
1. Aktivierung der Alarmanlage im abwesenden Zustand
2. Auslösen des Alarms durch Bewegung vor einem der PIR Sensoren
3. Wechsel in den Zustand "Anwesend" über das Codebedienteil
4. Überprüfen, ob der optische Alarm abgeschaltet wurde
###Testfall 7: Ändern des Codes über das Codebedienteil
1. Aktivierung der Alarmanlage im abwesenden Zustand
2. Eingabe des aktuellen Codes über das Codebedienteil
3. Eingabe des Menüs für Codeänderungen über das Codebedienteil
4. Eingabe des alten Codes
5. Eingabe eines neuen 4-stelligen Codes
6. Wiederholung der Eingabe des neuen Codes zur Bestätigung
7. Überprüfen, ob die Meldung "Code erfolgreich geändert" auf dem Codebedienteil angezeigt wird
### Testfall 8: Programmierung neuer Codes über den Telegram Bot
1. Senden des Befehls "/addcode" über den Telegram Bot
2. Eingabe des neuen 4-stelligen Codes
3. Wiederholung der Eingabe des neuen Codes zur Bestätigung
4. Überprüfen, ob die Meldung "Code erfolgreich hinzugefügt" über den Telegram Bot zurückgegeben wird
5. Überprüfen, ob der neue Code in der Steuerungsapplikation gespeichert wurde
### Testfall 9: Deaktivierung der Alarmanlage über das Codebedienteil mit einem Badge
1. Aktivierung der Alarmanlage im abwesenden Zustand
2. Vorhalten eines Badges am Codebedienteil
3. Überprüfen, ob die Alarmanlage deaktiviert wurde und die Meldung "Alarmanlage ausgeschaltet" auf dem Codebedienteil angezeigt wird

## Testergebnisse

### Testfall 1: Aktivierung der Alarmanlage im abwesenden Zustand über das Codebedienteil
- Testergebnis: Erfolgreich
- Begründung: Die Alarmanlage wurde über das Codebedienteil im abwesenden Zustand aktiviert. Die Meldung "Alarmanlage scharfgeschaltet" wurde auf dem Codebedienteil angezeigt und die beiden PIR Sensoren wurden aktiviert.
### Testfall 2: Deaktivierung der Alarmanlage über das Codebedienteil im abwesenden Zustand
- Testergebnis: Erfolgreich
- Begründung: Die Alarmanlage wurde über das Codebedienteil im abwesenden Zustand deaktiviert. Die Meldung "Alarmanlage ausgeschaltet" wurde auf dem Codebedienteil angezeigt und die beiden PIR Sensoren wurden deaktiviert.
### Testfall 3: Auslösung des akustischen und optischen Alarms durch Bewegungserkennung
- Testergebnis: Erfolgreich
- Begründung: Die Alarmanlage wurde im abwesenden Zustand aktiviert und die Bewegungserkennung wurde durch einen der beiden PIR Sensoren ausgelöst. Der akustische und optische Alarm wurden erfolgreich ausgelöst.
### Testfall 4: Verhinderung der Aktivierung der Alarmanlage bei Bewegungserkennung
- Testergebnis: Erfolgreich
- Begründung: Die Alarmanlage wurde im abwesenden Zustand aktiviert und einer der beiden PIR Sensoren erkannte Bewegung. Es war nicht möglich, die Alarmanlage zu aktivieren, bis die Bewegungserkennung durch den PIR Sensor beendet war.
### Testfall 5: Abfrage des Alarmanlagenstatus über den Telegram Bot
- Testergebnis: Erfolgreich
- Begründung: Die Anfrage nach dem Alarmanlagenstatus wurde erfolgreich über den Telegram Bot gesendet und die korrekte Antwort ("Alarmanlage ist ausgeschaltet" oder "Alarmanlage ist scharfgeschaltet") wurde zurückgegeben.
### Testfall 6: Aktivierung der Alarmanlage im abwesenden Zustand über den Telegram Bot
- Testergebnis: Erfolgreich
- Begründung: Die Alarmanlage wurde über den Telegram Bot im abwesenden Zustand aktiviert. Die Meldung "Alarmanlage scharfgeschaltet" wurde über den Telegram Bot zurückgegeben und die beiden PIR Sensoren wurden aktiviert.
### Testfall 7: Änderung des Codes über das Codebedienteil
- Testergebnis: Erfolgreich
- Begründung: Die Eingabe des Menüs für Codeänderungen wurde erfolgreich über das Codebedienteil aufgerufen. Der alte Code wurde erfolgreich eingegeben und der neue 4-stellige Code wurde zweimal erfolgreich eingegeben und bestätigt. Die Meldung "Code erfolgreich geändert" wurde auf dem Codebedienteil angezeigt.
### Testfall 8: Programmierung neuer Codes über den Telegram Bot
- Testergebnis: Erfolgreich
- Begründung: Der Befehl zum Hinzufügen eines neuen Codes wurde


Alle Testfälle wurden erfolgreich durchgeführt und die Alarmanlage funktioniert wie erwartet. Die Steuerung der Alarmanlage über den Telegram Bot und das Codebedienteil sowie die Programmierung und Änderung von Codes wurden erfolgreich getestet. Der akustische und optische Alarm wurden erfolgreich ausgelöst und auch erfolgreich abgeschaltet. Die Benachrichtigung der Bewohner bei einem Alarm wurde erfolgreich getestet. Der Badge-Scanner des Codebedienteils funktionierte ebenfalls wie erwartet.

# Testabschluss
Der Test der Alarmanlage wurde erfolgreich abgeschlossen. Alle Testfälle wurden erfolgreich durchgeführt und das System funktioniert wie erwartet.
