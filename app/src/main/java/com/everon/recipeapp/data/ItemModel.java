package com.everon.recipeapp.data;

/**
 * Model for steps and ingredients displayed in a recipe item recycler view.
 */
public class ItemModel {
    private int position;
    private String information;

    public ItemModel(int position, String information) {
        this.position = position;
        this.information = information;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getInformation() {
        return information;
    }

    public void setInformation(String information) {
        this.information = information;
    }
}
