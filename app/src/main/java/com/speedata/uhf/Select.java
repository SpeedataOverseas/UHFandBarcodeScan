package com.speedata.uhf;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;

public class Select extends AppCompatActivity {

    public ImageButton Scan;
    public ImageButton Rfid;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.select_page);
        Scan = findViewById(R.id.Scan);
        Rfid = findViewById(R.id.Rfid);
        Scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i1 = new Intent(Select.this,BarcodeDemoActivity.class);
                startActivity(i1);
            }
        });
        Rfid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i2 = new Intent(Select.this,HelloActivity.class);
                startActivity(i2);
            }
        });
    }
}
