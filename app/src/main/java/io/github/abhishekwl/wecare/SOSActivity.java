package io.github.abhishekwl.wecare;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SOSActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(getColor(R.color.colorAccent));
        setContentView(R.layout.activity_sos);
    }
}
