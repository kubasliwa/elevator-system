package com.system.activities;

import com.system.ElevatorState;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@ToString
@Getter
public abstract class ElevatorActivity {
    int step;
    ElevatorState state;
}