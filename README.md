# jennifer_testing

이 프로젝트는 다양한 예외를 의도적으로 발생시키는 엔드포인트를 제공하는 간단한 Spring Boot 애플리케이션입니다. 로그에서 실패 상황을 확인하거나 모니터링 솔루션을 테스트하는 데 사용할 수 있습니다.

## 요구 사항

- Java 17 (Gradle 빌드는 Java 툴체인을 사용합니다)
- Gradle Wrapper가 포함되어 있어 별도로 Gradle을 설치할 필요가 없습니다

## 빌드

아래 명령으로 빌드를 실행합니다.

```bash
./gradlew build
```

## 실행

Gradle을 이용하여 애플리케이션을 바로 실행할 수 있습니다.

```bash
./gradlew bootRun
```

또는 빌드 후 생성된 jar 파일을 실행합니다.

```bash
java -jar build/libs/example-1-0.0.1-SNAPSHOT.jar
```

애플리케이션을 실행한 뒤 `http://localhost:8080/index` 주소를 열어 원하는 동작을 선택하면 해당 예외가 발생합니다.
