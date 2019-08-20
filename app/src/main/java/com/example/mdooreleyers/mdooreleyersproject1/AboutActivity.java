package com.example.mdooreleyers.mdooreleyersproject1;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

public class AboutActivity extends AppCompatActivity {

    TextView info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        info = (TextView)findViewById(R.id.aboutInfo);
        info.setText(Html.fromHtml(getString(R.string.about_info), Html.FROM_HTML_MODE_LEGACY));
    }

    public void backClick(View view) {
        finish();
    }
}
