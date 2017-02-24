package arun.com.chromer.activities.settings.lookandfeel;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.Preference;
import android.support.v7.preference.SwitchPreferenceCompat;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import arun.com.chromer.R;
import arun.com.chromer.activities.settings.Preferences;
import arun.com.chromer.activities.settings.preferences.BasePreferenceFragment;
import arun.com.chromer.activities.settings.widgets.ColorPreference;
import arun.com.chromer.activities.settings.widgets.IconListPreference;
import arun.com.chromer.activities.settings.widgets.IconSwitchPreference;
import arun.com.chromer.activities.settings.widgets.SubCheckBoxPreference;
import arun.com.chromer.shared.AppDetectService;
import arun.com.chromer.util.ServiceUtil;
import arun.com.chromer.util.Utils;

import static arun.com.chromer.activities.settings.Preferences.ANIMATION_SPEED;
import static arun.com.chromer.activities.settings.Preferences.ANIMATION_TYPE;
import static arun.com.chromer.activities.settings.Preferences.DYNAMIC_COLOR;
import static arun.com.chromer.activities.settings.Preferences.DYNAMIC_COLOR_APP;
import static arun.com.chromer.activities.settings.Preferences.DYNAMIC_COLOR_WEB;
import static arun.com.chromer.activities.settings.Preferences.PREFERRED_ACTION;
import static arun.com.chromer.activities.settings.Preferences.TOOLBAR_COLOR;
import static arun.com.chromer.activities.settings.Preferences.TOOLBAR_COLOR_PREF;
import static arun.com.chromer.shared.Constants.ACTION_TOOLBAR_COLOR_SET;
import static arun.com.chromer.shared.Constants.EXTRA_KEY_TOOLBAR_COLOR;
import static arun.com.chromer.shared.Constants.NO_COLOR;

