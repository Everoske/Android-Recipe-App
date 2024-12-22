package com.everon.recipeapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.everon.recipeapp.adapters.OnRecipeItemChange;
import com.everon.recipeapp.data.ItemModel;
import com.everon.recipeapp.data.RecipeItemType;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

/**
 * Fragment to add steps and ingredients in the Create Recipe Activity.
 */
public class AddRecipeItemFragment extends BottomSheetDialogFragment {
    private EditText editText;
    private ImageButton saveButton;
    private OnRecipeItemChange onRecipeItemChange;
    private RecipeItemType type;

    /**
     * Instantiate a recipe item fragment.
     * @param onRecipeItemChange Interface for applying a recipe item change event.
     * @param type Indicates whether the item is a step or ingredient.
     */
    public AddRecipeItemFragment(OnRecipeItemChange onRecipeItemChange, RecipeItemType type) {
        this.onRecipeItemChange = onRecipeItemChange;
        this.type = type;
    }

    /**
     * Creates the recipe item fragment.
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return Recipe item fragment view.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout
        View view = inflater.inflate(R.layout.fragment_add_recipe_item, container, false);

        editText = view.findViewById(R.id.addRecipeItemEditText);
        saveButton = view.findViewById(R.id.addRecipeItemImageButton);

        return view;
    }

    /**
     * Set up the recipe item fragment.
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setHintText();
        saveButton.setOnClickListener(view12 -> {
            String itemText = editText.getText().toString().trim();
            if(!TextUtils.isEmpty(itemText)) {
                // Add Item
                ItemModel item = new ItemModel(0, itemText);
                onRecipeItemChange.addItem(item, type);
                editText.setText("");
                dismiss();
            } else {
                Toast.makeText(view.getContext(), "No Information Provided!",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Set hint based on whether the item is a step or ingredient.
     */
    public void setHintText() {
        switch (type) {
            case INGREDIENT:
                editText.setHint(R.string.ingredientsPlaceholderText);
                break;
            case INSTRUCTION:
                editText.setHint(R.string.instructionsPlaceholderText);
                break;
            default:
                break;
        }
    }
}