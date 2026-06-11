# 리팩토링 계획서

## 1. 현재 코드 구조 진단

### 1-1. 역할 분석

`Assemble.java` 한 파일(271줄)이 아래 6가지 역할을 모두 담당합니다.

| 역할 | 해당 코드 | 문제 |
|------|-----------|------|
| 화면 출력 | `showCarTypeMenu()` 등 5개 메서드 | UI와 로직이 섞임 |
| 입력 유효성 검사 | `isValidRange()` | 출력과 검사가 한 메서드 |
| 전역 상태 | `static int[] stack` | 어디서든 변경 가능 |
| 부품 선택 | `selectCarType()` 등 4개 메서드 | 상태 변경 + 출력 혼재 |
| 비즈니스 로직 | `isValidCheck()`, `runProducedCar()`, `testProducedCar()` | 중복 조건 존재 |
| 흐름 제어 | `main()` | 너무 많은 역할 위임 |

### 1-2. 문제점 목록

#### 문제 1 — 매직 넘버: 다른 의미인데 같은 값

```java
private static final int SEDAN   = 1, SUV  = 2, TRUCK = 3;
private static final int GM      = 1, TOYOTA = 2, WIA  = 3;  // GM == SEDAN
private static final int MANDO   = 1, CONTINENTAL = 2, ...;  // MANDO == GM
private static final int BOSCH_S = 1, MOBIS = 2;             // BOSCH_S == MANDO
```

`int` 타입이므로 `stack[CarType_Q] == GM` 같은 실수를 컴파일러가 잡지 못합니다.

#### 문제 2 — 전역 가변 상태

```java
private static int[] stack = new int[5];
```

모든 메서드가 암묵적으로 공유합니다. 단위 테스트에서 Java Reflection으로 강제 초기화해야 하는 원인입니다.

#### 문제 3 — 중복 로직 (DRY 위반)

`isValidCheck()`와 `testProducedCar()`가 동일한 조건을 각자 구현합니다.
규칙 하나를 수정하면 두 곳을 동시에 바꿔야 합니다.

```java
// isValidCheck() 에서
if (stack[CarType_Q] == SEDAN && stack[BrakeSystem_Q] == CONTINENTAL) return false;

// testProducedCar() 에서 — 완전히 동일한 조건
if (stack[CarType_Q] == SEDAN && stack[BrakeSystem_Q] == CONTINENTAL) {
    fail("Sedan에는 Continental제동장치 사용 불가");
}
```

#### 문제 4 — 문자열 매핑 산재

숫자 → 이름 변환이 3곳에 따로 구현되어 있습니다.

```java
// selectCarType()
a == 1 ? "Sedan" : a == 2 ? "SUV" : "Truck"

// selectEngine()
a == 1 ? "GM" : a == 2 ? "TOYOTA" : a == 3 ? "WIA" : "고장난 엔진"

// runProducedCar()
String[] carNames = {"", "Sedan", "SUV", "Truck"};   // 또 다른 방식
String[] engNames = {"", "GM", "TOYOTA", "WIA"};
```

---

## 2. 리팩토링 목표

| 목표 | 수단 |
|------|------|
| 매직 넘버 제거, 타입 안전성 | Enum 도입 |
| 전역 상태 제거 | Car 도메인 객체 도입 |
| 중복 로직 제거 | CompatibilityRule 통합 |
| 단일 책임 원칙 적용 | 클래스 분리 |
| 기존 동작 보존 | 각 Step 후 `.\gradlew.bat test` 전체 통과 확인 |

---

## 3. 단계별 리팩토링 계획

### Step 1. Enum 도입

**목적**: 매직 넘버를 제거하고 부품 타입 간 혼용을 컴파일 타임에 차단합니다.

**신규 파일**

```
java/
├── CarType.java
├── Engine.java
├── BrakeSystem.java
└── SteeringSystem.java
```

**설계**

```java
// CarType.java
public enum CarType {
    SEDAN(1, "Sedan"),
    SUV  (2, "SUV"),
    TRUCK(3, "Truck");

    private final int    index;
    private final String displayName;

    CarType(int index, String displayName) {
        this.index       = index;
        this.displayName = displayName;
    }

    public String getDisplayName() { return displayName; }

    /** 메뉴 입력 숫자 → enum 변환. 범위 외 입력은 null 반환 */
    public static CarType fromIndex(int i) {
        for (CarType t : values()) if (t.index == i) return t;
        return null;
    }
}
```

