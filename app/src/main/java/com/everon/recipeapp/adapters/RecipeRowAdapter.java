package com.everon.recipeapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.everon.recipeapp.GlideApp;
import com.everon.recipeapp.R;
import com.everon.recipeapp.data.Recipe;
import com.everon.recipeapp.data.RecipeModel;
import com.google.firebase.firestore.DocumentReference;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Represents a recipe object in the Feed Activity recycler view.
 */
public class RecipeRowAdapter extends RecyclerView.Adapter<RecipeRowAdapter.ViewHolder> {
    private List<RecipeModel> recipeList;
    private Context context;
    private final OnRecipeModelClickListener onRecipeModelClickListener;

    public RecipeRowAdapter(List<RecipeModel> recipeList, Context context, OnRecipeModelClickListener onRecipeModelClickListener) {
        this.recipeList = recipeList;
        this.context = context;
        this.onRecipeModelClickListener = onRecipeModelClickListener;
    }

    /**
     * Creates the view holder for the recipe object.
     * @param parent The ViewGroup into which the new View will be added after it is bound to
     * an adapter position.
     * @param viewType The view type of the new View.
     * @return View holder for recipe object.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recipe_row, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Binds data to the view holder.
     * @param holder The ViewHolder which should be updated to represent the contents of the
     * item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(recipeList.get(position), context);
    }

    /**
     * Returns the number of recipes being displayed.
     * @return Number of recipes being displayed.
     */
    @Override
    public int getItemCount() { return recipeList.size(); }

    /**
     * The view holder implementation for recipes in the Feed Activity.
     */
    public class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView avatarView;
        ImageView imageView;
        TextView usernameTextView;
        TextView recipeNameTextView;
        DocumentReference documentReference;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarView = itemView.findViewById(R.id.recipeRowAvatarImageView);
            imageView = itemView.findViewById(R.id.recipeRowImageView);
            usernameTextView = itemView.findViewById(R.id.recipeRowUsernameTextView);
            recipeNameTextView = itemView.findViewById(R.id.recipeRowNameTextView);

        }

        /**
         * Binds a recipe to the recipe object view.
         * @param recipeItem Recipe object.
         * @param context Context.
         */
        public void bind(RecipeModel recipeItem, Context context) {
            // For future, set a onclick listener to avatarView to take the user to the creators
            // page, also set the avatar to the creator's avatar

            Recipe recipe = recipeItem.getRecipe();
            documentReference = recipeItem.getDocumentReference();
            String userUid = recipe.getUserId();
            String username = recipe.getUsername();

            usernameTextView.setText(recipe.getUsername());
            recipeNameTextView.setText(recipe.getName());

            GlideApp.with(context)
                    .load(recipe.getImageUrl())
                    .placeholder(R.drawable.default_recipe_image)
                    .into(imageView);

            imageView.setOnClickListener(view -> {
                onRecipeModelClickListener.onRecipeClick(documentReference);
            });

            usernameTextView.setOnClickListener(view -> {
                onRecipeModelClickListener.onRecipeProfileClick(userUid, username);
            });
        }
    }
}
