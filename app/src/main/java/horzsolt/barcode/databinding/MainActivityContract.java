package horzsolt.barcode.databinding;

public interface MainActivityContract {

    public interface Presenter {
        void onShowData(SampleData sampleData);
    }

    public interface View {
        void showData(SampleData sampleData);
    }
}
