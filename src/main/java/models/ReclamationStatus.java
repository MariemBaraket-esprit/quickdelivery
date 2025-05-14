package models;

import javafx.scene.paint.Color;

public enum ReclamationStatus {
    NEW(Color.BLUE),
    RECEIVED(Color.DARKBLUE),
    IN_PROGRESS(Color.ORANGE),
    PENDING_RESPONSE(Color.YELLOW),
    RESOLVED(Color.GREEN),
    REJECTED(Color.RED),
    CLOSED(Color.GRAY);

    private final Color color;

    ReclamationStatus(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }
}
