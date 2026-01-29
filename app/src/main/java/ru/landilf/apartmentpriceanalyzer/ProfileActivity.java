package ru.landilf.apartmentpriceanalyzer;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvSurname, tvName, tvPatronymic, tvPhone;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        loadingDialog = new LoadingDialog(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        tvSurname = findViewById(R.id.tv_surname);
        tvName = findViewById(R.id.tv_name);
        tvPatronymic = findViewById(R.id.tv_patronymic);
        tvPhone = findViewById(R.id.tv_phone);

        loadUserData();
    }

    private void loadUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            loadingDialog.startLoadingDialog();
            String uid = user.getUid();
            db.collection("users").document(uid).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        loadingDialog.dismissDialog();
                        if (documentSnapshot.exists()) {
                            tvSurname.setText(documentSnapshot.getString("surname"));
                            tvName.setText(documentSnapshot.getString("name"));
                            tvPatronymic.setText(documentSnapshot.getString("patronymic"));
                            tvPhone.setText(documentSnapshot.getString("phone"));
                        } else {
                            Toast.makeText(ProfileActivity.this, "Данные профиля не найдены", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        loadingDialog.dismissDialog();
                        Toast.makeText(ProfileActivity.this, "Ошибка загрузки данных: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }
}
