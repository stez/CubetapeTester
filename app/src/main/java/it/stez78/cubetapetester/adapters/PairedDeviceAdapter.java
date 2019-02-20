package it.stez78.cubetapetester.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import it.stez78.cubetapetester.R;
import it.stez78.cubetapetester.models.PairedDevice;

/**
 * Created by Stefano Zanotti on 20/02/2019.
 */
public class PairedDeviceAdapter extends RecyclerView.Adapter<PairedDeviceAdapter.ViewHolder> {
    private static final String TAG = "ElGruppoTipoImballoAdapter";

    private List<PairedDevice> mDataSet;
    private OnItemClickListener listener;
    private Context context;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private CardView container;
        private TextView name;
        private TextView info;

        public ViewHolder(View v) {
            super(v);
            container = v.findViewById(R.id.list_element_bluetooth_device_card_container);
            name = v.findViewById(R.id.list_element_bluetooth_device_name);
            info = v.findViewById(R.id.list_element_bluetooth_device_info);
        }

        public TextView getName() {
            return name;
        }
        public TextView getInfo() {
            return info;
        }
        public CardView getContainer() {
            return container;
        }
        public void bindItemClickListener(PairedDevice el, OnItemClickListener listener) {
            container.setOnClickListener(v -> listener.onItemClick(el));
        }
    }

    public PairedDeviceAdapter(Context context, List<PairedDevice> dataSet, OnItemClickListener listener) {
        mDataSet = dataSet;
        this.listener = listener;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.list_item_bluetooth_device, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        PairedDevice el = mDataSet.get(position);
        viewHolder.getName().setText(el.getName());
        viewHolder.getInfo().setText(el.getMacAddress());
        viewHolder.bindItemClickListener(el,listener);
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }
}