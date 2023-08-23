package kr.jeet.edu.student.model.data;

public class MainMenuItemData {
    private int imgRes;
    private int titleRes;
    private Class<?> targetClass;

    public MainMenuItemData(int img_res, int title_res, Class<?> cls) {
        this.imgRes = img_res;
        this.titleRes = title_res;
        this.targetClass = cls;

    }

    public int getImgRes() {
        return imgRes;
    }

    public int getTitleRes() {
        return titleRes;
    }

    public Class<?> getTargetClass() {
        return targetClass;
    }
}
