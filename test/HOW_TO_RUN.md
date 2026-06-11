# 테스트 실행 가이드

## 필요 환경
- JDK 11 이상
- JUnit 5 (junit-platform-console-standalone JAR)

## JUnit 5 JAR 다운로드

아래 링크에서 `junit-platform-console-standalone-1.10.x.jar`를 다운로드하여  
`lib/` 폴더에 저장합니다.

```
https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/1.10.2/junit-platform-console-standalone-1.10.2.jar
```

```
car-assembly/
├── java/
│   └── Assemble.java
├── lib/
│   └── junit-platform-console-standalone-1.10.2.jar   ← 여기에 저장
└── test/
    └── AssembleTest.java
```

## 컴파일

프로젝트 루트(`car-assembly/`)에서 실행합니다.

```bash
# Windows
javac -cp "lib/junit-platform-console-standalone-1.10.2.jar" -d out/test java/Assemble.java test/AssembleTest.java

# macOS / Linux
javac -cp "lib/junit-platform-console-standalone-1.10.2.jar" -d out/test java/Assemble.java test/AssembleTest.java
```

## 테스트 실행

```bash
# Windows
java -jar lib/junit-platform-console-standalone-1.10.2.jar --class-path out/test --select-class AssembleTest

# macOS / Linux
java -jar lib/junit-platform-console-standalone-1.10.2.jar --class-path out/test --select-class AssembleTest
```

## 테스트 구조 (총 약 80개 테스트)

| 번호 | 테스트 클래스                    | 내용                              | 테스트 수 |
|------|----------------------------------|-----------------------------------|-----------|
| 1    | `InputValidationTest`            | 단계별 입력 유효 범위 검증         | ~25       |
| 2    | `ComponentSelectionOutputTest`   | 부품 선택 시 출력 메시지 확인      | ~9        |
| 3    | `StackStateTest`                 | 선택 후 내부 stack 값 저장 확인    | ~6        |
| 4    | `CombinationValidityTest`        | 부품 조합 허용/금지 판단 검증      | ~16       |
| 5    | `RunCarTest`                     | RUN 실행 결과 메시지 검증          | ~14       |
| 6    | `TestCarTest`                    | TEST 결과 PASS/FAIL 검증           | ~17       |

## 테스트 설계 원칙

- **Reflection 사용**: `private static` 메서드를 직접 호출하여 단위 테스트
- **stdout 캡처**: `ByteArrayOutputStream`으로 출력 메시지 검증
- **독립성**: `@BeforeEach`에서 `stack` 배열을 매번 초기화
- **리팩토링 기준선**: 리팩토링 후에도 모든 테스트가 PASS여야 함