```java
// Engine.java
public enum Engine {
    GM    (1, "GM"),
    TOYOTA(2, "TOYOTA"),
    WIA   (3, "WIA"),
    BROKEN(4, "고장난 엔진");

    private final int    index;
    private final String displayName;

    Engine(int index, String displayName) { ... }

    public String  getDisplayName()       { return displayName; }
    public boolean isBroken()             { return this == BROKEN; }
    public static Engine fromIndex(int i) { ... }
}
```

```java
// BrakeSystem.java
public enum BrakeSystem {
    MANDO      (1, "Mando"),
    CONTINENTAL(2, "Continental"),
    BOSCH      (3, "Bosch");
    ...
}

// SteeringSystem.java
public enum SteeringSystem {
    BOSCH(1, "Bosch"),
    MOBIS(2, "Mobis");
    ...
}
```

**Assemble.java 변경 전/후**

```java
// 변경 전
String name = a == 1 ? "Sedan" : a == 2 ? "SUV" : "Truck";
System.out.printf("차량 타입으로 %s을 선택하셨습니다.\n", name);

// 변경 후
CarType type = CarType.fromIndex(a);
System.out.printf("차량 타입으로 %s을 선택하셨습니다.\n", type.getDisplayName());
```

**완료 기준**

- [ ] 4개 enum 파일 생성
- [ ] `Assemble.java`의 int 상수 및 인라인 문자열 매핑 제거
- [ ] `.\gradlew.bat test` 전체 통과

---

### Step 2. Car 도메인 객체 도입

**목적**: `static int[] stack`을 제거하고 선택 상태를 명시적인 객체로 관리합니다.

**신규 파일**

```
java/
└── Car.java
```

**설계**

```java
// Car.java
public class Car {
    private CarType       carType;
    private Engine        engine;
    private BrakeSystem   brakeSystem;
    private SteeringSystem steeringSystem;

    public CarType        getCarType()        { return carType; }
    public Engine         getEngine()         { return engine; }
    public BrakeSystem    getBrakeSystem()    { return brakeSystem; }
    public SteeringSystem getSteeringSystem() { return steeringSystem; }

    public void setCarType       (CarType t)        { this.carType        = t; }
    public void setEngine        (Engine e)         { this.engine         = e; }
    public void setBrakeSystem   (BrakeSystem b)    { this.brakeSystem    = b; }
    public void setSteeringSystem(SteeringSystem s) { this.steeringSystem = s; }

    public boolean isFullyAssembled() {
        return carType != null && engine != null
            && brakeSystem != null && steeringSystem != null;
    }
}
```

**Assemble.java 변경 전/후**

```java
// 변경 전
private static int[] stack = new int[5];
stack[CarType_Q] = a;                        // a가 무엇인지 모름

// 변경 후 — main()에서 Car 인스턴스 생성 후 전달
Car car = new Car();
car.setCarType(CarType.fromIndex(a));         // 타입이 명확함
```

**테스트 변화**: Reflection 기반 → `new Car()` 직접 생성

```java
// 변경 전 (현재 테스트)
Field f = Assemble.class.getDeclaredField("stack");
f.setAccessible(true);
((int[]) f.get(null))[0] = 1;   // SEDAN

// 변경 후
Car car = new Car();
car.setCarType(CarType.SEDAN);
```

**완료 기준**

- [ ] `Car.java` 생성
- [ ] `Assemble.java`에서 `static int[] stack` 제거
- [ ] `AssembleTest.java` Reflection 코드를 `new Car()` 방식으로 교체
- [ ] `.\gradlew.bat test` 전체 통과

---

### Step 3. CompatibilityRule 도입

**목적**: `isValidCheck()`와 `testProducedCar()`에 중복된 5가지 조건을 한 곳으로 통합합니다.

**신규 파일**

```
java/
└── CompatibilityRule.java
```

**설계**

