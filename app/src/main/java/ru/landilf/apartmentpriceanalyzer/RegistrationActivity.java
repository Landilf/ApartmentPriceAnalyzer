package ru.landilf.apartmentpriceanalyzer;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegistrationActivity extends AppCompatActivity {

    private TextInputEditText etSurname, etName, etPatronymic, etPhone, etLogin, etPassword;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registration);

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

        etSurname = findViewById(R.id.et_surname);
        etName = findViewById(R.id.et_name);
        etPatronymic = findViewById(R.id.et_patronymic);
        etPhone = findViewById(R.id.et_phone);
        etLogin = findViewById(R.id.et_reg_login);
        etPassword = findViewById(R.id.et_reg_password);

        findViewById(R.id.btn_register).setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String surname = etSurname.getText().toString().trim();
        String name = etName.getText().toString().trim();
        String patronymic = etPatronymic.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String login = etLogin.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(surname)) { etSurname.setError("Введите фамилию"); return; }
        if (TextUtils.isEmpty(name)) { etName.setError("Введите имя"); return; }
        // Patronymic can be optional potentially, but assuming required here
        if (TextUtils.isEmpty(phone)) { etPhone.setError("Введите телефон"); return; }
        if (TextUtils.isEmpty(login)) { etLogin.setError("Введите логин"); return; }
        if (!login.matches("[a-zA-Z0-9_]+")) {
            etLogin.setError("Логин может содержать только латинские буквы, цифры и _");
            return;
        }
        if (TextUtils.isEmpty(password)) { etPassword.setError("Введите пароль"); return; }
        if (password.length() < 6) {
            etPassword.setError("Пароль должен содержать не менее 6 символов");
            return;
        }

        // Construct fake email
        String email = login + "@apartmentpriceanalyzer.local";

        findViewById(R.id.btn_register).setEnabled(false);
        loadingDialog.startLoadingDialog();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        String uid = mAuth.getCurrentUser().getUid();
                        saveUserToFirestore(uid, surname, name, patronymic, phone, login);
                    } else {
                        loadingDialog.dismissDialog();
                        findViewById(R.id.btn_register).setEnabled(true);
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            etLogin.setError("Этот логин уже занят");
                        } else {
                            Toast.makeText(RegistrationActivity.this, "Ошибка регистрации: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void saveUserToFirestore(String uid, String surname, String name, String patronymic, String phone, String login) {
        Map<String, Object> user = new HashMap<>();
        user.put("surname", surname);
        user.put("name", name);
        user.put("patronymic", patronymic);
        user.put("phone", phone);
        user.put("login", login);

        db.collection("users").document(uid)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    loadingDialog.dismissDialog();
                    Toast.makeText(RegistrationActivity.this, "Регистрация успешна", Toast.LENGTH_SHORT).show();
                    
                    Intent intent = new Intent(RegistrationActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish(); 
                })
                .addOnFailureListener(e -> {
                    loadingDialog.dismissDialog();
                    findViewById(R.id.btn_register).setEnabled(true);
                    Toast.makeText(RegistrationActivity.this, "Ошибка сохранения данных: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
}
