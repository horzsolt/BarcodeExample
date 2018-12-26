package horzsolt.barcode.databinding;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

public class MyClickHandlers {

    Context context;
    SampleData sampleData;

    public MyClickHandlers(Context context, SampleData sampleData) {
        this.context = context;
        this.sampleData = sampleData;
    }

    public void onClick(View view) {
        sampleData.setSampleText("onClick executed");
        Toast.makeText(context, "Followers is clicked!", Toast.LENGTH_SHORT).show();
    }
}
