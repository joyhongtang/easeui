package com.idwell.cloudframe.ui.tab.entity;

public class CategoryEntity {
    public int imageID;
    public  String desp;
    public CategoryEntity(int imageID, String desp){
        this.imageID = imageID;
        this.desp = desp;
    }
    public int getImageID() {
        return imageID;
    }

    public void setImageID(int imageID) {
        this.imageID = imageID;
    }

    public String getDesp() {
        return desp;
    }

    public void setDesp(String desp) {
        this.desp = desp;
    }
}
