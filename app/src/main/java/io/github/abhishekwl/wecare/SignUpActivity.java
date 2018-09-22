package io.github.abhishekwl.wecare;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.widget.Button;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import de.hdodenhof.circleimageview.CircleImageView;

public class SignUpActivity extends AppCompatActivity {

    @BindView(R.id.signUpUserImageView)
    CircleImageView signUpUserImageView;
    @BindView(R.id.signUpNameEditText)
    AppCompatEditText signUpNameEditText;
    @BindView(R.id.signUpEmailEditText)
    AppCompatEditText signUpEmailEditText;
    @BindView(R.id.signUpPasswordEditText)
    AppCompatEditText signUpPasswordEditText;
    @BindView(R.id.signUpConfirmPasswordEdtText)
    AppCompatEditText signUpConfirmPasswordEdtText;
    @BindView(R.id.signUpEC1EditText)
    AppCompatEditText signUpEC1EditText;
    @BindView(R.id.signUpEC2EditText)
    AppCompatEditText signUpEC2EditText;
    @BindView(R.id.signUpEC3EditText)
    AppCompatEditText signUpEC3EditText;
    @BindView(R.id.signUpButton)
    Button signUpButton;
    @BindView(R.id.signUpPhoneEdtText)
    AppCompatEditText signUpPhoneEditText;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private Unbinder unbinder;
    private String userImage;
    private MaterialDialog materialDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(getColor(R.color.colorBackground));
        setContentView(R.layout.activity_sign_up);
        unbinder = ButterKnife.bind(this);

        initializeComponents();
        initializeViews();
    }

    private void initializeComponents() {
        userImage = null;
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference();
    }

    private void initializeViews() {

    }

    @OnClick(R.id.signUpButton)
    public void onSignUpButtonPress() {
        String name = signUpNameEditText.getText().toString();
        String email = signUpEmailEditText.getText().toString();
        String password = signUpPasswordEditText.getText().toString();
        String confirmPassword = signUpConfirmPasswordEdtText.getText().toString();
        String phone = signUpPhoneEditText.getText().toString();
        String ec1 = signUpEC1EditText.getText().toString();
        String ec2 = signUpEC2EditText.getText().toString();
        String ec3 = signUpEC3EditText.getText().toString();

        if (password.equals(confirmPassword)) {
            materialDialog = new MaterialDialog.Builder(SignUpActivity.this)
                    .title(getString(R.string.app_name))
                    .content("Creating new account..")
                    .progress(true, 0)
                    .titleColor(Color.BLACK)
                    .contentColorRes(R.color.colorTextDark)
                    .show();

            firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    User user = new User(firebaseAuth.getUid(), name, userImage, phone, ec1, ec2, ec3);
                    databaseReference.child("wecare").child("users").child(Objects.requireNonNull(firebaseAuth.getUid())).setValue(user).addOnCompleteListener(task1 -> {
                        notifyMessage("Account created");
                        startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                        finish();
                    });
                } else notifyMessage(Objects.requireNonNull(task.getException()).getMessage());
            });
        } else notifyMessage("Passwords do not match.");
    }

    void notifyMessage(String message) {
        if (materialDialog.isShowing()) materialDialog.dismiss();
        Snackbar.make(signUpButton, message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        unbinder.unbind();
        super.onDestroy();
    }
}
