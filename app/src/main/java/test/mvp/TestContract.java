package test.mvp;

/**
 * Created by xiaoxin on 17-9-12.
 */

public interface TestContract {

    interface View {
        //显示数据
        void showData(String str);
    }

    interface Presenter  {
        //通知model要获取数据并把model返回的数据交给view层
        void getData();
    }

    interface Model {
        //获取数据
        String doData();
    }
}
