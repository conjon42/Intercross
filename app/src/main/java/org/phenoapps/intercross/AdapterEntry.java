package org.phenoapps.intercross;

import android.widget.Adapter;

public class AdapterEntry {
    public String crossId;
    public String timestamp;

    AdapterEntry(String cross, String timestamp) {
        this.crossId = cross;
        this.timestamp = timestamp;
    }
}
