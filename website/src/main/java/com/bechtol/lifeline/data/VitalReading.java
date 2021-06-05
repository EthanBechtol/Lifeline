package com.bechtol.lifeline.data;

public final class VitalReading {
  private final long id;
  private final long timestamp;
  private final int hr;
  private final int o2;
  private final String orientation;

  public VitalReading(long id, long timestamp, int hr, int o2, String orientation) {
    this.id = id;
    this.timestamp = timestamp;
    this.hr = hr;
    this.o2 = o2;
    this.orientation = orientation;
  }
}