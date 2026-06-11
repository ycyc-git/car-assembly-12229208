import java.util.Scanner;

public class Assemble {

    public static void main(String[] args) {
        Scanner         sc      = new Scanner(System.in);
        ConsoleMenu     menu    = new ConsoleMenu();
        AssemblyService service = new AssemblyService();
        Car             car     = new Car();
        int             step    = 0;

        while (true) {
            menu.show(step);
            int answer = menu.readInput(sc, step);

            if (answer == -1) {
                System.out.println("바이바이");
                break;
            }

            if (answer == 0) {
                step = (step == 4) ? 0 : Math.max(0, step - 1);
                continue;
            }

            if (step < 4) {
                service.select(step, answer, car);
                delay(800);
                step++;
            } else {
                if (answer == 1) {
                    service.run(car);
                    delay(2000);
                } else if (answer == 2) {
                    System.out.println("Test...");
                    delay(1500);
                    service.test(car);
                    delay(2000);
                }
            }
        }

        sc.close();
    }

    private static void delay(int ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}
