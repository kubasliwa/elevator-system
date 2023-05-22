package com.system.activities;

import com.system.ElevatorState;
import com.system.ElevatorSystem;
import lombok.ToString;

@ToString
public class DoorClosedActivity extends ElevatorActivity {
    int currentFloor;
    int stepsWaited;

    public DoorClosedActivity(ElevatorState state, int currentFloor, int stepsWaited) {
        super(ElevatorSystem.getCurrentStep(), state);
        this.currentFloor = currentFloor;
        this.stepsWaited = stepsWaited;
    }
}
