package com.everon.recipeapp.adapters;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.everon.recipeapp.R;
import com.everon.recipeapp.data.ItemModel;
import com.everon.recipeapp.data.RecipeItemType;

import java.util.Collections;
import java.util.List;

/**
 * Recycler view row adapter for ingredients and steps/instructions.
 */
public class ItemCreationAdapter extends RecyclerView.Adapter<ItemCreationAdapter.ViewHolder>
        implements RecyclerRowMoveCallback.RecyclerViewRowTouchHelperContract{

    private List<ItemModel> itemList;
    private RecipeItemType itemType;
    private OnRecipeItemChange onRecipeItemChange;

    public ItemCreationAdapter(List<ItemModel> itemList, RecipeItemType itemType, OnRecipeItemChange onRecipeItemChange) {
        this.itemList = itemList;
        this.itemType = itemType;
        this.onRecipeItemChange = onRecipeItemChange;
    }

    /**
     * Creates the view holder for the recipe item type.
     * @param parent The ViewGroup into which the new View will be added after it is bound to
     * an adapter position.
     * @param viewType The view type of the new View.
     * @return View holder.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recipe_creation_item, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Binds the item data to the view holder.
     * @param holder The ViewHolder which should be updated to represent the contents of the
     * item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        itemList.get(position).setPosition(position + 1);
        holder.bind(itemList.get(position));
    }

    /**
     * Returns number of steps/instructions or ingredients.
     * @return Number of steps/instructions or ingredients.
     */
    @Override
    public int getItemCount() {
        return itemList.size();
    }

    /**
     * Responds to rows being moved by the user.
     * @param from Old position in recycler view.
     * @param to New position in recycler view.
     */
    @Override
    public void onRowMoved(int from, int to) {
        if(from < to) {
            for(int i = from; i<to; i++) {
                Collections.swap(itemList, i, i+1);
            }
        } else {
            for(int i = from; i > to; i--) {
                Collections.swap(itemList, i, i-1);
            }
        }
        notifyItemMoved(from, to);
    }

    /**
     * Responds to recipe items being deleted by user (user swipes right on item).
     * @param position Position of item.
     */
    @Override
    public void onRowDismiss(int position) {
        itemList.remove(position);
        onRecipeItemChange.updateItemList(itemList, itemType);
        notifyItemRemoved(position);
    }

    /**
     * Responds to rows being selected by user.
     * @param myViewHolder View holder.
     */
    @Override
    public void onRowSelected(ViewHolder myViewHolder) {
        myViewHolder.cardView.setCardBackgroundColor(Color.GRAY);
    }

    /**
     * Responds to rows being successfully deleted.
     * @param myViewHolder View holder.
     */
    @Override
    public void onRowClear(ViewHolder myViewHolder) {
        myViewHolder.cardView.setCardBackgroundColor(Color.WHITE);
        onRecipeItemChange.updateItemList(itemList, itemType);
        //notifyDataSetChanged();
    }

    /**
     * View holder class for recipe items in a recycler view.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView positionTextView, informationTextView;
        CardView cardView;

        /**
         * Creates the view for the item.
         * @param itemView View for the item.
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            positionTextView = itemView.findViewById(R.id.recipeItemPositionTextView);
            informationTextView = itemView.findViewById(R.id.recipeItemInformationTextView);
            cardView = itemView.findViewById(R.id.recipeItemCardView);
        }

        /**
         * Binds item data to the view.
         * @param item Item data to bind.
         */
        public void bind(ItemModel item) {
            String positionText = item.getPosition() + ".";
            positionTextView.setText(positionText);
            informationTextView.setText(item.getInformation());
        }
    }
}
