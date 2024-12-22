package com.everon.recipeapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.everon.recipeapp.GlideApp;
import com.everon.recipeapp.R;
import com.everon.recipeapp.data.Recipe;
import com.everon.recipeapp.data.RecipeModel;
import com.google.firebase.firestore.DocumentReference;

import java.util.List;

/**
 * Recycler view row adapter for recipe objects in the Profile Activity.
 */
public class ProfileRecipeRowAdapter extends RecyclerView.Adapter<ProfileRecipeRowAdapter.ViewHolder> {
    private List<RecipeModel> recipeList;
    private Context context;
    private final OnRecipeModelClickListener onRecipeModelClickListener;

    public ProfileRecipeRowAdapter(List<RecipeModel> recipeList, Context context, OnRecipeModelClickListener onRecipeModelClickListener) {
        this.recipeList = recipeList;
        this.context = context;
        this.onRecipeModelClickListener = onRecipeModelClickListener;
    }

    /**
     * Create a row for a recipe object.
     * @param parent The ViewGroup into which the new View will be added after it is bound to
     * an adapter position.
     * @param viewType The view type of the new View.
     * @return Row to be displayed.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.profile_recipe_row, parent, false);
        return new ProfileRecipeRowAdapter.ViewHolder(view);
    }

    /**
     * Binds recipe data to view holder.
     * @param holder The ViewHolder which should be updated to represent the contents of the
     * item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(recipeList.get(position), context);
    }

    /**
     * Number of recipes owned by user being viewed.
     * @return Number of recipes owned by user being viewed.
     */
    @Override
    public int getItemCount() { return recipeList.size(); }


    /**
     * ViewHolder for displaying a recipe object in a recycler view.
     */
    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView recipeNameTextView;
        DocumentReference documentReference;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.profileRecipeRowImageView);
            recipeNameTextView = itemView.findViewById(R.id.profileRecipeRowNameTextView);
        }

        /**
         * Binds a recipe to the view holder.
         * @param recipeItem Recipe data.
         * @param context Current context.
         */
        public void bind(RecipeModel recipeItem, Context context) {
            Recipe recipe = recipeItem.getRecipe();
            documentReference = recipeItem.getDocumentReference();

            recipeNameTextView.setText(recipe.getName());

            GlideApp.with(context)
                    .load(recipe.getImageUrl())
                    .placeholder(R.drawable.default_recipe_image)
                    .into(imageView);

            imageView.setOnClickListener(view -> {
                onRecipeModelClickListener.onRecipeClick(documentReference);
            });
        }
    }
}
