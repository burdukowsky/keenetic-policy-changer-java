# keenetic-policy-changer-java

Меняет политику для определенного устройства по
MAC-адресу на роутере Keenetic по протоколу Telnet.

## Сборка
```
./gradlew uberJar
```

## Использование
### Установка политики по умолчанию
```
java -jar app\build\libs\app-uber.jar -password=password -action=SET_DEFAULT -mac=00:00:00:00:00:00
```
### Установка политики Policy0
```
java -jar app\build\libs\app-uber.jar -password=password -action=SET_CUSTOM -mac=00:00:00:00:00:00 -policy=Policy0
```
### Без доступа в Интернет
```
java -jar app\build\libs\app-uber.jar -password=password -action=SET_OFFLINE -mac=00:00:00:00:00:00
```

### Изменение параметров по умолчанию
По умолчанию используются IP "192.168.1.1", порт 23 и логин "admin". 
Для изменения используйте параметры `-ip`, `-port` и `-login`:
```
java -jar app\build\libs\app-uber.jar -password=password -action=SET_DEFAULT -mac=00:00:00:00:00:00 -ip="192.168.0.1" -port=8023 -login=administrator
```

## JDK
Temurin@11.0.26+4
