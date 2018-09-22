package io.github.abhishekwl.wecare;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class SignInActivity extends AppCompatActivity {

    @BindView(R.id.signInEmailIdEditText)
    AppCompatEditText signInEmailIdEditText;
    @BindView(R.id.signInPasswordEditText)
    AppCompatEditText signInPasswordEditText;
    @BindView(R.id.signInButton)
    FloatingActionButton signInButton;
    @BindView(R.id.signInSignUpTextView)
    TextView signUpTextView;

    private Unbinder unbinder;
    private FirebaseAuth firebaseAuth;
    private MaterialDialog materialDialog;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(getColor(R.color.colorAccent));
        setContentView(R.layout.activity_sign_in);
        unbinder = ButterKnife.bind(this);

        initializeComponents();
    }

    private void initializeComponents() {
        firebaseAuth = FirebaseAuth.getInstance();
    }

    @OnClick(R.id.signInButton)
    public void onSignInButtonPress() {
        String email = signInEmailIdEditText.getText().toString();
        String password = signInPasswordEditText.getText().toString();

        materialDialog = new MaterialDialog.Builder(SignInActivity.this)
                .title(getString(R.string.app_name))
                .content("Signing In..")
                .progress(true, 0)
                .titleColor(Color.BLACK)
                .contentColorRes(R.color.colorTextDark)
                .show();

        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                databaseReference = FirebaseDatabase.getInstance().getReference("wecare").child("users").child(Objects.requireNonNull(firebaseAuth.getUid()));
                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);
                        if (user==null) notifyMessage("You don't have an account. You need to sign up.");
                        else {
                            startActivity(new Intent(SignInActivity.this, MainActivity.class));
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        notifyMessage(databaseError.getMessage());
                    }
                });
            } else notifyMessage(Objects.requireNonNull(task.getException()).getMessage());
        });
    }

    @OnClick(R.id.signInSignUpTextView)
    public void onSignUpButtonPress() {
        startActivity(new Intent(SignInActivity.this, SignUpActivity.class));
        finish();
    }

    void notifyMessage(String message) {
        if (materialDialog.isShowing()) materialDialog.dismiss();
        Snackbar.make(signInButton, message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        unbinder.unbind();
        super.onDestroy();
    }
}
