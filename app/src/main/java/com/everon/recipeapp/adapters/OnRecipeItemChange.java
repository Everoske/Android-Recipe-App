package com.everon.recipeapp.adapters;

import com.everon.recipeapp.data.ItemModel;
import com.everon.recipeapp.data.RecipeItemType;

import java.util.List;

/**
 * Interface for adding/editing/removing recipe steps and ingredients.
 */
public interface OnRecipeItemChange {
    /**
     * Update the items for a recipe.
     * @param itemList List of items.
     * @param type Ingredients or steps.
     */
    void updateItemList(List<ItemModel> itemList, RecipeItemType type);

    /**
     * Add a new item to a recipe.
     * @param item Item to add.
     * @param type Ingredients or steps.
     */
    void addItem(ItemModel item, RecipeItemType type);
}
