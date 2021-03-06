package com.angarron.vframes.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.angarron.vframes.R;

import java.util.ArrayList;
import java.util.List;

import data.model.character.FrameData;
import data.model.move.IFrameDataEntry;
import data.model.move.IFrameDataEntryHolder;
import data.model.move.MoveCategory;

public class FrameDataRecyclerViewAdapter extends RecyclerView.Adapter {

    private static final int VIEW_TYPE_HEADER = 1;
    private static final int VIEW_TYPE_FRAME_DATA_ENTRY = 2;

    private static final int DISPLAY_CODE_MISSING_VALUE = 1001;
    private static final int DISPLAY_CODE_NOT_APPLICABLE = 1002;
    private static final int DISPLAY_CODE_KNOCKDOWN = 1003;
    private static final int DISPLAY_CODE_GUARD_BREAK = 1004;
    private static final int DISPLAY_CODE_CRUMPLE = 1005;

    //This is the order in which moves will be displayed in the frame data UI.
    //Categories which are missing for a particular character will not be displayed.
    private static MoveCategory[] categoriesOrder = {
            MoveCategory.NORMALS,
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
    private boolean showAlternate = false;

    public FrameDataRecyclerViewAdapter(Context context, FrameData frameData) {
        this.context = context;
        setupDisplayList(frameData);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        switch (viewType) {
            case VIEW_TYPE_HEADER:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_header, parent, false);
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
            if (position == 0) {
                headerItemViewHolder.setTopMargin(15);
            } else {
                headerItemViewHolder.setTopMargin(50);
            }
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
        } else if (displayList.get(position) instanceof IFrameDataEntryHolder) {
            //TODO: switch on the entry's type to decide the right view type
            return VIEW_TYPE_FRAME_DATA_ENTRY;
        } else {
            throw new RuntimeException("could not resolve Object to category: " + displayList.get(position).getClass().getSimpleName());
        }
    }

    public void setShowAlternate(boolean showAlternate) {
        if (this.showAlternate != showAlternate) {
            this.showAlternate = showAlternate;
            notifyDataSetChanged();
        }
    }

    private void setupFrameDataItemViewHolder(FrameDataItemViewHolder holder, int position) {
        IFrameDataEntryHolder frameDataEntryHolder = (IFrameDataEntryHolder) displayList.get(position);
        int distanceFromHeader = findDistanceFromHeader(position);

        boolean showingAlternateEntry = showAlternate && frameDataEntryHolder.hasAlternate();

        boolean shouldShade = (distanceFromHeader % 2 == 1);
        int backgroundColor = resolveBackgroundColor(shouldShade, showingAlternateEntry);

        if (showingAlternateEntry) {
            holder.setupView(frameDataEntryHolder.getAlternateFrameDataEntry(), backgroundColor);
        } else {
            holder.setupView(frameDataEntryHolder.getFrameDataEntry(), backgroundColor);
        }
    }

    private int resolveBackgroundColor(boolean shouldShade, boolean showingAlternateEntry) {
        if (showingAlternateEntry) {
            if (shouldShade) {
                return ContextCompat.getColor(context, R.color.frame_data_row_alternate_background_shaded);
            } else {
                return ContextCompat.getColor(context, R.color.frame_data_row_alternate_background_unshaded);
            }
        } else {
            if (shouldShade) {
                return ContextCompat.getColor(context, R.color.frame_data_row_background_shaded);
            } else {
                return Color.TRANSPARENT;
            }
        }
    }

    private int findDistanceFromHeader(int position) {
        int distanceFromHeader = 0;
        while(getItemViewType(position) != VIEW_TYPE_HEADER) {
            position--;
            distanceFromHeader++;
        }
        return distanceFromHeader;
    }

    private void setupDisplayList(FrameData frameData) {
        for (MoveCategory category : categoriesOrder) {
            if (frameData.hasCategory(category)) {
                List<IFrameDataEntryHolder> frameDataEntries = frameData.getFromCategory(category);
                if (!frameDataEntries.isEmpty()) {
                    displayList.add(category);
                    for (IFrameDataEntryHolder frameDataEntry : frameDataEntries) {
                        displayList.add(frameDataEntry);
                    }
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

        private void setTopMargin(int topMarginPx) {
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) rowContainer.getLayoutParams();
            params.setMargins(0, topMarginPx, 0, 0);
            rowContainer.setLayoutParams(params);
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
        private TextView damageValue;
        private TextView stunValue;
        private TextView description;

        private FrameDataItemViewHolder(View v) {
            super(v);
            rowContainer = v;
            moveName = (TextView) v.findViewById(R.id.name_textview);

            startupFrames = (TextView) v.findViewById(R.id.startup_textview);
            activeFrames = (TextView) v.findViewById(R.id.active_textview);
            recoveryFrames = (TextView) v.findViewById(R.id.recovery_textview);

            blockAdvantage = (TextView) v.findViewById(R.id.block_advantage_textview);
            hitAdvantage = (TextView) v.findViewById(R.id.hit_advantage_textview);

            damageValue = (TextView) v.findViewById(R.id.damage_textview);
            stunValue = (TextView) v.findViewById(R.id.stun_textview);

            description = (TextView) v.findViewById(R.id.description);
        }

        private void setupView(IFrameDataEntry frameDataEntry, int backgroundColor) {
            moveName.setText(frameDataEntry.getDisplayName());

            startupFrames.setText(getDisplayValue(frameDataEntry.getStartupFrames()));
            activeFrames.setText(getDisplayValue(frameDataEntry.getActiveFrames()));
            recoveryFrames.setText(getDisplayValue(frameDataEntry.getRecoveryFrames()));

            blockAdvantage.setText(getDisplayValue(frameDataEntry.getBlockAdvantage()));
            hitAdvantage.setText(getDisplayValue(frameDataEntry.getHitAdvantage()));

            if (damageValue != null) {
                damageValue.setText(getDisplayValue(frameDataEntry.getDamageValue()));
            }

            if (stunValue != null) {
                stunValue.setText(getDisplayValue(frameDataEntry.getStunValue()));
            }

            if (!TextUtils.isEmpty(frameDataEntry.getDescription())) {
                description.setText(frameDataEntry.getDescription());
                description.setVisibility(View.VISIBLE);
            } else {
                description.setVisibility(View.GONE);
            }

            rowContainer.setBackgroundColor(backgroundColor);
        }

        //Returns the String that should be displayed for a given
        //frame data entry. This will be the displayCode itself for a normal integer,
        //but certain codes will resolve to other Strings.
        private String getDisplayValue(int displayCode) {
            switch (displayCode) {
                case DISPLAY_CODE_MISSING_VALUE:
                case DISPLAY_CODE_NOT_APPLICABLE:
                    return context.getString(R.string.frame_data_not_applicable);
                case DISPLAY_CODE_KNOCKDOWN:
                    return context.getString(R.string.frame_data_knockdown);
                case DISPLAY_CODE_GUARD_BREAK:
                    return context.getString(R.string.frame_data_guard_break);
                case DISPLAY_CODE_CRUMPLE:
                    return context.getString(R.string.frame_data_crumple);
                default:
                    return String.valueOf(displayCode);
            }
        }
    }
}
