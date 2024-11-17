
# Запускалки

Server:
- ./gradlew :server:run

Android:
- Из MainActivity
- Из runConfiguration

iOS:
- пока не опробовано

Desktop:
- ./gradlew run

Web:
- ./gradlew :composeApp:wasmJsBrowserDevelopmentRun
- ./gradlew wasmJsBrowserRun -t --quiet