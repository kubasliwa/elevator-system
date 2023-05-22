package com.system.activities;

import com.system.ElevatorState;
import com.system.ElevatorSystem;
import lombok.ToString;

@ToString
public class MoveActivity extends ElevatorActivity {
    int previousFloor;
    int newFloor;

    public MoveActivity(ElevatorState state, int previousFloor, int newFloor) {
        super(ElevatorSystem.getCurrentStep(), state);
        this.previousFloor = previousFloor;
        this.newFloor = newFloor;
    }
}
