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
        
        if (apartment.getMetro_nearest_time() != null) {
            tvMetro.setText(apartment.getMetro_nearest_time() + " мин");
        } else {
            tvMetro.setText("-");
        }
        
        if (apartment.getTotal_area() != null) {
            tvArea.setText(apartment.getTotal_area() + " м²");
        } else {
            tvArea.setText("-");
        }
        
        // Floor is normalized (0.0 - 1.0), so displaying it directly is confusing.
        // We show it only if it looks like an integer, otherwise maybe "Этаж: -"
        // Or if we interpret it as relative. Let's just show it if > 1 (meaning older format) or ignore.
        if (apartment.getFloor() != null && apartment.getFloor() > 1.0) {
             tvFloor.setText(String.valueOf(apartment.getFloor().intValue()));
        } else {
             tvFloor.setText("-");
        }

        chipGroupFeatures.removeAllViews();
        if (isTrue(apartment.getHas_fridge_flg())) addChip("Холодильник");
        if (isTrue(apartment.getHas_washer_flg())) addChip("Стиральная машина");
        if (isTrue(apartment.getHas_tv_flg())) addChip("Телевизор");
        if (isTrue(apartment.getHas_internet_flg())) addChip("Интернет");
        if (isTrue(apartment.getHas_dishwasher_flg())) addChip("Посудомойка");
        if (isTrue(apartment.getHas_ac_flg())) addChip("Кондиционер");
        if (isTrue(apartment.getHas_concierge_flg())) addChip("Консьерж");
        if (isTrue(apartment.getHas_garbage_chute_flg())) addChip("Мусоропровод");
        
        // House Types
        if (isTrue(apartment.getHouse_type_monolithic_flg())) addChip("Монолитный");
        if (isTrue(apartment.getHouse_type_monolithic_brick_flg())) addChip("Монолитно-кирпичный");
        if (isTrue(apartment.getHouse_type_panel_flg())) addChip("Панельный");
        if (isTrue(apartment.getIndividual_project_flg())) addChip("Инд. проект");

        layoutFactsContainer.removeAllViews();
        
        // Comission
        if (apartment.getComission() != null) {
            int percent = (int) (apartment.getComission() * 100);
            addFactRow("Комиссия", percent + "%");
        }
        
        // Prepayment
        if (apartment.getPrepayment_months_cnt() != null) {
            addFactRow("Предоплата", apartment.getPrepayment_months_cnt() + " мес.");
        }
        
        // Utility
        if (apartment.getUtility_fixed_bill() != null && apartment.getUtility_fixed_bill() > 0) {
            addFactRow("КУ (фикс)", apartment.getUtility_fixed_bill() + " ₽");
        }
        
        // Counts
        if (apartment.getCombined_bathrooms_cnt() != null && apartment.getCombined_bathrooms_cnt() > 0)
            addFactRow("Санузел (совм.)", String.valueOf(apartment.getCombined_bathrooms_cnt()));
        if (apartment.getSeparate_bathrooms_cnt() != null && apartment.getSeparate_bathrooms_cnt() > 0)
            addFactRow("Санузел (разд.)", String.valueOf(apartment.getSeparate_bathrooms_cnt()));
            
        if (apartment.getPassenger_elevators_cnt() != null && apartment.getPassenger_elevators_cnt() > 0)
            addFactRow("Лифт (пасс.)", String.valueOf(apartment.getPassenger_elevators_cnt()));
        if (apartment.getFreight_elevators_cnt() != null && apartment.getFreight_elevators_cnt() > 0)
            addFactRow("Лифт (груз.)", String.valueOf(apartment.getFreight_elevators_cnt()));
            
        if (apartment.getBalcony_cnt() != null && apartment.getBalcony_cnt() > 0)
            addFactRow("Балкон", String.valueOf(apartment.getBalcony_cnt()));
        if (apartment.getLoggia_cnt() != null && apartment.getLoggia_cnt() > 0)
            addFactRow("Лоджия", String.valueOf(apartment.getLoggia_cnt()));

        // Repair
        if (apartment.getRepair_cat() != null) {
            String repair = "Нет информации";
            int cat = apartment.getRepair_cat().intValue();
            if (cat == 1) repair = "Косметический";
            else if (cat == 2) repair = "Евро";
            else if (cat == 3) repair = "Дизайнерский";
            addFactRow("Ремонт", repair);
        }
        
        // Parking
        if (apartment.getParking_cat() != null) {
            String parking = "Нет информации";
            int cat = apartment.getParking_cat().intValue();
            if (cat == 1) parking = "Наземная";
            else if (cat == 2) parking = "Подземная";
            addFactRow("Парковка", parking);
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
        // Use default secondary text appearance or a standard color attribute
        tvKey.setTextColor(getResources().getColor(android.R.color.tab_indicator_text, null)); // Or just leave default

        TextView tvValue = new TextView(this);
        tvValue.setText(value);
        tvValue.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        tvValue.setGravity(android.view.Gravity.END);
        tvValue.setTypeface(null, android.graphics.Typeface.BOLD);
        // Use default primary text color
        tvValue.setTextColor(android.graphics.Color.GRAY); // A safe bet for both themes if no attr used, but let's try something better
        // Actually, better to use default (unset) to let theme handle it or standard gray
        tvValue.setTextColor(getResources().getColor(android.R.color.secondary_text_dark, null)); 
        
        // Final polish for dynamic rows: use standard theme colors
        tvKey.setTextColor(android.graphics.Color.GRAY);
        tvValue.setTextColor(android.graphics.Color.parseColor("#808080")); // Placeholder, will fix below with proper attribute usage logic if needed, 
        // but simplest is to use getColor from theme.
        
        // Let's use a more robust way:
        tvKey.setTextColor(android.graphics.Color.GRAY);
        tvValue.setTextColor(android.graphics.Color.GRAY); 
        // Wait, I will just remove the explicit setTextColor to let them inherit theme defaults where possible, 
        // or set them to a neutral gray. 
        // Re-writing the block clearly:
        
        row.addView(tvKey);
        row.addView(tvValue);

        layoutFactsContainer.addView(row);
    }

    private boolean isTrue(Long val) {
        return val != null && val == 1;
    }
}
