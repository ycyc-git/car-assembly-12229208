import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Assemble.java 기능별 단위 테스트
 *
 * private static 메서드는 Java Reflection을 통해 직접 호출합니다.
 * 리팩토링 전 현재 동작 명세를 기준으로 작성되었습니다.
 *
 * 테스트 실행 방법: test/HOW_TO_RUN.md 참조
 */
@DisplayName("차량 조립 시뮬레이터 단위 테스트")
public class AssembleTest {

    // ── 단계 상수 (Assemble 내부 상수와 동일한 값) ──────────────────────
    private static final int STEP_CAR_TYPE = 0;
    private static final int STEP_ENGINE   = 1;
    private static final int STEP_BRAKE    = 2;
    private static final int STEP_STEERING = 3;
    private static final int STEP_RUN_TEST = 4;

    // ── 선택지 상수 ──────────────────────────────────────────────────────
    private static final int SEDAN = 1, SUV = 2, TRUCK = 3;
    private static final int GM = 1, TOYOTA = 2, WIA = 3, BROKEN_ENGINE = 4;
    private static final int MANDO = 1, CONTINENTAL = 2, BOSCH_B = 3;
    private static final int BOSCH_S = 1, MOBIS = 2;

    private ByteArrayOutputStream outCapture;
    private PrintStream originalOut;

    // ── 테스트 공통 설정 ─────────────────────────────────────────────────

