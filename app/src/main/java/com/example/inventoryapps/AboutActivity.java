package com.example.inventoryapps;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class AboutActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_about, findViewById(R.id.content_frame), true);
        setTitle("About");

        // Setup all expandable sections
        setupExpandableSection(R.id.aboutAppHeader, R.id.aboutAppContent, R.id.aboutAppArrow);
        setupExpandableSection(R.id.devCreditsHeader, R.id.devCreditsContent, R.id.devCreditsArrow);
        setupExpandableSection(R.id.contactHeader, R.id.contactContent, R.id.contactArrow);
        setupExpandableSection(R.id.howToUseHeader, R.id.howToUseContent, R.id.howToUseArrow);
    }

    private void setupExpandableSection(int headerId, int contentId, int arrowId) {
        View headerView = findViewById(headerId);
        final View contentView = findViewById(contentId);
        final ImageView arrowView = findViewById(arrowId);

        headerView.setOnClickListener(v -> {
            if (contentView.getVisibility() == View.VISIBLE) {
                contentView.setVisibility(View.GONE);
                arrowView.setRotation(0); // Rotate back up
            } else {
                contentView.setVisibility(View.VISIBLE);
                arrowView.setRotation(180); // Point down when expanded
            }
        });
    }
}
