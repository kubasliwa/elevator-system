package com.system;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@AllArgsConstructor
class ElevatorDoorCloser {
    @NonNull
    private final Elevator elevator;
    @Getter
    private int stepsUntilDoorClosed;

    public void step() {
        stepsUntilDoorClosed--;
        if (stepsUntilDoorClosed == 0) {
            elevator.closeDoor();
        }
    }
}