public class PersonalizationPreferenceFragment extends BasePreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private final String[] SUMMARY_GROUP = new String[]{
            ANIMATION_SPEED,
            ANIMATION_TYPE,
            PREFERRED_ACTION,
            TOOLBAR_COLOR
    };

    private final IntentFilter toolbarColorSetFilter = new IntentFilter(ACTION_TOOLBAR_COLOR_SET);

    private IconSwitchPreference dynamicColorPreference;
    private IconSwitchPreference coloredToolbarPreference;
    private ColorPreference toolbarColorPreference;
    private IconListPreference animationSpeedPreference;
    private IconListPreference openingAnimationPreference;
    private IconListPreference preferredActionPreference;
    private SubCheckBoxPreference dynamicAppPreference;
    private SubCheckBoxPreference dynamicWebPreference;

    public PersonalizationPreferenceFragment() {
        // Required empty public constructor
    }

    public static PersonalizationPreferenceFragment newInstance() {
        final PersonalizationPreferenceFragment fragment = new PersonalizationPreferenceFragment();
        final Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.personalization_preferences);
        // init and set icon
        init();
        setupIcons();
        // setup preferences after creation
        setupToolbarColorPreference();
        setupDynamicToolbar();
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(colorSelectionReceiver, toolbarColorSetFilter);
        updatePreferenceStates(TOOLBAR_COLOR_PREF);
        updatePreferenceStates(ANIMATION_TYPE);
        updatePreferenceStates(DYNAMIC_COLOR);
        updatePreferenceSummary(SUMMARY_GROUP);
    }

    @Override
    public void onPause() {
        unregisterReceiver(colorSelectionReceiver);
        super.onPause();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updatePreferenceStates(key);
        updatePreferenceSummary(key);
    }

    private void init() {
        dynamicColorPreference = (IconSwitchPreference) findPreference(DYNAMIC_COLOR);
        coloredToolbarPreference = (IconSwitchPreference) findPreference(TOOLBAR_COLOR_PREF);
        toolbarColorPreference = (ColorPreference) findPreference(TOOLBAR_COLOR);
        preferredActionPreference = (IconListPreference) findPreference(PREFERRED_ACTION);
        openingAnimationPreference = (IconListPreference) findPreference(ANIMATION_TYPE);
        animationSpeedPreference = (IconListPreference) findPreference(ANIMATION_SPEED);
        dynamicAppPreference = (SubCheckBoxPreference) findPreference(DYNAMIC_COLOR_APP);
        dynamicWebPreference = (SubCheckBoxPreference) findPreference(DYNAMIC_COLOR_WEB);
    }

    private void setupIcons() {
        final Drawable palette = new IconicsDrawable(getActivity())
                .icon(CommunityMaterial.Icon.cmd_palette)
                .color(ContextCompat.getColor(getActivity(), R.color.material_dark_light))
                .sizeDp(24);
        toolbarColorPreference.setIcon(palette);
        coloredToolbarPreference.setIcon(palette);
        dynamicColorPreference.setIcon(new IconicsDrawable(getActivity())
                .icon(CommunityMaterial.Icon.cmd_format_color_fill)
                .color(ContextCompat.getColor(getActivity(), R.color.material_dark_light))
                .sizeDp(24));
        preferredActionPreference.setIcon(new IconicsDrawable(getActivity())
                .icon(CommunityMaterial.Icon.cmd_heart)
                .color(ContextCompat.getColor(getActivity(), R.color.material_dark_light))
                .sizeDp(24));
        openingAnimationPreference.setIcon(new IconicsDrawable(getActivity())
                .icon(CommunityMaterial.Icon.cmd_image_filter_none)
                .color(ContextCompat.getColor(getActivity(), R.color.material_dark_light))
                .sizeDp(24));
        animationSpeedPreference.setIcon(new IconicsDrawable(getActivity())
                .icon(CommunityMaterial.Icon.cmd_speedometer)
                .color(ContextCompat.getColor(getActivity(), R.color.material_dark_light))
                .sizeDp(24));
    }


    private void updatePreferenceStates(String key) {
        if (key.equalsIgnoreCase(TOOLBAR_COLOR_PREF)) {
            final boolean coloredToolbar = Preferences.get(getContext()).isColoredToolbar();
            enableDisablePreference(coloredToolbar, TOOLBAR_COLOR, DYNAMIC_COLOR);
            if (!coloredToolbar) {
                dynamicColorPreference.setChecked(false);
            }
        } else if (key.equalsIgnoreCase(ANIMATION_TYPE)) {
            final boolean animationEnabled = Preferences.get(getContext()).isAnimationEnabled();
            enableDisablePreference(animationEnabled, ANIMATION_SPEED);
        } else if (key.equalsIgnoreCase(DYNAMIC_COLOR)
                || key.equalsIgnoreCase(DYNAMIC_COLOR_APP)
                || key.equalsIgnoreCase(DYNAMIC_COLOR_WEB)) {
            final boolean dynamicColor = Preferences.get(getContext()).dynamicToolbar();
            if (!dynamicColor) {
                dynamicAppPreference.setVisible(false);
                dynamicWebPreference.setVisible(false);
                dynamicAppPreference.setChecked(false);
                dynamicWebPreference.setChecked(false);
            } else {
                dynamicAppPreference.setVisible(true);
                dynamicWebPreference.setVisible(true);
            }
            if (key.equalsIgnoreCase(DYNAMIC_COLOR_APP)) {
                if (Preferences.get(getContext()).dynamicToolbarOnApp() && !Utils.canReadUsageStats(getActivity())) {
                    requestUsagePermission();
                    handleAppDetectionService();
                }
            }
            updateDynamicSummary();
        }
    }

    private void updateDynamicSummary() {
        dynamicColorPreference.setSummary(Preferences.get(getContext()).dynamicColorSummary());
        boolean isColoredToolbar = Preferences.get(getContext()).isColoredToolbar();
        if (!isColoredToolbar) {
            dynamicColorPreference.setChecked(false);
        }
    }

    private void setupDynamicToolbar() {
        dynamicColorPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                final SwitchPreferenceCompat switchCompat = (SwitchPreferenceCompat) preference;
                final boolean isChecked = switchCompat.isChecked();
                if (isChecked) {
                    new MaterialDialog.Builder(getActivity())
                            .title(R.string.dynamic_toolbar_color)
                            .content(R.string.dynamic_toolbar_help)
                            .positiveText(android.R.string.ok)
                            .show();
                }
                updateDynamicSummary();
                return false;
            }
        });
    }


    private void setupToolbarColorPreference() {
        toolbarColorPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                int chosenColor = ((ColorPreference) preference).getColor();
                new ColorChooserDialog.Builder((LookAndFeelActivity) getActivity(), R.string.default_toolbar_color)
                        .titleSub(R.string.default_toolbar_color)
                        .allowUserColorInputAlpha(false)
                        .preselect(chosenColor)
                        .dynamicButtonColor(false)
                        .show();
                return true;
            }
        });
    }

    private void requestUsagePermission() {
        new MaterialDialog.Builder(getActivity())
                .title(R.string.permission_required)
                .content(R.string.usage_permission_explanation_appcolor)
                .positiveText(R.string.grant)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        getActivity().startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
                    }
                })
                .dismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        dynamicAppPreference.setChecked(Utils.canReadUsageStats(getContext()));
                    }
                })
                .show();
    }

    private void handleAppDetectionService() {
        if (ServiceUtil.isAppBasedToolbarColor(getActivity()) || Preferences.get(getContext()).blacklist())
            getActivity().startService(new Intent(getActivity().getApplicationContext(), AppDetectService.class));
        else
            getActivity().stopService(new Intent(getActivity().getApplicationContext(), AppDetectService.class));
    }

    private final BroadcastReceiver colorSelectionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final int selectedColor = intent.getIntExtra(EXTRA_KEY_TOOLBAR_COLOR, NO_COLOR);
            if (selectedColor != NO_COLOR) {
                final ColorPreference preference = (ColorPreference) findPreference(TOOLBAR_COLOR);
                if (preference != null) {
                    preference.setColor(selectedColor);
                }
            }
        }
    };
}