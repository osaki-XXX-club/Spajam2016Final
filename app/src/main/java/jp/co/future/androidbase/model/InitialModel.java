package jp.co.future.androidbase.model;

/**
 * Created by itaru on 2016/07/03.
 */
public class InitialModel {
    long id;
    private String name;
    private int img;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getImg() {
        return img;
    }

    public void setImg(int img) {
        this.img = img;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
