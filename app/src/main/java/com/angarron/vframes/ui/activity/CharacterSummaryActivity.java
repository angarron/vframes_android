package com.angarron.vframes.ui.activity;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.Toast;

import com.angarron.vframes.BuildConfig;
import com.angarron.vframes.R;
import com.angarron.vframes.adapter.SummaryPagerAdapter;
import com.angarron.vframes.application.VFramesApplication;
import com.angarron.vframes.ui.fragment.FrameDataFragment;
import com.angarron.vframes.ui.fragment.MoveListFragment;
import com.angarron.vframes.util.FeedbackUtil;
import com.crashlytics.android.Crashlytics;

import java.util.List;
import java.util.Map;

import data.model.CharacterID;
import data.model.IDataModel;
import data.model.character.FrameData;
import data.model.character.SFCharacter;
import data.model.move.IMoveListEntry;
import data.model.move.MoveCategory;

public class CharacterSummaryActivity extends AppCompatActivity implements MoveListFragment.IMoveListFragmentHost, FrameDataFragment.IFrameDataFragmentHost {

    public static final String INTENT_EXTRA_TARGET_CHARACTER = "INTENT_EXTRA_TARGET_CHARACTER";
    private static final String ALTERNATE_FRAME_DATA_SELECTED = "ALTERNATE_FRAME_DATA_SELECTED";

    private CharacterID targetCharacter;
    private boolean alternateFrameDataSelected = false;

    private MenuItem alternateFrameDataItem;

