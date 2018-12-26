package horzsolt.barcode.databinding.viewmodel;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import horzsolt.barcode.BR;

public class MainActivityViewModel extends BaseObservable {

    private String sampleText;

    public MainActivityViewModel(String sampleText) {
        this.sampleText = sampleText;
    }

    @Bindable
    public String getSampleText() {
        return sampleText;
    }

    public  void setSampleText(String location){
        this.sampleText = location;
        notifyPropertyChanged(BR.sampleText);
    }

}
