package com.system;

import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ElevatorSystem {

    private static int currentStep = 0;
    private final int numberOfElevators;
    private final int numberOfFloors;
    private final int criticalStepsSinceElevatorDoorOpened;
    private final int realLeavingSteps;
    private final Elevator[] elevators;
    private final List<PickupRequest> requests;
    private final List<ElevatorDoorCloser> doorClosers;

    protected static final Logger logger = LogManager.getLogger();

    public ElevatorSystem(int numberOfElevators, int numberOfFloors, int criticalStepsSinceElevatorDoorOpened,
                          int estimatedEnteringSteps, int estimatedLeavingSteps, int realLeavingSteps, int[] elevatorStartingFloors) {
        this.numberOfElevators = numberOfElevators;
        this.numberOfFloors = numberOfFloors;
        this.criticalStepsSinceElevatorDoorOpened = criticalStepsSinceElevatorDoorOpened;
        this.realLeavingSteps = realLeavingSteps;
        this.elevators = new Elevator[numberOfElevators];
        this.requests = new LinkedList<>();
        this.doorClosers = new LinkedList<>();

        for (int i = 0; i < numberOfElevators; i++) {
            elevators[i] = new Elevator(i, elevatorStartingFloors[i], estimatedEnteringSteps, estimatedLeavingSteps, numberOfFloors);
        }
    }

    private static void incrementCurrentStep() {
        currentStep++;
    }

    public static int getCurrentStep() {
        return currentStep;
    }

    private void assignPickupRequests() {
        // tworzy optymalne rozdanie pickupów do wind
        // czyści wszystkie obecnie przyporządkowane pickupy
        // najpierw dla każdego pickupa zbiera estymowany czas dotarcia każdej windy
        // następnie szuka minimum czasu w macierzy, przyporządkowuje temu requestowi tę windę, uaktualnia wszystkie czasy dla tej windy
        // robi to, aż wszystkie requesty nie zostaną przyporządkowane do wind
        int[][] estimatedStepsMatrix = new int[requests.size()][numberOfElevators];
        for (int i = 0; i < estimatedStepsMatrix.length; i++) {
            for (int j = 0; j < estimatedStepsMatrix[0].length; j++) {
                if (elevators[j].isBroken(criticalStepsSinceElevatorDoorOpened)) {
                    estimatedStepsMatrix[i][j] = Integer.MAX_VALUE;
                } else {
                    estimatedStepsMatrix[i][j] = elevators[j].estimateNumberOfStepsUntilPickup(requests.get(i));
                }
            }
        }
        Set<Integer> assignedRequests = new HashSet<>();

        while (assignedRequests.size() < requests.size()) {
            int[] bestFitCoordinates = findMinimumInMatrixAndReturnCoordinates(estimatedStepsMatrix, assignedRequests);
            elevators[bestFitCoordinates[1]].addPickupRequest(requests.get(bestFitCoordinates[0]));
            assignedRequests.add(bestFitCoordinates[0]);
            modifyEstimatedStepsForGivenElevator(estimatedStepsMatrix, assignedRequests, bestFitCoordinates[1]);
        }

    }

    private void modifyEstimatedStepsForGivenElevator(int[][] matrix, Set<Integer> rowsToSkip, int elevatorId) {
        for (int i = 0; i < matrix.length; i++) {
            if (rowsToSkip.contains(i)) {
                continue;
            }
            if (elevators[elevatorId].isBroken(criticalStepsSinceElevatorDoorOpened)) {
                matrix[i][elevatorId] = Integer.MAX_VALUE;
            } else {
                matrix[i][elevatorId] = elevators[elevatorId].estimateNumberOfStepsUntilPickup(requests.get(i));
            }
        }
    }

    private int[] findMinimumInMatrixAndReturnCoordinates(int[][] matrix, Set<Integer> rowsToSkip) {
        int row = -1;
        int column = -1;
        int value = Integer.MAX_VALUE;
        for (int i = 0; i < matrix.length; i++) {
            if (rowsToSkip.contains(i)) {
                continue;
            }
            for (int j = 0; j < matrix[0].length; j++) {
                if (matrix[i][j] < value) {
                    row = i;
                    column = j;
                    value = matrix[i][j];
                }
            }
        }
        if (row == -1 && column == -1) {
            logger.error("All rows in matrix were skipped or all elevators are broken (more likely).");
        }
        return new int[] {row, column};
    }

    private void createDoorClosers() {
        // dla każdej windy, która otwarła drzwi w tym stepie tworzy doorClosera
        // z odpowiednią liczbą stepów do zamknięcia
        for (int i = 0; i < elevators.length; i++) {
            Elevator currentElevator = elevators[i];
            if (currentElevator.isDoorClosed() || currentElevator.getStepsSinceDoorOpened() != 0) {
                continue;
            }
            boolean isDeliveryFloor = currentElevator.getDeliveryDestinationFloors().stream()
                    .anyMatch(x -> x == currentElevator.getCurrentFloor());
            Optional<PickupRequest> pickupForThisFloor = currentElevator.getPickupRequestsToHandle().stream()
                    .filter(x -> x.isRequestDone() && x.getFloor() == currentElevator.getCurrentFloor()).findFirst();

            if (isDeliveryFloor && pickupForThisFloor.isEmpty()) {
                doorClosers.add(new ElevatorDoorCloser(currentElevator, realLeavingSteps));
            } else if (!isDeliveryFloor && pickupForThisFloor.isPresent()) {
                doorClosers.add(new ElevatorDoorCloser(currentElevator, pickupForThisFloor.get().getNumberOfEnteringSteps()));
            } else if (pickupForThisFloor.isPresent()) {
                doorClosers.add(new ElevatorDoorCloser(currentElevator, pickupForThisFloor.get().getNumberOfEnteringSteps() + realLeavingSteps));
            } else {
                logger.error("Elevator opened door for nothing.");
            }
        }
    }

    private void invokeDoorClosersStepsAndRemoveUnnecessary() {
        Iterator<ElevatorDoorCloser> iterator = doorClosers.iterator();
        while (iterator.hasNext()) {
            ElevatorDoorCloser doorCloser = iterator.next();
            if (doorCloser.getStepsUntilDoorClosed() == 0) {
                iterator.remove();
            } else {
                doorCloser.step();
            }
        }
    }

    private void updateDeliveriesBasedOnDonePickups() {
        for (int i = 0; i < elevators.length; i++) {
            for (PickupRequest request: elevators[i].getPickupRequestsToHandle()) {
                if (request.isRequestDone()) {
                    elevators[i].addDeliveryDestinationFloorList(request.getDestinationFloors());
                }
            }
        }
    }

    private void checkForBrokenElevators() {
        for (int i = 0; i < elevators.length; i++) {
            if (elevators[i].isBroken(criticalStepsSinceElevatorDoorOpened)) {
                elevators[i].notifyBrokenElevator();
            }
        }
    }

    private void removeDonePickups() {
        requests.removeIf(PickupRequest::isRequestDone);
    }

    private void removeSuccessfulDeliveries() {
        for (int i = 0; i < elevators.length; i++) {
            if (elevators[i].getDeliveryDestinationFloors().contains(elevators[i].getCurrentFloor())) {
                elevators[i].removeDelivery(elevators[i].getCurrentFloor());
            }
        }
    }

    private void invokeElevatorsSteps() {
        for (int i = 0; i < elevators.length; i++) {
            elevators[i].step();
        }
    }

    private void clearElevatorPickupRequests() {
        for (int i = 0; i < elevators.length; i++) {
            elevators[i].clearPickupRequests();
        }
    }

    public void pickup(PickupRequest request) {
        requests.add(request);
    }

    public Elevator[] status() {
        return elevators;
    }

    public void step() {
        // najpierw rozdanie pickupów do wind (w każdym stepie na nowo)
        // wywołanie stepów wszystkich wind
        // sprawdzenie, czy windy nie są zepsute
        // stworzenie door closerów tam, gdzie windy właśnie otwarły drzwi
        // wywołanie stepów na wszystkich door closerach i usuniecie niepotrzebnych
        // uaktualnia sety pięter windom na podstawie przyporządkowanych requestów
        // usunięcie spełnionych deliveries
        // usuwa przetworzone requesty
        clearElevatorPickupRequests();
        assignPickupRequests();
        invokeElevatorsSteps();
        checkForBrokenElevators();
        invokeDoorClosersStepsAndRemoveUnnecessary();
        createDoorClosers();
        updateDeliveriesBasedOnDonePickups();
        removeSuccessfulDeliveries();
        removeDonePickups();
    }


}
