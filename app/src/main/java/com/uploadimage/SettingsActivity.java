package com.uploadimage;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

public class SettingsActivity extends AppCompatActivity {

    private CheckBox checkBox;
    private Button doneButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        checkBox = findViewById(R.id.checkBox);
        doneButton = findViewById(R.id.doneButton);

        if (getIntent().getExtras() != null) {
            boolean checkBoxState = getIntent().getExtras().getBoolean("CHECKBOX_STATE");
            if(checkBoxState == true)
                checkBox.setChecked(true);
        }

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
                intent.putExtra("EXTRA_RESIZE", checkBox.isChecked());
                startActivity(intent);
            }
        });
    }
}