package com.system.activities;

import com.system.ElevatorState;
import com.system.ElevatorSystem;

public class ElevatorBrokenActivity extends ElevatorActivity {

    public ElevatorBrokenActivity(ElevatorState state) {
        super(ElevatorSystem.getCurrentStep(), state);
    }

}
