package edu.gatech.cs6310.powergrid.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class LocationTest {

    @Test
    void manhattanDistanceMatchesDefinition() {
        Location a = new Location(0, 0);
        Location b = new Location(3, 4);
        assertThat(a.manhattanDistanceTo(b)).isEqualTo(7);
        assertThat(b.manhattanDistanceTo(a)).isEqualTo(7);
    }

    @Test
    void distanceIsZeroForSamePoint() {
        Location a = new Location(-5, 7);
        assertThat(a.manhattanDistanceTo(a)).isZero();
    }

    @Test
    void negativeCoordinatesAreHandled() {
        Location a = new Location(-2, -3);
        Location b = new Location(4, 5);
        assertThat(a.manhattanDistanceTo(b)).isEqualTo(14);
    }
}
