package com.system.activities;

import com.system.ElevatorState;
import com.system.ElevatorSystem;
import lombok.ToString;

@ToString
public class ClearPickupRequestsActivity extends ElevatorActivity {

    public ClearPickupRequestsActivity(ElevatorState state) {
        super(ElevatorSystem.getCurrentStep(), state);
    }

}
