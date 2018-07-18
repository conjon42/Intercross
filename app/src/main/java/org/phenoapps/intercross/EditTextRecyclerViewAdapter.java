package org.phenoapps.intercross;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EditTextRecyclerViewAdapter extends RecyclerView.Adapter<EditTextRecyclerViewAdapter.ViewHolder> {

    private List<EditTextAdapterEntry> mData;
    private LayoutInflater mInflater;
    private Context mContext;
    private SQLiteDatabase db;

    private static List<EditTextAdapterEntry> mEditTextValues = new ArrayList<>();

    EditTextRecyclerViewAdapter(Context context, List<EditTextAdapterEntry> data) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.db = new IdEntryDbHelper(mContext).getWritableDatabase();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = mInflater.inflate(R.layout.value_input_row, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        //EditTextAdapterEntry entry = mData.get(position);

        holder.editText.setTag(position);

        holder.editText.setText(mData.get(position).editText.getText().toString());

        holder.headerValue.setText(mData.get(position).headerTextValue);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        EditText editText;
        TextView headerValue;
        String crossId;

        ViewHolder(View itemView) {
            super(itemView);
            headerValue = (TextView) itemView.findViewById(R.id.headerTextValue);
            editText = (EditText) itemView.findViewById(R.id.headerValueEditText);
            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    if (editText.getTag() != null) {
                        mData.get((int)editText.getTag()).editText.setText(charSequence.toString());
                        //mData.set((int)editText.getTag(), charSequence.toString());
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });
        }
    }
}