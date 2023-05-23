package com.system;

import com.system.activities.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Getter
@Setter
@ToString
class Elevator {

    private final int id;
    @ToString.Exclude
    private final int estimatedEnteringSteps;
    @ToString.Exclude
    private final int estimatedLeavingSteps;
    @ToString.Exclude
    private final int numberOfFloors;
    private int stepsSinceDoorOpened;
    private int currentFloor;
    private boolean isDoorClosed;
    private ElevatorState state;
    private final Set<Integer> deliveryDestinationFloors;
    private final Set<PickupRequest> pickupRequestsToHandle;
    @ToString.Exclude
    private final Map<Integer, List<ElevatorActivity>> activityLogsMap;

    protected static final Logger logger = LogManager.getLogger();

    public Elevator(int id, int currentFloor, int estimatedEnteringSteps, int estimatedLeavingSteps, int numberOfFloors) {
        this.id = id;
        this.currentFloor = currentFloor;
        this.isDoorClosed = true;
        this.stepsSinceDoorOpened = 0;
        this.numberOfFloors = numberOfFloors;
        this.estimatedEnteringSteps = estimatedEnteringSteps;
        this.estimatedLeavingSteps = estimatedLeavingSteps;
        this.state = ElevatorState.IDLE;
        this.deliveryDestinationFloors = new HashSet<>();
        this.pickupRequestsToHandle = new HashSet<>();
        this.activityLogsMap = new HashMap<>();
    }

    public void addPickupRequest(PickupRequest request) {
        pickupRequestsToHandle.add(request);
        logElevatorActivity(new AddPickupRequestActivity(state, request.getFloor()));
    }

    public void clearPickupRequests() {
        pickupRequestsToHandle.clear();
        logElevatorActivity(new ClearPickupRequestsActivity(state));
    }

    public void addDeliveryDestinationFloorList(List<Integer> destinationFloors) {
        deliveryDestinationFloors.addAll(destinationFloors);
    }

    public void removeDelivery(int floor) {
        deliveryDestinationFloors.remove(floor);
    }

    private void logElevatorActivity(ElevatorActivity activity) {
        if (activityLogsMap.containsKey(ElevatorSystem.getCurrentStep())) {
            List<ElevatorActivity> currentActivityList = new ArrayList<>(activityLogsMap.get(ElevatorSystem.getCurrentStep()));
            currentActivityList.add(activity);
            activityLogsMap.put(ElevatorSystem.getCurrentStep(), currentActivityList);
        } else {
            activityLogsMap.put(ElevatorSystem.getCurrentStep(), List.of(activity));
        }
    }

    // checks elevator state and performs elevator movement
    private void moveElevator() {
        switch (state) {
            case UP -> {
                if (currentFloor == numberOfFloors) {
                    logger.error("Elevator cannot go up anymore.");
                }
                currentFloor++;
                logElevatorActivity(new MoveActivity(state, currentFloor - 1, currentFloor));
            }
            case DOWN -> {
                if (currentFloor == 0) {
                    logger.error("Elevator cannot go below ground level.");
                }
                currentFloor--;
                logElevatorActivity(new MoveActivity(state, currentFloor + 1, currentFloor));
            }
        }
    }

    private void openDoor() {
        if (!isDoorClosed) {
            logger.warn("Elevator is trying to open the door that were already opened.");
        }
        isDoorClosed = false;
        logElevatorActivity(new DoorOpenedActivity(state, currentFloor));
    }

    public void closeDoor() {
        if (isDoorClosed) {
            logger.warn("Elevator's door is already closed - trying to close them once again.");
        }
        isDoorClosed = true;
        logElevatorActivity(new DoorClosedActivity(state, currentFloor, stepsSinceDoorOpened));
        stepsSinceDoorOpened = 0;
    }

    // elevator is said to be broken, when door is open for too long (above user-specified threshold)
    public boolean isBroken(int criticalStepsSinceElevatorDoorOpened) {
        return stepsSinceDoorOpened > criticalStepsSinceElevatorDoorOpened;
    }

    public void notifyBrokenElevator() {
        deliveryDestinationFloors.clear();
        pickupRequestsToHandle.clear();
        state = ElevatorState.IDLE;
        logElevatorActivity(new ElevatorBrokenActivity(state));
    }

