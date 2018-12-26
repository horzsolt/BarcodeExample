package horzsolt.barcode.databinding.handler;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

import horzsolt.barcode.databinding.viewmodel.MainActivityViewModel;

public class MainActivityHandler {

    Context context;
    MainActivityViewModel sampleData;

    public MainActivityHandler(Context context, MainActivityViewModel sampleData) {
        this.context = context;
        this.sampleData = sampleData;
    }

    public void onClick(View view) {
        sampleData.setSampleText("onClick executed");
        Toast.makeText(context, "Followers is clicked!", Toast.LENGTH_SHORT).show();
    }
}
