package ru.landilf.apartmentpriceanalyzer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ApartmentAdapter extends RecyclerView.Adapter<ApartmentAdapter.ApartmentViewHolder> {

    private List<Apartment> apartmentList = new ArrayList<>();
    private Set<String> favoriteIds = new HashSet<>();
    private OnApartmentClickListener listener;
    private OnFavoriteToggleListener favoriteListener;

    public interface OnApartmentClickListener {
        void onApartmentClick(Apartment apartment);
    }

    public interface OnFavoriteToggleListener {
        void onFavoriteToggle(Apartment apartment, boolean isFavorite);
    }

    public void setOnApartmentClickListener(OnApartmentClickListener listener) {
        this.listener = listener;
    }

    public void setOnFavoriteToggleListener(OnFavoriteToggleListener listener) {
        this.favoriteListener = listener;
    }

    public void setApartments(List<Apartment> apartments) {
        this.apartmentList = apartments;
        notifyDataSetChanged();
    }

    public void setFavoriteIds(Set<String> favoriteIds) {
        this.favoriteIds = favoriteIds;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ApartmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_apartment_listing, parent, false);
        return new ApartmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ApartmentViewHolder holder, int position) {
        Apartment apartment = apartmentList.get(position);
        holder.bind(apartment);
    }

    @Override
    public int getItemCount() {
        return apartmentList.size();
    }

    class ApartmentViewHolder extends RecyclerView.ViewHolder {
        private TextView tvPrice, tvTitle, tvAddress;
        private CheckBox cbFavorite;

        public ApartmentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvAddress = itemView.findViewById(R.id.tv_address);
            cbFavorite = itemView.findViewById(R.id.cb_favorite);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onApartmentClick(apartmentList.get(position));
                }
            });

            cbFavorite.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (favoriteListener != null && position != RecyclerView.NO_POSITION) {
                    Apartment apt = apartmentList.get(position);
                    boolean isChecked = cbFavorite.isChecked();
                    favoriteListener.onFavoriteToggle(apt, isChecked);
                }
            });
        }

        public void bind(Apartment apartment) {
            if (apartment.getPrice_per_month() != null) {
                tvPrice.setText(String.format("%,d %s", apartment.getPrice_per_month(), "₽"));
            } else {
                tvPrice.setText("Цена не указана");
            }
            tvTitle.setText(apartment.getTitle());
            tvAddress.setText(apartment.getAddress());
            
            // Set state based on favoriteIds set
            // Remove listener temporarily to avoid triggering it during binding if we used OnCheckedChangeListener
            // But we use OnClickListener on the View, so setChecked is safe.
            cbFavorite.setChecked(favoriteIds != null && favoriteIds.contains(apartment.getId()));
        }
    }
}