    // updates elevator state based on actions that elevator still needs to perform
    private void updateState() {
        if (deliveryDestinationFloors.isEmpty() && pickupRequestsToHandle.isEmpty()) {
            state = ElevatorState.IDLE;
            return;
        }

        boolean higherDelivery = deliveryDestinationFloors.stream().anyMatch(x -> x > currentFloor);
        boolean higherPickup = pickupRequestsToHandle.stream().map(PickupRequest::getFloor).anyMatch(x -> x > currentFloor);
        boolean lowerDelivery = deliveryDestinationFloors.stream().anyMatch(x -> x < currentFloor);
        boolean lowerPickup = pickupRequestsToHandle.stream().map(PickupRequest::getFloor).anyMatch(x -> x < currentFloor);

        switch (state) {
            case UP -> {
                if (higherDelivery || higherPickup) {
                    // do nothing
                    return;
                } else if (lowerDelivery || lowerPickup) {
                    state = ElevatorState.DOWN;
                }
            }
            case DOWN -> {
                if (lowerDelivery || lowerPickup) {
                    // do nothing
                    return;
                } else if (higherDelivery || higherPickup) {
                    state = ElevatorState.UP;
                }
            }
            case IDLE -> {
                if (higherDelivery) {
                    state = ElevatorState.UP;
                } else if (lowerDelivery) {
                    state = ElevatorState.DOWN;
                } else if (higherPickup) {
                    state = ElevatorState.UP;
                } else if (lowerPickup) {
                    state = ElevatorState.DOWN;
                }
            }
        }
    }

    // performs actions required for current floor based on elevator's state
    private void handleFloor() {
        if (state == ElevatorState.IDLE) {
            return;
        }

        boolean isDeliveryFloor = deliveryDestinationFloors.contains(currentFloor);
        Optional<PickupRequest> pickupInSameDirection = pickupRequestsToHandle.stream()
                .filter(x -> x.getFloor() == currentFloor &&
                        ((state == ElevatorState.UP && x.getDirection() == RequestDirection.UP) ||
                        (state == ElevatorState.DOWN && x.getDirection() == RequestDirection.DOWN))).findFirst();
        Optional<PickupRequest> pickupInOppositeDirection = pickupRequestsToHandle.stream()
                .filter(x -> x.getFloor() == currentFloor &&
                        ((state == ElevatorState.UP && x.getDirection() == RequestDirection.DOWN) ||
                                (state == ElevatorState.DOWN && x.getDirection() == RequestDirection.UP))).findFirst();
        boolean openDoorForOppositePickup = false;
        switch (state) {
            case UP -> {
                boolean noDeliveriesAbove = deliveryDestinationFloors.stream().noneMatch(x -> x > currentFloor);
                boolean noPickupsAbove = pickupRequestsToHandle.stream().noneMatch(x -> x.getFloor() > currentFloor);
                if (noDeliveriesAbove && noPickupsAbove) {
                    openDoorForOppositePickup = true;
                }
            }
            case DOWN -> {
                boolean noDeliveriesBelow = deliveryDestinationFloors.stream().noneMatch(x -> x < currentFloor);
                boolean noPickupsBelow = pickupRequestsToHandle.stream().noneMatch(x -> x.getFloor() < currentFloor);
                if (noDeliveriesBelow && noPickupsBelow) {
                    openDoorForOppositePickup = true;
                }
            }
        }

        if (isDeliveryFloor || pickupInSameDirection.isPresent() || (openDoorForOppositePickup && pickupInOppositeDirection.isPresent())) {
            openDoor();
        }
        pickupInSameDirection.ifPresent(x -> x.setRequestDone(true));
        if (openDoorForOppositePickup) {
            pickupInOppositeDirection.ifPresent(x -> x.setRequestDone(true));
        }
    }

    // updates elevator's state, if door is closed performs movement, handles floor and updates state once again
    public void step() {
        updateState();
        if (isDoorClosed) {
            moveElevator();
            handleFloor();
        } else {
            stepsSinceDoorOpened++;
        }
        updateState();
    }

