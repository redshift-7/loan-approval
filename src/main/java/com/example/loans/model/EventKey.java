package com.example.loans.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventKey implements Comparable<EventKey> {

    ZonedDateTime timestamp;
    UUID uuid;

    public EventKey(ZonedDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public int compareTo(EventKey o) {
        int compareTimestamps = Long.compare(this.timestamp.toInstant().toEpochMilli(), o.timestamp.toInstant().toEpochMilli());
        if (compareTimestamps != 0) {
            return compareTimestamps;
        } else if (this.uuid != null && o.uuid != null){
            return this.uuid.compareTo(o.uuid);
        } else {
            return 0;
        }
    }
}