```java
// CompatibilityRule.java
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class CompatibilityRule {
    private final Predicate<Car> violated;
    private final String         message;

    public CompatibilityRule(Predicate<Car> violated, String message) {
        this.violated = violated;
        this.message  = message;
    }

    public boolean isViolated(Car car) { return violated.test(car); }
    public String  getMessage()        { return message; }

    /** 모든 호환성 규칙 목록 — 규칙 추가/변경은 여기서만 */
    public static final List<CompatibilityRule> RULES = List.of(
        new CompatibilityRule(
            car -> car.getCarType() == CarType.SEDAN
                && car.getBrakeSystem() == BrakeSystem.CONTINENTAL,
            "Sedan에는 Continental제동장치 사용 불가"
        ),
        new CompatibilityRule(
            car -> car.getCarType() == CarType.SUV
                && car.getEngine() == Engine.TOYOTA,
            "SUV에는 TOYOTA엔진 사용 불가"
        ),
        new CompatibilityRule(
            car -> car.getCarType() == CarType.TRUCK
                && car.getEngine() == Engine.WIA,
            "Truck에는 WIA엔진 사용 불가"
        ),
        new CompatibilityRule(
            car -> car.getCarType() == CarType.TRUCK
                && car.getBrakeSystem() == BrakeSystem.MANDO,
            "Truck에는 Mando제동장치 사용 불가"
        ),
        new CompatibilityRule(
            car -> car.getBrakeSystem() == BrakeSystem.BOSCH
                && car.getSteeringSystem() != SteeringSystem.BOSCH,
            "Bosch제동장치에는 Bosch조향장치 이외 사용 불가"
        )
    );

    /** 첫 번째로 위반된 규칙의 메시지를 반환. 없으면 empty */
    public static Optional<String> findViolation(Car car) {
        return RULES.stream()
                    .filter(r -> r.isViolated(car))
                    .map(CompatibilityRule::getMessage)
                    .findFirst();
    }

    /** 모든 규칙을 통과하면 true */
    public static boolean isValid(Car car) {
        return RULES.stream().noneMatch(r -> r.isViolated(car));
    }
}
```

**Assemble.java 변경 전/후**

```java
// 변경 전 — isValidCheck()와 testProducedCar()에 조건 중복
private static boolean isValidCheck() {
    if (stack[CarType_Q] == SEDAN && stack[BrakeSystem_Q] == CONTINENTAL) return false;
    ...
}
private static void testProducedCar() {
    if (stack[CarType_Q] == SEDAN && stack[BrakeSystem_Q] == CONTINENTAL) {
        fail("...");
    }
    ...
}

// 변경 후 — 두 메서드 모두 CompatibilityRule 참조
private static void runProducedCar(Car car) {
    if (!CompatibilityRule.isValid(car)) {
        System.out.println("자동차가 동작되지 않습니다");
        return;
    }
    ...
}

private static void testProducedCar(Car car) {
    Optional<String> violation = CompatibilityRule.findViolation(car);
    if (violation.isPresent()) {
        System.out.println("자동차 부품 조합 테스트 결과 : FAIL");
        System.out.println(violation.get());
    } else {
        System.out.println("자동차 부품 조합 테스트 결과 : PASS");
    }
}
```

**완료 기준**

- [ ] `CompatibilityRule.java` 생성
- [ ] `Assemble.java`에서 `isValidCheck()` 제거, 중복 조건 제거
- [ ] `.\gradlew.bat test` 전체 통과

---

### Step 4. 클래스 분리 (관심사 분리)

**목적**: 남은 역할들을 각자의 클래스로 분리해 `Assemble.java`를 진입점으로만 남깁니다.

**신규 파일**

```
java/
├── ConsoleMenu.java      // 화면 출력 + 입력 처리
└── AssemblyService.java  // run / test 비즈니스 로직
```

**ConsoleMenu 설계**

```java
// ConsoleMenu.java
public class ConsoleMenu {

    public void show(int step, Car car) {
        System.out.print("\033[H\033[2J");
        System.out.flush();
        switch (step) {
            case 0: showCarTypeMenu();    break;
            case 1: showEngineMenu();     break;
            case 2: showBrakeMenu();      break;
            case 3: showSteeringMenu();   break;
            case 4: showRunTestMenu();    break;
        }
    }

    /** 입력값을 읽고 범위 검사. 실패 시 재입력 요청, "exit" 시 -1 반환 */
    public int readInput(Scanner sc, int step) { ... }

    private void showCarTypeMenu()  { ... }
    private void showEngineMenu()   { ... }
    private void showBrakeMenu()    { ... }
    private void showSteeringMenu() { ... }
    private void showRunTestMenu()  { ... }
}
```

**AssemblyService 설계**

