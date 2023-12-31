package kr.jeet.edu.student.model.data;

public class MainMenuItemData {
    private int imgRes;
    private int titleRes;
    private String type;
    private String title;
    private boolean isNew;
    private Class<?> targetClass;

    public MainMenuItemData(String type, int img_res, int title_res, boolean isNew, Class<?> cls) {
        this.type = type;
        this.imgRes = img_res;
        this.titleRes = title_res;
        this.isNew = isNew;
        this.targetClass = cls;
    }

    public String getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getImgRes() {
        return imgRes;
    }

    public int getTitleRes() {
        return titleRes;
    }

    public void setIsNew(boolean isNew) {this.isNew = isNew;}
    public boolean getIsNew() {return isNew;}

    public Class<?> getTargetClass() {
        return targetClass;
    }
}