    @BeforeEach
    void setUp() throws Exception {
        originalOut = System.out;
        outCapture  = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outCapture, true, "UTF-8"));
        resetStack();
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    // ── Reflection 헬퍼 ─────────────────────────────────────────────────

    private void resetStack() throws Exception {
        Field f = Assemble.class.getDeclaredField("stack");
        f.setAccessible(true);
        f.set(null, new int[5]);
    }

    private void setStack(int index, int value) throws Exception {
        Field f = Assemble.class.getDeclaredField("stack");
        f.setAccessible(true);
        ((int[]) f.get(null))[index] = value;
    }

    private int getStack(int index) throws Exception {
        Field f = Assemble.class.getDeclaredField("stack");
        f.setAccessible(true);
        return ((int[]) f.get(null))[index];
    }

    private boolean callIsValidRange(int step, int ans) throws Exception {
        Method m = Assemble.class.getDeclaredMethod("isValidRange", int.class, int.class);
        m.setAccessible(true);
        return (boolean) m.invoke(null, step, ans);
    }

    private boolean callIsValidCheck() throws Exception {
        Method m = Assemble.class.getDeclaredMethod("isValidCheck");
        m.setAccessible(true);
        return (boolean) m.invoke(null);
    }

    private void callSelectCarType(int a) throws Exception {
        Method m = Assemble.class.getDeclaredMethod("selectCarType", int.class);
        m.setAccessible(true);
        m.invoke(null, a);
    }

    private void callSelectEngine(int a) throws Exception {
        Method m = Assemble.class.getDeclaredMethod("selectEngine", int.class);
        m.setAccessible(true);
        m.invoke(null, a);
    }

    private void callSelectBrakeSystem(int a) throws Exception {
        Method m = Assemble.class.getDeclaredMethod("selectBrakeSystem", int.class);
        m.setAccessible(true);
        m.invoke(null, a);
    }

    private void callSelectSteeringSystem(int a) throws Exception {
        Method m = Assemble.class.getDeclaredMethod("selectSteeringSystem", int.class);
        m.setAccessible(true);
        m.invoke(null, a);
    }

    private void callRunProducedCar() throws Exception {
        Method m = Assemble.class.getDeclaredMethod("runProducedCar");
        m.setAccessible(true);
        m.invoke(null);
    }

    private void callTestProducedCar() throws Exception {
        Method m = Assemble.class.getDeclaredMethod("testProducedCar");
        m.setAccessible(true);
        m.invoke(null);
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

        // ── 1-1. 차량 타입 단계 (step=0, 유효 범위: 1~3) ─────────────────

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

            @Test
            @DisplayName("0 입력 시 에러 메시지 포함 'ERROR'")
            void zeroInput_errorMessage() throws Exception {
                callIsValidRange(STEP_CAR_TYPE, 0);
                assertTrue(output().contains("ERROR"));
            }

            @Test
            @DisplayName("4 입력 시 에러 메시지 포함 'ERROR'")
            void overRangeInput_errorMessage() throws Exception {
                callIsValidRange(STEP_CAR_TYPE, 4);
                assertTrue(output().contains("ERROR"));
            }
        }

        // ── 1-2. 엔진 단계 (step=1, 유효 범위: 0~4, 0은 뒤로가기) ────────

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

            @Test
            @DisplayName("5 입력 시 에러 메시지 포함 'ERROR'")
            void overRangeInput_errorMessage() throws Exception {
                callIsValidRange(STEP_ENGINE, 5);
                assertTrue(output().contains("ERROR"));
            }
        }

        // ── 1-3. 제동장치 단계 (step=2, 유효 범위: 0~3) ──────────────────

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

            @Test
            @DisplayName("4 입력 시 에러 메시지 포함 'ERROR'")
            void overRangeInput_errorMessage() throws Exception {
                callIsValidRange(STEP_BRAKE, 4);
                assertTrue(output().contains("ERROR"));
            }
        }

        // ── 1-4. 조향장치 단계 (step=3, 유효 범위: 0~2) ──────────────────

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

            @Test
            @DisplayName("3 입력 시 에러 메시지 포함 'ERROR'")
            void overRangeInput_errorMessage() throws Exception {
                callIsValidRange(STEP_STEERING, 3);
                assertTrue(output().contains("ERROR"));
            }
        }

        // ── 1-5. 실행/테스트 단계 (step=4, 유효 범위: 0~2) ───────────────

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

            @Test
            @DisplayName("3 입력 시 에러 메시지 포함 'ERROR'")
            void overRangeInput_errorMessage() throws Exception {
                callIsValidRange(STEP_RUN_TEST, 3);
                assertTrue(output().contains("ERROR"));
            }
        }
    }

    // ====================================================================
    // 2. 부품 선택 출력 메시지 (selectXxx)
    // ====================================================================

    @Nested
    @DisplayName("2. 부품 선택 출력 메시지")
    class ComponentSelectionOutputTest {

        // ── 2-1. 차량 타입 ────────────────────────────────────────────────

        @Nested
        @DisplayName("2-1. 차량 타입 선택")
        class CarTypeOutput {

            @Test
            @DisplayName("1 선택 → 'Sedan' 출력")
            void sedan() throws Exception {
                callSelectCarType(SEDAN);
                assertTrue(output().contains("Sedan"));
            }

            @Test
            @DisplayName("2 선택 → 'SUV' 출력")
            void suv() throws Exception {
                callSelectCarType(SUV);
                assertTrue(output().contains("SUV"));
            }

            @Test
            @DisplayName("3 선택 → 'Truck' 출력")
            void truck() throws Exception {
                callSelectCarType(TRUCK);
                assertTrue(output().contains("Truck"));
            }
        }

        // ── 2-2. 엔진 ─────────────────────────────────────────────────────

        @Nested
        @DisplayName("2-2. 엔진 선택")
        class EngineOutput {

            @Test
            @DisplayName("1 선택 → 'GM' 출력")
            void gm() throws Exception {
                callSelectEngine(GM);
                assertTrue(output().contains("GM"));
            }

            @Test
            @DisplayName("2 선택 → 'TOYOTA' 출력")
            void toyota() throws Exception {
                callSelectEngine(TOYOTA);
                assertTrue(output().contains("TOYOTA"));
            }

            @Test
            @DisplayName("3 선택 → 'WIA' 출력")
            void wia() throws Exception {
                callSelectEngine(WIA);
                assertTrue(output().contains("WIA"));
            }

            @Test
            @DisplayName("4 선택 → '고장난 엔진' 출력")
            void brokenEngine() throws Exception {
                callSelectEngine(BROKEN_ENGINE);
                assertTrue(output().contains("고장난 엔진"));
            }
        }

        // ── 2-3. 제동장치 ─────────────────────────────────────────────────

        @Nested
        @DisplayName("2-3. 제동장치 선택")
        class BrakeOutput {

            @Test
            @DisplayName("1 선택 → 'MANDO' 출력")
            void mando() throws Exception {
                callSelectBrakeSystem(MANDO);
                assertTrue(output().contains("MANDO"));
            }

            @Test
            @DisplayName("2 선택 → 'CONTINENTAL' 출력")
            void continental() throws Exception {
                callSelectBrakeSystem(CONTINENTAL);
                assertTrue(output().contains("CONTINENTAL"));
            }

            @Test
            @DisplayName("3 선택 → 'BOSCH' 출력")
            void bosch() throws Exception {
                callSelectBrakeSystem(BOSCH_B);
                assertTrue(output().contains("BOSCH"));
            }
        }

        // ── 2-4. 조향장치 ─────────────────────────────────────────────────

        @Nested
        @DisplayName("2-4. 조향장치 선택")
        class SteeringOutput {

            @Test
            @DisplayName("1 선택 → 'BOSCH' 출력")
            void bosch() throws Exception {
                callSelectSteeringSystem(BOSCH_S);
                assertTrue(output().contains("BOSCH"));
            }

            @Test
            @DisplayName("2 선택 → 'MOBIS' 출력")
            void mobis() throws Exception {
                callSelectSteeringSystem(MOBIS);
                assertTrue(output().contains("MOBIS"));
            }
        }
    }

    // ====================================================================
    // 3. 선택 후 stack 상태 검증
    // ====================================================================

    @Nested
    @DisplayName("3. 선택 후 stack 상태 검증")
    class StackStateTest {

        @Test
        @DisplayName("차량 타입 선택 → stack[0]에 저장")
        void carType_savedToStack0() throws Exception {
            callSelectCarType(SUV);
            assertEquals(SUV, getStack(STEP_CAR_TYPE));
        }

        @Test
        @DisplayName("엔진 선택 → stack[1]에 저장")
        void engine_savedToStack1() throws Exception {
            callSelectEngine(TOYOTA);
            assertEquals(TOYOTA, getStack(STEP_ENGINE));
        }

        @Test
        @DisplayName("제동장치 선택 → stack[2]에 저장")
        void brake_savedToStack2() throws Exception {
            callSelectBrakeSystem(CONTINENTAL);
            assertEquals(CONTINENTAL, getStack(STEP_BRAKE));
        }

        @Test
        @DisplayName("조향장치 선택 → stack[3]에 저장")
        void steering_savedToStack3() throws Exception {
            callSelectSteeringSystem(MOBIS);
            assertEquals(MOBIS, getStack(STEP_STEERING));
        }

        @Test
        @DisplayName("재선택 시 이전 값이 덮어써짐")
        void reselect_overwritesPreviousValue() throws Exception {
            callSelectCarType(SEDAN);
            callSelectCarType(TRUCK);
            assertEquals(TRUCK, getStack(STEP_CAR_TYPE));
        }

        @Test
        @DisplayName("다른 부품 선택이 기존 stack 값에 영향 없음")
        void selectOne_doesNotAffectOtherSlots() throws Exception {
            callSelectCarType(SEDAN);
            callSelectEngine(GM);
            assertEquals(SEDAN, getStack(STEP_CAR_TYPE)); // 차량 타입 유지
            assertEquals(GM,    getStack(STEP_ENGINE));   // 엔진만 변경
            assertEquals(0,     getStack(STEP_BRAKE));    // 제동장치 미선택
        }

        @Test
        @DisplayName("고장난 엔진(4) 선택 → stack[1]에 4 저장")
        void brokenEngine_savedAs4() throws Exception {
            callSelectEngine(BROKEN_ENGINE);
            assertEquals(4, getStack(STEP_ENGINE));
        }
    }

    // ====================================================================
    // 4. 부품 조합 유효성 검증 (isValidCheck)
    // ====================================================================

    @Nested
    @DisplayName("4. 부품 조합 유효성 검증 (isValidCheck)")
    class CombinationValidityTest {

        // ── 4-1. 금지 조합 → false ────────────────────────────────────────

        @Nested
        @DisplayName("4-1. 금지 조합 → false")
        class ForbiddenCombinations {

            @Test
            @DisplayName("Sedan + CONTINENTAL 제동장치 → false")
            void sedan_continental() throws Exception {
                setStack(STEP_CAR_TYPE, SEDAN);
                setStack(STEP_BRAKE, CONTINENTAL);
                assertFalse(callIsValidCheck());
            }

            @Test
            @DisplayName("SUV + TOYOTA 엔진 → false")
            void suv_toyota() throws Exception {
                setStack(STEP_CAR_TYPE, SUV);
                setStack(STEP_ENGINE, TOYOTA);
                assertFalse(callIsValidCheck());
            }

            @Test
            @DisplayName("Truck + WIA 엔진 → false")
            void truck_wia() throws Exception {
                setStack(STEP_CAR_TYPE, TRUCK);
                setStack(STEP_ENGINE, WIA);
                assertFalse(callIsValidCheck());
            }

            @Test
            @DisplayName("Truck + MANDO 제동장치 → false")
            void truck_mando() throws Exception {
                setStack(STEP_CAR_TYPE, TRUCK);
                setStack(STEP_BRAKE, MANDO);
                assertFalse(callIsValidCheck());
            }

            @Test
            @DisplayName("BOSCH 제동장치 + MOBIS 조향장치 → false")
            void boschBrake_mobisSteering() throws Exception {
                setStack(STEP_BRAKE, BOSCH_B);
                setStack(STEP_STEERING, MOBIS);
                assertFalse(callIsValidCheck());
            }
        }

        // ── 4-2. 허용 조합 → true ────────────────────────────────────────

        @Nested
        @DisplayName("4-2. 허용 조합 → true")
        class AllowedCombinations {

            @Test
            @DisplayName("Sedan + GM + MANDO + BOSCH → true")
            void sedan_gm_mando_bosch() throws Exception {
                setStack(STEP_CAR_TYPE, SEDAN);
                setStack(STEP_ENGINE, GM);
                setStack(STEP_BRAKE, MANDO);
                setStack(STEP_STEERING, BOSCH_S);
                assertTrue(callIsValidCheck());
            }

            @Test
            @DisplayName("Sedan + GM + MANDO + MOBIS → true")
            void sedan_gm_mando_mobis() throws Exception {
                setStack(STEP_CAR_TYPE, SEDAN);
                setStack(STEP_ENGINE, GM);
                setStack(STEP_BRAKE, MANDO);
                setStack(STEP_STEERING, MOBIS);
                assertTrue(callIsValidCheck());
            }

            @Test
            @DisplayName("Sedan + GM + BOSCH 제동 + BOSCH 조향 → true (BOSCH 세트 허용)")
            void sedan_gm_boschBrake_boschSteering() throws Exception {
                setStack(STEP_CAR_TYPE, SEDAN);
                setStack(STEP_ENGINE, GM);
                setStack(STEP_BRAKE, BOSCH_B);
                setStack(STEP_STEERING, BOSCH_S);
                assertTrue(callIsValidCheck());
            }

            @Test
            @DisplayName("Sedan + TOYOTA 엔진 → true (Sedan에 TOYOTA 허용)")
            void sedan_toyota_allowed() throws Exception {
                setStack(STEP_CAR_TYPE, SEDAN);
                setStack(STEP_ENGINE, TOYOTA);
                setStack(STEP_BRAKE, MANDO);
                setStack(STEP_STEERING, MOBIS);
                assertTrue(callIsValidCheck());
            }

            @Test
            @DisplayName("Sedan + WIA 엔진 → true (Sedan에 WIA 허용)")
            void sedan_wia_allowed() throws Exception {
                setStack(STEP_CAR_TYPE, SEDAN);
                setStack(STEP_ENGINE, WIA);
                setStack(STEP_BRAKE, MANDO);
                setStack(STEP_STEERING, MOBIS);
                assertTrue(callIsValidCheck());
            }

            @Test
            @DisplayName("SUV + GM + CONTINENTAL + MOBIS → true")
            void suv_gm_continental_mobis() throws Exception {
                setStack(STEP_CAR_TYPE, SUV);
                setStack(STEP_ENGINE, GM);
                setStack(STEP_BRAKE, CONTINENTAL);
                setStack(STEP_STEERING, MOBIS);
                assertTrue(callIsValidCheck());
            }

            @Test
            @DisplayName("SUV + WIA 엔진 → true (SUV에 WIA 허용)")
            void suv_wia_allowed() throws Exception {
                setStack(STEP_CAR_TYPE, SUV);
                setStack(STEP_ENGINE, WIA);
                setStack(STEP_BRAKE, MANDO);
                setStack(STEP_STEERING, MOBIS);
                assertTrue(callIsValidCheck());
            }

            @Test
            @DisplayName("Truck + GM + CONTINENTAL + MOBIS → true")
            void truck_gm_continental_mobis() throws Exception {
                setStack(STEP_CAR_TYPE, TRUCK);
                setStack(STEP_ENGINE, GM);
                setStack(STEP_BRAKE, CONTINENTAL);
                setStack(STEP_STEERING, MOBIS);
                assertTrue(callIsValidCheck());
            }

            @Test
            @DisplayName("Truck + TOYOTA + BOSCH 세트 → true")
            void truck_toyota_bosch_bosch() throws Exception {
                setStack(STEP_CAR_TYPE, TRUCK);
                setStack(STEP_ENGINE, TOYOTA);
                setStack(STEP_BRAKE, BOSCH_B);
                setStack(STEP_STEERING, BOSCH_S);
                assertTrue(callIsValidCheck());
            }

            @Test
            @DisplayName("Sedan + MANDO 제동장치 → true (Truck에만 MANDO 금지)")
            void sedan_mando_allowed() throws Exception {
                setStack(STEP_CAR_TYPE, SEDAN);
                setStack(STEP_ENGINE, GM);
                setStack(STEP_BRAKE, MANDO);
                setStack(STEP_STEERING, MOBIS);
                assertTrue(callIsValidCheck());
            }

            @Test
            @DisplayName("SUV + MANDO 제동장치 → true (Truck에만 MANDO 금지)")
            void suv_mando_allowed() throws Exception {
                setStack(STEP_CAR_TYPE, SUV);
                setStack(STEP_ENGINE, GM);
                setStack(STEP_BRAKE, MANDO);
                setStack(STEP_STEERING, MOBIS);
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

        // ── 5-1. 금지 조합 → 동작 불가 ───────────────────────────────────

        @Nested
        @DisplayName("5-1. 금지 조합 → 동작 불가 메시지")
        class InvalidCombinationRun {

            @Test
            @DisplayName("Sedan + CONTINENTAL → '자동차가 동작되지 않습니다' 출력")
            void sedan_continental_doesNotRun() throws Exception {
                setStack(STEP_CAR_TYPE, SEDAN);
                setStack(STEP_BRAKE, CONTINENTAL);
                callRunProducedCar();
                assertTrue(output().contains("자동차가 동작되지 않습니다"));
            }

            @Test
            @DisplayName("SUV + TOYOTA → '자동차가 동작되지 않습니다' 출력")
            void suv_toyota_doesNotRun() throws Exception {
                setStack(STEP_CAR_TYPE, SUV);
                setStack(STEP_ENGINE, TOYOTA);
                callRunProducedCar();
                assertTrue(output().contains("자동차가 동작되지 않습니다"));
            }

            @Test
            @DisplayName("Truck + WIA → '자동차가 동작되지 않습니다' 출력")
            void truck_wia_doesNotRun() throws Exception {
                setStack(STEP_CAR_TYPE, TRUCK);
                setStack(STEP_ENGINE, WIA);
                callRunProducedCar();
                assertTrue(output().contains("자동차가 동작되지 않습니다"));
            }

            @Test
            @DisplayName("Truck + MANDO → '자동차가 동작되지 않습니다' 출력")
            void truck_mando_doesNotRun() throws Exception {
                setStack(STEP_CAR_TYPE, TRUCK);
                setStack(STEP_BRAKE, MANDO);
                callRunProducedCar();
                assertTrue(output().contains("자동차가 동작되지 않습니다"));
            }

            @Test
            @DisplayName("BOSCH 제동 + MOBIS 조향 → '자동차가 동작되지 않습니다' 출력")
            void boschBrake_mobisSteering_doesNotRun() throws Exception {
                setStack(STEP_CAR_TYPE, SEDAN);
                setStack(STEP_ENGINE, GM);
                setStack(STEP_BRAKE, BOSCH_B);
                setStack(STEP_STEERING, MOBIS);
                callRunProducedCar();
                assertTrue(output().contains("자동차가 동작되지 않습니다"));
            }

            @Test
            @DisplayName("금지 조합 → '자동차가 동작됩니다' 출력 안 됨")
            void invalidCombination_noRunMessage() throws Exception {
                setStack(STEP_CAR_TYPE, SEDAN);
                setStack(STEP_BRAKE, CONTINENTAL);
                callRunProducedCar();
                assertFalse(output().contains("자동차가 동작됩니다"));
            }
        }

        // ── 5-2. 고장난 엔진 ──────────────────────────────────────────────

        @Nested
        @DisplayName("5-2. 고장난 엔진(4)")
        class BrokenEngineRun {

            @Test
            @DisplayName("고장난 엔진 → '엔진이 고장나있습니다' 출력")
            void brokenEngine_engineFailMessage() throws Exception {
                setStack(STEP_CAR_TYPE, SEDAN);
                setStack(STEP_ENGINE, BROKEN_ENGINE);
                setStack(STEP_BRAKE, MANDO);
                setStack(STEP_STEERING, BOSCH_S);
                callRunProducedCar();
                assertTrue(output().contains("엔진이 고장나있습니다"));
            }

            @Test
            @DisplayName("고장난 엔진 → '자동차가 움직이지 않습니다' 출력")
            void brokenEngine_noMoveMessage() throws Exception {
                setStack(STEP_CAR_TYPE, SEDAN);
                setStack(STEP_ENGINE, BROKEN_ENGINE);
                setStack(STEP_BRAKE, MANDO);
                setStack(STEP_STEERING, BOSCH_S);
                callRunProducedCar();
                assertTrue(output().contains("자동차가 움직이지 않습니다"));
            }

            @Test
            @DisplayName("고장난 엔진 → '자동차가 동작됩니다' 출력 안 됨")
            void brokenEngine_noRunMessage() throws Exception {
                setStack(STEP_CAR_TYPE, SEDAN);
                setStack(STEP_ENGINE, BROKEN_ENGINE);
                setStack(STEP_BRAKE, MANDO);
                setStack(STEP_STEERING, BOSCH_S);
                callRunProducedCar();
                assertFalse(output().contains("자동차가 동작됩니다"));
            }

            @Test
            @DisplayName("금지 조합 + 고장난 엔진 → 조합 오류 메시지 우선 출력")
            void invalidCombinationTakesPriorityOverBrokenEngine() throws Exception {
                // Sedan + CONTINENTAL 은 금지 조합이면서 동시에 고장난 엔진
                setStack(STEP_CAR_TYPE, SEDAN);
                setStack(STEP_ENGINE, BROKEN_ENGINE);
                setStack(STEP_BRAKE, CONTINENTAL);
                callRunProducedCar();
                assertTrue(output().contains("자동차가 동작되지 않습니다"));
                assertFalse(output().contains("엔진이 고장나있습니다"));
            }
        }

        // ── 5-3. 정상 동작 ────────────────────────────────────────────────

        @Nested
        @DisplayName("5-3. 정상 조합 → 동작 메시지 및 부품 정보 출력")
        class ValidRun {

            @Test
            @DisplayName("정상 조합 → '자동차가 동작됩니다' 출력")
            void validCombination_runsSuccessfully() throws Exception {
                setStack(STEP_CAR_TYPE, SEDAN);
                setStack(STEP_ENGINE, GM);
                setStack(STEP_BRAKE, MANDO);
                setStack(STEP_STEERING, BOSCH_S);
                callRunProducedCar();
                assertTrue(output().contains("자동차가 동작됩니다"));
            }

            @Test
            @DisplayName("정상 조합 → 차량 타입 출력 (SUV)")
            void validCombination_showsCarType() throws Exception {
                setStack(STEP_CAR_TYPE, SUV);
                setStack(STEP_ENGINE, GM);
                setStack(STEP_BRAKE, MANDO);
                setStack(STEP_STEERING, MOBIS);
                callRunProducedCar();
                assertTrue(output().contains("SUV"));
            }

            @Test
            @DisplayName("정상 조합 → 엔진 정보 출력 (TOYOTA)")
            void validCombination_showsEngine() throws Exception {
                setStack(STEP_CAR_TYPE, TRUCK);
                setStack(STEP_ENGINE, TOYOTA);
                setStack(STEP_BRAKE, CONTINENTAL);
                setStack(STEP_STEERING, MOBIS);
                callRunProducedCar();
                assertTrue(output().contains("TOYOTA"));
            }

            @Test
            @DisplayName("정상 조합 → 제동장치 출력 (Bosch)")
            void validCombination_showsBrake() throws Exception {
                setStack(STEP_CAR_TYPE, SEDAN);
                setStack(STEP_ENGINE, GM);
                setStack(STEP_BRAKE, BOSCH_B);
                setStack(STEP_STEERING, BOSCH_S);
                callRunProducedCar();
                assertTrue(output().contains("Bosch"));
            }

            @Test
            @DisplayName("정상 조합 → 조향장치 출력 (Mobis)")
            void validCombination_showsSteering() throws Exception {
                setStack(STEP_CAR_TYPE, SEDAN);
                setStack(STEP_ENGINE, GM);
                setStack(STEP_BRAKE, MANDO);
                setStack(STEP_STEERING, MOBIS);
                callRunProducedCar();
                assertTrue(output().contains("Mobis"));
            }

            @Test
            @DisplayName("정상 조합 → 제동장치 출력 (Continental)")
            void validCombination_showsContinentalBrake() throws Exception {
                setStack(STEP_CAR_TYPE, SUV);
                setStack(STEP_ENGINE, GM);
                setStack(STEP_BRAKE, CONTINENTAL);
                setStack(STEP_STEERING, MOBIS);
                callRunProducedCar();
                assertTrue(output().contains("Continental"));
            }

            @Test
            @DisplayName("정상 조합 → 제동장치 출력 (Mando)")
            void validCombination_showsMandoBrake() throws Exception {
                setStack(STEP_CAR_TYPE, SEDAN);
                setStack(STEP_ENGINE, GM);
                setStack(STEP_BRAKE, MANDO);
                setStack(STEP_STEERING, BOSCH_S);
                callRunProducedCar();
                assertTrue(output().contains("Mando"));
            }

            @Test
            @DisplayName("정상 조합 → 조향장치 출력 (Bosch)")
            void validCombination_showsBoschSteering() throws Exception {
                setStack(STEP_CAR_TYPE, SEDAN);
                setStack(STEP_ENGINE, GM);
                setStack(STEP_BRAKE, MANDO);
                setStack(STEP_STEERING, BOSCH_S);
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

        // ── 6-1. 금지 조합 → FAIL ─────────────────────────────────────────

        @Nested
        @DisplayName("6-1. 금지 조합 → FAIL + 원인 메시지")
        class FailCases {

            @Test
            @DisplayName("Sedan + CONTINENTAL → FAIL 출력")
            void sedan_continental_fail() throws Exception {
                setStack(STEP_CAR_TYPE, SEDAN);
                setStack(STEP_BRAKE, CONTINENTAL);
                callTestProducedCar();
                assertTrue(output().contains("FAIL"));
            }

            @Test
            @DisplayName("Sedan + CONTINENTAL → 원인 메시지에 'Continental' 포함")
            void sedan_continental_failMessage() throws Exception {
                setStack(STEP_CAR_TYPE, SEDAN);
                setStack(STEP_BRAKE, CONTINENTAL);
                callTestProducedCar();
                assertTrue(output().contains("Continental"));
            }

            @Test
            @DisplayName("SUV + TOYOTA → FAIL 출력")
            void suv_toyota_fail() throws Exception {
                setStack(STEP_CAR_TYPE, SUV);
                setStack(STEP_ENGINE, TOYOTA);
                callTestProducedCar();
                assertTrue(output().contains("FAIL"));
            }

            @Test
            @DisplayName("SUV + TOYOTA → 원인 메시지에 'TOYOTA' 포함")
            void suv_toyota_failMessage() throws Exception {
                setStack(STEP_CAR_TYPE, SUV);
                setStack(STEP_ENGINE, TOYOTA);
                callTestProducedCar();
                assertTrue(output().contains("TOYOTA"));
            }

            @Test
            @DisplayName("Truck + WIA → FAIL 출력")
            void truck_wia_fail() throws Exception {
                setStack(STEP_CAR_TYPE, TRUCK);
                setStack(STEP_ENGINE, WIA);
                callTestProducedCar();
                assertTrue(output().contains("FAIL"));
            }

            @Test
            @DisplayName("Truck + WIA → 원인 메시지에 'WIA' 포함")
            void truck_wia_failMessage() throws Exception {
                setStack(STEP_CAR_TYPE, TRUCK);
                setStack(STEP_ENGINE, WIA);
                callTestProducedCar();
                assertTrue(output().contains("WIA"));
            }

            @Test
            @DisplayName("Truck + MANDO → FAIL 출력")
            void truck_mando_fail() throws Exception {
                setStack(STEP_CAR_TYPE, TRUCK);
                setStack(STEP_BRAKE, MANDO);
                callTestProducedCar();
                assertTrue(output().contains("FAIL"));
            }

            @Test
            @DisplayName("Truck + MANDO → 원인 메시지에 'Mando' 포함")
            void truck_mando_failMessage() throws Exception {
                setStack(STEP_CAR_TYPE, TRUCK);
                setStack(STEP_BRAKE, MANDO);
                callTestProducedCar();
                assertTrue(output().contains("Mando"));
            }

            @Test
            @DisplayName("BOSCH 제동 + MOBIS 조향 → FAIL 출력")
            void boschBrake_mobisSteering_fail() throws Exception {
                setStack(STEP_BRAKE, BOSCH_B);
                setStack(STEP_STEERING, MOBIS);
                callTestProducedCar();
                assertTrue(output().contains("FAIL"));
            }

            @Test
            @DisplayName("BOSCH 제동 + MOBIS 조향 → 원인 메시지에 'Bosch' 포함")
            void boschBrake_mobisSteering_failMessage() throws Exception {
                setStack(STEP_BRAKE, BOSCH_B);
                setStack(STEP_STEERING, MOBIS);
                callTestProducedCar();
                assertTrue(output().contains("Bosch"));
            }

            @Test
            @DisplayName("FAIL 결과 → 'PASS' 출력 안 됨")
            void failResult_noPassMessage() throws Exception {
                setStack(STEP_CAR_TYPE, SUV);
                setStack(STEP_ENGINE, TOYOTA);
                callTestProducedCar();
                assertFalse(output().contains("PASS"));
            }

            @Test
            @DisplayName("복수 금지 조건 해당 시 첫 번째 조건만 출력 (if-else 체인)")
            void multipleViolations_onlyFirstReported() throws Exception {
                // Sedan + CONTINENTAL(1번 조건) + SUV가 아니므로 TOYOTA 조건은 해당 안 됨
                // 대신: Sedan + CONTINENTAL 조건이 먼저 걸림
                setStack(STEP_CAR_TYPE, SEDAN);
                setStack(STEP_BRAKE, CONTINENTAL);
                callTestProducedCar();
                // Sedan + Continental 메시지만 포함, SUV 관련 메시지는 없음
                assertTrue(output().contains("Sedan"));
                assertFalse(output().contains("SUV"));
            }
        }

        // ── 6-2. 허용 조합 → PASS ─────────────────────────────────────────

        @Nested
        @DisplayName("6-2. 허용 조합 → PASS")
        class PassCases {

            @Test
            @DisplayName("Sedan + GM + MANDO + BOSCH → PASS")
            void sedan_gm_mando_bosch_pass() throws Exception {
                setStack(STEP_CAR_TYPE, SEDAN);
                setStack(STEP_ENGINE, GM);
                setStack(STEP_BRAKE, MANDO);
                setStack(STEP_STEERING, BOSCH_S);
                callTestProducedCar();
                assertTrue(output().contains("PASS"));
            }

            @Test
            @DisplayName("Sedan + GM + MANDO + MOBIS → PASS")
            void sedan_gm_mando_mobis_pass() throws Exception {
                setStack(STEP_CAR_TYPE, SEDAN);
                setStack(STEP_ENGINE, GM);
                setStack(STEP_BRAKE, MANDO);
                setStack(STEP_STEERING, MOBIS);
                callTestProducedCar();
                assertTrue(output().contains("PASS"));
            }

            @Test
            @DisplayName("Sedan + GM + BOSCH 세트 → PASS")
            void sedan_gm_bosch_bosch_pass() throws Exception {
                setStack(STEP_CAR_TYPE, SEDAN);
                setStack(STEP_ENGINE, GM);
                setStack(STEP_BRAKE, BOSCH_B);
                setStack(STEP_STEERING, BOSCH_S);
                callTestProducedCar();
                assertTrue(output().contains("PASS"));
            }

            @Test
            @DisplayName("SUV + GM + CONTINENTAL + MOBIS → PASS")
            void suv_gm_continental_mobis_pass() throws Exception {
                setStack(STEP_CAR_TYPE, SUV);
                setStack(STEP_ENGINE, GM);
                setStack(STEP_BRAKE, CONTINENTAL);
                setStack(STEP_STEERING, MOBIS);
                callTestProducedCar();
                assertTrue(output().contains("PASS"));
            }

            @Test
            @DisplayName("Truck + GM + CONTINENTAL + MOBIS → PASS")
            void truck_gm_continental_mobis_pass() throws Exception {
                setStack(STEP_CAR_TYPE, TRUCK);
                setStack(STEP_ENGINE, GM);
                setStack(STEP_BRAKE, CONTINENTAL);
                setStack(STEP_STEERING, MOBIS);
                callTestProducedCar();
                assertTrue(output().contains("PASS"));
            }

            @Test
            @DisplayName("Truck + TOYOTA + BOSCH 세트 → PASS")
            void truck_toyota_bosch_bosch_pass() throws Exception {
                setStack(STEP_CAR_TYPE, TRUCK);
                setStack(STEP_ENGINE, TOYOTA);
                setStack(STEP_BRAKE, BOSCH_B);
                setStack(STEP_STEERING, BOSCH_S);
                callTestProducedCar();
                assertTrue(output().contains("PASS"));
            }

            @Test
            @DisplayName("PASS 결과 → 'FAIL' 출력 안 됨")
            void passResult_noFailMessage() throws Exception {
                setStack(STEP_CAR_TYPE, SEDAN);
                setStack(STEP_ENGINE, GM);
                setStack(STEP_BRAKE, MANDO);
                setStack(STEP_STEERING, MOBIS);
                callTestProducedCar();
                assertFalse(output().contains("FAIL"));
            }
        }
    }
}
