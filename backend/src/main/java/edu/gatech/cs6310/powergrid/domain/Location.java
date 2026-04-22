package edu.gatech.cs6310.powergrid.domain;

public record Location(int x, int y) {

    public int manhattanDistanceTo(Location other) {
        return Math.abs(this.x - other.x) + Math.abs(this.y - other.y);
    }
}
