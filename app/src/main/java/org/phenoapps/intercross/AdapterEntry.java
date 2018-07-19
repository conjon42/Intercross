package org.phenoapps.intercross;

import android.widget.Adapter;

public class AdapterEntry {
    public String crossId;
    public String timestamp;
    public String crossName;
    AdapterEntry(String cross, String timestamp, String crossName) {
        this.crossId = cross;
        this.timestamp = timestamp;
        this.crossName = crossName;
    }
}
