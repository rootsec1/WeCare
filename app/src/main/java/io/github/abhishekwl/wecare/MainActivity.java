package io.github.abhishekwl.wecare;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.gaurav.gesto.OnGestureListener;
import com.github.tbouron.shakedetector.library.ShakeDetector;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import cafe.adriel.androidaudiorecorder.AndroidAudioRecorder;
import cafe.adriel.androidaudiorecorder.model.AudioChannel;
import cafe.adriel.androidaudiorecorder.model.AudioSampleRate;
import cafe.adriel.androidaudiorecorder.model.AudioSource;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.mainLogoAnimationView)
    LottieAnimationView mainLogoAnimationView;
    @BindView(R.id.mainUserImageView)
    ImageView mainUserImageView;
    @BindView(R.id.mainLogoRelativeLayout)
    RelativeLayout mainLogoRelativeLayout;
    @BindView(R.id.mainForumTextView)
    TextView mainForumTextView;
    @BindView(R.id.mainPostsRecyclerView)
    RecyclerView mainPostsRecyclerView;
    @BindView(R.id.mainProgressAnimationView)
    LottieAnimationView mainLotteProgressAnimationView;
    @BindView(R.id.mainAppNameTextView)
    TextView appNameTextView;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private static final int RC_AUDIO_RECORD = 242;
    private Unbinder unbinder;
    private User currentUser;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location deviceLocation;
    private PostRecyclerViewAdapter postRecyclerViewAdapter;
    private ArrayList<Post> postArrayList = new ArrayList<>();
    private MaterialDialog materialDialog;
    private String filePath;
    private StorageReference storageReference;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(getColor(R.color.colorBackground));
        setContentView(R.layout.activity_main);
        unbinder = ButterKnife.bind(this);

        initializeComponents();
        initializeViews();
    }

    @SuppressLint("MissingPermission")
    private void initializeComponents() {
        firebaseAuth = FirebaseAuth.getInstance();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> deviceLocation = location);
        storageReference = FirebaseStorage.getInstance().getReference("wecare").child("recordings").child(Objects.requireNonNull(firebaseAuth.getUid()));
        databaseReference = FirebaseDatabase.getInstance().getReference("wecare");
        databaseReference.child("users").child(Objects.requireNonNull(firebaseAuth.getUid())).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                currentUser = dataSnapshot.getValue(User.class);
                Glide.with(getApplicationContext()).load(Objects.requireNonNull(dataSnapshot.getValue(User.class)).getImage()).into(mainUserImageView);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                notifyMessage(databaseError.getMessage());
            }
        });
        postRecyclerViewAdapter = new PostRecyclerViewAdapter(postArrayList);
        filePath = Environment.getExternalStorageDirectory() + "/recorded_audio.wav";
    }

    private void initializeViews() {
        ShakeDetector.create(this, this::triggerSos);
        mainLogoAnimationView.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                if (postArrayList.isEmpty()) fetchData();
            }
        });
        mainPostsRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        mainPostsRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mainPostsRecyclerView.setHasFixedSize(true);
        mainPostsRecyclerView.setAdapter(postRecyclerViewAdapter);
        mainPostsRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getApplicationContext(), (view, position) -> {
            Post post = postArrayList.get(position);
            Uri gmmIntentUri = Uri.parse("google.navigation:q="+post.getLatitude()+","+post.getLongitude());
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);
        }));
        setupGestures();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupGestures() {
        mainLogoRelativeLayout.setOnTouchListener(new OnGestureListener(MainActivity.this) {
            @Override
            public void onSwipeRight() {
                super.onSwipeRight();
                triggerSos();
            }

            @Override
            public void onSwipeLeft() {
                super.onSwipeLeft();
                triggerSos();
            }
        });
    }

    private void triggerSos() {
        String collectedData = "DETAILS: \n"+currentUser.getName()+"\n";
        collectedData = collectedData.concat("PHONE: "+currentUser.getContactNumber());
        collectedData = collectedData.concat("\n\n*ALERT* I AM IN NEED OF IMMEDIATE HELP, PLEASE RESPOND ASAP. YOU CAN TRACK ME AT:\n");
        collectedData = collectedData.concat("\nLOCATION: https://www.google.com/maps/search/?api=1&query="+deviceLocation.getLatitude()+","+deviceLocation.getLongitude());

        materialDialog = new MaterialDialog.Builder(MainActivity.this)
                .title(getString(R.string.app_name))
                .content("Are you sure want to trigger an SOS?\n\nContent that will be shared is:\n".concat(collectedData))
                .titleColor(Color.BLACK)
                .contentColorRes(R.color.colorTextDark)
                .positiveText("YES")
                .negativeText("NO")
                .positiveColorRes(R.color.colorPrimary)
                .negativeColorRes(R.color.colorAccent)
                .onPositive((dialog, which) -> {
                    materialDialog.dismiss();
                    recordAudio();
                })
                .show();
    }

    private void doNext() {
        call(currentUser.getEmergencyContact1());
        String collectedData = "DETAILS: \n"+currentUser.getName()+"\n";
        collectedData = collectedData.concat("PHONE: "+currentUser.getContactNumber());
        collectedData = collectedData.concat("\n\n*ALERT* I AM IN NEED OF IMMEDIATE HELP, PLEASE RESPOND ASAP. YOU CAN TRACK ME AT:\n");
        collectedData = collectedData.concat("\nLOCATION: https://www.google.com/maps/search/?api=1&query="+deviceLocation.getLatitude()+","+deviceLocation.getLongitude());
        Intent sendIntent = new Intent(Intent.ACTION_VIEW);
        sendIntent.putExtra("sms_body", collectedData);
        sendIntent.setType("vnd.android-dir/mms-sms");
        sendIntent.putExtra("address"  ,currentUser.getEmergencyContact1());
        startActivity(sendIntent);
    }

    private void recordAudio() {
        AndroidAudioRecorder.with(this)
                .setFilePath(filePath)
                .setColor(getColor(R.color.colorAccent))
                .setRequestCode(RC_AUDIO_RECORD)
                .setSource(AudioSource.MIC)
                .setChannel(AudioChannel.STEREO)
                .setSampleRate(AudioSampleRate.HZ_48000)
                .setAutoStart(true)
                .setKeepDisplayOn(true)
                .record();
    }

    private void fetchData() {
        postArrayList.clear();
        mainLotteProgressAnimationView.setVisibility(View.VISIBLE);
        postRecyclerViewAdapter.notifyDataSetChanged();

        databaseReference.child("posts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    Post post = postSnapshot.getValue(Post.class);
                    postArrayList.add(post);
                }
                postRecyclerViewAdapter.notifyDataSetChanged();
                notifyMessage("Fetched latest posts.");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                notifyMessage(databaseError.getMessage());
            }
        });
    }

    private void notifyMessage(String message) {
        if (mainLotteProgressAnimationView.getVisibility()== View.VISIBLE) mainLotteProgressAnimationView.setVisibility(View.GONE);
        if (materialDialog!=null && materialDialog.isShowing()) materialDialog.dismiss();
        Snackbar.make(mainForumTextView, message, Snackbar.LENGTH_LONG).show();
    }

    @SuppressLint("MissingPermission")
    @OnClick(R.id.mainNeedHelpButton)
    public void onNeedHelpButtonPress() {
        call("09513886363");
    }

    @SuppressLint("MissingPermission")
    void call(String phoneNumber) {
        startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber)));
    }

    @OnClick(R.id.mainComplaintButton)
    public void onComplaintButtonPress() {
        Intent complaintIntent = new Intent(MainActivity.this, NewPostActivity.class);
        complaintIntent.putExtra("USER", currentUser);
        startActivity(complaintIntent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mainPostsRecyclerView!=null && mainPostsRecyclerView.getAdapter()!=null) fetchData();
    }

    @OnClick(R.id.mainNeedACabButton)
    public void onCabHail() {
        Intent intent = new Intent (Intent.ACTION_VIEW);
        intent.setData (Uri.parse("https://book.olacabs.com?lat="+deviceLocation.getLatitude()+"&lng="+deviceLocation.getLongitude()+"&category=micro"));
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==RC_AUDIO_RECORD && resultCode==RESULT_OK) {
            uploadFileToFirebase();
        }
    }

    private void uploadFileToFirebase() {
        Uri fileUri = Uri.fromFile(new File(filePath));
        @SuppressLint("SimpleDateFormat") SimpleDateFormat s = new SimpleDateFormat("ddMMyyyyhhmmss");
        String format = s.format(new Date());

        materialDialog = new MaterialDialog.Builder(MainActivity.this)
                .title(getString(R.string.app_name))
                .content("Uploading audio recording to secure storage.")
                .titleColor(Color.BLACK)
                .progress(true, 0)
                .contentColorRes(R.color.colorTextDark)
                .show();

        storageReference.child(format.concat(".wav")).putFile(fileUri).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                notifyMessage("Audio recording uploaded successfully");
                doNext();
            }
            else notifyMessage(Objects.requireNonNull(task.getException()).getMessage());
        });
    }

    @OnClick(R.id.mainUserImageView)
    public void onUserImagePress() {
        materialDialog = new MaterialDialog.Builder(MainActivity.this)
                .title(getString(R.string.app_name))
                .content("Do you want to sign out?")
                .titleColor(Color.BLACK)
                .contentColorRes(R.color.colorTextDark)
                .positiveText("YES")
                .negativeText("NO")
                .positiveColorRes(R.color.colorPrimary)
                .negativeColorRes(R.color.colorAccent)
                .onPositive((dialog, which) -> {
                    firebaseAuth.signOut();
                    startActivity(new Intent(MainActivity.this, SignInActivity.class));
                    finish();
                }).show();
    }

    @OnClick(R.id.mainAlarmButton)
    public void onAlarmButtonPress() {
        if (mediaPlayer!=null && mediaPlayer.isPlaying()) mediaPlayer.stop();
        else {
            mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.alarm);
                mediaPlayer.setOnPreparedListener(mp -> mediaPlayer.start());
                mediaPlayer.prepare();
            } catch (Exception ignored) { }
        }
    }

    @Override
    protected void onDestroy() {
        unbinder.unbind();
        super.onDestroy();
    }
}
