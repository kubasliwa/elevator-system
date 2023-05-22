package com.system.activities;

import com.system.ElevatorState;
import lombok.AllArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@ToString
public abstract class ElevatorActivity {
    int step;
    ElevatorState state;
}