    private FrameDataFragment frameDataFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_character_summary);

        postponeEnterTransition();

        try {
            targetCharacter = (CharacterID) getIntent().getSerializableExtra(INTENT_EXTRA_TARGET_CHARACTER);
        } catch (ClassCastException e) {
            Crashlytics.log(Log.ERROR, VFramesApplication.APP_LOGGING_TAG, "failed to parse intent in CharacterSummaryActivity");
            finish();
        }

        //Verify the data is still available. If not, send to splash screen.
        verifyDataAvailable();

        if (savedInstanceState != null && savedInstanceState.containsKey(ALTERNATE_FRAME_DATA_SELECTED)) {
            alternateFrameDataSelected = savedInstanceState.getBoolean(ALTERNATE_FRAME_DATA_SELECTED);
        }

        //Load the toolbar based on the target character
        setupToolbar();
        setupViewPager();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        verifyDataAvailable();
        alternateFrameDataItem = menu.findItem(R.id.action_alternate_frame_data_toggle);
        setAlternateFrameDataMenuState();
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_summary, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_feedback:
                FeedbackUtil.sendFeedback(this);
                return true;
            case android.R.id.home:
                supportFinishAfterTransition();
                return true;
            case R.id.action_alternate_frame_data_toggle:
                switch (targetCharacter) {
                    case MIKA:
                    case DHALSIM:
                    case RASHID:
                    case NASH:
                        throw new RuntimeException("toggled vtrigger for invalid character");
                    case KEN:
                        Toast.makeText(this, R.string.ken_vtrigger_framedata_not_ready, Toast.LENGTH_SHORT).show();
                        return true;
                    case LAURA:
                        Toast.makeText(this, R.string.laura_vtrigger_framedata_not_ready, Toast.LENGTH_SHORT).show();
                        return true;
                    default:
                        toggleFrameData();
                        return true;
                }
            default:
                throw new RuntimeException("invalid menu item clicked");
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(ALTERNATE_FRAME_DATA_SELECTED, alternateFrameDataSelected);
    }

    @Override
    public Map<MoveCategory, List<IMoveListEntry>> getMoveList() {
        VFramesApplication application = (VFramesApplication) getApplication();
        IDataModel dataModel = application.getDataModel();
        SFCharacter targetCharacterModel = dataModel.getCharactersModel().getCharacter(targetCharacter);
        return targetCharacterModel.getMoveList();
    }

    @Override
    public void registerFrameDataFragment(FrameDataFragment frameDataFragment) {
        this.frameDataFragment = frameDataFragment;
    }

    @Override
    public void unregisterFrameDataFragment() {
        frameDataFragment = null;
    }

    @Override
    public FrameData getFrameData() {
        VFramesApplication application = (VFramesApplication) getApplication();
        IDataModel dataModel = application.getDataModel();
        SFCharacter targetCharacterModel = dataModel.getCharactersModel().getCharacter(targetCharacter);
        return targetCharacterModel.getFrameData();
    }

    @Override
    public void onBackPressed() {
        supportFinishAfterTransition();
    }

    private void verifyDataAvailable() {
        VFramesApplication application = (VFramesApplication) getApplication();
        if (application.getDataModel() == null) {

            //If this is a release build, log this issue to Crashlytics.
            if (!BuildConfig.DEBUG) {
                Crashlytics.logException(new Throwable("Sending user to splash screen because data was unavailable"));
            }

            Intent startSplashIntent = new Intent(this, SplashActivity.class);
            startActivity(startSplashIntent);
            finish();
        }
    }

    private void setupViewPager() {
        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        PagerAdapter pagerAdapter = new SummaryPagerAdapter(this, getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);

        PagerTabStrip pagerTabStrip = (PagerTabStrip) findViewById(R.id.pager_tab_strip);
        pagerTabStrip.setTabIndicatorColor(ContextCompat.getColor(this, R.color.tab_indicator_color));
    }

    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.summary_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);

            String toolbarTitleFormat = getString(R.string.summary_toolbar_title);
            String characterName = getString(getNameResource());
            actionBar.setTitle(String.format(toolbarTitleFormat, characterName));
            actionBar.setBackgroundDrawable(getCharacterAccentColorDrawable());

            if (viewExists(R.id.summary_character_image)) {
                final ImageView summaryCharacterImage = (ImageView) findViewById(R.id.summary_character_image);
                ViewTreeObserver viewTreeObserver = summaryCharacterImage.getViewTreeObserver();
                viewTreeObserver.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        summaryCharacterImage.getViewTreeObserver().removeOnPreDrawListener(this);
                        startPostponedEnterTransition();
                        return true;
                    }
                });
                summaryCharacterImage.setImageDrawable(getDrawable(R.drawable.face_base));
            } else {
                startPostponedEnterTransition();
            }
        }
    }

    private void toggleFrameData() {
        alternateFrameDataSelected = !alternateFrameDataSelected;
        showAlternateFrameDataToast();
        setAlternateFrameDataMenuState();
        //get reference to frame data fragment and update it with new frame data
        if (frameDataFragment != null) {
            frameDataFragment.setShowAlternateFrameData(alternateFrameDataSelected);
        }
    }

    private void showAlternateFrameDataToast() {
        int stringRes;
        if (targetCharacter != CharacterID.CLAW) {
            stringRes = alternateFrameDataSelected ? R.string.showing_trigger_data : R.string.showing_non_trigger_data;
        } else {
            stringRes = alternateFrameDataSelected ? R.string.showing_claw_off_data : R.string.showing_claw_on_data;
        }
        Toast.makeText(this, stringRes, Toast.LENGTH_SHORT).show();
    }

    private void setAlternateFrameDataMenuState() {
        VFramesApplication application = (VFramesApplication) getApplication();
        IDataModel dataModel = application.getDataModel();
        SFCharacter targetCharacterModel = dataModel.getCharactersModel().getCharacter(targetCharacter);
        FrameData characterFrameData = targetCharacterModel.getFrameData();

        if (characterFrameData != null && characterFrameData.hasAlternateFrameData()) {
            alternateFrameDataItem.setIcon(resolveAlternateFrameDataMenuDrawable());
        } else {
            alternateFrameDataItem.setVisible(false);
        }
    }

    private int resolveAlternateFrameDataMenuDrawable() {
        if(targetCharacter == CharacterID.CLAW) {
            return alternateFrameDataSelected ? R.drawable.claw_off : R.drawable.claw_on;
        } else {
            return alternateFrameDataSelected ? R.drawable.fire_logo : R.drawable.logo;
        }
    }

    private boolean viewExists(int viewId) {
        return findViewById(viewId) != null;
    }

    private int getCharacterBannerResource() {
        switch(targetCharacter) {
            case RYU:
                return R.drawable.ryu_banner;
            case CHUN:
                return R.drawable.chun_banner;
            case DICTATOR:
                return R.drawable.dictator_banner;
            case BIRDIE:
                return R.drawable.birdie_banner;
            case NASH:
                return R.drawable.nash_banner;
            case CAMMY:
                return R.drawable.cammy_banner;
            case KEN:
                return R.drawable.ken_banner;
            case MIKA:
                return R.drawable.mika_banner;
            case NECALLI:
                return R.drawable.necalli_banner;
            case CLAW:
                return R.drawable.claw_banner;
            case RASHID:
                return R.drawable.rashid_banner;
            case KARIN:
                return R.drawable.karin_banner;
            case LAURA:
                return R.drawable.laura_banner;
            case DHALSIM:
                return R.drawable.dhalsim_banner;
            case ZANGIEF:
                return R.drawable.zangief_banner;
            case FANG:
                return R.drawable.fang_banner;
            default:
                throw new RuntimeException("unable to resolve character drawable: " + targetCharacter);
        }
    }

    private ColorDrawable getCharacterAccentColorDrawable() {
        switch(targetCharacter) {
            case RYU:
                return new ColorDrawable(ContextCompat.getColor(this, R.color.ryu_accent));
            case CHUN:
                return new ColorDrawable(ContextCompat.getColor(this, R.color.chun_accent));
            case DICTATOR:
                return new ColorDrawable(ContextCompat.getColor(this, R.color.dictator_accent));
            case BIRDIE:
                return new ColorDrawable(ContextCompat.getColor(this, R.color.birdie_accent));
            case NASH:
                return new ColorDrawable(ContextCompat.getColor(this, R.color.nash_accent));
            case CAMMY:
                return new ColorDrawable(ContextCompat.getColor(this, R.color.cammy_accent));
            case KEN:
                return new ColorDrawable(ContextCompat.getColor(this, R.color.ken_accent));
            case MIKA:
                return new ColorDrawable(ContextCompat.getColor(this, R.color.mika_accent));
            case NECALLI:
                return new ColorDrawable(ContextCompat.getColor(this, R.color.necalli_accent));
            case CLAW:
                return new ColorDrawable(ContextCompat.getColor(this, R.color.claw_accent));
            case RASHID:
                return new ColorDrawable(ContextCompat.getColor(this, R.color.rashid_accent));
            case KARIN:
                return new ColorDrawable(ContextCompat.getColor(this, R.color.karin_accent));
            case LAURA:
                return new ColorDrawable(ContextCompat.getColor(this, R.color.laura_accent));
            case DHALSIM:
                return new ColorDrawable(ContextCompat.getColor(this, R.color.dhalsim_accent));
            case ZANGIEF:
                return new ColorDrawable(ContextCompat.getColor(this, R.color.zangief_accent));
            case FANG:
                return new ColorDrawable(ContextCompat.getColor(this, R.color.fang_accent));
            default:
                throw new RuntimeException("unable to resolve character accent color drawable: " + targetCharacter);
        }
    }

    private int getNameResource() {
        switch(targetCharacter) {
            case RYU:
                return R.string.ryu_name;
            case CHUN:
                return R.string.chun_name;
            case DICTATOR:
                return R.string.dictator_name;
            case BIRDIE:
                return R.string.birdie_name;
            case NASH:
                return R.string.nash_name;
            case CAMMY:
                return R.string.cammy_name;
            case KEN:
                return R.string.ken_name;
            case MIKA:
                return R.string.mika_name;
            case NECALLI:
                return R.string.necalli_name;
            case CLAW:
                return R.string.claw_name;
            case RASHID:
                return R.string.rashid_name;
            case KARIN:
                return R.string.karin_name;
            case LAURA:
                return R.string.laura_name;
            case DHALSIM:
                return R.string.dhalsim_name;
            case ZANGIEF:
                return R.string.zangief_name;
            case FANG:
                return R.string.fang_name;
            default:
                throw new RuntimeException("unable to resolve character name: " + targetCharacter);
        }
    }
}
