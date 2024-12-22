package com.everon.recipeapp.nav;

import android.content.Intent;

import com.everon.recipeapp.CreateRecipe;
import com.everon.recipeapp.FeedActivity;
import com.everon.recipeapp.R;
import com.everon.recipeapp.RecipeActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Represents a navigation bar for the application.
 */
public class NavigationManager {
    public OnNavigationClickListener onNavigationClickListener;
    private BottomNavigationView navigationView;

    public NavigationManager(OnNavigationClickListener onNavigationClickListener,
                             BottomNavigationView navigationView) {
        this.onNavigationClickListener = onNavigationClickListener;
        this.navigationView = navigationView;
    }

    /**
     * Sets listeners for navigation buttons.
     */
    public void setNavigationView() {
        navigationView.setOnItemSelectedListener(item -> {
            switch(item.getItemId()) {
                case R.id.feedNavigation:
                    onNavigationClickListener.onFeedClick();
                    break;
                case R.id.newRecipeNavigation:
                    onNavigationClickListener.onRecipeCreateClick();
                    break;
                case R.id.recipeSearchNavigation:
                    onNavigationClickListener.onSearchClick();
                    break;
                case R.id.profileNavigation:
                    onNavigationClickListener.onProfileClick();
                    break;
                default:
                    return false;
            }
            return true;
        });


    }



}
