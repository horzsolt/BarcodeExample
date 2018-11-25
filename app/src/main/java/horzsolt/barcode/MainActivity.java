package horzsolt.barcode;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import com.symbol.emdk.EMDKManager;
import com.symbol.emdk.EMDKResults;

public class MainActivity extends Activity implements BarcodeListener, StatusListener {

    private TextView textViewData = null;
    private TextView textViewStatus = null;

    private int dataLength = 0;
    private BarcodeUtility barcodeUtility;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        textViewData = (TextView)findViewById(R.id.textViewData);
        textViewStatus = (TextView)findViewById(R.id.textViewStatus);

        barcodeUtility = new BarcodeUtility();

		EMDKResults results = EMDKManager.getEMDKManager(getApplicationContext(), barcodeUtility);
		if (results.statusCode != EMDKResults.STATUS_CODE.SUCCESS) {
			return;
		}

		barcodeUtility.addBarcodeListener(this);
		barcodeUtility.addStatusListener(this);

        addStartScanButtonListener();
        addStopScanButtonListener();

        textViewData.setSelected(true);
        textViewData.setMovementMethod(new ScrollingMovementMethod());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        barcodeUtility.closeScanner();
    }

    @Override
    protected void onPause() {
        super.onPause();
        barcodeUtility.closeScanner();
    }

    @Override
    protected void onResume() {
        super.onResume();
        barcodeUtility.openScanner();
    }

    private void addStartScanButtonListener() {

        Button btnStartScan = (Button)findViewById(R.id.buttonStartScan);

        btnStartScan.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                barcodeUtility.startScan();
            }
        });
    }

    private void addStopScanButtonListener() {

        Button btnStopScan = (Button)findViewById(R.id.buttonStopScan);

        btnStopScan.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                barcodeUtility.stopScan();
            }
        });
    }

    @Override
    public void barcodeRead(String data) {
        new AsyncDataUpdate().execute(data);
    }

    @Override
    public void statusChanged(String statusData) {
        new AsyncStatusUpdate().execute(statusData);
    }

    private class AsyncDataUpdate extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            return params[0];
        }

        protected void onPostExecute(String result) {

            if (result != null) {
                if(dataLength ++ > 100) { //Clear the cache after 100 scans
                    textViewData.setText("");
                    dataLength = 0;
                }

                textViewData.append(result+"\n");


                ((View) findViewById(R.id.scrollView1)).post(new Runnable()
                {
                    public void run()
                    {
                        ((ScrollView) findViewById(R.id.scrollView1)).fullScroll(View.FOCUS_DOWN);
                    }
                });

            }
        }
    }

    private class AsyncStatusUpdate extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            return params[0];
        }

        @Override
        protected void onPostExecute(String result) {
            textViewStatus.setText("Status: " + result);
        }
    }
}

