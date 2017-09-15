package test.mvp;

/**
 * Created by xiaoxin on 17-9-12.
 * 实现p接口
 */

public class TestPresenter implements TestContract.Presenter {

    private TestContract.Model model;
    private TestContract.View view;

    public TestPresenter (TestContract.Model model, TestContract.View view) {
        this.model = model;
        this.view = view;
    }


    @Override
    public void getData() {
        view.showData(model.doData());
    }


}
