package horzsolt.barcode.databinding;

import horzsolt.barcode.databinding.viewmodel.MainActivityViewModel;

public interface MainActivityContract {

    public interface Presenter {
        void onShowData(MainActivityViewModel sampleData);
    }

    public interface View {
        void showData(MainActivityViewModel sampleData);
    }
}
