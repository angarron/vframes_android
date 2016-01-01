package com.angarron.vframes.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.angarron.vframes.R;
import com.angarron.vframes.resource_resolution.StringResolver;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import data.model.input.InputElement;
import data.model.move.IMoveListMove;
import data.model.move.MoveCategory;

/**
 * Created by andy on 12/20/15
 */
public class MovesRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_HEADER = 1;
    private static final int VIEW_TYPE_MOVE = 2;

    //This is the order in which moves will be displayed in the move list UI.
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

    public MovesRecyclerViewAdapter(Context context, Map<MoveCategory, List<IMoveListMove>> moves) {
        this.context = context;
        setupDisplayList(moves);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        switch (viewType) {
            case VIEW_TYPE_HEADER:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.moves_list_header, parent, false);
                return new HeaderItemViewHolder(v);
            case VIEW_TYPE_MOVE:
                v = LayoutInflater.from(parent.getContext()).inflate(R.layout.moves_list_item, parent, false);
                return new MoveItemViewHolder(v);
            default:
                throw new RuntimeException("unable to find ViewHolder for view type: " + viewType);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MoveItemViewHolder) {
            setupMoveItemViewHolder((MoveItemViewHolder) holder, position);
        } else if (holder instanceof HeaderItemViewHolder) {
            HeaderItemViewHolder headerItemViewHolder = (HeaderItemViewHolder) holder;
            MoveCategory moveCategory = (MoveCategory) displayList.get(position);
            headerItemViewHolder.label.setText(getHeaderString(moveCategory));
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (displayList.get(position) instanceof MoveCategory) {
            return VIEW_TYPE_HEADER;
        } else if (displayList.get(position) instanceof IMoveListMove) {
            return VIEW_TYPE_MOVE;
        } else {
            throw new RuntimeException("could not resolve Object to category: " + displayList.get(position).getClass().getSimpleName());
        }
    }

    @Override
    public int getItemCount() {
        return displayList.size();
    }

    private void setupDisplayList(Map<MoveCategory, List<IMoveListMove>> moves) {
        for (MoveCategory category : categoriesOrder) {
            if (moves.containsKey(category) && !moves.get(category).isEmpty()) {
                displayList.add(category);
                for (IMoveListMove move : moves.get(category)) {
                    displayList.add(move);
                }
            }
        }
    }

    private void setupMoveItemViewHolder(MoveItemViewHolder moveItemViewHolder, int position) {
        IMoveListMove move = (IMoveListMove) displayList.get(position);
        moveItemViewHolder.label.setText(move.getNameId());

        List<InputElement> input = move.getInput();
        moveItemViewHolder.input.removeAllViews();

        for (InputElement inputElement : input) {
            moveItemViewHolder.input.addView(getViewForInputElement(inputElement));
        }

        if (!TextUtils.isEmpty(move.getPretextId())) {
            moveItemViewHolder.pretext.setText(StringResolver.getStringId(move.getPretextId()));
            moveItemViewHolder.pretext.setVisibility(View.VISIBLE);
        } else {
            moveItemViewHolder.pretext.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(move.getPosttextId())) {
            moveItemViewHolder.posttext.setText(StringResolver.getStringId(move.getPosttextId()));
            moveItemViewHolder.posttext.setVisibility(View.VISIBLE);
        } else {
            moveItemViewHolder.posttext.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(move.getDescriptionId())) {
            moveItemViewHolder.description.setText(StringResolver.getStringId(move.getDescriptionId()));
            moveItemViewHolder.description.setVisibility(View.VISIBLE);
        } else {
            moveItemViewHolder.description.setVisibility(View.GONE);
        }
    }

    private View getViewForIndex(int i) {
        ImageView imageView = new ImageView(context);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setAdjustViewBounds(true);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, dpToPixels(35), 1);
        layoutParams.setMargins(dpToPixels(3), 0, dpToPixels(3), 0);
        imageView.setLayoutParams(layoutParams);

        switch (i) {
            case 0:
                imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.input_direction_down));
                break;
            case 1:
                imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.input_direction_down_back));
                break;
            case 2:
                imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.input_direction_back));
                break;
            case 3:
                imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.input_direction_up_back));
                break;
            case 4:
                imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.input_direction_up));
                break;
            case 5:
                imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.input_direction_up_forward));
                break;
            case 6:
                imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.input_direction_forward));
                break;
            case 7:
                imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.input_direction_down_forward));
                break;
            case 8:
                imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.input_direction_srk));
                break;
            case 9:
                imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.input_direction_hcb));
                break;
            case 10:
                imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.input_direction_qcf));
                break;
            case 11:
                imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.input_direction_spd));
                break;
        }
        return imageView;
    }

    private View getViewForInputElement(InputElement inputElement) {

        if (inputElement != InputElement.OR && inputElement != InputElement.NO_INPUT) {
            return getImageViewForInputElement(inputElement);
        } else {
            return getTextViewForInputElement(inputElement);
        }


    }

    private TextView getTextViewForInputElement(InputElement inputElement) {
        TextView textView = new TextView(context);

        //TODO: turn size into a dimension resolved at runtime
        int textSize = 10;

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(dpToPixels(3), 0, dpToPixels(3), 0);
        textView.setLayoutParams(layoutParams);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
        textView.setAllCaps(true);
        switch (inputElement) {
            case OR:
                textView.setText(R.string.or);
                break;
            case NO_INPUT:
                textView.setText(R.string.no_input);
                break;
            default:
                textView.setText("???");
        }

        return textView;
    }

    private ImageView getImageViewForInputElement(InputElement inputElement) {
        ImageView imageView = new ImageView(context);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setAdjustViewBounds(true);

        //TODO: turn size into a dimension resolved at runtime
        int size = 30;
        if (inputElement == InputElement.PLUS || inputElement == InputElement.ARROW) {
            size = 15;
        }

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, dpToPixels(size), 1);
        layoutParams.setMargins(dpToPixels(3), 0, dpToPixels(3), 0);
        imageView.setLayoutParams(layoutParams);

        switch(inputElement) {
            case HK:
                imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.input_icon_hk));
                break;
            case LP:
                imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.input_icon_lp));
                break;
            case MP:
                imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.input_icon_mp));
                break;
            case HP:
                imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.input_icon_hp));
                break;
            case LK:
                imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.input_icon_lk));
                break;
            case MK:
                imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.input_icon_mk));
                break;
            case PUNCH:
                imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.input_icon_generic_punch));
                break;
            case KICK:
                imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.input_icon_generic_kick));
                break;
            case ALL_PUNCHES:
                imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.input_icon_all_punches));
                break;
            case ALL_KICKS:
                imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.input_icon_all_kicks));
                break;
            case UP:
                imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.input_direction_up));
                break;
            case UP_FORWARD:
                imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.input_direction_up_forward));
                break;
            case FORWARD:
                imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.input_direction_forward));
                break;
            case DOWN_FORWARD:
                imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.input_direction_down_forward));
                break;
            case DOWN:
                imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.input_direction_down));
                break;
            case DOWN_BACK:
                imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.input_direction_down_back));
                break;
            case BACK:
                imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.input_direction_back));
                break;
            case UP_BACK:
                imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.input_direction_up_back));
                break;
            case QCF:
                imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.input_direction_qcf));
                break;
            case QCB:
                imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.input_icon_qcb));
                break;
            case SRK:
                imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.input_direction_srk));
                break;
            case SRK_BACK:
                imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.input_direction_srk_back));
                break;
            case HCF:
                imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.input_direction_hcf));
                break;
            case HCB:
                imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.input_direction_hcb));
                break;
            case CHARGE_BACK:
                imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.input_direction_charge_back));
                break;
            case CHARGE_DOWN:
                imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.input_direction_charge_down));
                break;
            case RELEASE_FORWARD:
                imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.input_direction_forward));
                break;
            case RELEASE_UP:
                imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.input_direction_up));
                break;
            case SPD:
                imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.input_direction_spd));
                break;
            case PLUS:
                imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.input_icon_plus));
                break;
            case ARROW:
                imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.input_icon_arrow));
                break;
            default:
                imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.input_icon_unknown));
        }
        return imageView;
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

        private TextView label;

        public HeaderItemViewHolder(View v) {
            super(v);
            label = (TextView) v.findViewById(R.id.label);
        }
    }

    private class MoveItemViewHolder extends RecyclerView.ViewHolder {

        private TextView label;
        private TextView pretext;
        private LinearLayout input;
        private TextView posttext;
        private TextView description;

        public MoveItemViewHolder(View v) {
            super(v);
            label = (TextView) v.findViewById(R.id.name);
            input = (LinearLayout) v.findViewById(R.id.input_container);
            pretext = (TextView) v.findViewById(R.id.pretext);
            posttext = (TextView) v.findViewById(R.id.posttext);
            description = (TextView) v.findViewById(R.id.description);
        }
    }

    //Stolen from http://stackoverflow.com/a/5960030
    private int dpToPixels(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

}
