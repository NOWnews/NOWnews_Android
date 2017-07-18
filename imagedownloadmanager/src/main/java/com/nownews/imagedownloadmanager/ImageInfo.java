package com.nownews.imagedownloadmanager;

/**
 * Created by cindy on 2017/6/30.
 */

public class ImageInfo {

    private int imgW;
    private int imgH;
    private String imgUrl;

    public void setImgW(int imgW){
        this.imgW = imgW;
    }

    public int getImgW(){
        return imgW;
    }

    public void setImgH(int imgH){
        this.imgH = imgH;
    }

    public int getImgH(){
        return imgH;
    }

    public void setUrl(String imgUrl){
        this.imgUrl = imgUrl;
    }

    public String getImgUrl(){
        return imgUrl;
    }

    public int[] getImgWH(){
        int[] imgWH = new int[2];
        imgWH[0] = imgW;
        imgWH[1] = imgH;
        return imgWH;
    }

}
