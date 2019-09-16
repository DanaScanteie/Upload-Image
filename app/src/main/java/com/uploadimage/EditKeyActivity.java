package com.uploadimage;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class EditKeyActivity extends AppCompatActivity {

    private EditText editTextKey;
    private Button editButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_key);

        editTextKey = findViewById(R.id.editTextKey);
        editButton = findViewById(R.id.editButton);

        if (getIntent().getExtras() != null) {
            editTextKey.setText(getIntent().getExtras().getString("EXTRA_KEY"));
        }

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditKeyActivity.this, MainActivity.class);
                intent.putExtra("EXTRA", editTextKey.getText().toString());

                SharedPreferences prefs = getSharedPreferences("preferences", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("key", editTextKey.getText().toString());
                editor.apply();

                startActivity(intent);
            }
        });
    }
}
