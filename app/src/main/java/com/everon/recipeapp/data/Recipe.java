package com.everon.recipeapp.data;

import com.google.firebase.Timestamp;

import java.util.ArrayList;

/**
 * Represents an instance of a user-created recipe
 */
public class Recipe {
    private String name;
    private String creator;
    private String userId;
    private String imageUrl;
    private Timestamp timeAdded;
    private ArrayList<String> ingredients;
    private ArrayList<String> steps;
    private String username;
    private boolean privateRecipe;
    // Possibly Add Prep and Cook Times

    public Recipe() {}

    public Recipe(String name, String creator,
                  String userId, String imageUrl,
                  Timestamp timeAdded, ArrayList<String> ingredients,
                  ArrayList<String> steps, String username,
                  boolean privateRecipe) {
        this.name = name;
        this.creator = creator;
        this.userId = userId;
        this.imageUrl = imageUrl;
        this.timeAdded = timeAdded;
        this.ingredients = ingredients;
        this.steps = steps;
        this.username = username;
        this.privateRecipe = privateRecipe;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public ArrayList<String> getIngredients() {
        return ingredients;
    }

    public void setIngredients(ArrayList<String> ingredients) {
        this.ingredients = ingredients;
    }

    public ArrayList<String> getSteps() {
        return steps;
    }

    public void setSteps(ArrayList<String> steps) {
        this.steps = steps;
    }

    public Timestamp getTimeAdded() {
        return timeAdded;
    }

    public void setTimeAdded(Timestamp timeAdded) {
        this.timeAdded = timeAdded;
    }

    public String getUsername() {return username;}

    public void setUsername(String username) {this.username = username;}

    public boolean isPrivateRecipe() {return privateRecipe;}

    public void setPrivate(boolean privateRecipe) {this.privateRecipe = privateRecipe;}
}
