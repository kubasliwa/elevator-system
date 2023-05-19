package com.system;

import lombok.AllArgsConstructor;
import lombok.ToString;

// TODO: dodać zapisywanie logów w Elevator
abstract class ElevatorActivity {
    int elevatorId;
    ElevatorState state;
}

@AllArgsConstructor
@ToString
class DoorOpenedActivity extends ElevatorActivity {
    int currentFloor;
}

@AllArgsConstructor
@ToString
class DoorClosedActivity extends ElevatorActivity {
    int currentFloor;
    int stepsWaited;
}

@AllArgsConstructor
@ToString
class MoveUpActivity extends ElevatorActivity {
    int previousFloor;
    int newFloor;
}

@AllArgsConstructor
@ToString
class MoveDownActivity extends ElevatorActivity {
    int previousFloor;
    int newFloor;
}