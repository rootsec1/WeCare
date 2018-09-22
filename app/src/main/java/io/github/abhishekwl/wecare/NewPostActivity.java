package io.github.abhishekwl.wecare;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class NewPostActivity extends AppCompatActivity {

    @BindView(R.id.newPostLinearLayout)
    LinearLayout newPostLinearLayout;
    @BindView(R.id.newPostProfilePictureImageView)
    ImageView profilePictureImageView;
    @BindView(R.id.newPostTextView)
    TextView newPostTextView;
    @BindView(R.id.newPostRelativeLayout)
    RelativeLayout newPostRelativeLayout;
    @BindView(R.id.newPostContentEditText)
    TextInputEditText newPostContentEditText;
    @BindView(R.id.newPostButton)
    Button newPostButton;

    private Unbinder unbinder;
    private DatabaseReference databaseReference;
    private Location deviceLocation;
    private FirebaseAuth firebaseAuth;
    private MaterialDialog materialDialog;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private SupportMapFragment mapFragment;
    private GoogleMap googleMapPost;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_new_post);
        unbinder = ButterKnife.bind(this);

        initializeComponents();
        initializeViews();
    }

    @SuppressLint("MissingPermission")
    private void initializeComponents() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(NewPostActivity.this);
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
            deviceLocation = location;
            LatLng userLatLang = new LatLng(deviceLocation.getLatitude(), deviceLocation.getLongitude());
            googleMapPost.addMarker(new MarkerOptions().position(userLatLang).title("Me"));
            googleMapPost.moveCamera(CameraUpdateFactory.newLatLng(userLatLang));
            googleMapPost.setMyLocationEnabled(true);
            googleMapPost.getUiSettings().setMyLocationButtonEnabled(true);
            googleMapPost.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLang, 16.0f));
        }).addOnFailureListener(e -> notifyMessage(e.getMessage()));
        firebaseAuth = FirebaseAuth.getInstance();
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.newPostMapFragment);
        mapFragment.getMapAsync(googleMap -> googleMapPost = googleMap);
        databaseReference = FirebaseDatabase.getInstance().getReference("wecare");
        databaseReference.child("users").child(Objects.requireNonNull(firebaseAuth.getUid())).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                currentUser = dataSnapshot.getValue(User.class);
                Glide.with(getApplicationContext()).load(currentUser.getImage()).into(profilePictureImageView);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                notifyMessage(databaseError.getMessage());
            }
        });
    }

    private void initializeViews() {

    }

    private void notifyMessage(String message) {
        if (materialDialog != null && materialDialog.isShowing()) materialDialog.dismiss();
        Snackbar.make(newPostLinearLayout, message, Snackbar.LENGTH_INDEFINITE).show();
    }

    @OnClick(R.id.newPostButton)
    public void onNewPostButtonPressed() {
        String content = newPostContentEditText.getText().toString();
        materialDialog = new MaterialDialog.Builder(NewPostActivity.this)
                .title(getString(R.string.app_name))
                .content("Uploading new post to forum..")
                .titleColor(Color.BLACK)
                .contentColorRes(R.color.colorTextDark)
                .progress(true, 0)
                .show();

        if (TextUtils.isEmpty(content)) notifyMessage("Please enter something.");
        else {
            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
            List<Address> addresses = null;
            try {
                addresses = geocoder.getFromLocation(deviceLocation.getLatitude(), deviceLocation.getLongitude(), 1);
                String city = addresses.get(0).getLocality();
                Post post = new Post(currentUser, deviceLocation.getLatitude(), deviceLocation.getLongitude(), content, city);
                databaseReference.child("posts").push().setValue(post).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) notifyMessage("Post successful");
                    else notifyMessage(Objects.requireNonNull(task.getException()).getMessage());
                });
            } catch (IOException ignored) { }
        }
    }

    @Override
    protected void onDestroy() {
        unbinder.unbind();
        super.onDestroy();
    }
}
