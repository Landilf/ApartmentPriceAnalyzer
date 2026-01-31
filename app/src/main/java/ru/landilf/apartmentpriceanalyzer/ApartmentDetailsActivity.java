package ru.landilf.apartmentpriceanalyzer;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class ApartmentDetailsActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private LoadingDialog loadingDialog;
    private TextView tvTitle, tvPrice, tvAddress, tvMetro, tvArea, tvFloor;
    private ChipGroup chipGroupFeatures;
    private LinearLayout layoutFactsContainer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_apartment_details);

        db = FirebaseFirestore.getInstance();
        loadingDialog = new LoadingDialog(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        tvTitle = findViewById(R.id.tv_detail_title);
        tvPrice = findViewById(R.id.tv_detail_price);
        tvAddress = findViewById(R.id.tv_detail_address);
        tvMetro = findViewById(R.id.tv_detail_metro);
        tvArea = findViewById(R.id.tv_detail_area);
        tvFloor = findViewById(R.id.tv_detail_floor);
        chipGroupFeatures = findViewById(R.id.chip_group_features);
        layoutFactsContainer = findViewById(R.id.layout_facts_container);

        String apartmentId = getIntent().getStringExtra("apartmentId");
        if (apartmentId != null) {
            loadApartmentData(apartmentId);
        } else {
            Toast.makeText(this, "Ошибка: ID объявления не передан", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadApartmentData(String apartmentId) {
        loadingDialog.startLoadingDialog();
        db.collection("apartments").document(apartmentId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    loadingDialog.dismissDialog();
                    if (documentSnapshot.exists()) {
                        try {
                            Apartment apartment = documentSnapshot.toObject(Apartment.class);
                            if (apartment != null) {
                                populateViews(apartment);
                            }
                        } catch (Exception e) {
                            Toast.makeText(this, "Ошибка обработки данных: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(this, "Объявление не найдено", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    loadingDialog.dismissDialog();
                    Toast.makeText(this, "Ошибка загрузки: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void populateViews(Apartment apartment) {
        tvTitle.setText(apartment.getTitle());
        
        if (apartment.getPrice_per_month() != null) {
            tvPrice.setText(String.format("%,d %s/мес", apartment.getPrice_per_month(), "₽"));
        } else {
            tvPrice.setText("Цена не указана");
        }
        
        tvAddress.setText(apartment.getAddress());
        
        Apartment.Features features = apartment.getFeatures();
        if (features != null) {
            if (features.getMetro_nearest_time() != null) {
                tvMetro.setText(features.getMetro_nearest_time() + " мин");
            } else {
                tvMetro.setText("-");
            }
            
            if (features.getTotal_area() != null) {
                tvArea.setText(features.getTotal_area() + " м²");
            } else {
                tvArea.setText("-");
            }
            
            if (features.getFloor_number() != null) {
                 String floorStr = String.valueOf(features.getFloor_number());
                 if (features.getTotal_floors_cnt() != null) {
                     floorStr += "/" + features.getTotal_floors_cnt();
                 }
                 tvFloor.setText(floorStr);
            } else {
                 tvFloor.setText("-");
            }
        } else {
            tvMetro.setText("-");
            tvArea.setText("-");
            tvFloor.setText("-");
        }

        chipGroupFeatures.removeAllViews();
        
        // Facts (Amenities)
        if (apartment.getFacts() != null) {
            for (String fact : apartment.getFacts()) {
                switch (fact) {
                    case "refrigerator": addChip("Холодильник"); break;
                    case "washing_machine": addChip("Стиральная машина"); break;
                    case "tv": addChip("Телевизор"); break;
                    case "internet": addChip("Интернет"); break;
                    case "dishwasher": addChip("Посудомойка"); break;
                    case "ac": addChip("Кондиционер"); break;
                    case "shower_cabin": addChip("Душевая кабина"); break;
                    case "bathtub": addChip("Ванна"); break;
                    case "room_furniture": addChip("Мебель в комнатах"); break;
                    case "kitchen_furniture": addChip("Кухонная мебель"); break;
                }
            }
        }

        if (features != null) {
            // House Type
            if (features.getHouse_type_cat() != null && !features.getHouse_type_cat().isEmpty()) {
                addChip(features.getHouse_type_cat());
            }
            if (features.getEntrance_info() != null && features.getEntrance_info().contains("мусоропровод")) {
                addChip("Мусоропровод");
            }

            layoutFactsContainer.removeAllViews();
            
            // Comission
            if (features.getComission() != null) {
                int percent = (int) (features.getComission() * 100);
                addFactRow("Комиссия", percent + "%");
            }
            
            // Prepayment
            if (features.getPrepayment_months_cnt() != null) {
                addFactRow("Предоплата", features.getPrepayment_months_cnt() + " мес.");
            }
            
            // Utility (HCS Price)
            if (features.getHcs_price() != null && !features.getHcs_price().isEmpty()) {
                addFactRow("КУ", features.getHcs_price());
            }
            
            // Counts
            if (features.getCombined_bathrooms_cnt() != null && features.getCombined_bathrooms_cnt() > 0)
                addFactRow("Санузел (совм.)", String.valueOf(features.getCombined_bathrooms_cnt()));
            if (features.getSeparate_bathrooms_cnt() != null && features.getSeparate_bathrooms_cnt() > 0)
                addFactRow("Санузел (разд.)", String.valueOf(features.getSeparate_bathrooms_cnt()));
                
            if (features.getPassenger_elevators_cnt() != null && features.getPassenger_elevators_cnt() > 0)
                addFactRow("Лифт (пасс.)", String.valueOf(features.getPassenger_elevators_cnt()));
            if (features.getFreight_elevators_cnt() != null && features.getFreight_elevators_cnt() > 0)
                addFactRow("Лифт (груз.)", String.valueOf(features.getFreight_elevators_cnt()));
                
            if (features.getBalcony_loggia_cnt() != null && !features.getBalcony_loggia_cnt().isEmpty()) {
                addFactRow("Балкон/Лоджия", features.getBalcony_loggia_cnt());
            }

            // Repair
            if (features.getRepair_cat() != null && !features.getRepair_cat().isEmpty()) {
                addFactRow("Ремонт", features.getRepair_cat());
            }
            
            // Parking
            if (features.getParking_cat() != null && !features.getParking_cat().isEmpty()) {
                addFactRow("Парковка", features.getParking_cat());
            }
            
            // Construction Year
             if (features.getConstruction_year() != null && !features.getConstruction_year().isEmpty()) {
                addFactRow("Год постройки", features.getConstruction_year());
            }
             
             // Ceiling Height
             if (features.getCeiling_height() != null && !features.getCeiling_height().isEmpty()) {
                addFactRow("Высота потолков", features.getCeiling_height() + " м");
            }
        }
    }

    private void addChip(String text) {
        Chip chip = new Chip(this);
        chip.setText(text);
        chip.setClickable(false);
        chipGroupFeatures.addView(chip);
    }

    private void addFactRow(String key, String value) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 16);
        row.setLayoutParams(params);

        TextView tvKey = new TextView(this);
        tvKey.setText(key);
        tvKey.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        
        TextView tvValue = new TextView(this);
        tvValue.setText(value);
        tvValue.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        tvValue.setGravity(android.view.Gravity.END);
        tvValue.setTypeface(null, android.graphics.Typeface.BOLD);
        
        tvKey.setTextColor(android.graphics.Color.GRAY);
        tvValue.setTextColor(android.graphics.Color.GRAY);
        
        row.addView(tvKey);
        row.addView(tvValue);

        layoutFactsContainer.addView(row);
    }
}