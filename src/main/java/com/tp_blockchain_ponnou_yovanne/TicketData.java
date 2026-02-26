package com.tp_blockchain_ponnou_yovanne;

public class TicketData {
    public final String eventId;
    public final String artist;
    public final String status;
    public final String owner;

    public TicketData(String eventId, String artist, String status, String owner) {
        this.eventId = eventId;
        this.artist = artist;
        this.status = status;
        this.owner = owner;
    }

    public String canonicalValue() {
        return eventId + "|" + artist + "|" + status + "|" + owner;
    }

    @Override
    public String toString() {
        return "eventId=" + eventId + ", artist=" + artist + ", status=" + status + ", owner=" + owner;
    }
}
