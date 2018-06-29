package org.phenoapps.intercross;

import android.widget.Adapter;

public class AdapterEntry {
    public String crossId;
    public String timestamp;
    public String count;

    AdapterEntry(String cross, String timestamp, String count) {
        this.crossId = cross;
        this.timestamp = timestamp;
        this.count = count;
    }
}
