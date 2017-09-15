package test.mvp;

/**
 * Created by xiaoxin on 17-9-12.
 * 实现m接口
 */

public class TestModel implements TestContract.Model {
    private static TestModel model;

    public static synchronized TestModel getInstance() {
        if (model == null) {
            model = new TestModel();
        }
        return model;
    }

    @Override
    public String doData() {
        return "mvp架构";
    }
}