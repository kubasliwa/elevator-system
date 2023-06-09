package com.system;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ElevatorTest {

    @Nested
    class ElevatorUpRequestUpCurrentFloorLowerThanRequestFloorClass {

        @Test
        void estimateNumberOfStepsUntilPickup_noRequestsNoDeliveries_simpleFloorDifference() {
            // given
            Elevator elevator = new Elevator(0, 2, 3, 2, 10);
            PickupRequest request = new PickupRequest(5, RequestDirection.UP, 2, List.of(8));
            elevator.setState(ElevatorState.IDLE);

            // when & then
            assertEquals(3, elevator.estimateNumberOfStepsUntilPickup(request));
        }

        @Test
        void estimateNumberOfStepsUntilPickup_oneRequestUpBetween_enteringTimeConsidered() {
            // given
            Elevator elevator = new Elevator(0, 2, 3, 2, 10);
            PickupRequest request = new PickupRequest(5, RequestDirection.UP, 2, List.of(8));
            elevator.addPickupRequest(request);
            elevator.setState(ElevatorState.UP);
            PickupRequest newRequest = new PickupRequest(7, RequestDirection.UP, 5, List.of(9));

            // when & then
            assertEquals(8, elevator.estimateNumberOfStepsUntilPickup(newRequest));
        }

        @Test
        void estimateNumberOfStepsUntilPickup_oneDeliveryBetween_leavingTimeConsidered() {
            // given
            Elevator elevator = new Elevator(0, 2, 3, 2, 10);
            elevator.addDeliveryDestinationFloorList(List.of(5));
            elevator.setState(ElevatorState.UP);
            PickupRequest newRequest = new PickupRequest(7, RequestDirection.UP, 5, List.of(9));

            // when & then
            assertEquals(7, elevator.estimateNumberOfStepsUntilPickup(newRequest));
        }

        @Test
        void estimateNumberOfStepsUntilPickup_oneDeliveryOneRequestUpBetween_leavingAndEnteringTimeConsidered() {
            // given
            Elevator elevator = new Elevator(0, 2, 3, 2, 10);
            elevator.addDeliveryDestinationFloorList(List.of(5));
            elevator.setState(ElevatorState.UP);
            PickupRequest request = new PickupRequest(4, RequestDirection.UP, 5, List.of(9));
            elevator.addPickupRequest(request);
            PickupRequest newRequest = new PickupRequest(7, RequestDirection.UP, 5, List.of(9));

            // when & then
            assertEquals(10, elevator.estimateNumberOfStepsUntilPickup(newRequest));
        }

        @Test
        void estimateNumberOfStepsUntilPickup_oneDeliveryOneRequestDownBetween_onlyLeavingTimeConsidered() {
            // given
            Elevator elevator = new Elevator(0, 2, 3, 2, 10);
            elevator.addDeliveryDestinationFloorList(List.of(5));
            elevator.setState(ElevatorState.UP);
            elevator.addPickupRequest(new PickupRequest(4, RequestDirection.DOWN, 5, List.of(0)));
            PickupRequest newRequest = new PickupRequest(7, RequestDirection.UP, 5, List.of(9));

            // when & then
            assertEquals(7, elevator.estimateNumberOfStepsUntilPickup(newRequest));
        }

        @Test
        void estimateNumberOfStepsUntilPickup_doorCurrentlyOpen_oneDeliveryBetween_closingDoorTimeConsidered() {
            // given
            Elevator elevator = new Elevator(0, 2, 3, 2, 10);
            elevator.addDeliveryDestinationFloorList(List.of(6));
            elevator.setDoorClosed(false);
            elevator.setState(ElevatorState.UP);
            elevator.setStepsSinceDoorOpened(1);
            PickupRequest newRequest = new PickupRequest(7, RequestDirection.UP, 5, List.of(9));

            // when & then
            assertEquals(9, elevator.estimateNumberOfStepsUntilPickup(newRequest));
        }
    }

    @Nested
    class ElevatorDownRequestDownCurrentFloorHigherThanRequestFloorClass {

        @Test
        void estimateNumberOfStepsUntilPickup_noRequestsNoDeliveries_simpleFloorDifference() {
            // given
            Elevator elevator = new Elevator(0, 8, 3, 2, 10);
            PickupRequest request = new PickupRequest(5, RequestDirection.DOWN, 2, List.of(1));
            elevator.setState(ElevatorState.IDLE);

            // when & then
            assertEquals(3, elevator.estimateNumberOfStepsUntilPickup(request));
        }

        @Test
        void estimateNumberOfStepsUntilPickup_oneRequestDownBetween_enteringTimeConsidered() {
            // given
            Elevator elevator = new Elevator(0, 8, 3, 2, 10);
            PickupRequest request = new PickupRequest(7, RequestDirection.DOWN, 2, List.of(0));
            elevator.addPickupRequest(request);
            elevator.setState(ElevatorState.DOWN);
            PickupRequest newRequest = new PickupRequest(5, RequestDirection.DOWN, 5, List.of(0));

            // when & then
            assertEquals(6, elevator.estimateNumberOfStepsUntilPickup(newRequest));
        }

        @Test
        void estimateNumberOfStepsUntilPickup_oneDeliveryBetween_leavingTimeConsidered() {
            // given
            Elevator elevator = new Elevator(0, 8, 3, 2, 10);
            elevator.addDeliveryDestinationFloorList(List.of(5));
            elevator.setState(ElevatorState.DOWN);
            PickupRequest newRequest = new PickupRequest(4, RequestDirection.DOWN, 5, List.of(0));

            // when & then
            assertEquals(6, elevator.estimateNumberOfStepsUntilPickup(newRequest));
        }

        @Test
        void estimateNumberOfStepsUntilPickup_oneDeliveryOneRequestDownBetween_leavingAndEnteringTimeConsidered() {
            // given
            Elevator elevator = new Elevator(0, 8, 3, 2, 10);
            elevator.addDeliveryDestinationFloorList(List.of(5));
            elevator.setState(ElevatorState.DOWN);
            PickupRequest request = new PickupRequest(4, RequestDirection.DOWN, 5, List.of(0));
            elevator.addPickupRequest(request);
            PickupRequest newRequest = new PickupRequest(2, RequestDirection.DOWN, 5, List.of(0));

            // when & then
            assertEquals(11, elevator.estimateNumberOfStepsUntilPickup(newRequest));
        }

        @Test
        void estimateNumberOfStepsUntilPickup_oneDeliveryOneRequestUpBetween_onlyLeavingTimeConsidered() {
            // given
            Elevator elevator = new Elevator(0, 8, 3, 2, 10);
            elevator.addDeliveryDestinationFloorList(List.of(5));
            elevator.setState(ElevatorState.DOWN);
            elevator.addPickupRequest(new PickupRequest(4, RequestDirection.UP, 5, List.of(10)));
            PickupRequest newRequest = new PickupRequest(3, RequestDirection.DOWN, 5, List.of(0));

            // when & then
            assertEquals(7, elevator.estimateNumberOfStepsUntilPickup(newRequest));
        }

        @Test
        void estimateNumberOfStepsUntilPickup_doorCurrentlyOpen_oneDeliveryBetween_closingDoorTimeConsidered() {
            // given
            Elevator elevator = new Elevator(0, 8, 3, 2, 10);
            elevator.addDeliveryDestinationFloorList(List.of(6));
            elevator.setDoorClosed(false);
            elevator.setState(ElevatorState.DOWN);
            elevator.setStepsSinceDoorOpened(1);
            PickupRequest newRequest = new PickupRequest(4, RequestDirection.DOWN, 5, List.of(0));

            // when & then
            assertEquals(8, elevator.estimateNumberOfStepsUntilPickup(newRequest));
        }
    }

    @Nested
    class ElevatorUpRequestDownCurrentFloorLowerThanRequestFloorClass {

        @Test
        void estimateNumberOfStepsUntilPickup_oneDeliveryBetween_leavingTimeConsidered() {
            // given
            Elevator elevator = new Elevator(0, 2, 3, 2, 10);
            elevator.addDeliveryDestinationFloorList(List.of(5));
            elevator.setState(ElevatorState.UP);
            PickupRequest newRequest = new PickupRequest(8, RequestDirection.DOWN, 5, List.of(0));

            // when && then
            assertEquals(8, elevator.estimateNumberOfStepsUntilPickup(newRequest));
        }

        @Test
        void estimateNumberOfStepsUntilPickup_oneDeliveryAbove_deliveryFirstThenReturnsForRequest() {
            // given
            Elevator elevator = new Elevator(0, 2, 3, 2, 10);
            elevator.setState(ElevatorState.UP);
            elevator.addDeliveryDestinationFloorList(List.of(9));
            PickupRequest newRequest = new PickupRequest(5, RequestDirection.DOWN, 1, List.of(0));

            // when & then
            assertEquals(13, elevator.estimateNumberOfStepsUntilPickup(newRequest));
        }

        @Test
        void estimateNumberOfStepsUntilPickup_oneDeliveryAbove_oneRequestDownOnWayBack_deliveryFirstThenRequestDownThenReturnsForRequest() {
            // given
            Elevator elevator = new Elevator(0, 2, 3, 2, 10);
            elevator.setState(ElevatorState.UP);
            elevator.addDeliveryDestinationFloorList(List.of(9));
            elevator.addPickupRequest(new PickupRequest(7, RequestDirection.DOWN, 2, List.of(0)));
            PickupRequest newRequest = new PickupRequest(5, RequestDirection.DOWN, 1, List.of(0));

            // when & then
            assertEquals(16, elevator.estimateNumberOfStepsUntilPickup(newRequest));
        }

        @Test
        void estimateNumberOfStepsUntilPickup_oneRequestDownBetween_middleRequestEnteringTimeNotConsidered() {
            // given
            Elevator elevator = new Elevator(0, 2, 3, 2, 10);
            elevator.setState(ElevatorState.UP);
            elevator.addPickupRequest(new PickupRequest(5, RequestDirection.DOWN, 5, List.of(0)));
            PickupRequest newRequest = new PickupRequest(7, RequestDirection.DOWN, 4, List.of(0));

            // when & then
            assertEquals(5, elevator.estimateNumberOfStepsUntilPickup(newRequest));
        }

        @Test
        void estimateNumberOfStepsUntilPickup_onePessimisticRequestUpBetween_noRequestsOnWayBack_mostPessimisticDeliveryConsidered() {
            // given
            Elevator elevator = new Elevator(0, 2, 3, 2, 10);
            elevator.setState(ElevatorState.UP);
            elevator.addPickupRequest(new PickupRequest(3, RequestDirection.UP, 4, List.of(5)));
            PickupRequest newRequest = new PickupRequest(7, RequestDirection.DOWN, 2, List.of(0));

            // when & then
            assertEquals(16, elevator.estimateNumberOfStepsUntilPickup(newRequest));
        }

        @Test
        void estimateNumberOfStepsUntilPickup_twoPessimisticRequestUpBetween_noRequestsOnWayBack_mostPessimisticDeliveryConsidered() {
            // given
            Elevator elevator = new Elevator(0, 2, 3, 2, 10);
            elevator.setState(ElevatorState.UP);
            elevator.addPickupRequest(new PickupRequest(3, RequestDirection.UP, 4, List.of(5)));
            elevator.addPickupRequest(new PickupRequest(4, RequestDirection.UP, 4, List.of(5)));
            PickupRequest newRequest = new PickupRequest(7, RequestDirection.DOWN, 2, List.of(0));

            // when & then
            assertEquals(19, elevator.estimateNumberOfStepsUntilPickup(newRequest));
        }

        @Test
        void estimateNumberOfStepsUntilPickup_onePessimisticRequestUpBetween_oneRequestDownOnWayBack_mostPessimisticDeliveryConsidered() {
            // given
            Elevator elevator = new Elevator(0, 2, 3, 2, 10);
            elevator.setState(ElevatorState.UP);
            elevator.addPickupRequest(new PickupRequest(3, RequestDirection.UP, 4, List.of(5)));
            elevator.addPickupRequest(new PickupRequest(8, RequestDirection.DOWN, 4, List.of(0)));
            PickupRequest newRequest = new PickupRequest(7, RequestDirection.DOWN, 2, List.of(0));

            // when & then
            assertEquals(19, elevator.estimateNumberOfStepsUntilPickup(newRequest));
        }
    }

    @Nested
    class ElevatorDownRequestUpCurrentFloorHigherThanRequestFloorClass {

        @Test
        void estimateNumberOfStepsUntilPickup_oneDeliveryBetween_leavingTimeConsidered() {
            // given
            Elevator elevator = new Elevator(0, 8, 3, 2, 10);
            elevator.addDeliveryDestinationFloorList(List.of(5));
            elevator.setState(ElevatorState.DOWN);
            PickupRequest newRequest = new PickupRequest(2, RequestDirection.UP, 5, List.of(10));

            // when && then
            assertEquals(8, elevator.estimateNumberOfStepsUntilPickup(newRequest));
        }

        @Test
        void estimateNumberOfStepsUntilPickup_oneDeliveryBelow_deliveryFirstThenReturnsForRequest() {
            // given
            Elevator elevator = new Elevator(0, 8, 3, 2, 10);
            elevator.setState(ElevatorState.DOWN);
            elevator.addDeliveryDestinationFloorList(List.of(0));
            PickupRequest newRequest = new PickupRequest(5, RequestDirection.UP, 1, List.of(10));

            // when & then
            assertEquals(15, elevator.estimateNumberOfStepsUntilPickup(newRequest));
        }

        @Test
        void estimateNumberOfStepsUntilPickup_oneDeliveryBelow_oneRequestUpOnWayBack_deliveryFirstThenRequestUpThenReturnsForRequest() {
            // given
            Elevator elevator = new Elevator(0, 8, 3, 2, 10);
            elevator.setState(ElevatorState.DOWN);
            elevator.addDeliveryDestinationFloorList(List.of(1));
            elevator.addPickupRequest(new PickupRequest(5, RequestDirection.UP, 2, List.of(10)));
            PickupRequest newRequest = new PickupRequest(7, RequestDirection.UP, 1, List.of(10));

            // when & then
            assertEquals(18, elevator.estimateNumberOfStepsUntilPickup(newRequest));
        }

        @Test
        void estimateNumberOfStepsUntilPickup_oneRequestUpBetween_middleRequestEnteringTimeNotConsidered() {
            // given
            Elevator elevator = new Elevator(0, 8, 3, 2, 10);
            elevator.setState(ElevatorState.DOWN);
            elevator.addPickupRequest(new PickupRequest(5, RequestDirection.UP, 5, List.of(10)));
            PickupRequest newRequest = new PickupRequest(3, RequestDirection.UP, 4, List.of(9));

            // when & then
            assertEquals(5, elevator.estimateNumberOfStepsUntilPickup(newRequest));
        }

        @Test
        void estimateNumberOfStepsUntilPickup_onePessimisticRequestDownBetween_noRequestsOnWayBack_mostPessimisticDeliveryConsidered() {
            // given
            Elevator elevator = new Elevator(0, 8, 3, 2, 10);
            elevator.setState(ElevatorState.DOWN);
            elevator.addPickupRequest(new PickupRequest(7, RequestDirection.DOWN, 4, List.of(5)));
            PickupRequest newRequest = new PickupRequest(3, RequestDirection.UP, 2, List.of(10));

            // when & then
            assertEquals(16, elevator.estimateNumberOfStepsUntilPickup(newRequest));
        }

        @Test
        void estimateNumberOfStepsUntilPickup_twoPessimisticRequestDownBetween_noRequestsOnWayBack_mostPessimisticDeliveryConsidered() {
            // given
            Elevator elevator = new Elevator(0, 8, 3, 2, 10);
            elevator.setState(ElevatorState.DOWN);
            elevator.addPickupRequest(new PickupRequest(6, RequestDirection.DOWN, 4, List.of(2)));
            elevator.addPickupRequest(new PickupRequest(4, RequestDirection.DOWN, 4, List.of(1)));
            PickupRequest newRequest = new PickupRequest(7, RequestDirection.UP, 2, List.of(10));

            // when & then
            assertEquals(23, elevator.estimateNumberOfStepsUntilPickup(newRequest));
        }

        @Test
        void estimateNumberOfStepsUntilPickup_onePessimisticRequestDownBetween_oneRequestUpOnWayBack_mostPessimisticDeliveryConsidered() {
            // given
            Elevator elevator = new Elevator(0, 8, 3, 2, 10);
            elevator.setState(ElevatorState.DOWN);
            elevator.addPickupRequest(new PickupRequest(3, RequestDirection.DOWN, 4, List.of(1)));
            elevator.addPickupRequest(new PickupRequest(4, RequestDirection.UP, 4, List.of(10)));
            PickupRequest newRequest = new PickupRequest(7, RequestDirection.UP, 2, List.of(10));

            // when & then
            assertEquals(23, elevator.estimateNumberOfStepsUntilPickup(newRequest));
        }
    }

    @Nested
    class ElevatorUpRequestUpCurrentFloorHigherThanRequestFloorClass {

        @Test
        void estimateNumberOfStepsUntilPickup_oneDeliveryAbove_deliveryFirstThenReturnsForRequest() {
            // given
            Elevator elevator = new Elevator(0, 5, 3, 2, 10);
            elevator.setState(ElevatorState.UP);
            elevator.addDeliveryDestinationFloorList(List.of(7));
            PickupRequest newRequest = new PickupRequest(3, RequestDirection.UP, 3, List.of(10));

            // when & then
            assertEquals(8, elevator.estimateNumberOfStepsUntilPickup(newRequest));
        }

        @Test
        void estimateNumberOfStepsUntilPickup_onePessimisticRequestUpAbove_mostPessimisticScenario() {
            // given
            Elevator elevator = new Elevator(0, 5, 3, 2, 10);
            elevator.setState(ElevatorState.UP);
            elevator.addPickupRequest(new PickupRequest(7, RequestDirection.UP, 3, List.of(9)));
            PickupRequest newRequest = new PickupRequest(3, RequestDirection.UP, 2, List.of(7));

            // when & then
            assertEquals(17, elevator.estimateNumberOfStepsUntilPickup(newRequest));
        }

        @Test
        void estimateNumberOfStepsUntilPickup_twoPessimisticRequestsUpAbove_mostPessimisticScenario() {
            // given
            Elevator elevator = new Elevator(0, 5, 3, 2, 10);
            elevator.setState(ElevatorState.UP);
            elevator.addPickupRequest(new PickupRequest(7, RequestDirection.UP, 3, List.of(9)));
            elevator.addPickupRequest(new PickupRequest(8, RequestDirection.UP, 2, List.of(10)));
            PickupRequest newRequest = new PickupRequest(3, RequestDirection.UP, 2, List.of(7));

            // when & then
            assertEquals(20, elevator.estimateNumberOfStepsUntilPickup(newRequest));
        }

        @Test
        void estimateNumberOfStepsUntilPickup_onePessimisticRequestUpAndOneDown_mostPessimisticGoesUpDownAndThenUp() {
            // given
            Elevator elevator = new Elevator(0, 5, 3, 2, 10);
            elevator.setState(ElevatorState.UP);
            elevator.addPickupRequest(new PickupRequest(7, RequestDirection.UP, 3, List.of(9)));
            elevator.addPickupRequest(new PickupRequest(8, RequestDirection.DOWN, 2, List.of(1)));
            PickupRequest newRequest = new PickupRequest(3, RequestDirection.UP, 2, List.of(7));

            // when & then
            assertEquals(28, elevator.estimateNumberOfStepsUntilPickup(newRequest));
        }

        @Test
        void estimateNumberOfStepsUntilPickup_onePessimisticRequestUpAndOneDownAndDeliveryBetween_allTimesConsidered() {
            // given
            Elevator elevator = new Elevator(0, 5, 3, 2, 10);
            elevator.setState(ElevatorState.UP);
            elevator.addPickupRequest(new PickupRequest(7, RequestDirection.UP, 3, List.of(9)));
            elevator.addPickupRequest(new PickupRequest(8, RequestDirection.DOWN, 2, List.of(1)));
            elevator.addDeliveryDestinationFloorList(List.of(4));
            PickupRequest newRequest = new PickupRequest(3, RequestDirection.UP, 2, List.of(7));

            // when & then
            assertEquals(30, elevator.estimateNumberOfStepsUntilPickup(newRequest));
        }

        @Test
        void estimateNumberOfStepsUntilPickup_onePessimisticRequestUpAndOneDownAndDeliveryBetweenAndOneRequestUpBefore_allTimesConsidered() {
            // given
            Elevator elevator = new Elevator(0, 5, 3, 2, 10);
            elevator.setState(ElevatorState.UP);
            elevator.addPickupRequest(new PickupRequest(7, RequestDirection.UP, 3, List.of(9)));
            elevator.addPickupRequest(new PickupRequest(8, RequestDirection.DOWN, 2, List.of(1)));
            elevator.addPickupRequest(new PickupRequest(1, RequestDirection.UP, 2, List.of(10)));
            elevator.addDeliveryDestinationFloorList(List.of(4));
            PickupRequest newRequest = new PickupRequest(3, RequestDirection.UP, 2, List.of(7));

            // when & then
            assertEquals(33, elevator.estimateNumberOfStepsUntilPickup(newRequest));
        }
    }

    @Nested
    class ElevatorDownRequestDownCurrentFloorLowerThanRequestFloorClass {

        @Test
        void estimateNumberOfStepsUntilPickup_oneDeliveryBelow_deliveryFirstThenReturnsForRequest() {
            // given
            Elevator elevator = new Elevator(0, 5, 3, 2, 10);
            elevator.setState(ElevatorState.DOWN);
            elevator.addDeliveryDestinationFloorList(List.of(3));
            PickupRequest newRequest = new PickupRequest(7, RequestDirection.DOWN, 3, List.of(10));

            // when & then
            assertEquals(8, elevator.estimateNumberOfStepsUntilPickup(newRequest));
        }

        @Test
        void estimateNumberOfStepsUntilPickup_onePessimisticRequestDownBelow_mostPessimisticScenario() {
            // given
            Elevator elevator = new Elevator(0, 5, 3, 2, 10);
            elevator.setState(ElevatorState.DOWN);
            elevator.addPickupRequest(new PickupRequest(3, RequestDirection.DOWN, 3, List.of(1)));
            PickupRequest newRequest = new PickupRequest(7, RequestDirection.DOWN, 2, List.of(0));

            // when & then
            assertEquals(17, elevator.estimateNumberOfStepsUntilPickup(newRequest));
        }

        @Test
        void estimateNumberOfStepsUntilPickup_twoPessimisticRequestsDownBelow_mostPessimisticScenario() {
            // given
            Elevator elevator = new Elevator(0, 5, 3, 2, 10);
            elevator.setState(ElevatorState.DOWN);
            elevator.addPickupRequest(new PickupRequest(3, RequestDirection.DOWN, 3, List.of(1)));
            elevator.addPickupRequest(new PickupRequest(2, RequestDirection.DOWN, 2, List.of(0)));
            PickupRequest newRequest = new PickupRequest(7, RequestDirection.DOWN, 2, List.of(3));

            // when & then
            assertEquals(20, elevator.estimateNumberOfStepsUntilPickup(newRequest));
        }

        @Test
        void estimateNumberOfStepsUntilPickup_onePessimisticRequestDownAndOneUp_mostPessimisticGoesDownUpAndThenDown() {
            // given
            Elevator elevator = new Elevator(0, 5, 3, 2, 10);
            elevator.setState(ElevatorState.DOWN);
            elevator.addPickupRequest(new PickupRequest(3, RequestDirection.DOWN, 3, List.of(1)));
            elevator.addPickupRequest(new PickupRequest(8, RequestDirection.UP, 2, List.of(9)));
            PickupRequest newRequest = new PickupRequest(7, RequestDirection.DOWN, 2, List.of(2));

            // when & then
            assertEquals(28, elevator.estimateNumberOfStepsUntilPickup(newRequest));
        }

        @Test
        void estimateNumberOfStepsUntilPickup_onePessimisticRequestDownAndOneUpAndDeliveryBetween_allTimesConsidered() {
            // given
            Elevator elevator = new Elevator(0, 5, 3, 2, 10);
            elevator.setState(ElevatorState.DOWN);
            elevator.addPickupRequest(new PickupRequest(3, RequestDirection.DOWN, 3, List.of(1)));
            elevator.addPickupRequest(new PickupRequest(8, RequestDirection.UP, 2, List.of(9)));
            elevator.addDeliveryDestinationFloorList(List.of(6));
            PickupRequest newRequest = new PickupRequest(7, RequestDirection.DOWN, 2, List.of(2));

            // when & then
            assertEquals(30, elevator.estimateNumberOfStepsUntilPickup(newRequest));
        }

        @Test
        void estimateNumberOfStepsUntilPickup_onePessimisticRequestDownAndOneUpAndDeliveryBetweenAndOneRequestDownBefore_allTimesConsidered() {
            // given
            Elevator elevator = new Elevator(0, 5, 3, 2, 10);
            elevator.setState(ElevatorState.DOWN);
            elevator.addPickupRequest(new PickupRequest(3, RequestDirection.DOWN, 3, List.of(1)));
            elevator.addPickupRequest(new PickupRequest(2, RequestDirection.UP, 2, List.of(9)));
            elevator.addPickupRequest(new PickupRequest(9, RequestDirection.DOWN, 2, List.of(0)));
            elevator.addDeliveryDestinationFloorList(List.of(6));
            PickupRequest newRequest = new PickupRequest(7, RequestDirection.DOWN, 2, List.of(2));

            // when & then
            assertEquals(33, elevator.estimateNumberOfStepsUntilPickup(newRequest));
        }
    }

    @Nested
    class elevatorUpRequestDownCurrentFloorHigherThanRequestFloorClass {

        @Test
        void estimateNumberOfStepsUntilPickup_deliveryAbove_firstDeliveryThenReturnsForRequest() {
            // given
            Elevator elevator = new Elevator(0, 5, 3, 2, 10);
            elevator.setState(ElevatorState.UP);
            elevator.addDeliveryDestinationFloorList(List.of(8));
            PickupRequest newRequest = new PickupRequest(3, RequestDirection.DOWN, 3, List.of(0));

            // when & then
            assertEquals(10, elevator.estimateNumberOfStepsUntilPickup(newRequest));
        }

        @Test
        void estimateNumberOfStepsUntilPickup_pessimisticRequestUpAbove_firstPessimisticRequestThenReturnsForRequest() {
            // given
            Elevator elevator = new Elevator(0, 5, 3, 2, 10);
            elevator.setState(ElevatorState.UP);
            elevator.addPickupRequest(new PickupRequest(7, RequestDirection.UP, 2, List.of(9)));
            PickupRequest newRequest = new PickupRequest(3, RequestDirection.DOWN, 2, List.of(1));

            // when & then
            assertEquals(17, elevator.estimateNumberOfStepsUntilPickup(newRequest));
        }

        @Test
        void estimateNumberOfStepsUntilPickup_requestDownAbove_firstRequestThenReturnsForNewRequest() {
            // given
            Elevator elevator = new Elevator(0, 5, 3, 2, 10);
            elevator.setState(ElevatorState.UP);
            elevator.addPickupRequest(new PickupRequest(8, RequestDirection.DOWN, 2, List.of(0)));
            PickupRequest newRequest = new PickupRequest(3, RequestDirection.DOWN, 2, List.of(1));

            // when & then
            assertEquals(11, elevator.estimateNumberOfStepsUntilPickup(newRequest));
        }

        @Test
        void estimateNumberOfStepsUntilPickup_requestUpAboveAndDeliveryBelow_pessimisticRequestConsideredAndDeliveryNotConsidered() {
            // given
            Elevator elevator = new Elevator(0, 5, 3, 2, 10);
            elevator.setState(ElevatorState.UP);
            elevator.addPickupRequest(new PickupRequest(7, RequestDirection.UP, 2, List.of(9)));
            elevator.addDeliveryDestinationFloorList(List.of(2));
            PickupRequest newRequest = new PickupRequest(4, RequestDirection.DOWN, 2, List.of(0));

            // when & then
            assertEquals(16, elevator.estimateNumberOfStepsUntilPickup(newRequest));
        }

        @Test
        void estimateNumberOfStepsUntilPickup_pessimisticRequestUpAboveAndRequestDownOnWayBack_bothRequestsConsidered() {
            // given
            Elevator elevator = new Elevator(0, 5, 3, 2, 10);
            elevator.setState(ElevatorState.UP);
            elevator.addPickupRequest(new PickupRequest(7, RequestDirection.UP, 2, List.of(8)));
            elevator.addPickupRequest(new PickupRequest(9, RequestDirection.DOWN, 2, List.of(1)));
            PickupRequest newRequest = new PickupRequest(3, RequestDirection.DOWN, 2, List.of(1));

            // when & then
            assertEquals(20, elevator.estimateNumberOfStepsUntilPickup(newRequest));
        }
    }

    @Nested
    class ElevatorDownRequestUpCurrentFloorLowerThanRequestFloorClass {

        @Test
        void estimateNumberOfStepsUntilPickup_deliveryBelow_firstDeliveryThenReturnsForRequest() {
            // given
            Elevator elevator = new Elevator(0, 5, 3, 2, 10);
            elevator.setState(ElevatorState.DOWN);
            elevator.addDeliveryDestinationFloorList(List.of(2));
            PickupRequest newRequest = new PickupRequest(7, RequestDirection.UP, 3, List.of(10));

            // when & then
            assertEquals(10, elevator.estimateNumberOfStepsUntilPickup(newRequest));
        }

        @Test
        void estimateNumberOfStepsUntilPickup_pessimisticRequestDownBelow_firstPessimisticRequestThenReturnsForRequest() {
            // given
            Elevator elevator = new Elevator(0, 5, 3, 2, 10);
            elevator.setState(ElevatorState.DOWN);
            elevator.addPickupRequest(new PickupRequest(3, RequestDirection.DOWN, 2, List.of(1)));
            PickupRequest newRequest = new PickupRequest(7, RequestDirection.UP, 2, List.of(9));

            // when & then
            assertEquals(17, elevator.estimateNumberOfStepsUntilPickup(newRequest));
        }

        @Test
        void estimateNumberOfStepsUntilPickup_requestUpBelow_firstRequestThenReturnsForNewRequest() {
            // given
            Elevator elevator = new Elevator(0, 5, 3, 2, 10);
            elevator.setState(ElevatorState.DOWN);
            elevator.addPickupRequest(new PickupRequest(2, RequestDirection.UP, 2, List.of(10)));
            PickupRequest newRequest = new PickupRequest(7, RequestDirection.UP, 2, List.of(9));

            // when & then
            assertEquals(11, elevator.estimateNumberOfStepsUntilPickup(newRequest));
        }

        @Test
        void estimateNumberOfStepsUntilPickup_requestDownBelowAndDeliveryAbove_pessimisticRequestConsideredAndDeliveryNotConsidered() {
            // given
            Elevator elevator = new Elevator(0, 5, 3, 2, 10);
            elevator.setState(ElevatorState.DOWN);
            elevator.addPickupRequest(new PickupRequest(3, RequestDirection.DOWN, 2, List.of(1)));
            elevator.addDeliveryDestinationFloorList(List.of(8));
            PickupRequest newRequest = new PickupRequest(6, RequestDirection.UP, 2, List.of(10));

            // when & then
            assertEquals(16, elevator.estimateNumberOfStepsUntilPickup(newRequest));
        }

        @Test
        void estimateNumberOfStepsUntilPickup_pessimisticRequestDownBelowAndRequestUpOnWayBack_bothRequestsConsidered() {
            // given
            Elevator elevator = new Elevator(0, 5, 3, 2, 10);
            elevator.setState(ElevatorState.DOWN);
            elevator.addPickupRequest(new PickupRequest(3, RequestDirection.DOWN, 2, List.of(2)));
            elevator.addPickupRequest(new PickupRequest(1, RequestDirection.UP, 2, List.of(9)));
            PickupRequest newRequest = new PickupRequest(7, RequestDirection.UP, 2, List.of(9));

            // when & then
            assertEquals(20, elevator.estimateNumberOfStepsUntilPickup(newRequest));
        }
    }

    @Nested
    class CurrentFloorEqualsRequestFloorClass {

        @Test
        void estimateNumberOfStepsUntilPickup_elevatorUpRequestUp_opensDoor() {
            // given
            Elevator elevator = new Elevator(0, 5, 3, 2, 10);
            elevator.setState(ElevatorState.UP);
            elevator.addDeliveryDestinationFloorList(List.of(7));
            PickupRequest newRequest = new PickupRequest(5, RequestDirection.UP, 2, List.of(7));

            // when & then
            assertEquals(0, elevator.estimateNumberOfStepsUntilPickup(newRequest));
        }

        @Test
        void estimateNumberOfStepsUntilPickup_elevatorDownRequestDown_opensDoor() {
            // given
            Elevator elevator = new Elevator(0, 5, 3, 2, 10);
            elevator.setState(ElevatorState.DOWN);
            elevator.addDeliveryDestinationFloorList(List.of(3));
            PickupRequest newRequest = new PickupRequest(5, RequestDirection.DOWN, 2, List.of(3));

            // when & then
            assertEquals(0, elevator.estimateNumberOfStepsUntilPickup(newRequest));
        }

        @Test
        void estimateNumberOfStepsUntilPickup_elevatorUpRequestDown_goesUpAndReturns() {
            // given
            Elevator elevator = new Elevator(0, 5, 3, 2, 10);
            elevator.setState(ElevatorState.UP);
            elevator.addDeliveryDestinationFloorList(List.of(7));
            PickupRequest newRequest = new PickupRequest(5, RequestDirection.DOWN, 2, List.of(0));

            // when & then
            assertEquals(6, elevator.estimateNumberOfStepsUntilPickup(newRequest));
        }

        @Test
        void estimateNumberOfStepsUntilPickup_elevatorDownRequestUp_goesDownAndReturns() {
            // given
            Elevator elevator = new Elevator(0, 5, 3, 2, 10);
            elevator.setState(ElevatorState.DOWN);
            elevator.addDeliveryDestinationFloorList(List.of(3));
            PickupRequest newRequest = new PickupRequest(5, RequestDirection.UP, 2, List.of(10));

            // when & then
            assertEquals(6, elevator.estimateNumberOfStepsUntilPickup(newRequest));
        }

        @Test
        void estimateNumberOfStepsUntilPickup_elevatorUpRequestDownPessimistic_pessimisticScenario() {
            // given
            Elevator elevator = new Elevator(0, 5, 3, 2, 10);
            elevator.setState(ElevatorState.UP);
            elevator.addPickupRequest(new PickupRequest(7, RequestDirection.UP, 2, List.of(9)));
            PickupRequest newRequest = new PickupRequest(5, RequestDirection.DOWN, 2, List.of(0));

            // when & then
            assertEquals(15, elevator.estimateNumberOfStepsUntilPickup(newRequest));
        }

        @Test
        void estimateNumberOfStepsUntilPickup_elevatorDownRequestUpPessimistic_pessimisticScenario() {
            // given
            Elevator elevator = new Elevator(0, 5, 3, 2, 10);
            elevator.setState(ElevatorState.DOWN);
            elevator.addPickupRequest(new PickupRequest(3, RequestDirection.DOWN, 2, List.of(1)));
            PickupRequest newRequest = new PickupRequest(5, RequestDirection.UP, 2, List.of(10));

            // when & then
            assertEquals(15, elevator.estimateNumberOfStepsUntilPickup(newRequest));
        }
    }
}