```java
// AssemblyService.java
public class AssemblyService {

    public void run(Car car) {
        if (!CompatibilityRule.isValid(car)) {
            System.out.println("자동차가 동작되지 않습니다");
            return;
        }
        if (car.getEngine().isBroken()) {
            System.out.println("엔진이 고장나있습니다.");
            System.out.println("자동차가 움직이지 않습니다.");
            return;
        }
        System.out.printf("Car Type : %s\n", car.getCarType().getDisplayName());
        System.out.printf("Engine   : %s\n", car.getEngine().getDisplayName());
        System.out.printf("Brake    : %s\n", car.getBrakeSystem().getDisplayName());
        System.out.printf("Steering : %s\n", car.getSteeringSystem().getDisplayName());
        System.out.println("자동차가 동작됩니다.");
    }

    public void test(Car car) {
        Optional<String> violation = CompatibilityRule.findViolation(car);
        if (violation.isPresent()) {
            System.out.println("자동차 부품 조합 테스트 결과 : FAIL");
            System.out.println(violation.get());
        } else {
            System.out.println("자동차 부품 조합 테스트 결과 : PASS");
        }
    }
}
```

**Assemble.java 최종 모습 (진입점만 남음)**

```java
// Assemble.java — main + 흐름 제어만
public class Assemble {

    public static void main(String[] args) {
        Scanner         sc      = new Scanner(System.in);
        ConsoleMenu     menu    = new ConsoleMenu();
        AssemblyService service = new AssemblyService();
        Car             car     = new Car();
        int             step    = 0;

        while (true) {
            menu.show(step, car);
            int answer = menu.readInput(sc, step);

            if (answer == -1) { System.out.println("바이바이"); break; }
            if (answer ==  0) { step = (step == 4) ? 0 : Math.max(0, step - 1); continue; }

            switch (step) {
                case 0: car.setCarType(CarType.fromIndex(answer));           step = 1; break;
                case 1: car.setEngine(Engine.fromIndex(answer));             step = 2; break;
                case 2: car.setBrakeSystem(BrakeSystem.fromIndex(answer));   step = 3; break;
                case 3: car.setSteeringSystem(SteeringSystem.fromIndex(answer)); step = 4; break;
                case 4:
                    if (answer == 1) service.run(car);
                    if (answer == 2) service.test(car);
                    break;
            }
        }
        sc.close();
    }
}
```

**완료 기준**

- [ ] `ConsoleMenu.java` 생성
- [ ] `AssemblyService.java` 생성
- [ ] `Assemble.java`에서 메뉴·비즈니스 로직 제거 (흐름 제어만 유지)
- [ ] `.\gradlew.bat test` 전체 통과

---

## 4. 단계별 진행 요약

```
Step 1  Enum 도입
        CarType / Engine / BrakeSystem / SteeringSystem
        → 매직 넘버 제거, 문자열 매핑 통합
        ✓ gradlew test 통과 확인
           ↓
Step 2  Car 도메인 객체
        int[] stack  →  Car 인스턴스
        → 전역 상태 제거, 테스트 코드에서 Reflection 제거
        ✓ gradlew test 통과 확인
           ↓
Step 3  CompatibilityRule
        isValidCheck + testProducedCar 중복 조건 통합
        → 규칙 수정 시 한 곳만 변경
        ✓ gradlew test 통과 확인
           ↓
Step 4  클래스 분리
        ConsoleMenu / AssemblyService / Assemble(축소)
        → 단일 책임, 독립 테스트 가능
        ✓ gradlew test 통과 확인
```

---

## 5. 최종 파일 구조

```
car-assembly/
├── java/
│   ├── Assemble.java           변경  흐름 제어 + main만 (약 30줄)
│   ├── Car.java                신규  조립 상태 도메인 객체
│   ├── CarType.java            신규  enum (SEDAN / SUV / TRUCK)
│   ├── Engine.java             신규  enum (GM / TOYOTA / WIA / BROKEN)
│   ├── BrakeSystem.java        신규  enum (MANDO / CONTINENTAL / BOSCH)
│   ├── SteeringSystem.java     신규  enum (BOSCH / MOBIS)
│   ├── CompatibilityRule.java  신규  호환성 규칙 5개 + isValid / findViolation
│   ├── ConsoleMenu.java        신규  메뉴 출력 + 입력 처리
│   └── AssemblyService.java    신규  run / test 비즈니스 로직
├── test/
│   └── AssembleTest.java       변경  Reflection 제거 → new Car() 방식으로 교체
├── doc/
│   ├── README.md
│   └── REFACTORING_PLAN.md     (현재 파일)
└── build.gradle
```
