import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Assemble.java 기능별 단위 테스트
 *
 * Step 2 이후: int[] stack 제거 → new Car() 방식으로 상태 설정
 * private static 메서드는 Java Reflection으로 호출
 */
@DisplayName("차량 조립 시뮬레이터 단위 테스트")
public class AssembleTest {

    private static final int STEP_CAR_TYPE = 0;
    private static final int STEP_ENGINE   = 1;
    private static final int STEP_BRAKE    = 2;
    private static final int STEP_STEERING = 3;
    private static final int STEP_RUN_TEST = 4;

    private ByteArrayOutputStream outCapture;
    private PrintStream           originalOut;
    private Car                   car;

    @BeforeEach
    void setUp() throws Exception {
        originalOut = System.out;
        outCapture  = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outCapture, true, "UTF-8"));
        car = new Car();
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    // ── Reflection 헬퍼 ─────────────────────────────────────────────────

    private boolean callIsValidRange(int step, int ans) throws Exception {
        Method m = Assemble.class.getDeclaredMethod("isValidRange", int.class, int.class);
        m.setAccessible(true);
        return (boolean) m.invoke(null, step, ans);
    }

    private boolean callIsValidCheck() throws Exception {
        Method m = Assemble.class.getDeclaredMethod("isValidCheck", Car.class);
        m.setAccessible(true);
        return (boolean) m.invoke(null, car);
    }

    private void callSelectCarType(int a) throws Exception {
        Method m = Assemble.class.getDeclaredMethod("selectCarType", int.class, Car.class);
        m.setAccessible(true);
        m.invoke(null, a, car);
    }

    private void callSelectEngine(int a) throws Exception {
        Method m = Assemble.class.getDeclaredMethod("selectEngine", int.class, Car.class);
        m.setAccessible(true);
        m.invoke(null, a, car);
    }

    private void callSelectBrakeSystem(int a) throws Exception {
        Method m = Assemble.class.getDeclaredMethod("selectBrakeSystem", int.class, Car.class);
        m.setAccessible(true);
        m.invoke(null, a, car);
    }

    private void callSelectSteeringSystem(int a) throws Exception {
        Method m = Assemble.class.getDeclaredMethod("selectSteeringSystem", int.class, Car.class);
        m.setAccessible(true);
        m.invoke(null, a, car);
    }

    private void callRunProducedCar() throws Exception {
        Method m = Assemble.class.getDeclaredMethod("runProducedCar", Car.class);
        m.setAccessible(true);
        m.invoke(null, car);
    }

    private void callTestProducedCar() throws Exception {
        Method m = Assemble.class.getDeclaredMethod("testProducedCar", Car.class);
        m.setAccessible(true);
        m.invoke(null, car);
    }

    private String output() throws Exception {
        return outCapture.toString("UTF-8");
    }

    // ====================================================================
    // 1. 입력 유효성 검사 (isValidRange)
    // ====================================================================

    @Nested
    @DisplayName("1. 입력 유효성 검사 (isValidRange)")
    class InputValidationTest {

        @Nested
        @DisplayName("1-1. 차량 타입 단계 (step=0)")
        class CarTypeStep {
            @ParameterizedTest(name = "입력 {0} → 유효")
            @ValueSource(ints = {1, 2, 3})
            void validInputs(int input) throws Exception {
                assertTrue(callIsValidRange(STEP_CAR_TYPE, input));
            }

            @ParameterizedTest(name = "입력 {0} → 무효")
            @ValueSource(ints = {0, 4, 5, -1, Integer.MAX_VALUE})
            void invalidInputs(int input) throws Exception {
                assertFalse(callIsValidRange(STEP_CAR_TYPE, input));
            }

            @Test @DisplayName("0 입력 시 에러 메시지 포함 'ERROR'")
            void zeroInput_errorMessage() throws Exception {
                callIsValidRange(STEP_CAR_TYPE, 0);
                assertTrue(output().contains("ERROR"));
            }

            @Test @DisplayName("4 입력 시 에러 메시지 포함 'ERROR'")
            void overRangeInput_errorMessage() throws Exception {
                callIsValidRange(STEP_CAR_TYPE, 4);
                assertTrue(output().contains("ERROR"));
            }
        }

        @Nested
        @DisplayName("1-2. 엔진 단계 (step=1)")
        class EngineStep {
            @ParameterizedTest(name = "입력 {0} → 유효")
            @ValueSource(ints = {0, 1, 2, 3, 4})
            void validInputs(int input) throws Exception {
                assertTrue(callIsValidRange(STEP_ENGINE, input));
            }

            @ParameterizedTest(name = "입력 {0} → 무효")
            @ValueSource(ints = {5, 6, -1, Integer.MIN_VALUE})
            void invalidInputs(int input) throws Exception {
                assertFalse(callIsValidRange(STEP_ENGINE, input));
            }

            @Test @DisplayName("5 입력 시 에러 메시지 포함 'ERROR'")
            void overRangeInput_errorMessage() throws Exception {
                callIsValidRange(STEP_ENGINE, 5);
                assertTrue(output().contains("ERROR"));
            }
        }

        @Nested
        @DisplayName("1-3. 제동장치 단계 (step=2)")
        class BrakeStep {
            @ParameterizedTest(name = "입력 {0} → 유효")
            @ValueSource(ints = {0, 1, 2, 3})
            void validInputs(int input) throws Exception {
                assertTrue(callIsValidRange(STEP_BRAKE, input));
            }

            @ParameterizedTest(name = "입력 {0} → 무효")
            @ValueSource(ints = {4, 5, -1, Integer.MIN_VALUE})
            void invalidInputs(int input) throws Exception {
                assertFalse(callIsValidRange(STEP_BRAKE, input));
            }

            @Test @DisplayName("4 입력 시 에러 메시지 포함 'ERROR'")
            void overRangeInput_errorMessage() throws Exception {
                callIsValidRange(STEP_BRAKE, 4);
                assertTrue(output().contains("ERROR"));
            }
        }

        @Nested
        @DisplayName("1-4. 조향장치 단계 (step=3)")
        class SteeringStep {
            @ParameterizedTest(name = "입력 {0} → 유효")
            @ValueSource(ints = {0, 1, 2})
            void validInputs(int input) throws Exception {
                assertTrue(callIsValidRange(STEP_STEERING, input));
            }

            @ParameterizedTest(name = "입력 {0} → 무효")
            @ValueSource(ints = {3, 4, -1, Integer.MIN_VALUE})
            void invalidInputs(int input) throws Exception {
                assertFalse(callIsValidRange(STEP_STEERING, input));
            }

            @Test @DisplayName("3 입력 시 에러 메시지 포함 'ERROR'")
            void overRangeInput_errorMessage() throws Exception {
                callIsValidRange(STEP_STEERING, 3);
                assertTrue(output().contains("ERROR"));
            }
        }

        @Nested
        @DisplayName("1-5. 실행/테스트 단계 (step=4)")
        class RunTestStep {
            @ParameterizedTest(name = "입력 {0} → 유효")
            @ValueSource(ints = {0, 1, 2})
            void validInputs(int input) throws Exception {
                assertTrue(callIsValidRange(STEP_RUN_TEST, input));
            }

            @ParameterizedTest(name = "입력 {0} → 무효")
            @ValueSource(ints = {3, 4, -1, Integer.MIN_VALUE})
            void invalidInputs(int input) throws Exception {
                assertFalse(callIsValidRange(STEP_RUN_TEST, input));
            }

            @Test @DisplayName("3 입력 시 에러 메시지 포함 'ERROR'")
            void overRangeInput_errorMessage() throws Exception {
                callIsValidRange(STEP_RUN_TEST, 3);
                assertTrue(output().contains("ERROR"));
            }
        }
    }

    // ====================================================================
    // 2. 부품 선택 출력 메시지
    // ====================================================================

    @Nested
    @DisplayName("2. 부품 선택 출력 메시지")
    class ComponentSelectionOutputTest {

        @Nested
        @DisplayName("2-1. 차량 타입 선택")
        class CarTypeOutput {
            @Test @DisplayName("1 선택 → 'Sedan' 출력")
            void sedan() throws Exception { callSelectCarType(1); assertTrue(output().contains("Sedan")); }
            @Test @DisplayName("2 선택 → 'SUV' 출력")
            void suv()   throws Exception { callSelectCarType(2); assertTrue(output().contains("SUV")); }
            @Test @DisplayName("3 선택 → 'Truck' 출력")
            void truck() throws Exception { callSelectCarType(3); assertTrue(output().contains("Truck")); }
        }

        @Nested
        @DisplayName("2-2. 엔진 선택")
        class EngineOutput {
            @Test @DisplayName("1 선택 → 'GM' 출력")
            void gm()    throws Exception { callSelectEngine(1); assertTrue(output().contains("GM")); }
            @Test @DisplayName("2 선택 → 'TOYOTA' 출력")
            void toyota()throws Exception { callSelectEngine(2); assertTrue(output().contains("TOYOTA")); }
            @Test @DisplayName("3 선택 → 'WIA' 출력")
            void wia()   throws Exception { callSelectEngine(3); assertTrue(output().contains("WIA")); }
            @Test @DisplayName("4 선택 → '고장난 엔진' 출력")
            void broken()throws Exception { callSelectEngine(4); assertTrue(output().contains("고장난 엔진")); }
        }

        @Nested
        @DisplayName("2-3. 제동장치 선택")
        class BrakeOutput {
            @Test @DisplayName("1 선택 → 'MANDO' 출력")
            void mando()      throws Exception { callSelectBrakeSystem(1); assertTrue(output().contains("MANDO")); }
            @Test @DisplayName("2 선택 → 'CONTINENTAL' 출력")
            void continental()throws Exception { callSelectBrakeSystem(2); assertTrue(output().contains("CONTINENTAL")); }
            @Test @DisplayName("3 선택 → 'BOSCH' 출력")
            void bosch()      throws Exception { callSelectBrakeSystem(3); assertTrue(output().contains("BOSCH")); }
        }

        @Nested
        @DisplayName("2-4. 조향장치 선택")
        class SteeringOutput {
            @Test @DisplayName("1 선택 → 'BOSCH' 출력")
            void bosch()throws Exception { callSelectSteeringSystem(1); assertTrue(output().contains("BOSCH")); }
            @Test @DisplayName("2 선택 → 'MOBIS' 출력")
            void mobis()throws Exception { callSelectSteeringSystem(2); assertTrue(output().contains("MOBIS")); }
        }
    }

    // ====================================================================
    // 3. 선택 후 Car 객체 상태 검증
    // ====================================================================

    @Nested
    @DisplayName("3. 선택 후 Car 객체 상태 검증")
    class CarStateTest {

        @Test @DisplayName("차량 타입 선택 → car.getCarType() 에 저장")
        void carType_savedToCar() throws Exception {
            callSelectCarType(2);
            assertEquals(CarType.SUV, car.getCarType());
        }

        @Test @DisplayName("엔진 선택 → car.getEngine() 에 저장")
        void engine_savedToCar() throws Exception {
            callSelectEngine(2);
            assertEquals(Engine.TOYOTA, car.getEngine());
        }

        @Test @DisplayName("제동장치 선택 → car.getBrakeSystem() 에 저장")
        void brake_savedToCar() throws Exception {
            callSelectBrakeSystem(2);
            assertEquals(BrakeSystem.CONTINENTAL, car.getBrakeSystem());
        }

        @Test @DisplayName("조향장치 선택 → car.getSteeringSystem() 에 저장")
        void steering_savedToCar() throws Exception {
            callSelectSteeringSystem(2);
            assertEquals(SteeringSystem.MOBIS, car.getSteeringSystem());
        }

        @Test @DisplayName("재선택 시 이전 값이 덮어써짐")
        void reselect_overwritesPreviousValue() throws Exception {
            callSelectCarType(1);
            callSelectCarType(3);
            assertEquals(CarType.TRUCK, car.getCarType());
        }

        @Test @DisplayName("다른 부품 선택이 기존 Car 상태에 영향 없음")
        void selectOne_doesNotAffectOtherFields() throws Exception {
            callSelectCarType(1);
            callSelectEngine(1);
            assertEquals(CarType.SEDAN, car.getCarType());
            assertEquals(Engine.GM,    car.getEngine());
            assertNull(car.getBrakeSystem());
        }

        @Test @DisplayName("고장난 엔진(4) 선택 → car.getEngine() == Engine.BROKEN")
        void brokenEngine_savedAsBroken() throws Exception {
            callSelectEngine(4);
            assertEquals(Engine.BROKEN, car.getEngine());
            assertTrue(car.getEngine().isBroken());
        }
    }

    // ====================================================================
    // 4. 부품 조합 유효성 검증 (isValidCheck)
    // ====================================================================

    @Nested
    @DisplayName("4. 부품 조합 유효성 검증 (isValidCheck)")
    class CombinationValidityTest {

        @Nested
        @DisplayName("4-1. 금지 조합 → false")
        class ForbiddenCombinations {

            @Test @DisplayName("Sedan + CONTINENTAL 제동장치 → false")
            void sedan_continental() throws Exception {
                car.setCarType(CarType.SEDAN);
                car.setBrakeSystem(BrakeSystem.CONTINENTAL);
                assertFalse(callIsValidCheck());
            }

            @Test @DisplayName("SUV + TOYOTA 엔진 → false")
            void suv_toyota() throws Exception {
                car.setCarType(CarType.SUV);
                car.setEngine(Engine.TOYOTA);
                assertFalse(callIsValidCheck());
            }

            @Test @DisplayName("Truck + WIA 엔진 → false")
            void truck_wia() throws Exception {
                car.setCarType(CarType.TRUCK);
                car.setEngine(Engine.WIA);
                assertFalse(callIsValidCheck());
            }

            @Test @DisplayName("Truck + MANDO 제동장치 → false")
            void truck_mando() throws Exception {
                car.setCarType(CarType.TRUCK);
                car.setBrakeSystem(BrakeSystem.MANDO);
                assertFalse(callIsValidCheck());
            }

            @Test @DisplayName("BOSCH 제동장치 + MOBIS 조향장치 → false")
            void boschBrake_mobisSteering() throws Exception {
                car.setBrakeSystem(BrakeSystem.BOSCH);
                car.setSteeringSystem(SteeringSystem.MOBIS);
                assertFalse(callIsValidCheck());
            }
        }

        @Nested
        @DisplayName("4-2. 허용 조합 → true")
        class AllowedCombinations {

            @Test @DisplayName("Sedan + GM + MANDO + BOSCH → true")
            void sedan_gm_mando_bosch() throws Exception {
                car.setCarType(CarType.SEDAN); car.setEngine(Engine.GM);
                car.setBrakeSystem(BrakeSystem.MANDO); car.setSteeringSystem(SteeringSystem.BOSCH);
                assertTrue(callIsValidCheck());
            }

            @Test @DisplayName("Sedan + GM + MANDO + MOBIS → true")
            void sedan_gm_mando_mobis() throws Exception {
                car.setCarType(CarType.SEDAN); car.setEngine(Engine.GM);
                car.setBrakeSystem(BrakeSystem.MANDO); car.setSteeringSystem(SteeringSystem.MOBIS);
                assertTrue(callIsValidCheck());
            }

            @Test @DisplayName("Sedan + GM + BOSCH 제동 + BOSCH 조향 → true")
            void sedan_gm_boschBrake_boschSteering() throws Exception {
                car.setCarType(CarType.SEDAN); car.setEngine(Engine.GM);
                car.setBrakeSystem(BrakeSystem.BOSCH); car.setSteeringSystem(SteeringSystem.BOSCH);
                assertTrue(callIsValidCheck());
            }

            @Test @DisplayName("Sedan + TOYOTA 엔진 → true (Sedan에 TOYOTA 허용)")
            void sedan_toyota_allowed() throws Exception {
                car.setCarType(CarType.SEDAN); car.setEngine(Engine.TOYOTA);
                car.setBrakeSystem(BrakeSystem.MANDO); car.setSteeringSystem(SteeringSystem.MOBIS);
                assertTrue(callIsValidCheck());
            }

            @Test @DisplayName("Sedan + WIA 엔진 → true (Sedan에 WIA 허용)")
            void sedan_wia_allowed() throws Exception {
                car.setCarType(CarType.SEDAN); car.setEngine(Engine.WIA);
                car.setBrakeSystem(BrakeSystem.MANDO); car.setSteeringSystem(SteeringSystem.MOBIS);
                assertTrue(callIsValidCheck());
            }

            @Test @DisplayName("SUV + GM + CONTINENTAL + MOBIS → true")
            void suv_gm_continental_mobis() throws Exception {
                car.setCarType(CarType.SUV); car.setEngine(Engine.GM);
                car.setBrakeSystem(BrakeSystem.CONTINENTAL); car.setSteeringSystem(SteeringSystem.MOBIS);
                assertTrue(callIsValidCheck());
            }

            @Test @DisplayName("SUV + WIA 엔진 → true (SUV에 WIA 허용)")
            void suv_wia_allowed() throws Exception {
                car.setCarType(CarType.SUV); car.setEngine(Engine.WIA);
                car.setBrakeSystem(BrakeSystem.MANDO); car.setSteeringSystem(SteeringSystem.MOBIS);
                assertTrue(callIsValidCheck());
            }

            @Test @DisplayName("Truck + GM + CONTINENTAL + MOBIS → true")
            void truck_gm_continental_mobis() throws Exception {
                car.setCarType(CarType.TRUCK); car.setEngine(Engine.GM);
                car.setBrakeSystem(BrakeSystem.CONTINENTAL); car.setSteeringSystem(SteeringSystem.MOBIS);
                assertTrue(callIsValidCheck());
            }

            @Test @DisplayName("Truck + TOYOTA + BOSCH 세트 → true")
            void truck_toyota_bosch_bosch() throws Exception {
                car.setCarType(CarType.TRUCK); car.setEngine(Engine.TOYOTA);
                car.setBrakeSystem(BrakeSystem.BOSCH); car.setSteeringSystem(SteeringSystem.BOSCH);
                assertTrue(callIsValidCheck());
            }

            @Test @DisplayName("Sedan + MANDO 제동장치 → true (Truck에만 MANDO 금지)")
            void sedan_mando_allowed() throws Exception {
                car.setCarType(CarType.SEDAN); car.setEngine(Engine.GM);
                car.setBrakeSystem(BrakeSystem.MANDO); car.setSteeringSystem(SteeringSystem.MOBIS);
                assertTrue(callIsValidCheck());
            }

            @Test @DisplayName("SUV + MANDO 제동장치 → true (Truck에만 MANDO 금지)")
            void suv_mando_allowed() throws Exception {
                car.setCarType(CarType.SUV); car.setEngine(Engine.GM);
                car.setBrakeSystem(BrakeSystem.MANDO); car.setSteeringSystem(SteeringSystem.MOBIS);
                assertTrue(callIsValidCheck());
            }
        }
    }

    // ====================================================================
    // 5. 차량 실행 (runProducedCar)
    // ====================================================================

    @Nested
    @DisplayName("5. 차량 실행 (runProducedCar)")
    class RunCarTest {

        @Nested
        @DisplayName("5-1. 금지 조합 → 동작 불가 메시지")
        class InvalidCombinationRun {

            @Test @DisplayName("Sedan + CONTINENTAL → '자동차가 동작되지 않습니다' 출력")
            void sedan_continental_doesNotRun() throws Exception {
                car.setCarType(CarType.SEDAN); car.setBrakeSystem(BrakeSystem.CONTINENTAL);
                callRunProducedCar();
                assertTrue(output().contains("자동차가 동작되지 않습니다"));
            }

            @Test @DisplayName("SUV + TOYOTA → '자동차가 동작되지 않습니다' 출력")
            void suv_toyota_doesNotRun() throws Exception {
                car.setCarType(CarType.SUV); car.setEngine(Engine.TOYOTA);
                callRunProducedCar();
                assertTrue(output().contains("자동차가 동작되지 않습니다"));
            }

            @Test @DisplayName("Truck + WIA → '자동차가 동작되지 않습니다' 출력")
            void truck_wia_doesNotRun() throws Exception {
                car.setCarType(CarType.TRUCK); car.setEngine(Engine.WIA);
                callRunProducedCar();
                assertTrue(output().contains("자동차가 동작되지 않습니다"));
            }

            @Test @DisplayName("Truck + MANDO → '자동차가 동작되지 않습니다' 출력")
            void truck_mando_doesNotRun() throws Exception {
                car.setCarType(CarType.TRUCK); car.setBrakeSystem(BrakeSystem.MANDO);
                callRunProducedCar();
                assertTrue(output().contains("자동차가 동작되지 않습니다"));
            }

            @Test @DisplayName("BOSCH 제동 + MOBIS 조향 → '자동차가 동작되지 않습니다' 출력")
            void boschBrake_mobisSteering_doesNotRun() throws Exception {
                car.setCarType(CarType.SEDAN); car.setEngine(Engine.GM);
                car.setBrakeSystem(BrakeSystem.BOSCH); car.setSteeringSystem(SteeringSystem.MOBIS);
                callRunProducedCar();
                assertTrue(output().contains("자동차가 동작되지 않습니다"));
            }

            @Test @DisplayName("금지 조합 → '자동차가 동작됩니다' 출력 안 됨")
            void invalidCombination_noRunMessage() throws Exception {
                car.setCarType(CarType.SEDAN); car.setBrakeSystem(BrakeSystem.CONTINENTAL);
                callRunProducedCar();
                assertFalse(output().contains("자동차가 동작됩니다"));
            }
        }

        @Nested
        @DisplayName("5-2. 고장난 엔진(BROKEN)")
        class BrokenEngineRun {

            @Test @DisplayName("고장난 엔진 → '엔진이 고장나있습니다' 출력")
            void brokenEngine_engineFailMessage() throws Exception {
                car.setCarType(CarType.SEDAN); car.setEngine(Engine.BROKEN);
                car.setBrakeSystem(BrakeSystem.MANDO); car.setSteeringSystem(SteeringSystem.BOSCH);
                callRunProducedCar();
                assertTrue(output().contains("엔진이 고장나있습니다"));
            }

            @Test @DisplayName("고장난 엔진 → '자동차가 움직이지 않습니다' 출력")
            void brokenEngine_noMoveMessage() throws Exception {
                car.setCarType(CarType.SEDAN); car.setEngine(Engine.BROKEN);
                car.setBrakeSystem(BrakeSystem.MANDO); car.setSteeringSystem(SteeringSystem.BOSCH);
                callRunProducedCar();
                assertTrue(output().contains("자동차가 움직이지 않습니다"));
            }

            @Test @DisplayName("고장난 엔진 → '자동차가 동작됩니다' 출력 안 됨")
            void brokenEngine_noRunMessage() throws Exception {
                car.setCarType(CarType.SEDAN); car.setEngine(Engine.BROKEN);
                car.setBrakeSystem(BrakeSystem.MANDO); car.setSteeringSystem(SteeringSystem.BOSCH);
                callRunProducedCar();
                assertFalse(output().contains("자동차가 동작됩니다"));
            }

            @Test @DisplayName("금지 조합 + 고장난 엔진 → 조합 오류 메시지 우선 출력")
            void invalidCombinationTakesPriorityOverBrokenEngine() throws Exception {
                car.setCarType(CarType.SEDAN); car.setEngine(Engine.BROKEN);
                car.setBrakeSystem(BrakeSystem.CONTINENTAL);
                callRunProducedCar();
                assertTrue(output().contains("자동차가 동작되지 않습니다"));
                assertFalse(output().contains("엔진이 고장나있습니다"));
            }
        }

        @Nested
        @DisplayName("5-3. 정상 조합 → 동작 메시지 및 부품 정보 출력")
        class ValidRun {

            @Test @DisplayName("정상 조합 → '자동차가 동작됩니다' 출력")
            void validCombination_runsSuccessfully() throws Exception {
                car.setCarType(CarType.SEDAN); car.setEngine(Engine.GM);
                car.setBrakeSystem(BrakeSystem.MANDO); car.setSteeringSystem(SteeringSystem.BOSCH);
                callRunProducedCar();
                assertTrue(output().contains("자동차가 동작됩니다"));
            }

            @Test @DisplayName("정상 조합 → 차량 타입 출력 (SUV)")
            void validCombination_showsCarType() throws Exception {
                car.setCarType(CarType.SUV); car.setEngine(Engine.GM);
                car.setBrakeSystem(BrakeSystem.MANDO); car.setSteeringSystem(SteeringSystem.MOBIS);
                callRunProducedCar();
                assertTrue(output().contains("SUV"));
            }

            @Test @DisplayName("정상 조합 → 엔진 정보 출력 (TOYOTA)")
            void validCombination_showsEngine() throws Exception {
                car.setCarType(CarType.TRUCK); car.setEngine(Engine.TOYOTA);
                car.setBrakeSystem(BrakeSystem.CONTINENTAL); car.setSteeringSystem(SteeringSystem.MOBIS);
                callRunProducedCar();
                assertTrue(output().contains("TOYOTA"));
            }

            @Test @DisplayName("정상 조합 → 제동장치 출력 (Bosch)")
            void validCombination_showsBrake() throws Exception {
                car.setCarType(CarType.SEDAN); car.setEngine(Engine.GM);
                car.setBrakeSystem(BrakeSystem.BOSCH); car.setSteeringSystem(SteeringSystem.BOSCH);
                callRunProducedCar();
                assertTrue(output().contains("Bosch"));
            }

            @Test @DisplayName("정상 조합 → 조향장치 출력 (Mobis)")
            void validCombination_showsSteering() throws Exception {
                car.setCarType(CarType.SEDAN); car.setEngine(Engine.GM);
                car.setBrakeSystem(BrakeSystem.MANDO); car.setSteeringSystem(SteeringSystem.MOBIS);
                callRunProducedCar();
                assertTrue(output().contains("Mobis"));
            }

            @Test @DisplayName("정상 조합 → 제동장치 출력 (Continental)")
            void validCombination_showsContinentalBrake() throws Exception {
                car.setCarType(CarType.SUV); car.setEngine(Engine.GM);
                car.setBrakeSystem(BrakeSystem.CONTINENTAL); car.setSteeringSystem(SteeringSystem.MOBIS);
                callRunProducedCar();
                assertTrue(output().contains("Continental"));
            }

            @Test @DisplayName("정상 조합 → 제동장치 출력 (Mando)")
            void validCombination_showsMandoBrake() throws Exception {
                car.setCarType(CarType.SEDAN); car.setEngine(Engine.GM);
                car.setBrakeSystem(BrakeSystem.MANDO); car.setSteeringSystem(SteeringSystem.BOSCH);
                callRunProducedCar();
                assertTrue(output().contains("Mando"));
            }

            @Test @DisplayName("정상 조합 → 조향장치 출력 (Bosch)")
            void validCombination_showsBoschSteering() throws Exception {
                car.setCarType(CarType.SEDAN); car.setEngine(Engine.GM);
                car.setBrakeSystem(BrakeSystem.MANDO); car.setSteeringSystem(SteeringSystem.BOSCH);
                callRunProducedCar();
                assertTrue(output().contains("Bosch"));
            }
        }
    }

    // ====================================================================
    // 6. 부품 조합 테스트 (testProducedCar)
    // ====================================================================

    @Nested
    @DisplayName("6. 부품 조합 테스트 (testProducedCar)")
    class TestCarTest {

        @Nested
        @DisplayName("6-1. 금지 조합 → FAIL + 원인 메시지")
        class FailCases {

            @Test @DisplayName("Sedan + CONTINENTAL → FAIL 출력")
            void sedan_continental_fail() throws Exception {
                car.setCarType(CarType.SEDAN); car.setBrakeSystem(BrakeSystem.CONTINENTAL);
                callTestProducedCar();
                assertTrue(output().contains("FAIL"));
            }

            @Test @DisplayName("Sedan + CONTINENTAL → 원인 메시지에 'Continental' 포함")
            void sedan_continental_failMessage() throws Exception {
                car.setCarType(CarType.SEDAN); car.setBrakeSystem(BrakeSystem.CONTINENTAL);
                callTestProducedCar();
                assertTrue(output().contains("Continental"));
            }

            @Test @DisplayName("SUV + TOYOTA → FAIL 출력")
            void suv_toyota_fail() throws Exception {
                car.setCarType(CarType.SUV); car.setEngine(Engine.TOYOTA);
                callTestProducedCar();
                assertTrue(output().contains("FAIL"));
            }

            @Test @DisplayName("SUV + TOYOTA → 원인 메시지에 'TOYOTA' 포함")
            void suv_toyota_failMessage() throws Exception {
                car.setCarType(CarType.SUV); car.setEngine(Engine.TOYOTA);
                callTestProducedCar();
                assertTrue(output().contains("TOYOTA"));
            }

            @Test @DisplayName("Truck + WIA → FAIL 출력")
            void truck_wia_fail() throws Exception {
                car.setCarType(CarType.TRUCK); car.setEngine(Engine.WIA);
                callTestProducedCar();
                assertTrue(output().contains("FAIL"));
            }

            @Test @DisplayName("Truck + WIA → 원인 메시지에 'WIA' 포함")
            void truck_wia_failMessage() throws Exception {
                car.setCarType(CarType.TRUCK); car.setEngine(Engine.WIA);
                callTestProducedCar();
                assertTrue(output().contains("WIA"));
            }

            @Test @DisplayName("Truck + MANDO → FAIL 출력")
            void truck_mando_fail() throws Exception {
                car.setCarType(CarType.TRUCK); car.setBrakeSystem(BrakeSystem.MANDO);
                callTestProducedCar();
                assertTrue(output().contains("FAIL"));
            }

            @Test @DisplayName("Truck + MANDO → 원인 메시지에 'Mando' 포함")
            void truck_mando_failMessage() throws Exception {
                car.setCarType(CarType.TRUCK); car.setBrakeSystem(BrakeSystem.MANDO);
                callTestProducedCar();
                assertTrue(output().contains("Mando"));
            }

            @Test @DisplayName("BOSCH 제동 + MOBIS 조향 → FAIL 출력")
            void boschBrake_mobisSteering_fail() throws Exception {
                car.setBrakeSystem(BrakeSystem.BOSCH); car.setSteeringSystem(SteeringSystem.MOBIS);
                callTestProducedCar();
                assertTrue(output().contains("FAIL"));
            }

            @Test @DisplayName("BOSCH 제동 + MOBIS 조향 → 원인 메시지에 'Bosch' 포함")
            void boschBrake_mobisSteering_failMessage() throws Exception {
                car.setBrakeSystem(BrakeSystem.BOSCH); car.setSteeringSystem(SteeringSystem.MOBIS);
                callTestProducedCar();
                assertTrue(output().contains("Bosch"));
            }

            @Test @DisplayName("FAIL 결과 → 'PASS' 출력 안 됨")
            void failResult_noPassMessage() throws Exception {
                car.setCarType(CarType.SUV); car.setEngine(Engine.TOYOTA);
                callTestProducedCar();
                assertFalse(output().contains("PASS"));
            }

            @Test @DisplayName("복수 금지 조건 해당 시 첫 번째 조건만 출력 (if-else 체인)")
            void multipleViolations_onlyFirstReported() throws Exception {
                car.setCarType(CarType.SEDAN); car.setBrakeSystem(BrakeSystem.CONTINENTAL);
                callTestProducedCar();
                assertTrue(output().contains("Sedan"));
                assertFalse(output().contains("SUV"));
            }
        }

        @Nested
        @DisplayName("6-2. 허용 조합 → PASS")
        class PassCases {

            @Test @DisplayName("Sedan + GM + MANDO + BOSCH → PASS")
            void sedan_gm_mando_bosch_pass() throws Exception {
                car.setCarType(CarType.SEDAN); car.setEngine(Engine.GM);
                car.setBrakeSystem(BrakeSystem.MANDO); car.setSteeringSystem(SteeringSystem.BOSCH);
                callTestProducedCar();
                assertTrue(output().contains("PASS"));
            }

            @Test @DisplayName("Sedan + GM + MANDO + MOBIS → PASS")
            void sedan_gm_mando_mobis_pass() throws Exception {
                car.setCarType(CarType.SEDAN); car.setEngine(Engine.GM);
                car.setBrakeSystem(BrakeSystem.MANDO); car.setSteeringSystem(SteeringSystem.MOBIS);
                callTestProducedCar();
                assertTrue(output().contains("PASS"));
            }

            @Test @DisplayName("Sedan + GM + BOSCH 세트 → PASS")
            void sedan_gm_bosch_bosch_pass() throws Exception {
                car.setCarType(CarType.SEDAN); car.setEngine(Engine.GM);
                car.setBrakeSystem(BrakeSystem.BOSCH); car.setSteeringSystem(SteeringSystem.BOSCH);
                callTestProducedCar();
                assertTrue(output().contains("PASS"));
            }

            @Test @DisplayName("SUV + GM + CONTINENTAL + MOBIS → PASS")
            void suv_gm_continental_mobis_pass() throws Exception {
                car.setCarType(CarType.SUV); car.setEngine(Engine.GM);
                car.setBrakeSystem(BrakeSystem.CONTINENTAL); car.setSteeringSystem(SteeringSystem.MOBIS);
                callTestProducedCar();
                assertTrue(output().contains("PASS"));
            }

            @Test @DisplayName("Truck + GM + CONTINENTAL + MOBIS → PASS")
            void truck_gm_continental_mobis_pass() throws Exception {
                car.setCarType(CarType.TRUCK); car.setEngine(Engine.GM);
                car.setBrakeSystem(BrakeSystem.CONTINENTAL); car.setSteeringSystem(SteeringSystem.MOBIS);
                callTestProducedCar();
                assertTrue(output().contains("PASS"));
            }

            @Test @DisplayName("Truck + TOYOTA + BOSCH 세트 → PASS")
            void truck_toyota_bosch_bosch_pass() throws Exception {
                car.setCarType(CarType.TRUCK); car.setEngine(Engine.TOYOTA);
                car.setBrakeSystem(BrakeSystem.BOSCH); car.setSteeringSystem(SteeringSystem.BOSCH);
                callTestProducedCar();
                assertTrue(output().contains("PASS"));
            }

            @Test @DisplayName("PASS 결과 → 'FAIL' 출력 안 됨")
            void passResult_noFailMessage() throws Exception {
                car.setCarType(CarType.SEDAN); car.setEngine(Engine.GM);
                car.setBrakeSystem(BrakeSystem.MANDO); car.setSteeringSystem(SteeringSystem.MOBIS);
                callTestProducedCar();
                assertFalse(output().contains("FAIL"));
            }
        }
    }
}
