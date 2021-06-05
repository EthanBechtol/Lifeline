package com.bechtol.lifeline.data;

public final class Event {
  private final long id;
  private final long timestamp;
  private final String type;
  private final String description;

  public Event(long id, long timestamp, String type, String description) {
    this.id = id;
    this.timestamp = timestamp;
    this.type = type;
    this.description = description;
  }
}