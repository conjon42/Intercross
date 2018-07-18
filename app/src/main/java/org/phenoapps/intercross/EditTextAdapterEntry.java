package org.phenoapps.intercross;

import android.widget.EditText;
import android.widget.TextView;

public class EditTextAdapterEntry {
    public EditText editText;
    public String headerTextValue;
    EditTextAdapterEntry(EditText et, String tv) {
        this.editText = et;
        this.headerTextValue = tv;
    }
}
