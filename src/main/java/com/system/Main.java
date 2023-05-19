package com.system;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Cześć, witaj w prostym symulatorze jazdy wind w budynku!\n");
        System.out.println("Na początku poproszę Cię o podanie kilku niezbędnych parametrów:\n");

        int numberOfElevators = requestInt(scanner, "1) Liczba wind w budynku (od 1 do 16):", 1, 16);
        int numberOfFloors = requestInt(scanner, "2) Liczba pięter w budynku (minimum 3):", 3);
        int criticalStepsSinceElevatorDoorOpened = requestInt(scanner, "3) Liczba jednostek czasu, po których uznajemy, że winda z otwartymi drzwiami jest zepsuta (minimum 5):", 5);
        int estimatedStepsEntry = requestInt(scanner, "4) Estymowana liczba jednostek czasu potrzebna na wejście do windy (minimum 1):",1);
        int estimatedStepsLeave = requestInt(scanner, "5) Estymowana liczba jednostek czasu potrzebna na wyjście z windy (minimum 1):", 1);
        int realStepsLeave = requestInt(scanner, "6) Rzeczywista liczba jednostek czasu potrzebna na wyjście z windy (minimum 1)", 1);
        int[] startingFloors = requestElevatorsStartingFloors(scanner, numberOfElevators, numberOfFloors);

        ElevatorSystem elevatorSystem = new ElevatorSystem(numberOfElevators, numberOfFloors,
                criticalStepsSinceElevatorDoorOpened, estimatedStepsEntry,
                estimatedStepsLeave, realStepsLeave, startingFloors);

        boolean finished = false;

        while (!finished) {
            System.out.println("""
                    Co chciałbyś zrobić?
                    
                    1) Stworzyć nowe przywołanie windy (pickup)
                    2) Wykonać jeden krok symulacji (step)
                    3) Sprawdzić status wind (status)
                    4) Zakończyć program
                    
                    Podaj liczbę od 1 do 4:""");
            int response = scanner.nextInt();
            switch (response) {
                case 1 -> pickup(scanner, numberOfFloors, elevatorSystem);
                case 2 -> step(elevatorSystem);
                case 3 -> status(elevatorSystem);
                case 4 -> finished = true;
                default -> System.out.println("Wybrana liczba nie jest obsługiwana (tylko liczby od 1 do 4).");
            }
        }

        scanner.close();
        System.out.println("\nDzięki, do zobaczenia!");
    }

    public static void pickup(Scanner scanner, int numberOfFloors, ElevatorSystem elevatorSystem) {
        System.out.println("Wybrałeś opcję: 1) Stworzyć nowe przywołanie windy. Niezbędne jest podanie parametrów przywołania windy.");
        int requestFloor = requestInt(scanner, "Podaj piętro, na którym następuje wezwanie windy: ", 0, numberOfFloors);
        int direction = requestInt(scanner, "Podaj kierunek, w którym nastąpi wywołanie (0 - DOWN, 1 - UP): ", 0, 1);
        int numberOfEnteringSteps = requestInt(scanner, "Podaj liczbę jednostek czasu wsiadania do windy (minimum 1): ", 1);
        int numberOfDeliveries = requestInt(scanner, "Podaj liczbę guzików, wciśniętych przez ludzi wsiadających na tym piętrze (minimum 1): ", 1);
        List<Integer> deliveryFloors = new ArrayList<>(numberOfDeliveries);
        for (int i = 0; i < numberOfDeliveries; i++) {
            deliveryFloors.add(requestInt(scanner, "Guzik numer " + i + ": ",0, numberOfFloors));
        }
        RequestDirection requestDirection;
        if (direction == 0) {
            requestDirection = RequestDirection.DOWN;
        } else {
            requestDirection = RequestDirection.UP;
        }
        PickupRequest request = new PickupRequest(requestFloor, requestDirection, numberOfEnteringSteps, deliveryFloors);
        elevatorSystem.pickup(request);
        System.out.println("Zapisano wezwanie windy o podanych charakterystykach.");
    }

    public static void step(ElevatorSystem elevatorSystem) {
        System.out.println("Wybrałeś opcję: 2) Wykonać jeden krok symulacji.");
        elevatorSystem.step();
        System.out.println("Żądany krok symulacji został wykonany.");
    }

    public static void status(ElevatorSystem elevatorSystem) {
        System.out.println("Wybrałeś opcję: 3) Sprawdzić status wind.\n");
        Elevator[] elevators = elevatorSystem.status();
        for (Elevator elevator: elevators) {
            System.out.println(elevator);
        }
        System.out.println("\nZakończono sprawdzanie statusu wind.");
    }

    public static int requestInt(Scanner scanner, String s, int criticalValue) {
        int res = -1;
        while (true) {
            System.out.println(s);
            int input = scanner.nextInt();
            if (input >= criticalValue) {
                res = input;
                break;
            } else {
                System.out.println("Podano niepoprawną wartość. Spróbuj ponownie.");
            }
        }
        return res;
    }

    public static int requestInt(Scanner scanner, String s, int criticalValueOne, int criticalValueTwo) {
        int res = -1;
        while (true) {
            System.out.println(s);
            int input = scanner.nextInt();
            if (input >= criticalValueOne && input <= criticalValueTwo) {
                res = input;
                break;
            } else {
                System.out.println("Podano niepoprawną wartość. Spróbuj ponownie.");
            }
        }
        return res;
    }

    public static int[] requestElevatorsStartingFloors(Scanner scanner, int numberOfElevators, int numberOfFloors) {
        int[] res = new int[numberOfElevators];
        for (int i = 0; i < numberOfElevators; i++) {
            res[i] = requestInt(scanner, "Piętro startowe windy " + i + " (od 0 do " + numberOfFloors + ")", 0, numberOfFloors);
        }
        return res;
    }

}