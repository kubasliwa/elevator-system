package com.system;

import lombok.*;

import java.util.List;

@RequiredArgsConstructor
@EqualsAndHashCode
@Getter
@ToString
class PickupRequest {
    @Setter
    private boolean requestDone = false;
    private final int floor;
    private final RequestDirection direction;
    private final int numberOfEnteringSteps;
    private final List<Integer> destinationFloors;
}


