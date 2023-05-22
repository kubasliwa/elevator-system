package com.system.activities;

import com.system.ElevatorState;
import com.system.ElevatorSystem;


public class ClearPickupRequestsActivity extends ElevatorActivity {

    public ClearPickupRequestsActivity(ElevatorState state) {
        super(ElevatorSystem.getCurrentStep(), state);
    }

}
