# 리팩토링 계획

## 현재 코드의 문제점

### 문제 1. 단일 책임 원칙(SRP) 위반 — God Class
`Assemble.java` 한 파일이 아래 역할을 모두 담당하고 있습니다.

| 역할 | 해당 메서드 |
|------|-------------|
| UI 출력 | `showCarTypeMenu()`, `showEngineMenu()` ... |
| 입력 유효성 검사 | `isValidRange()` |
| 상태 저장 | `int[] stack` |
| 부품 선택 처리 | `selectCarType()`, `selectEngine()` ... |
| 비즈니스 로직 | `isValidCheck()`, `runProducedCar()`, `testProducedCar()` |
| 흐름 제어 | `main()` |

---

### 문제 2. static 전역 상태 (`int[] stack`)
```java
private static int[] stack = new int[5]; // 전역 가변 상태
```
- 어떤 메서드에서든 암묵적으로 읽고 쓸 수 있어 흐름 추적이 어렵습니다.
- 단위 테스트 시 Reflection으로 강제 초기화해야 하는 원인입니다.

---

### 문제 3. 매직 넘버 — 같은 숫자가 다른 의미
```java
private static final int SEDAN  = 1, SUV = 2, TRUCK = 3;
private static final int GM     = 1, TOYOTA = 2, WIA = 3;   // GM == SEDAN == 1
private static final int MANDO  = 1, CONTINENTAL = 2, ...;  // MANDO == GM == 1
private static final int BOSCH_S = 1, MOBIS = 2;            // BOSCH_S == MANDO == 1
```
int 타입이라서 컴파일러가 혼용을 잡아주지 못합니다.  
예: `stack[CarType_Q] == GM`은 실수로 써도 컴파일이 통과합니다.

---

### 문제 4. 중복 로직 — DRY 위반
`isValidCheck()`와 `testProducedCar()`가 **동일한 5가지 조건**을 별도로 구현합니다.
규칙이 추가되거나 변경될 때 두 곳을 동시에 수정해야 합니다.

```java
// isValidCheck()
if (stack[CarType_Q] == SEDAN && stack[BrakeSystem_Q] == CONTINENTAL) return false;

// testProducedCar() — 동일 조건 반복
if (stack[CarType_Q] == SEDAN && stack[BrakeSystem_Q] == CONTINENTAL) {
    fail("Sedan에는 Continental제동장치 사용 불가");
}
```

---

### 문제 5. 인라인 문자열 매핑 산재
숫자 → 이름 변환 코드가 여러 메서드에 흩어져 있습니다.
```java
// selectCarType()
a == 1 ? "Sedan" : a == 2 ? "SUV" : "Truck"

// runProducedCar()
String[] carNames = {"", "Sedan", "SUV", "Truck"};  // 또 다른 방식으로 중복
```

---

## 리팩토링 계획

### Step 1. Enum 도입 — 매직 넘버 제거

**대상 파일 (신규 생성)**
```
java/
├── CarType.java
├── Engine.java
├── BrakeSystem.java
└── SteeringSystem.java
```

**변경 내용**
- 4종 부품을 각각 `enum`으로 추출
- 각 enum 항목이 `displayName`(출력용 이름)을 직접 보유
- `fromIndex(int)`로 숫자 입력을 enum으로 변환

```java
// 변경 전
private static final int SEDAN = 1, SUV = 2, TRUCK = 3;
String name = a == 1 ? "Sedan" : a == 2 ? "SUV" : "Truck";

// 변경 후
public enum CarType {
    SEDAN(1, "Sedan"), SUV(2, "SUV"), TRUCK(3, "Truck");

    private final int index;
    private final String displayName;
    ...
}
```

**기대 효과**
- 타입 안전성: `CarType` 자리에 `Engine`을 넣으면 컴파일 오류
- 이름 매핑 중복 제거: `displayName`을 enum에 한 곳으로 통합
- 단위 테스트에서 Reflection 없이 enum 직접 사용 가능

---

### Step 2. Car 도메인 객체 도입 — 전역 상태 제거

**대상 파일 (신규 생성)**
```
java/
└── Car.java
```

**변경 내용**
- `int[] stack`을 대체하는 `Car` 클래스
- 4가지 부품 선택 상태를 인스턴스 필드로 보유

```java
// 변경 전
private static int[] stack = new int[5];
stack[CarType_Q] = 2; // SUV인지 알 수 없음

// 변경 후
public class Car {
    private CarType carType;
    private Engine engine;
    private BrakeSystem brakeSystem;
    private SteeringSystem steeringSystem;

    // getter / setter
}
```

