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

    /** 모든 호환성 규칙 — 규칙 추가·변경은 여기서만 */
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

    /** 모든 규칙을 통과하면 true */
    public static boolean isValid(Car car) {
        return RULES.stream().noneMatch(r -> r.isViolated(car));
    }

    /** 첫 번째로 위반된 규칙의 메시지 반환. 없으면 empty */
    public static Optional<String> findViolation(Car car) {
        return RULES.stream()
                    .filter(r -> r.isViolated(car))
                    .map(CompatibilityRule::getMessage)
                    .findFirst();
    }
}
