package com.system.activities;

import com.system.ElevatorState;
import com.system.ElevatorSystem;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class DoorOpenedActivity extends ElevatorActivity {
    int currentFloor;

    public DoorOpenedActivity(ElevatorState state, int currentFloor) {
        super(ElevatorSystem.getCurrentStep(), state);
        this.currentFloor = currentFloor;
    }
}
