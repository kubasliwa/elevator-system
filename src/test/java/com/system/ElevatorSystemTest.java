package com.system;

import com.system.activities.DoorClosedActivity;
import com.system.activities.DoorOpenedActivity;
import com.system.activities.ElevatorBrokenActivity;
import com.system.activities.MoveActivity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ElevatorSystemTest {

    @AfterEach
    void resetElevatorSystemStaticField() {
        ElevatorSystem.setCurrentStepToZeroForTesting();
    }

    @Test
    void testSinglePickupRequest_elevatorsOnTheSameFloor_picksTheOneWithLowerId() {
        // given
        ElevatorSystem system = new ElevatorSystem(2, 10, 9, 3, 3, 2, new int[] {0, 0});
        PickupRequest request = new PickupRequest(5, RequestDirection.UP, 2, List.of(10));

        // when
        system.pickup(request);
        for (int i = 0; i < 15; i++) {
            system.step();
        }
        boolean elevatorZeroOpenDoorInStep5 = system.status()[0].getActivityLogsMap().get(5).stream().anyMatch(x -> x instanceof DoorOpenedActivity && ((DoorOpenedActivity) x).getCurrentFloor() == 5);
        boolean elevatorZeroCloseDoorInStep7 = system.status()[0].getActivityLogsMap().get(7).stream().anyMatch(x -> x instanceof DoorClosedActivity && ((DoorClosedActivity) x).getCurrentFloor() == 5 && ((DoorClosedActivity) x).getStepsWaited() == 2);
        boolean elevatorZeroOpenDoorInStep12 = system.status()[0].getActivityLogsMap().get(12).stream().anyMatch(x -> x instanceof DoorOpenedActivity && ((DoorOpenedActivity) x).getCurrentFloor() == 10);
        boolean elevatorZeroCloseDoorInStep14 = system.status()[0].getActivityLogsMap().get(14).stream().anyMatch(x -> x instanceof DoorClosedActivity && ((DoorClosedActivity) x).getCurrentFloor() == 10 && ((DoorClosedActivity) x).getStepsWaited() == 2);
        boolean elevatorOneOnlyInStateIdle = system.status()[1].getActivityLogsMap().entrySet().stream().flatMap(x -> x.getValue().stream()).allMatch(x -> x.getState() == ElevatorState.IDLE);

        // then
        assertTrue(elevatorOneOnlyInStateIdle && elevatorZeroOpenDoorInStep5 &&
                elevatorZeroCloseDoorInStep7 && elevatorZeroOpenDoorInStep12 && elevatorZeroCloseDoorInStep14);
    }

    @Test
    void testSinglePickupRequest_oneElevatorCloserThanTheOther_picksCloserElevator() {
        // given
        ElevatorSystem system = new ElevatorSystem(2, 10, 9, 3, 3, 2, new int[] {0, 1});
        PickupRequest request = new PickupRequest(5, RequestDirection.DOWN, 3, List.of(2));

        // when
        system.pickup(request);
        for (int i = 0; i < 15; i++) {
            system.step();
        }
        boolean elevatorZeroOnlyInStateIdle = system.status()[0].getActivityLogsMap().entrySet().stream().flatMap(x -> x.getValue().stream()).allMatch(x -> x.getState() == ElevatorState.IDLE);
        boolean elevatorOneOpenDoorInStep4 = system.status()[1].getActivityLogsMap().get(4).stream().anyMatch(x -> x instanceof DoorOpenedActivity && ((DoorOpenedActivity) x).getCurrentFloor() == 5);
        boolean elevatorOneCloseDoorInStep7 = system.status()[1].getActivityLogsMap().get(7).stream().anyMatch(x -> x instanceof DoorClosedActivity && ((DoorClosedActivity) x).getCurrentFloor() == 5 && ((DoorClosedActivity) x).getStepsWaited() == 3);
        boolean elevatorOneOpenDoorInStep10 = system.status()[1].getActivityLogsMap().get(10).stream().anyMatch(x -> x instanceof DoorOpenedActivity && ((DoorOpenedActivity) x).getCurrentFloor() == 2);
        boolean elevatorOneCloseDoorInStep12 = system.status()[1].getActivityLogsMap().get(12).stream().anyMatch(x -> x instanceof DoorClosedActivity && ((DoorClosedActivity) x).getCurrentFloor() == 2 && ((DoorClosedActivity) x).getStepsWaited() == 2);

        // then
        assertTrue(elevatorZeroOnlyInStateIdle && elevatorOneOpenDoorInStep4 && elevatorOneCloseDoorInStep7 &&
                elevatorOneOpenDoorInStep10 && elevatorOneCloseDoorInStep12);
    }

    @Test
    void testMultiplePickupRequestsInSingleStepForSingleElevator_oneRequestUpOneRequestDown_performsActionsInCorrectOrder() {
        // given
        ElevatorSystem system = new ElevatorSystem(1, 10, 9, 3, 3, 2, new int[] {2});
        PickupRequest requestOne = new PickupRequest(3, RequestDirection.UP, 2, List.of(6));
        PickupRequest requestTwo = new PickupRequest(7, RequestDirection.DOWN, 4, List.of(0, 2));

        // when
        system.pickup(requestOne);
        system.pickup(requestTwo);
        for (int i = 0; i < 25; i++) {
            system.step();
        }
        boolean elevatorOpenDoorInStep1 = system.status()[0].getActivityLogsMap().get(1).stream().anyMatch(x -> x instanceof DoorOpenedActivity && ((DoorOpenedActivity) x).getCurrentFloor() == 3);
        boolean elevatorCloseDoorInStep3 = system.status()[0].getActivityLogsMap().get(3).stream().anyMatch(x -> x instanceof DoorClosedActivity && ((DoorClosedActivity) x).getCurrentFloor() == 3 && ((DoorClosedActivity) x).getStepsWaited() == 2);
        boolean elevatorOpenDoorInStep6 = system.status()[0].getActivityLogsMap().get(6).stream().anyMatch(x -> x instanceof DoorOpenedActivity && ((DoorOpenedActivity) x).getCurrentFloor() == 6);
        boolean elevatorCloseDoorInStep8 = system.status()[0].getActivityLogsMap().get(8).stream().anyMatch(x -> x instanceof DoorClosedActivity && ((DoorClosedActivity) x).getCurrentFloor() == 6 && ((DoorClosedActivity) x).getStepsWaited() == 2);
        boolean elevatorOpenDoorInStep9 = system.status()[0].getActivityLogsMap().get(9).stream().anyMatch(x -> x instanceof DoorOpenedActivity && ((DoorOpenedActivity) x).getCurrentFloor() == 7);
        boolean elevatorCloseDoorInStep13 = system.status()[0].getActivityLogsMap().get(13).stream().anyMatch(x -> x instanceof DoorClosedActivity && ((DoorClosedActivity) x).getCurrentFloor() == 7 && ((DoorClosedActivity) x).getStepsWaited() == 4);
        boolean elevatorOpenDoorInStep18 = system.status()[0].getActivityLogsMap().get(18).stream().anyMatch(x -> x instanceof DoorOpenedActivity && ((DoorOpenedActivity) x).getCurrentFloor() == 2);
        boolean elevatorCloseDoorInStep20 = system.status()[0].getActivityLogsMap().get(20).stream().anyMatch(x -> x instanceof DoorClosedActivity && ((DoorClosedActivity) x).getCurrentFloor() == 2 && ((DoorClosedActivity) x).getStepsWaited() == 2);
        boolean elevatorOpenDoorInStep22 = system.status()[0].getActivityLogsMap().get(22).stream().anyMatch(x -> x instanceof DoorOpenedActivity && ((DoorOpenedActivity) x).getCurrentFloor() == 0);
        boolean elevatorCloseDoorInStep24 = system.status()[0].getActivityLogsMap().get(24).stream().anyMatch(x -> x instanceof DoorClosedActivity && ((DoorClosedActivity) x).getCurrentFloor() == 0 && ((DoorClosedActivity) x).getStepsWaited() == 2);

        // then
        assertTrue(elevatorOpenDoorInStep1 && elevatorCloseDoorInStep3 && elevatorOpenDoorInStep6 && elevatorCloseDoorInStep8 &&
                elevatorOpenDoorInStep9 && elevatorCloseDoorInStep13 && elevatorOpenDoorInStep18 && elevatorCloseDoorInStep20 &&
                elevatorOpenDoorInStep22 && elevatorCloseDoorInStep24);
    }

    @Test
    void testMultiplePickupRequestsInMultipleStepsForSingleElevator_oneRequestDownAndOnePoppingRequestUpOnTheSameFloor_stopsForThisRequest() {
        // given
        ElevatorSystem system = new ElevatorSystem(1, 10, 9, 3, 3, 2, new int[] {1});
        PickupRequest requestOne = new PickupRequest(8, RequestDirection.DOWN, 4, List.of(0));
        PickupRequest requestTwo = new PickupRequest(4, RequestDirection.UP, 1, List.of(10));

        // when
        system.pickup(requestOne);
        for (int i = 0; i < 30; i++) {
            system.step();
            if (i == 1) {
                system.pickup(requestTwo);
            }
        }
        boolean elevatorOpenDoorInStep3 = system.status()[0].getActivityLogsMap().get(3).stream().anyMatch(x -> x instanceof DoorOpenedActivity && ((DoorOpenedActivity) x).getCurrentFloor() == 4);
        boolean elevatorCloseDoorInStep4 = system.status()[0].getActivityLogsMap().get(4).stream().anyMatch(x -> x instanceof DoorClosedActivity && ((DoorClosedActivity) x).getCurrentFloor() == 4 && ((DoorClosedActivity) x).getStepsWaited() == 1);
        boolean elevatorOpenDoorInStep10 = system.status()[0].getActivityLogsMap().get(10).stream().anyMatch(x -> x instanceof DoorOpenedActivity && ((DoorOpenedActivity) x).getCurrentFloor() == 10);
        boolean elevatorCloseDoorInStep11 = system.status()[0].getActivityLogsMap().get(12).stream().anyMatch(x -> x instanceof DoorClosedActivity && ((DoorClosedActivity) x).getCurrentFloor() == 10 && ((DoorClosedActivity) x).getStepsWaited() == 2);
        boolean elevatorOpenDoorInStep14 = system.status()[0].getActivityLogsMap().get(14).stream().anyMatch(x -> x instanceof DoorOpenedActivity && ((DoorOpenedActivity) x).getCurrentFloor() == 8);
        boolean elevatorCloseDoorInStep18 = system.status()[0].getActivityLogsMap().get(18).stream().anyMatch(x -> x instanceof DoorClosedActivity && ((DoorClosedActivity) x).getCurrentFloor() == 8 && ((DoorClosedActivity) x).getStepsWaited() == 4);
        boolean elevatorOpenDoorInStep26 = system.status()[0].getActivityLogsMap().get(26).stream().anyMatch(x -> x instanceof DoorOpenedActivity && ((DoorOpenedActivity) x).getCurrentFloor() == 0);
        boolean elevatorCloseDoorInStep28 = system.status()[0].getActivityLogsMap().get(28).stream().anyMatch(x -> x instanceof DoorClosedActivity && ((DoorClosedActivity) x).getCurrentFloor() == 0 && ((DoorClosedActivity) x).getStepsWaited() == 2);

        // then
        assertTrue(elevatorOpenDoorInStep3 && elevatorCloseDoorInStep4 && elevatorOpenDoorInStep10 && elevatorCloseDoorInStep11 &&
                elevatorOpenDoorInStep14 && elevatorCloseDoorInStep18 && elevatorOpenDoorInStep26 && elevatorCloseDoorInStep28);
    }

    @Test
    void testMultiplePickupRequestsInSingleStepForMultipleElevators_oneRequestUpAndOneRequestDown_eachElevatorHandlesOneRequest() {
        // given
        ElevatorSystem system = new ElevatorSystem(2, 10, 9, 3, 3, 2, new int[] {1, 8});
        PickupRequest requestOne = new PickupRequest(3, RequestDirection.UP, 4, List.of(10));
        PickupRequest requestTwo = new PickupRequest(4, RequestDirection.DOWN, 1, List.of(0));

        // when
        system.pickup(requestOne);
        system.pickup(requestTwo);
        for (int i = 0; i < 16; i++) {
            system.step();
        }
        boolean elevatorZeroOpenDoorInStep2 = system.status()[0].getActivityLogsMap().get(2).stream().anyMatch(x -> x instanceof DoorOpenedActivity && ((DoorOpenedActivity) x).getCurrentFloor() == 3);
        boolean elevatorZeroCloseDoorInStep6 = system.status()[0].getActivityLogsMap().get(6).stream().anyMatch(x -> x instanceof DoorClosedActivity && ((DoorClosedActivity) x).getCurrentFloor() == 3 && ((DoorClosedActivity) x).getStepsWaited() == 4);
        boolean elevatorZeroOpenDoorInStep13 = system.status()[0].getActivityLogsMap().get(13).stream().anyMatch(x -> x instanceof DoorOpenedActivity && ((DoorOpenedActivity) x).getCurrentFloor() == 10);
        boolean elevatorZeroCloseDoorInStep15 = system.status()[0].getActivityLogsMap().get(15).stream().anyMatch(x -> x instanceof DoorClosedActivity && ((DoorClosedActivity) x).getCurrentFloor() == 10 && ((DoorClosedActivity) x).getStepsWaited() == 2);

        boolean elevatorOneOpenDoorInStep5 = system.status()[1].getActivityLogsMap().get(5).stream().anyMatch(x -> x instanceof DoorOpenedActivity && ((DoorOpenedActivity) x).getCurrentFloor() == 4);
        boolean elevatorOneCloseDoorInStep6 = system.status()[1].getActivityLogsMap().get(6).stream().anyMatch(x -> x instanceof DoorClosedActivity && ((DoorClosedActivity) x).getCurrentFloor() == 4 && ((DoorClosedActivity) x).getStepsWaited() == 1);
        boolean elevatorOneOpenDoorInStep10 = system.status()[1].getActivityLogsMap().get(10).stream().anyMatch(x -> x instanceof DoorOpenedActivity && ((DoorOpenedActivity) x).getCurrentFloor() == 0);
        boolean elevatorOneCloseDoorInStep12 = system.status()[1].getActivityLogsMap().get(12).stream().anyMatch(x -> x instanceof DoorClosedActivity && ((DoorClosedActivity) x).getCurrentFloor() == 0 && ((DoorClosedActivity) x).getStepsWaited() == 2);


        // then
        assertTrue(elevatorZeroOpenDoorInStep2 && elevatorZeroCloseDoorInStep6 && elevatorZeroOpenDoorInStep13 && elevatorZeroCloseDoorInStep15 &&
                elevatorOneOpenDoorInStep5 && elevatorOneCloseDoorInStep6 && elevatorOneOpenDoorInStep10 && elevatorOneCloseDoorInStep12);
    }

    @Test
    void testBrokenElevatorShouldNotBeGivenRequests_closerElevatorBrokenFurtherElevatorIdle_afterCriticalTimeFurtherElevatorComesForRequest() {
        // given
        ElevatorSystem system = new ElevatorSystem(2, 10, 9, 3, 3, 2, new int[] {1, 5});
        PickupRequest requestOne = new PickupRequest(6, RequestDirection.UP, 1000, List.of(10));
        PickupRequest requestTwo = new PickupRequest(7, RequestDirection.UP, 1, List.of(9));

        // when
        system.pickup(requestOne);
        for (int i = 0; i < 100; i++) {
            system.step();
            if (i == 15) {
                system.pickup(requestTwo);
            }
        }
        boolean elevatorZeroHandlesRequestTwo = system.status()[0].getActivityLogsMap().entrySet().stream().flatMap(x -> x.getValue().stream()).anyMatch(x -> x instanceof DoorOpenedActivity && ((DoorOpenedActivity) x).getCurrentFloor() == 9);
        boolean elevatorOneIsReportedToBeBroken = system.status()[1].getActivityLogsMap().entrySet().stream().flatMap(x -> x.getValue().stream()).anyMatch(x -> x instanceof ElevatorBrokenActivity);

        // then
        assertTrue(elevatorZeroHandlesRequestTwo && elevatorOneIsReportedToBeBroken);
    }

    @Test
    void testBrokenElevatorShouldBeGivenRequestsAfterBeingFixed_oneElevatorExtremelyFarSecondReportedBrokenButFixed_freshlyFixedElevatorHandlesRequest() {
        // given
        ElevatorSystem system = new ElevatorSystem(2, 100, 9, 3, 3, 2, new int[] {0, 90});
        PickupRequest requestOne = new PickupRequest(91, RequestDirection.UP, 20, List.of(98));
        PickupRequest requestTwo = new PickupRequest(92, RequestDirection.UP, 1, List.of(99));

        // when
        system.pickup(requestOne);
        for (int i = 0; i < 40; i++) {
            system.step();
            if (i == 15) {
                system.pickup(requestTwo);
            }
        }
        boolean elevatorZeroMovedButStopped = system.status()[0].getActivityLogsMap().entrySet().stream().flatMap(x -> x.getValue().stream()).anyMatch(x -> x instanceof MoveActivity);
        boolean elevatorOneHandledBothRequests = system.status()[1].getActivityLogsMap().entrySet().stream().flatMap(x -> x.getValue().stream()).anyMatch(x -> x instanceof DoorClosedActivity && ((DoorClosedActivity) x).getCurrentFloor() == 99);

        // then
        assertTrue(elevatorZeroMovedButStopped && elevatorOneHandledBothRequests);
    }

    @Test
    void testElevatorShouldBeGivenRequestsWhenPessimisticRequestWasReevaluated() {
        // given
        ElevatorSystem system = new ElevatorSystem(2, 100, 9, 3, 3, 2, new int[] {0, 14});
        PickupRequest requestOne = new PickupRequest(16, RequestDirection.UP, 2, List.of(18));
        PickupRequest requestTwo = new PickupRequest(17, RequestDirection.DOWN, 1, List.of(0));

        // when
        system.pickup(requestOne);
        system.pickup(requestTwo);
        for (int i = 0; i < 30; i++) {
            system.step();
        }
        boolean elevatorZeroMovedButStopped = system.status()[0].getActivityLogsMap().entrySet().stream().flatMap(x -> x.getValue().stream()).anyMatch(x -> x instanceof MoveActivity);
        boolean elevatorOneHandledBothRequests = system.status()[1].getActivityLogsMap().entrySet().stream().flatMap(x -> x.getValue().stream()).anyMatch(x -> x instanceof DoorClosedActivity && ((DoorClosedActivity) x).getCurrentFloor() == 0);

        // then
        assertTrue(elevatorZeroMovedButStopped && elevatorOneHandledBothRequests);
    }
}
