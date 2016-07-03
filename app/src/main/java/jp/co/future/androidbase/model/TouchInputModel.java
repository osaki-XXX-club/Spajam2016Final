package jp.co.future.androidbase.model;

/**
 * Created by mano on 2016/07/03.
 */
public class TouchInputModel {

    /**
     * X座標
     */
    private float pointX;

    /**
     * Y座g票
     */
    private float pointY;

    /**
     * 指の数
     */
    private int pointCount;

    /**
     * 入力時刻
     */
    private long inputTime;

    /** 判定された文字 */
    private char charset;


    public float getPointX() {
        return pointX;
    }

    public void setPointX(float pointX) {
        this.pointX = pointX;
    }

    public float getPointY() {
        return pointY;
    }

    public void setPointY(float pointY) {
        this.pointY = pointY;
    }

    public int getPointCount() {
        return pointCount;
    }

    public void setPointCount(int pointCount) {
        this.pointCount = pointCount;
    }

    public long getInputTime() {
        return inputTime;
    }

    public void setInputTime(long inputTime) {
        this.inputTime = inputTime;
    }

    public char getCharset() {
        return charset;
    }

    public void setCharset(char charset) {
        this.charset = charset;
    }

    @Override
    public String toString() {
        return "TouchInputModel{" +
                "pointX=" + pointX +
                ", pointY=" + pointY +
                ", pointCount=" + pointCount +
                ", inputTime=" + inputTime +
                '}';
    }
}
