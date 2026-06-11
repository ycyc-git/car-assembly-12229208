public class AssemblyService {

    public void select(int step, int answer, Car car) {
        switch (step) {
            case 0:
                car.setCarType(CarType.fromIndex(answer));
                System.out.printf("차량 타입으로 %s을 선택하셨습니다.\n",
                        car.getCarType().getDisplayName());
                break;
            case 1:
                car.setEngine(Engine.fromIndex(answer));
                System.out.printf("%s 엔진을 선택하셨습니다.\n",
                        car.getEngine().getDisplayName());
                break;
            case 2:
                car.setBrakeSystem(BrakeSystem.fromIndex(answer));
                System.out.printf("%s 제동장치를 선택하셨습니다.\n",
                        car.getBrakeSystem().name());
                break;
            case 3:
                car.setSteeringSystem(SteeringSystem.fromIndex(answer));
                System.out.printf("%s 조향장치를 선택하셨습니다.\n",
                        car.getSteeringSystem().name());
                break;
        }
    }

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
        CompatibilityRule.findViolation(car).ifPresentOrElse(
            msg -> {
                System.out.println("자동차 부품 조합 테스트 결과 : FAIL");
                System.out.println(msg);
            },
            () -> System.out.println("자동차 부품 조합 테스트 결과 : PASS")
        );
    }
}
