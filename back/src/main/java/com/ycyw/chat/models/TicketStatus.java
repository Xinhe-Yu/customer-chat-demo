package com.ycyw.chat.models;

public enum TicketStatus {
    OPEN("open"),
    IN_PROGRESS("in_progress"),
    RESOLVED("resolved"),
    CLOSED("closed");

    private final String value;

    TicketStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static TicketStatus fromValue(String value) {
        for (TicketStatus status : TicketStatus.values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown status: " + value);
    }
}