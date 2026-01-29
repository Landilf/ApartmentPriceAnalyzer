package ru.landilf.apartmentpriceanalyzer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FavoritesActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ApartmentAdapter adapter;
    private LoadingDialog loadingDialog;
    private List<Apartment> favoriteList = new ArrayList<>();
    private Set<String> favoriteIds = new HashSet<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_favorites);

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

        RecyclerView recyclerView = findViewById(R.id.recycler_view_favorites);
        adapter = new ApartmentAdapter();
        recyclerView.setAdapter(adapter);

        adapter.setOnApartmentClickListener(apartment -> {
            Intent intent = new Intent(FavoritesActivity.this, ApartmentDetailsActivity.class);
            intent.putExtra("apartmentId", apartment.getId());
            startActivity(intent);
        });

        adapter.setOnFavoriteToggleListener((apartment, isFavorite) -> {
            // In favorites screen, checking is redundant (already checked), unchecking means remove.
            if (!isFavorite) {
                removeFavorite(apartment);
            }
        });

        loadFavorites();
    }

    private void loadFavorites() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Ошибка авторизации", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadingDialog.startLoadingDialog();
        String uid = user.getUid();
        
        db.collection("users").document(uid).collection("favorites").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    loadingDialog.dismissDialog();
                    favoriteList.clear();
                    favoriteIds.clear();
                    
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        try {
                            Apartment apartment = doc.toObject(Apartment.class);
                            apartment.setId(doc.getId()); // Ensure ID is set
                            favoriteList.add(apartment);
                            favoriteIds.add(doc.getId());
                        } catch (Exception e) {
                             android.util.Log.e("FavoritesActivity", "Error parsing favorite: " + doc.getId(), e);
                        }
                    }
                    
                    adapter.setApartments(favoriteList);
                    adapter.setFavoriteIds(favoriteIds);
                    
                    if (favoriteList.isEmpty()) {
                        Toast.makeText(this, "Список избранного пуст", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    loadingDialog.dismissDialog();
                    Toast.makeText(this, "Ошибка загрузки: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void removeFavorite(Apartment apartment) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;
        
        String uid = user.getUid();
        db.collection("users").document(uid).collection("favorites")
                .document(apartment.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Удалено из избранного", Toast.LENGTH_SHORT).show();
                    // Update local list
                    favoriteList.remove(apartment); // Needs equals/hashCode or iterator remove by ID
                    // Simple remove by object might not work if reference differs, but here we passed the object from the list.
                    // However, safer to remove by ID match
                    favoriteList.removeIf(apt -> apt.getId().equals(apartment.getId()));
                    favoriteIds.remove(apartment.getId());
                    
                    adapter.setApartments(favoriteList);
                    adapter.setFavoriteIds(favoriteIds);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Ошибка удаления: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}