package com.system.activities;

import com.system.ElevatorState;
import com.system.ElevatorSystem;
import lombok.ToString;

@ToString
public class AddPickupRequestActivity extends ElevatorActivity {
    int pickupFloor;

    public AddPickupRequestActivity(ElevatorState state, int floor) {
        super(ElevatorSystem.getCurrentStep(), state);
        this.pickupFloor = floor;
    }

}
