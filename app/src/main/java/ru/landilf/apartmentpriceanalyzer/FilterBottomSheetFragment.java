package ru.landilf.apartmentpriceanalyzer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.HashSet;
import java.util.Set;

public class FilterBottomSheetFragment extends BottomSheetDialogFragment {

    private FilterSettings settings;
    private OnFilterAppliedListener listener;

    private TextInputEditText etPriceMin, etPriceMax, etAreaMin, etAreaMax;
    private ChipGroup chipGroupRooms;

    public interface OnFilterAppliedListener {
        void onFilterApplied(FilterSettings settings);
    }

    public static FilterBottomSheetFragment newInstance(FilterSettings currentSettings) {
        FilterBottomSheetFragment fragment = new FilterBottomSheetFragment();
        Bundle args = new Bundle();
        args.putSerializable("settings", currentSettings);
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnFilterAppliedListener(OnFilterAppliedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_filter, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            settings = (FilterSettings) getArguments().getSerializable("settings");
        }
        if (settings == null) {
            settings = new FilterSettings();
        }

        etPriceMin = view.findViewById(R.id.et_price_min);
        etPriceMax = view.findViewById(R.id.et_price_max);
        etAreaMin = view.findViewById(R.id.et_area_min);
        etAreaMax = view.findViewById(R.id.et_area_max);
        chipGroupRooms = view.findViewById(R.id.chip_group_rooms);

        // Populate views
        if (settings.getMinPrice() != null) etPriceMin.setText(String.valueOf(settings.getMinPrice()));
        if (settings.getMaxPrice() != null) etPriceMax.setText(String.valueOf(settings.getMaxPrice()));
        if (settings.getMinArea() != null) etAreaMin.setText(String.valueOf(settings.getMinArea()));
        if (settings.getMaxArea() != null) etAreaMax.setText(String.valueOf(settings.getMaxArea()));

        for (int room : settings.getRoomCounts()) {
            int chipId = -1;
            if (room == 0) chipId = R.id.chip_room_studio;
            else if (room == 1) chipId = R.id.chip_room_1;
            else if (room == 2) chipId = R.id.chip_room_2;
            else if (room == 3) chipId = R.id.chip_room_3;
            else if (room == 4) chipId = R.id.chip_room_4;

            if (chipId != -1) {
                Chip chip = view.findViewById(chipId);
                if (chip != null) chip.setChecked(true);
            }
        }

        view.findViewById(R.id.btn_apply_filters).setOnClickListener(v -> applyFilters());
        view.findViewById(R.id.btn_reset_filters).setOnClickListener(v -> resetFilters());
    }

    private void applyFilters() {
        FilterSettings newSettings = new FilterSettings();

        // Price
        String pMin = etPriceMin.getText().toString();
        if (!pMin.isEmpty()) newSettings.setMinPrice(Integer.parseInt(pMin));
        String pMax = etPriceMax.getText().toString();
        if (!pMax.isEmpty()) newSettings.setMaxPrice(Integer.parseInt(pMax));

        // Area
        String aMin = etAreaMin.getText().toString();
        if (!aMin.isEmpty()) newSettings.setMinArea(Integer.parseInt(aMin));
        String aMax = etAreaMax.getText().toString();
        if (!aMax.isEmpty()) newSettings.setMaxArea(Integer.parseInt(aMax));

        // Rooms
        Set<Integer> rooms = new HashSet<>();
        if (((Chip) getView().findViewById(R.id.chip_room_studio)).isChecked()) rooms.add(0);
        if (((Chip) getView().findViewById(R.id.chip_room_1)).isChecked()) rooms.add(1);
        if (((Chip) getView().findViewById(R.id.chip_room_2)).isChecked()) rooms.add(2);
        if (((Chip) getView().findViewById(R.id.chip_room_3)).isChecked()) rooms.add(3);
        if (((Chip) getView().findViewById(R.id.chip_room_4)).isChecked()) rooms.add(4); // 4+
        newSettings.setRoomCounts(rooms);

        if (listener != null) {
            listener.onFilterApplied(newSettings);
        }
        dismiss();
    }

    private void resetFilters() {
        if (listener != null) {
            listener.onFilterApplied(new FilterSettings());
        }
        dismiss();
    }
}
