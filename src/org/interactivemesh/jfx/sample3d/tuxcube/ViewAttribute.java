package org.interactivemesh.jfx.sample3d.tuxcube;

import javafx.scene.transform.Translate;

import java.io.Serializable;

public class ViewAttribute implements Serializable {
    private static final long serialVersionUID = 1L;
//    Translate viewingTranslate = null;

    double tX;
    double tY;
    double tZ;

    double xx;
    double xy;
    double xz;
    double xt;
    double yx;
    double yy;
    double yz;
    double yt;
    double zx;
    double zy;
    double zz;
    double zt;

    public ViewAttribute() {
    }

    public ViewAttribute(double tX, double tY, double tZ,
                         double mxx, double mxy, double mxz, double tx,
                         double myx, double myy, double myz, double ty,
                         double mzx, double mzy, double mzz, double tz) {

        xx = mxx;
        xy = mxy;
        xz = mxz;
        xt = tx;

        yx = myx;
        yy = myy;
        yz = myz;
        yt = ty;

        zx = mzx;
        zy = mzy;
        zz = mzz;
        zt = tz;


        this.tX = tX;
        this.tY = tY;
        this.tZ = tZ;
    }
//    public ViewAttribute(Translate translate, double x, double y) {
//        this.viewingTranslate = translate;
//        this.startX = x;
//        this.startY=y;
//    }


    @Override
    public String toString() {
        return "ViewAttribute{" +
                "tX=" + tX +
                ", tY=" + tY +
                ", tZ=" + tZ +
                ", xx=" + xx +
                ", xy=" + xy +
                ", xz=" + xz +
                ", xt=" + xt +
                ", yx=" + yx +
                ", yy=" + yy +
                ", yz=" + yz +
                ", yt=" + yt +
                ", zx=" + zx +
                ", zy=" + zy +
                ", zz=" + zz +
                ", zt=" + zt +
                '}';
    }

    public double getXx() {
        return xx;
    }

    public void setXx(double xx) {
        this.xx = xx;
    }

    public double getXy() {
        return xy;
    }

    public void setXy(double xy) {
        this.xy = xy;
    }

    public double getXz() {
        return xz;
    }

    public void setXz(double xz) {
        this.xz = xz;
    }

    public double getXt() {
        return xt;
    }

    public void setXt(double xt) {
        this.xt = xt;
    }

    public double getYx() {
        return yx;
    }

    public void setYx(double yx) {
        this.yx = yx;
    }

    public double getYy() {
        return yy;
    }

    public void setYy(double yy) {
        this.yy = yy;
    }

    public double getYz() {
        return yz;
    }

    public void setYz(double yz) {
        this.yz = yz;
    }

    public double getYt() {
        return yt;
    }

    public void setYt(double yt) {
        this.yt = yt;
    }

    public double getZx() {
        return zx;
    }

    public void setZx(double zx) {
        this.zx = zx;
    }

    public double getZy() {
        return zy;
    }

    public void setZy(double zy) {
        this.zy = zy;
    }

    public double getZz() {
        return zz;
    }

    public void setZz(double zz) {
        this.zz = zz;
    }

    public double getZt() {
        return zt;
    }

    public void setZt(double zt) {
        this.zt = zt;
    }

    public double gettX() {
        return tX;
    }

    public void settX(double tX) {
        this.tX = tX;
    }

    public double gettY() {
        return tY;
    }

    public void settY(double tY) {
        this.tY = tY;
    }

    public double gettZ() {
        return tZ;
    }

    public void settZ(double tZ) {
        this.tZ = tZ;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

//    public Translate getViewingTranslate() {
//        return viewingTranslate;
//    }
//
//    public void setViewingTranslate(Translate viewingTranslate) {
//        this.viewingTranslate = viewingTranslate;
//    }

}