    // given all actions elevator needs to perform, estimates the time elevator needs to pick up given request
    public int estimateNumberOfStepsUntilPickup(PickupRequest request) {
        int estimatedStepsToMoveIfDoorOpen = Math.max((Math.max(estimatedEnteringSteps, estimatedLeavingSteps) - stepsSinceDoorOpened), 0);
        int distanceBetweenFloors = Math.abs(currentFloor - request.getFloor());

        if (state == ElevatorState.IDLE) {
            return distanceBetweenFloors;
        }

        // (1) elevator up, request up, currentFloor < requestFloor
        if (state == ElevatorState.UP && request.getDirection() == RequestDirection.UP && request.getFloor() > currentFloor) {
            long deliveriesBetween = deliveryDestinationFloors.stream()
                    .filter(x -> x > currentFloor && x < request.getFloor())
                    .count();
            long pickupsSameDirectionBetween = pickupRequestsToHandle.stream()
                    .filter(x -> x.getFloor() > currentFloor && x.getFloor() < request.getFloor() && x.getDirection() == RequestDirection.UP)
                    .count();
            int result = (int) (deliveriesBetween * estimatedLeavingSteps +
                    pickupsSameDirectionBetween * estimatedEnteringSteps + distanceBetweenFloors);
            if (!isDoorClosed) {
                return result + estimatedStepsToMoveIfDoorOpen;
            } else {
                return result;
            }
        }

        // (2) elevator down, request down, currentFloor > requestFloor
        if (state == ElevatorState.DOWN && request.getDirection() == RequestDirection.DOWN && request.getFloor() < currentFloor) {
            long deliveriesBetween = deliveryDestinationFloors.stream()
                    .filter(x -> x < currentFloor && x > request.getFloor())
                    .count();
            long pickupsSameDirectionBetween = pickupRequestsToHandle.stream()
                    .filter(x -> x.getFloor() < currentFloor && x.getFloor() > request.getFloor() && x.getDirection() == RequestDirection.DOWN)
                    .count();
            int result = (int) (deliveriesBetween * estimatedLeavingSteps +
                    pickupsSameDirectionBetween * estimatedEnteringSteps + distanceBetweenFloors);
            if (!isDoorClosed) {
                return result + estimatedStepsToMoveIfDoorOpen;
            } else {
                return result;
            }
        }

        // (3) elevator up, request down, currentFloor < requestFloor
        if (state == ElevatorState.UP && request.getDirection() == RequestDirection.DOWN && request.getFloor() > currentFloor) {
            boolean pessimisticPickup = pickupRequestsToHandle.stream()
                    .anyMatch(x -> x.getFloor() > currentFloor && x.getDirection() == RequestDirection.UP);
            Optional<Integer> maxDeliveryFloor = deliveryDestinationFloors.stream()
                    .filter(x -> x > currentFloor)
                    .max(Integer::compareTo);
            Optional<Integer> maxPickupFloor = pickupRequestsToHandle.stream()
                    .filter(x -> x.getFloor() > currentFloor && x.getDirection() == RequestDirection.DOWN)
                    .map(PickupRequest::getFloor)
                    .max(Integer::compareTo);

            int maxFloorToTravel;
            if (pessimisticPickup) {
                maxFloorToTravel = numberOfFloors;
            } else {
                if (maxDeliveryFloor.isPresent() && maxPickupFloor.isPresent()) {
                    maxFloorToTravel = Math.max(maxPickupFloor.get(), maxDeliveryFloor.get());
                } else if (maxDeliveryFloor.isPresent()) {
                    maxFloorToTravel = maxDeliveryFloor.get();
                } else if (maxPickupFloor.isPresent()) {
                    maxFloorToTravel = maxPickupFloor.get();
                } else {
                    maxFloorToTravel = request.getFloor();
                }
            }

            long deliveriesUntilMaxFloor = deliveryDestinationFloors.stream()
                    .filter(x -> x > currentFloor && x <= maxFloorToTravel)
                    .count();
            long pickupsUntilMaxFloorAndRequestFloor = pickupRequestsToHandle.stream()
                    .filter(x -> x.getFloor() > currentFloor)
                    .filter(x -> x.getDirection() == RequestDirection.UP ||
                            (x.getDirection() == RequestDirection.DOWN && x.getFloor() > request.getFloor()))
                    .count();

            if (pessimisticPickup) {
                deliveriesUntilMaxFloor++;
            }

            int result = (int) (deliveriesUntilMaxFloor * estimatedLeavingSteps +
                    pickupsUntilMaxFloorAndRequestFloor * estimatedEnteringSteps +
                    Math.abs(currentFloor - maxFloorToTravel) + Math.abs(request.getFloor() - maxFloorToTravel));
            if (!isDoorClosed) {
                return result + estimatedStepsToMoveIfDoorOpen;
            } else {
                return result;
            }
        }

        // (4) elevator down, request up, currentFloor > requestFloor
        if (state == ElevatorState.DOWN && request.getDirection() == RequestDirection.UP && currentFloor > request.getFloor()) {
            boolean pessimisticPickup = pickupRequestsToHandle.stream()
                    .anyMatch(x -> x.getFloor() < currentFloor && x.getDirection() == RequestDirection.DOWN);
            Optional<Integer> minDeliveryFloor = deliveryDestinationFloors.stream()
                    .filter(x -> x < currentFloor)
                    .min(Integer::compareTo);
            Optional<Integer> minPickupFloor = pickupRequestsToHandle.stream()
                    .filter(x -> x.getFloor() < currentFloor && x.getDirection() == RequestDirection.UP)
                    .map(PickupRequest::getFloor)
                    .min(Integer::compareTo);

            int minFloorToTravel;
            if (pessimisticPickup) {
                minFloorToTravel = 0;
            } else {
                if (minDeliveryFloor.isPresent() && minPickupFloor.isPresent()) {
                    minFloorToTravel = Math.min(minPickupFloor.get(), minDeliveryFloor.get());
                } else if (minDeliveryFloor.isPresent()) {
                    minFloorToTravel = minDeliveryFloor.get();
                } else if (minPickupFloor.isPresent()) {
                    minFloorToTravel = minPickupFloor.get();
                } else {
                    minFloorToTravel = request.getFloor();
                }
            }

            long deliveriesUntilMinFloor = deliveryDestinationFloors.stream()
                    .filter(x -> x < currentFloor && x >= minFloorToTravel)
                    .count();
            long pickupsUntilMinFloorAndRequestFloor = pickupRequestsToHandle.stream()
                    .filter(x -> x.getFloor() < currentFloor)
                    .filter(x -> x.getDirection() == RequestDirection.DOWN ||
                            (x.getDirection() == RequestDirection.UP && x.getFloor() < request.getFloor()))
                    .count();

            if (pessimisticPickup) {
                deliveriesUntilMinFloor++;
            }

            int result = (int) (deliveriesUntilMinFloor * estimatedLeavingSteps +
                    pickupsUntilMinFloorAndRequestFloor * estimatedEnteringSteps +
                    Math.abs(currentFloor - minFloorToTravel) + Math.abs(request.getFloor() - minFloorToTravel));
            if (!isDoorClosed) {
                return result + estimatedStepsToMoveIfDoorOpen;
            } else {
                return result;
            }
        }

        // (5) elevator up, request up, currentFloor > requestFloor
        if (state == ElevatorState.UP && request.getDirection() == RequestDirection.UP && currentFloor > request.getFloor()) {
            boolean pessimisticPickupUp = pickupRequestsToHandle.stream()
                    .anyMatch(x -> x.getFloor() > currentFloor && x.getDirection() == RequestDirection.UP);
            Optional<Integer> maxDeliveryFloor = deliveryDestinationFloors.stream()
                    .filter(x -> x > currentFloor)
                    .max(Integer::compareTo);
            Optional<Integer> maxPickupFloor = pickupRequestsToHandle.stream()
                    .filter(x -> x.getFloor() > currentFloor && x.getDirection() == RequestDirection.DOWN)
                    .map(PickupRequest::getFloor)
                    .max(Integer::compareTo);

            int maxFloorToTravel;
            if (pessimisticPickupUp) {
                maxFloorToTravel = numberOfFloors;
            } else {
                if (maxDeliveryFloor.isPresent() && maxPickupFloor.isPresent()) {
                    maxFloorToTravel = Math.max(maxPickupFloor.get(), maxDeliveryFloor.get());
                } else if (maxDeliveryFloor.isPresent()) {
                    maxFloorToTravel = maxDeliveryFloor.get();
                } else if (maxPickupFloor.isPresent()) {
                    maxFloorToTravel = maxPickupFloor.get();
                } else {
                    maxFloorToTravel = request.getFloor();
                }
            }

            boolean pessimisticPickupDown = pickupRequestsToHandle.stream()
                    .anyMatch(x -> x.getFloor() <= maxFloorToTravel && x.getDirection() == RequestDirection.DOWN);
            Optional<Integer> minDeliveryFloor = deliveryDestinationFloors.stream()
                    .filter(x -> x < maxFloorToTravel)
                    .min(Integer::compareTo);
            Optional<Integer> minPickupFloor = pickupRequestsToHandle.stream()
                    .filter(x -> x.getFloor() < maxFloorToTravel && x.getDirection() == RequestDirection.UP)
                    .map(PickupRequest::getFloor)
                    .min(Integer::compareTo);

            int minFloorToTravel;
            if (pessimisticPickupDown) {
                minFloorToTravel = 0;
            } else {
                if (minDeliveryFloor.isPresent() && minPickupFloor.isPresent()) {
                    minFloorToTravel = Math.min(minPickupFloor.get(), minDeliveryFloor.get());
                } else if (minDeliveryFloor.isPresent()) {
                    minFloorToTravel = minDeliveryFloor.get();
                } else if (minPickupFloor.isPresent()) {
                    minFloorToTravel = minPickupFloor.get();
                } else {
                    minFloorToTravel = request.getFloor();
                }
            }

            long deliveriesBetweenMinAndMax = deliveryDestinationFloors.stream()
                    .filter(x -> x >= minFloorToTravel && x <= maxFloorToTravel)
                    .count();
            long pickupsBetweenCurrentMaxMinAndRequest = pickupRequestsToHandle.stream()
                    .filter(x -> (x.getFloor() > currentFloor && x.getDirection() == RequestDirection.UP) ||
                            (x.getDirection() == RequestDirection.DOWN && (x.getFloor() >= minFloorToTravel && x.getFloor() <= maxFloorToTravel)) ||
                            (x.getDirection() == RequestDirection.UP && x.getFloor() < request.getFloor()))
                    .count();
            int result = (int) (deliveriesBetweenMinAndMax * estimatedLeavingSteps +
                    pickupsBetweenCurrentMaxMinAndRequest * estimatedEnteringSteps +
                    Math.abs(currentFloor - maxFloorToTravel) + Math.abs(maxFloorToTravel - minFloorToTravel) + Math.abs(request.getFloor() - minFloorToTravel));

            if (pessimisticPickupUp) {
                result += estimatedLeavingSteps;
            }
            if (pessimisticPickupDown) {
                result += estimatedLeavingSteps;
            }

            if (!isDoorClosed) {
                return result + estimatedStepsToMoveIfDoorOpen;
            } else {
                return result;
            }
        }

        // (6) elevator down, request down, currentFloor < requestFloor
        if (state == ElevatorState.DOWN && request.getDirection() == RequestDirection.DOWN && currentFloor < request.getFloor()) {
            boolean pessimisticPickupDown = pickupRequestsToHandle.stream()
                    .anyMatch(x -> x.getFloor() < currentFloor && x.getDirection() == RequestDirection.DOWN);
            Optional<Integer> minDeliveryFloor = deliveryDestinationFloors.stream()
                    .filter(x -> x < currentFloor)
                    .min(Integer::compareTo);
            Optional<Integer> minPickupFloor = pickupRequestsToHandle.stream()
                    .filter(x -> x.getFloor() < currentFloor && x.getDirection() == RequestDirection.UP)
                    .map(PickupRequest::getFloor)
                    .min(Integer::compareTo);

            int minFloorToTravel;
            if (pessimisticPickupDown) {
                minFloorToTravel = 0;
            } else {
                if (minDeliveryFloor.isPresent() && minPickupFloor.isPresent()) {
                    minFloorToTravel = Math.min(minPickupFloor.get(), minDeliveryFloor.get());
                } else if (minDeliveryFloor.isPresent()) {
                    minFloorToTravel = minDeliveryFloor.get();
                } else if (minPickupFloor.isPresent()) {
                    minFloorToTravel = minPickupFloor.get();
                } else {
                    minFloorToTravel = request.getFloor();
                }
            }

            boolean pessimisticPickupUp = pickupRequestsToHandle.stream()
                    .anyMatch(x -> x.getFloor() >= minFloorToTravel && x.getDirection() == RequestDirection.UP);
            Optional<Integer> maxDeliveryFloor = deliveryDestinationFloors.stream()
                    .filter(x -> x > minFloorToTravel)
                    .max(Integer::compareTo);
            Optional<Integer> maxPickupFloor = pickupRequestsToHandle.stream()
                    .filter(x -> x.getFloor() > minFloorToTravel && x.getDirection() == RequestDirection.DOWN)
                    .map(PickupRequest::getFloor)
                    .max(Integer::compareTo);

            int maxFloorToTravel;
            if (pessimisticPickupUp) {
                maxFloorToTravel = numberOfFloors;
            } else {
                if (maxDeliveryFloor.isPresent() && maxPickupFloor.isPresent()) {
                    maxFloorToTravel = Math.max(maxPickupFloor.get(), maxDeliveryFloor.get());
                } else if (maxDeliveryFloor.isPresent()) {
                    maxFloorToTravel = maxDeliveryFloor.get();
                } else if (maxPickupFloor.isPresent()) {
                    maxFloorToTravel = maxPickupFloor.get();
                } else {
                    maxFloorToTravel = request.getFloor();
                }
            }

            long deliveriesBetweenMinAndMax = deliveryDestinationFloors.stream()
                    .filter(x -> x >= minFloorToTravel && x <= maxFloorToTravel)
                    .count();
            long pickupsBetweenCurrentMinMaxAndRequest = pickupRequestsToHandle.stream()
                    .filter(x -> (x.getFloor() < currentFloor && x.getDirection() == RequestDirection.DOWN) ||
                            (x.getDirection() == RequestDirection.UP && (x.getFloor() >= minFloorToTravel && x.getFloor() <= maxFloorToTravel)) ||
                            (x.getDirection() == RequestDirection.DOWN && x.getFloor() > request.getFloor()))
                    .count();
            int result = (int) (deliveriesBetweenMinAndMax * estimatedLeavingSteps +
                    pickupsBetweenCurrentMinMaxAndRequest * estimatedEnteringSteps +
                    Math.abs(currentFloor - minFloorToTravel) + Math.abs(maxFloorToTravel - minFloorToTravel) + Math.abs(request.getFloor() - maxFloorToTravel));

            if (pessimisticPickupDown) {
                result += estimatedLeavingSteps;
            }
            if (pessimisticPickupUp) {
                result += estimatedLeavingSteps;
            }

            if (!isDoorClosed) {
                return result + estimatedStepsToMoveIfDoorOpen;
            } else {
                return result;
            }
        }

        // (7) elevator up, request down, currentFloor >= requestFloor
        if (state == ElevatorState.UP && request.getDirection() == RequestDirection.DOWN/* && currentFloor >= request.getFloor()*/) {
            boolean pessimisticPickup = pickupRequestsToHandle.stream()
                    .anyMatch(x -> x.getFloor() > currentFloor && x.getDirection() == RequestDirection.UP);
            Optional<Integer> maxDeliveryFloor = deliveryDestinationFloors.stream()
                    .filter(x -> x > currentFloor)
                    .max(Integer::compareTo);
            Optional<Integer> maxPickupFloor = pickupRequestsToHandle.stream()
                    .filter(x -> x.getFloor() > currentFloor && x.getDirection() == RequestDirection.DOWN)
                    .map(PickupRequest::getFloor)
                    .max(Integer::compareTo);

            int maxFloorToTravel;
            if (pessimisticPickup) {
                maxFloorToTravel = numberOfFloors;
            } else {
                if (maxDeliveryFloor.isPresent() && maxPickupFloor.isPresent()) {
                    maxFloorToTravel = Math.max(maxPickupFloor.get(), maxDeliveryFloor.get());
                } else if (maxDeliveryFloor.isPresent()) {
                    maxFloorToTravel = maxDeliveryFloor.get();
                } else if (maxPickupFloor.isPresent()) {
                    maxFloorToTravel = maxPickupFloor.get();
                } else {
                    maxFloorToTravel = request.getFloor();
                }
            }

            long deliveriesBetweenCurrentMaxAndRequestFloor = deliveryDestinationFloors.stream()
                    .filter(x -> x > request.getFloor() && x <= maxFloorToTravel)
                    .count();
            long pickupsUntilMaxFloorAndRequestFloor = pickupRequestsToHandle.stream()
                    .filter(x -> x.getFloor() > currentFloor ||
                            (x.getFloor() > request.getFloor() && x.getDirection() == RequestDirection.DOWN))
                    .count();

            int result = (int) (deliveriesBetweenCurrentMaxAndRequestFloor * estimatedLeavingSteps +
                    pickupsUntilMaxFloorAndRequestFloor * estimatedEnteringSteps +
                    Math.abs(currentFloor - maxFloorToTravel) + Math.abs(maxFloorToTravel - request.getFloor()));

            if (pessimisticPickup) {
                result += estimatedLeavingSteps;
            }

            if (!isDoorClosed) {
                return result + estimatedStepsToMoveIfDoorOpen;
            } else {
                return result;
            }
        }

        // (8) elevator down, request up, currentFloor <= requestFloor
        if (state == ElevatorState.DOWN && request.getDirection() == RequestDirection.UP/* && currentFloor <= request.getFloor()*/) {
            boolean pessimisticPickup = pickupRequestsToHandle.stream()
                    .anyMatch(x -> x.getFloor() < currentFloor && x.getDirection() == RequestDirection.DOWN);
            Optional<Integer> minDeliveryFloor = deliveryDestinationFloors.stream()
                    .filter(x -> x < currentFloor)
                    .min(Integer::compareTo);
            Optional<Integer> minPickupFloor = pickupRequestsToHandle.stream()
                    .filter(x -> x.getFloor() < currentFloor && x.getDirection() == RequestDirection.UP)
                    .map(PickupRequest::getFloor)
                    .min(Integer::compareTo);

            int minFloorToTravel;
            if (pessimisticPickup) {
                minFloorToTravel = 0;
            } else {
                if (minDeliveryFloor.isPresent() && minPickupFloor.isPresent()) {
                    minFloorToTravel = Math.min(minPickupFloor.get(), minDeliveryFloor.get());
                } else if (minDeliveryFloor.isPresent()) {
                    minFloorToTravel = minDeliveryFloor.get();
                } else if (minPickupFloor.isPresent()) {
                    minFloorToTravel = minPickupFloor.get();
                } else {
                    minFloorToTravel = request.getFloor();
                }
            }

            long deliveriesBetweenCurrentMinAndRequestFloor = deliveryDestinationFloors.stream()
                    .filter(x -> x < request.getFloor() && x >= minFloorToTravel)
                    .count();
            long pickupsUntilMinFloorAndRequestFloor = pickupRequestsToHandle.stream()
                    .filter(x -> x.getFloor() < currentFloor ||
                            (x.getFloor() < request.getFloor() && x.getDirection() == RequestDirection.UP))
                    .count();

            int result = (int) (deliveriesBetweenCurrentMinAndRequestFloor * estimatedLeavingSteps +
                    pickupsUntilMinFloorAndRequestFloor * estimatedEnteringSteps +
                    Math.abs(currentFloor - minFloorToTravel) + Math.abs(minFloorToTravel - request.getFloor()));

            if (pessimisticPickup) {
                result += estimatedLeavingSteps;
            }

            if (!isDoorClosed) {
                return result + estimatedStepsToMoveIfDoorOpen;
            } else {
                return result;
            }
        }

        if (currentFloor == request.getFloor() &&
                ((state == ElevatorState.UP && request.getDirection() == RequestDirection.UP) ||
                (state == ElevatorState.DOWN && request.getDirection() == RequestDirection.DOWN))) {
            return 0;
        }

        logger.error("Failed pickup time estimation.");
        return Integer.MAX_VALUE;
    }
}
