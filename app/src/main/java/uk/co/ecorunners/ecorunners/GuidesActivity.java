package uk.co.ecorunners.ecorunners;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class GuidesActivity extends AppCompatActivity {

    private TextView guidesContactDetails, couriersRates, shopsGuides,spareBicycleRecord;
    private Button homeBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guides);


        guidesContactDetails = (TextView)findViewById(R.id.courierDetails);

        couriersRates = (TextView) findViewById(R.id.courierRates);

        shopsGuides = (TextView) findViewById(R.id.shopsGuides);

        spareBicycleRecord = (TextView)findViewById(R.id.spareBicycleRecord);

        homeBtn = (Button) findViewById(R.id.homeBtn);

        guidesContactDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(GuidesActivity.this, CouriersContactDetailsActivity.class);

                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                startActivity(intent);
            }
        });

        guidesContactDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(GuidesActivity.this, CouriersContactDetailsActivity.class);

                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                startActivity(intent);

            }
        });

        couriersRates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(GuidesActivity.this, CouriersRatesActivity.class);

                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                startActivity(intent);
            }
        });

        shopsGuides.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(GuidesActivity.this, ShopsGuidesActivity.class);

                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                startActivity(intent);

            }
        });

        spareBicycleRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(GuidesActivity.this, SpareBicycleRecordActivity.class);

                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                startActivity(intent);
            }
        });

        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(GuidesActivity.this, MainActivity.class);

                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                startActivity(intent);
            }
        });

    }
}
