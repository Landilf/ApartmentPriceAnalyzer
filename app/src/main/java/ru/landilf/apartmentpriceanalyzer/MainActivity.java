package ru.landilf.apartmentpriceanalyzer;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final int PAGE_SIZE = 28;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ImageView ivLogout;
    private TextView tvLogout;
    private View logoutContainer;
    private ApartmentAdapter adapter;
    private LoadingDialog loadingDialog;
    private Set<String> favoriteIds = new HashSet<>();
    private List<Apartment> allApartments = new ArrayList<>();
    private DocumentSnapshot lastVisible;
    private View btnLoadMore;
    private FilterSettings currentFilters = new FilterSettings();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        loadingDialog = new LoadingDialog(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        logoutContainer = findViewById(R.id.logout_container);
        ivLogout = findViewById(R.id.iv_logout);
        tvLogout = findViewById(R.id.tv_logout);
        btnLoadMore = findViewById(R.id.btn_load_more);

        RecyclerView recyclerView = findViewById(R.id.recycler_view_apartments);
        adapter = new ApartmentAdapter();
        recyclerView.setAdapter(adapter);

        btnLoadMore.setOnClickListener(v -> loadApartments(true));

        adapter.setOnApartmentClickListener(apartment -> {
            Intent intent = new Intent(MainActivity.this, ApartmentDetailsActivity.class);
            intent.putExtra("apartmentId", apartment.getId());
            startActivity(intent);
        });

        adapter.setOnFavoriteToggleListener((apartment, isFavorite) -> {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user == null) {
                Toast.makeText(this, "Авторизуйтесь для добавления в избранное", Toast.LENGTH_SHORT).show();
                return;
            }

            String uid = user.getUid();
            if (isFavorite) {
                db.collection("users").document(uid).collection("favorites")
                        .document(apartment.getId())
                        .set(apartment)
                        .addOnSuccessListener(aVoid -> {
                            favoriteIds.add(apartment.getId());
                            adapter.setFavoriteIds(favoriteIds);
                            Toast.makeText(this, "Добавлено в избранное", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, "Ошибка добавления: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            } else {
                db.collection("users").document(uid).collection("favorites")
                        .document(apartment.getId())
                        .delete()
                        .addOnSuccessListener(aVoid -> {
                            favoriteIds.remove(apartment.getId());
                            adapter.setFavoriteIds(favoriteIds);
                            Toast.makeText(this, "Удалено из избранного", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, "Ошибка удаления: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.drawer_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            findViewById(R.id.main_content).setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            findViewById(R.id.drawer_container).setPadding(0, systemBars.top, 0, systemBars.bottom);
            return insets;
        });

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_profile) {
                Intent intent = new Intent(this, ProfileActivity.class);
                startActivity(intent);
            } else if (id == R.id.nav_favorites) {
                Intent intent = new Intent(this, FavoritesActivity.class);
                startActivity(intent);
            }
            drawerLayout.closeDrawers();
            return true;
        });

        logoutContainer.setOnClickListener(v -> handleAuthAction());

        loadApartments(false);
    }

    private void loadApartments(boolean isNextPage) {
        Query query = db.collection("apartments")
                .limit(PAGE_SIZE);

        if (isNextPage && lastVisible != null) {
            query = query.startAfter(lastVisible);
        } else if (!isNextPage) {
            allApartments.clear();
            adapter.setApartments(new ArrayList<>());
            lastVisible = null;
        }

        loadingDialog.startLoadingDialog();
        query.get().addOnCompleteListener(task -> {
            loadingDialog.dismissDialog();
            if (task.isSuccessful()) {
                List<Apartment> newItems = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    try {
                        Apartment apartment = document.toObject(Apartment.class);
                        apartment.setId(document.getId());
                        newItems.add(apartment);
                    } catch (Exception e) {
                        // Skip malformed document
                        android.util.Log.e("MainActivity", "Error parsing apartment: " + document.getId(), e);
                    }
                }

                if (!task.getResult().isEmpty()) {
                    lastVisible = task.getResult().getDocuments().get(task.getResult().size() - 1);
                }

                allApartments.addAll(newItems);

                // Show "Load More" only if we got a full page of results
                if (newItems.size() == PAGE_SIZE) {
                    btnLoadMore.setVisibility(View.VISIBLE);
                } else {
                    btnLoadMore.setVisibility(View.GONE);
                }

                applyFilters(); // Apply current filters to the (potentially growing) list
            } else {
                Toast.makeText(this, "Ошибка загрузки: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyFilters() {
        if (!currentFilters.isActive()) {
            adapter.setApartments(new ArrayList<>(allApartments));
            return;
        }

        List<Apartment> filtered = new ArrayList<>();
        for (Apartment apt : allApartments) {
            if (matchesFilter(apt)) {
                filtered.add(apt);
            }
        }
        adapter.setApartments(filtered);
    }

    private boolean matchesFilter(Apartment apt) {
        // Price
        Long price = apt.getPrice_per_month();
        if (currentFilters.getMinPrice() != null) {
            if (price == null || price < currentFilters.getMinPrice()) return false;
        }
        if (currentFilters.getMaxPrice() != null) {
            if (price == null || price > currentFilters.getMaxPrice()) return false;
        }

        // Area
        // New structure: area is inside features
        if (apt.getFeatures() != null) {
            Double area = apt.getFeatures().getTotal_area();
            if (currentFilters.getMinArea() != null) {
                if (area == null || area < currentFilters.getMinArea()) return false;
            }
            if (currentFilters.getMaxArea() != null) {
                if (area == null || area > currentFilters.getMaxArea()) return false;
            }
        } else {
            // If features are missing but we have area filters, filter it out (or include? let's filter out)
            if (currentFilters.getMinArea() != null || currentFilters.getMaxArea() != null)
                return false;
        }

        // Rooms (parse title)
        if (!currentFilters.getRoomCounts().isEmpty()) {
            int rooms = parseRooms(apt.getTitle());
            boolean matchesRoom = false;
            // logic: 0=studio, 1=1, 2=2, 3=3, 4=4+
            if (rooms >= 4) {
                if (currentFilters.getRoomCounts().contains(4)) matchesRoom = true;
            } else {
                if (currentFilters.getRoomCounts().contains(rooms)) matchesRoom = true;
            }
            return matchesRoom;
        }

        return true;
    }

    private int parseRooms(String title) {
        if (title == null) return -1;
        String lower = title.toLowerCase();
        if (lower.contains("студия")) return 0;
        if (lower.contains("1-комн")) return 1;
        if (lower.contains("2-комн")) return 2;
        if (lower.contains("3-комн")) return 3;
        if (lower.contains("4-комн") || lower.contains("5-комн")) return 4;
        return -1;
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateUI(mAuth.getCurrentUser());
    }

    private void updateUI(FirebaseUser user) {
        Menu menu = navigationView.getMenu();
        MenuItem navProfile = menu.findItem(R.id.nav_profile);
        MenuItem navFavorites = menu.findItem(R.id.nav_favorites);

        if (user != null) {
            if (navProfile != null) navProfile.setVisible(true);
            if (navFavorites != null) navFavorites.setVisible(true);

            tvLogout.setText("Выйти из аккаунта");
            ivLogout.setImageResource(R.drawable.ic_logout);

            loadFavorites(user.getUid());
        } else {
            if (navProfile != null) navProfile.setVisible(false);
            if (navFavorites != null) navFavorites.setVisible(false);

            tvLogout.setText("Войти");
            ivLogout.setImageResource(R.drawable.ic_login);

            favoriteIds.clear();
            adapter.setFavoriteIds(favoriteIds);
        }
    }

    private void loadFavorites(String uid) {
        db.collection("users").document(uid).collection("favorites").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    favoriteIds.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        favoriteIds.add(doc.getId());
                    }
                    adapter.setFavoriteIds(favoriteIds);
                });
    }

    private void handleAuthAction() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            mAuth.signOut();
            updateUI(null);
            Toast.makeText(this, "Вы вышли из аккаунта", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
        drawerLayout.closeDrawers();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_filter) {
            FilterBottomSheetFragment fragment = FilterBottomSheetFragment.newInstance(currentFilters);
            fragment.setOnFilterAppliedListener(settings -> {
                this.currentFilters = settings;
                applyFilters();
            });
            fragment.show(getSupportFragmentManager(), "filters");
            return true;
        } else if (id == R.id.action_refresh) {
            loadApartments(false);
            return true;
        } else if (id == R.id.action_upload_data) {
            new ApartmentUploader(this).uploadApartments();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
