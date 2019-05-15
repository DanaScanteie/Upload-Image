package com.uploadimage;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

public class RemoveActivity extends AppCompatActivity {
    private static final String FILE_NAME = "uploadFile.txt";

    private EditText editTextName;
    private Button removeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remove);

        editTextName = (EditText) findViewById(R.id.editTextName);
        removeButton = (Button) findViewById(R.id.removeButton);

        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RemoveActivity.this, MainActivity.class);
                intent.putExtra("EXTRA", editTextName.getText().toString());
                Boolean removed = false;

                try {
                    FileInputStream fis = openFileInput(FILE_NAME);
                    InputStreamReader isr = new InputStreamReader(fis);
                    BufferedReader br = new BufferedReader(isr);
                    List<String> lines = new LinkedList<>();
                    String line;

                    while ((line = br.readLine()) != null) {
                        if(!line.contains(editTextName.getText().toString())) {
                            lines.add(line);
                        }
                        else {
                            Toast.makeText(getApplicationContext(), editTextName.getText().toString() + " has been removed.",
                                    Toast.LENGTH_LONG).show();
                            removed = true;
                        }
                    }
                    if(removed == false) {
                        Toast.makeText(getApplicationContext(), editTextName.getText().toString() + " doesn't exist",
                                Toast.LENGTH_LONG).show();
                    }

                    br.close();

                    deleteFile(FILE_NAME);
                    FileOutputStream  fos = openFileOutput(FILE_NAME, MODE_APPEND);
                    for (String l : lines) {
                        fos.write(l.getBytes());
                        fos.write("\n".getBytes());
                    }
                    fos.close();

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                startActivity(intent);
            }
        });
    }
}
