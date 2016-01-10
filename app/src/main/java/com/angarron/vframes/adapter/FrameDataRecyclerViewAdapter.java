package com.angarron.vframes.adapter;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.angarron.vframes.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import data.model.move.IFrameDataEntry;
import data.model.move.MoveCategory;

public class FrameDataRecyclerViewAdapter extends RecyclerView.Adapter {

    private static final int VIEW_TYPE_HEADER = 1;
    private static final int VIEW_TYPE_FRAME_DATA_ENTRY = 2;

    //This is the order in which moves will be displayed in the frame data UI.
    //Categories which are missing for a particular character will not be displayed.
    private static MoveCategory[] categoriesOrder = {
            MoveCategory.UNIQUE_MOVES,
            MoveCategory.SPECIALS,
            MoveCategory.VSKILL,
            MoveCategory.VTRIGGER,
            MoveCategory.VREVERSAL,
            MoveCategory.CRITICAL_ARTS,
            MoveCategory.THROWS
    };

    private Context context;
    private List<Object> displayList = new ArrayList<>();

    public FrameDataRecyclerViewAdapter(Context context, Map<MoveCategory, List<IFrameDataEntry>> frameData) {
        this.context = context;
        setupDisplayList(frameData);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        switch (viewType) {
            case VIEW_TYPE_HEADER:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.moves_list_header, parent, false);
                return new HeaderItemViewHolder(v);
            case VIEW_TYPE_FRAME_DATA_ENTRY:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.frame_data_row_layout, parent, false);
                return new FrameDataItemViewHolder(v);
            default:
                throw new RuntimeException("unable to find ViewHolder for view type: " + viewType);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof FrameDataItemViewHolder) {
            setupFrameDataItemViewHolder((FrameDataItemViewHolder) holder, position);
        } else if (holder instanceof HeaderItemViewHolder) {
            HeaderItemViewHolder headerItemViewHolder = (HeaderItemViewHolder) holder;
            MoveCategory moveCategory = (MoveCategory) displayList.get(position);
            headerItemViewHolder.setupHeader(getHeaderString(moveCategory));
        }
    }

    @Override
    public int getItemCount() {
        return displayList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (displayList.get(position) instanceof MoveCategory) {
            return VIEW_TYPE_HEADER;
        } else if (displayList.get(position) instanceof IFrameDataEntry) {
            //TODO: switch on the entry's type to decide the right view type
            return VIEW_TYPE_FRAME_DATA_ENTRY;
        } else {
            throw new RuntimeException("could not resolve Object to category: " + displayList.get(position).getClass().getSimpleName());
        }
    }

    private void setupFrameDataItemViewHolder(FrameDataItemViewHolder holder, int position) {
        IFrameDataEntry frameDataEntry = (IFrameDataEntry) displayList.get(position);
        int distanceFromHeader = findDistanceFromHeader(position);
        boolean shouldShade = (distanceFromHeader % 2 == 1);
        holder.setupView(frameDataEntry, shouldShade);
    }

    private int findDistanceFromHeader(int position) {
        int distanceFromHeader = 0;
        while(getItemViewType(position) != VIEW_TYPE_HEADER) {
            position--;
            distanceFromHeader++;
        }
        return distanceFromHeader;
    }

    private void setupDisplayList(Map<MoveCategory, List<IFrameDataEntry>> frameData) {
        for (MoveCategory category : categoriesOrder) {
            if (frameData.containsKey(category) && !frameData.get(category).isEmpty()) {
                displayList.add(category);
                for (IFrameDataEntry frameDataEntry : frameData.get(category)) {
                    displayList.add(frameDataEntry);
                }
            }
        }
    }

    private String getHeaderString(MoveCategory moveCategory) {
        switch (moveCategory) {
            case NORMALS:
                return context.getString(R.string.normals_header);
            case SPECIALS:
                return context.getString(R.string.specials_header);
            case VSKILL:
                return context.getString(R.string.vskill_header);
            case VTRIGGER:
                return context.getString(R.string.vtrigger_header);
            case VREVERSAL:
                return context.getString(R.string.vreversal_header);
            case CRITICAL_ARTS:
                return context.getString(R.string.critical_arts_header);
            case UNIQUE_MOVES:
                return context.getString(R.string.unique_attacks_header);
            case THROWS:
                return context.getString(R.string.throws_header);
            default:
                throw new RuntimeException("Could not resolve header for category: " + moveCategory);
        }
    }

    private class HeaderItemViewHolder extends RecyclerView.ViewHolder {

        private View rowContainer;
        private TextView label;

        private HeaderItemViewHolder(View v) {
            super(v);
            rowContainer = v;
            label = (TextView) v.findViewById(R.id.label);
        }

        private void setupHeader(String headerText) {
            label.setText(headerText);
            rowContainer.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    private class FrameDataItemViewHolder extends RecyclerView.ViewHolder {

        private View rowContainer;

        private TextView moveName;
        private TextView startupFrames;
        private TextView activeFrames;
        private TextView recoveryFrames;
        private TextView blockAdvantage;
        private TextView hitAdvantage;
//        private TextView damageValue;
//        private TextView stunValue;

        private FrameDataItemViewHolder(View v) {
            super(v);
            rowContainer = v;
            moveName = (TextView) v.findViewById(R.id.name_textview);

            startupFrames = (TextView) v.findViewById(R.id.startup_textview);
            activeFrames = (TextView) v.findViewById(R.id.active_textview);
            recoveryFrames = (TextView) v.findViewById(R.id.recovery_textview);

            blockAdvantage = (TextView) v.findViewById(R.id.block_advantage_textview);
            hitAdvantage = (TextView) v.findViewById(R.id.hit_advantage_textview);
        }

        private void setupView(IFrameDataEntry frameDataEntry, boolean shade) {
            moveName.setText(frameDataEntry.getDisplayName());

            startupFrames.setText(String.valueOf(frameDataEntry.getStartupFrames()));
            activeFrames.setText(String.valueOf(frameDataEntry.getActiveFrames()));
            recoveryFrames.setText(String.valueOf(frameDataEntry.getRecoveryFrames()));

            blockAdvantage.setText(String.valueOf(frameDataEntry.getBlockAdvantage()));
            hitAdvantage.setText(String.valueOf(frameDataEntry.getHitAdvantage()));

            if (shade) {
                rowContainer.setBackgroundColor(ContextCompat.getColor(context, R.color.frame_data_row_background_shaded));
            } else {
                rowContainer.setBackgroundColor(Color.TRANSPARENT);
            }
//
//            damageValue.setText(frameDataEntry.getDamageValue());
//            stunValue.setText(frameDataEntry.getStunValue());
        }
    }
}