**기대 효과**
- 전역 상태 제거 → 흐름이 명확해짐
- 단위 테스트에서 `new Car()`로 독립적인 상태 생성 가능 → Reflection 불필요
- `Car` 객체를 다른 메서드에 명시적으로 전달하므로 의존성이 드러남

---

### Step 3. CompatibilityRule 도입 — 중복 로직 통합

**대상 파일 (신규 생성)**
```
java/
└── CompatibilityRule.java
```

**변경 내용**
- 5가지 호환성 규칙을 `CompatibilityRule` 객체 리스트로 관리
- 각 규칙은 **조건** + **오류 메시지**를 함께 보유
- `isValidCheck()`와 `testProducedCar()` 모두 이 리스트를 참조

```java
// 변경 전 — 두 메서드에 동일 조건 중복
// isValidCheck()  ← 조건만
// testProducedCar() ← 조건 + 메시지 따로

// 변경 후
public class CompatibilityRule {
    private final Predicate<Car> condition;   // 위반 조건
    private final String message;             // 오류 메시지

    public static final List<CompatibilityRule> RULES = List.of(
        new CompatibilityRule(
            car -> car.getCarType() == SEDAN && car.getBrakeSystem() == CONTINENTAL,
            "Sedan에는 Continental제동장치 사용 불가"
        ),
        ...
    );

    public boolean isViolated(Car car) {
        return condition.test(car);
    }
}
```

```java
// isValidCheck() — RULES 참조
boolean isValid(Car car) {
    return RULES.stream().noneMatch(rule -> rule.isViolated(car));
}

// testProducedCar() — 동일 RULES 참조
Optional<String> findViolation(Car car) {
    return RULES.stream()
                .filter(rule -> rule.isViolated(car))
                .map(CompatibilityRule::getMessage)
                .findFirst();
}
```

**기대 효과**
- 규칙 추가/변경 시 한 곳(`RULES` 리스트)만 수정
- 규칙과 메시지가 같은 위치에 정의되어 일관성 보장

---

### Step 4. 관심사 분리 — 클래스 분리

**대상 파일 (신규 생성)**
```
java/
├── ConsoleMenu.java      // 메뉴 출력
├── AssemblyService.java  // 비즈니스 로직 (run / test)
└── Assemble.java         // 흐름 제어 + main (현재 파일 대폭 축소)
```

| 클래스 | 역할 | 포함 메서드 |
|--------|------|-------------|
| `ConsoleMenu` | 화면 출력, 입력 유효성 검사 | `showCarTypeMenu()` 등 5개 메뉴, `isValidRange()` |
| `AssemblyService` | 비즈니스 로직 | `run(Car)`, `test(Car)` |
| `Assemble` (축소) | 흐름 제어 + main | `main()`, 단계 이동 로직 |

```java
// 변경 후 Assemble.main() 의 모습
public static void main(String[] args) {
    ConsoleMenu menu = new ConsoleMenu();
    AssemblyService service = new AssemblyService();
    Car car = new Car();
    int step = 0;

    while (true) {
        menu.show(step, car);
        int answer = menu.readInput(step);
        if (answer == EXIT) break;

        step = service.process(step, answer, car);
    }
}
```

**기대 효과**
- `Assemble.java`의 코드 라인 수가 대폭 줄어듦
- 각 클래스를 독립적으로 테스트 가능
- UI 변경(예: GUI 전환)이 `ConsoleMenu`만 교체로 가능

---

## 리팩토링 순서 요약

```
Step 1  Enum 도입          CarType / Engine / BrakeSystem / SteeringSystem
   ↓
Step 2  Car 도메인 객체     int[] stack → Car 인스턴스
   ↓
Step 3  CompatibilityRule  isValidCheck + testProducedCar 중복 제거
   ↓
Step 4  클래스 분리         ConsoleMenu / AssemblyService / Assemble(축소)
```

각 Step이 완료될 때마다 `.\gradlew.bat test`로 기존 테스트 80개가 전부 통과하는지 확인합니다.

---

## 최종 파일 구조

```
java/
├── Assemble.java          // main + 흐름 제어만
├── Car.java               // 부품 선택 상태 도메인 객체
├── CarType.java           // enum
├── Engine.java            // enum
├── BrakeSystem.java       // enum
├── SteeringSystem.java    // enum
├── CompatibilityRule.java // 호환성 규칙 목록 + 검증
├── ConsoleMenu.java       // 메뉴 출력 + 입력 처리
└── AssemblyService.java   // run / test 비즈니스 로직
```
