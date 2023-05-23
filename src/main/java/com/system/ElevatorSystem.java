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

    // assigns pickup requests to elevators in an optimal way
    // first, it clears all the request that have not already been picked
    // then, for each pickup request it collects an estimated time of arrival for every elevator
    // then, it searches for a minimum time in matrix, assigns the elevator to this request, updates all the estimated arrival times for this elevator
    // repeat above steps until all pickup requests are assigned
    private void assignPickupRequests() {
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

    // for every elevator that opened door in this step, creates a doorCloser which is responsible for closing
    // elevator's door after a given number of steps
    private void createDoorClosers() {
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

    // invokes doorClosers' steps and removes the one that have already closed their elevator's door
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

    // creates a pickup request (somebody clicks on a button at some floor)
    public void pickup(PickupRequest request) {
        requests.add(request);
    }

    // returns current state of all elevators
    public Elevator[] status() {
        return elevators;
    }

    // performs a step in our simulation
    // (1) pickup requests are assigned to optimal elevators
    // (2) elevators' steps are invoked
    // (3) checks if any elevator is broken. if so - notify them
    // (4) invokes doorCloser steps, removes done doorClosers
    // (5) creates doorClosers where necessary
    // (6) updates deliveries (buttons inside elevators) base on pickup requests
    // (7) removes successful deliveries and done pickup requests
    public void step() {
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
