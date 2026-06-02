package com.niit.memory.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.niit.memory.R;
import com.niit.memory.data.model.CalendarNote;
import java.util.ArrayList;
import java.util.List;

public class CalendarNoteAdapter extends RecyclerView.Adapter<CalendarNoteAdapter.ViewHolder> {

    private List<CalendarNote> items = new ArrayList<>();
    private final OnDeleteListener deleteListener;

    public interface OnDeleteListener {
        void onDelete(long id);
    }

    public CalendarNoteAdapter(OnDeleteListener listener) {
        this.deleteListener = listener;
    }

    public void submitList(List<CalendarNote> list) {
        items = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_calendar_note, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int pos) {
        CalendarNote item = items.get(pos);
        holder.icon.setText(item.getIcon() != null ? item.getIcon() : "📝");
        holder.text.setText(item.getText());
        holder.deleteBtn.setOnClickListener(v -> {
            if (deleteListener != null && item.getId() != null) {
                deleteListener.onDelete(item.getId());
            }
        });
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView icon, text;
        View deleteBtn;
        ViewHolder(View v) {
            super(v);
            icon = v.findViewById(R.id.note_icon);
            text = v.findViewById(R.id.note_text);
            deleteBtn = v.findViewById(R.id.delete_note);
        }
    }
}
