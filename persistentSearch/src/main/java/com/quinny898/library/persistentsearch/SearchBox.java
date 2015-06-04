package com.quinny898.library.persistentsearch;

import android.animation.LayoutTransition;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.balysv.materialmenu.MaterialMenuDrawable.IconState;
import com.balysv.materialmenu.MaterialMenuView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.codetail.animation.ReverseInterpolator;
import io.codetail.animation.SupportAnimator;
import io.codetail.animation.ViewAnimationUtils;

public class SearchBox extends RelativeLayout {

    public static final int VOICE_RECOGNITION_CODE = 1234;

    private MaterialMenuView materialMenu;
    private TextView logo;
    private EditText mEditText;
    private Context context;
    private ListView results;
    private ArrayList<SearchResult> resultList;
    private ArrayList<SearchResult> searchables;
    private boolean searchOpen;
    private boolean animate;
    private boolean isMic;
    private ImageView mic;
    private ImageView drawerLogo;
    private SearchListener listener;
    private MenuListener menuListener;
    private ProgressBar pb;
    private ArrayList<SearchResult> initialResults;
    private boolean searchWithoutSuggestions = Boolean.TRUE;

    private boolean isVoiceRecognitionIntentSupported;
    private Activity mContainerActivity;
    private Fragment mContainerFragment;
    private android.support.v4.app.Fragment mContainerSupportFragment;

    /**
     * Create a new searchbox
     *
     * @param context Context
     */
    public SearchBox(Context context) {
        this(context, null);
    }

    /**
     * Create a searchbox with params
     *
     * @param context Context
     * @param attrs   Attributes
     */
    public SearchBox(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * Create a searchbox with params and a style
     *
     * @param context  Context
     * @param attrs    Attributes
     * @param defStyle Style
     */
    public SearchBox(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        inflate(context, R.layout.searchbox, this);
        this.searchOpen = Boolean.FALSE;
        this.isMic = Boolean.TRUE;
        this.materialMenu = (MaterialMenuView) findViewById(R.id.material_menu_button);
        this.logo = (TextView) findViewById(R.id.logo);
        this.mEditText = (EditText) findViewById(R.id.search);
        this.results = (ListView) findViewById(R.id.results);
        this.context = context;
        this.pb = (ProgressBar) findViewById(R.id.pb);
        this.mic = (ImageView) findViewById(R.id.mic);
        this.drawerLogo = (ImageView) findViewById(R.id.drawer_logo);
        materialMenu.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (searchOpen) {
                    closeSearch();
                } else {
                    if (menuListener != null)
                        menuListener.onMenuClick();
                }
            }
        });
        resultList = new ArrayList<>();
        results.setAdapter(new SearchAdapter(context, resultList));
        animate = Boolean.TRUE;
        isVoiceRecognitionIntentSupported = isIntentAvailable(context, new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH));
        logo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleSearch();
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            RelativeLayout searchRoot = (RelativeLayout) findViewById(R.id.search_root);
            LayoutTransition lt = new LayoutTransition();
            lt.setDuration(100);
            searchRoot.setLayoutTransition(lt);
        }
        searchables = new ArrayList<>();
        mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    search(getSearchText());
                    return Boolean.TRUE;
                }
                return Boolean.FALSE;
            }
        });
        mEditText.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    if (TextUtils.isEmpty(getSearchText())) {
                        toggleSearch();
                    } else {
                        search(getSearchText());
                    }
                    return Boolean.TRUE;
                }
                return Boolean.FALSE;
            }
        });
        micStateChanged();
        mic.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                micClick();
            }
        });
        closeSearch();
    }

    private static boolean isIntentAvailable(Context context, Intent intent) {
        PackageManager mgr = context.getPackageManager();
        List<ResolveInfo> list = mgr.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }


    /**
     * Reveal the searchbox from a menu item. Specify the menu item id and pass the activity so the item can be found
     *
     * @param id       View ID
     * @param activity Activity
     */
    @SuppressWarnings("unused")
    public void revealFromMenuItem(int id, Activity activity) {
        setVisibility(View.VISIBLE);
        View menuButton = activity.findViewById(id);
        if (menuButton != null) {
            FrameLayout layout = (FrameLayout) activity.getWindow().getDecorView()
                    .findViewById(android.R.id.content);
            if (layout.findViewWithTag("searchBox") == null) {
                int[] location = new int[2];
                menuButton.getLocationInWindow(location);
                revealFrom(activity, this);
            }
        }
    }

    /**
     * Hide the searchbox using the circle animation. Can be called regardless of result list length
     *
     * @param activity Activity
     */
    @SuppressWarnings("unused")
    public void hideCircularly(final Activity activity) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        final FrameLayout layout = (FrameLayout) activity.getWindow().getDecorView()
                .findViewById(android.R.id.content);
        RelativeLayout root = (RelativeLayout) findViewById(R.id.search_root);
        display.getSize(size);
        Resources r = getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 96,
                r.getDisplayMetrics());
        int cx = layout.getLeft() + layout.getRight();
        int cy = layout.getTop();
        int finalRadius = (int) Math.max(layout.getWidth() * 1.5, px);

        SupportAnimator animator = ViewAnimationUtils.createCircularReveal(
                root, cx, cy, 0, finalRadius);
        animator.setInterpolator(new ReverseInterpolator());
        animator.setDuration(500);
        animator.start();
        animator.addListener(new SupportAnimator.AnimatorListener() {

            @Override
            public void onAnimationStart() {

            }

            @Override
            public void onAnimationEnd() {
                setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel() {

            }

            @Override
            public void onAnimationRepeat() {

            }

        });
    }

    /**
     * Toggle the searchbox's open/closed state manually
     */
    public void toggleSearch() {
        if (searchOpen) {
            closeSearch();
        } else {
            openSearch(getSearchables().isEmpty());
        }
    }

    /**
     * Hide the mEditText results manually
     */
    @SuppressWarnings("unused")
    public void hideResults() {
        this.results.setVisibility(View.GONE);
    }

    /**
     * Start the voice input activity manually
     */
    public void startVoiceRecognition() {
        if (isMicEnabled()) {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                    context.getString(R.string.speak_now));
            if (mContainerActivity != null) {
                mContainerActivity.startActivityForResult(intent, VOICE_RECOGNITION_CODE);
            } else if (mContainerFragment != null) {
                mContainerFragment.startActivityForResult(intent, VOICE_RECOGNITION_CODE);
            } else if (mContainerSupportFragment != null) {
                mContainerSupportFragment.startActivityForResult(intent, VOICE_RECOGNITION_CODE);
            }
        }
    }

    /**
     * Enable voice recognition for Activity
     *
     * @param context Context
     */
    @SuppressWarnings("unused")
    public void enableVoiceRecognition(Activity context) {
        mContainerActivity = context;
        micStateChanged();
    }

    /**
     * Enable voice recognition for Fragment
     *
     * @param context Fragment
     */
    @SuppressWarnings("unused")
    public void enableVoiceRecognition(Fragment context) {
        mContainerFragment = context;
        micStateChanged();
    }

    /**
     * Enable voice recognition for Support Fragment
     *
     * @param fragment Fragment
     */
    @SuppressWarnings("unused")
    public void enableVoiceRecognition(android.support.v4.app.Fragment fragment) {
        mContainerSupportFragment = fragment;
        micStateChanged();
    }


    private boolean isMicEnabled() {
        return isVoiceRecognitionIntentSupported && (mContainerActivity != null || mContainerSupportFragment != null || mContainerFragment != null);
    }

    private void micStateChanged() {
        mic.setVisibility((!isMic || isMicEnabled()) ? VISIBLE : INVISIBLE);
    }

    private void micStateChanged(boolean isMic) {
        this.isMic = isMic;
        micStateChanged();
    }

    /**
     * Set whether to show the progress bar spinner
     *
     * @param show Whether to show
     */
    @SuppressWarnings("unused")
    public void showLoading(boolean show) {
        if (show) {
            pb.setVisibility(View.VISIBLE);
            mic.setVisibility(View.INVISIBLE);
        } else {
            pb.setVisibility(View.INVISIBLE);
            mic.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Mandatory method for the onClick event
     */
    public void micClick() {
        if (!isMic) {
            setSearchText("");
        } else {
            startVoiceRecognition();
        }

    }

    /**
     * Populate the searchbox with words, in an arraylist. Used by the voice input
     *
     * @param matches Matches
     */
    @SuppressWarnings("unused")
    public void populateEditText(final ArrayList<String> matches) {
        toggleSearch();
        if (matches.isEmpty())
            return;
        final String text = matches.get(0).trim();
        setSearchText(text);
        search(text);
    }

    /**
     * Force an update of the results
     */
    public void updateResults() {
        resultList.clear();
        int count = 0;
        for (int x = 0; x < searchables.size(); x++) {
            if (searchables.get(x).mTitle.toLowerCase(Locale.ENGLISH).startsWith(
                    getSearchText().toLowerCase(Locale.ENGLISH))
                    && count < 5) {
                addResult(searchables.get(x));
                count++;
            }
        }
        if (resultList.size() == 0) {
            results.setVisibility(View.GONE);
        } else {
            results.setVisibility(View.VISIBLE);
        }

    }

    /**
     * Set the results that are shown (up to 5) when the searchbox is opened with no text
     *
     * @param results Results
     */
    @SuppressWarnings("unused")
    public void setInitialResults(ArrayList<SearchResult> results) {
        this.initialResults = results;
    }

    /**
     * Set whether the menu button should be shown. Particularly useful for apps that adapt to screen sizes
     *
     * @param visibility Whether to show
     */
    @SuppressWarnings("unused")
    public void setMenuVisibility(int visibility) {
        materialMenu.setVisibility(visibility);
    }

    /**
     * Set the menu listener
     *
     * @param menuListener MenuListener
     */
    @SuppressWarnings("unused")
    public void setMenuListener(MenuListener menuListener) {
        this.menuListener = menuListener;
    }

    /**
     * Set the mEditText listener
     *
     * @param listener SearchListener
     */
    @SuppressWarnings("unused")
    public void setSearchListener(SearchListener listener) {
        this.listener = listener;
    }

    /**
     * Set whether to mEditText without suggestions being available (default is Boolean.TRUE). Disable if your app only works with provided options
     *
     * @param state Whether to show
     */
    @SuppressWarnings("unused")
    public void setSearchWithoutSuggestions(boolean state) {
        this.searchWithoutSuggestions = state;
    }

    /**
     * Set the maximum length of the searchbox's edittext
     *
     * @param length Length
     */
    @SuppressWarnings("unused")
    public void setMaxLength(int length) {
        mEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(
                length)});
    }

    /**
     * Set the image drawable of the drawer mIcon logo (do not set if you have not hidden the menu mIcon)
     *
     * @param icon Icon
     */
    @SuppressWarnings("unused")
    public void setDrawerLogo(final Drawable icon) {
        drawerLogo.setImageDrawable(icon);
    }

    /**
     * Get the searchbox's current text
     *
     * @return Text
     */
    public String getSearchText() {
        return mEditText.getText().toString();
    }

    /**
     * Add a result
     *
     * @param result SearchResult
     */
    private void addResult(SearchResult result) {
        if (resultList != null && resultList.size() < 6) {
            resultList.add(result);
            ((SearchAdapter) results.getAdapter()).notifyDataSetChanged();
        }
    }

    /**
     * Clear all the results
     */
    @SuppressWarnings("unused")
    public void clearResults() {
        if (resultList != null) {
            resultList.clear();
            ((SearchAdapter) results.getAdapter()).notifyDataSetChanged();
        }
        listener.onSearchCleared();
    }

    /**
     * Return the number of results that are currently shown
     *
     * @return Number of Results
     */
    public int getNumberOfResults() {
        if (resultList != null) return resultList.size();
        return 0;
    }

    /**
     * Set the searchable items from a list (replaces any current items)
     */
    @SuppressWarnings("unused")
    public void setSearchables(ArrayList<SearchResult> searchables) {
        this.searchables = searchables;
    }

    /**
     * Add a searchable item
     *
     * @param searchable SearchResult
     */
    @SuppressWarnings("unused")
    public void addSearchable(SearchResult searchable) {
        if (!searchables.contains(searchable))
            searchables.add(searchable);
    }

    /**
     * Remove a searchable item
     *
     * @param searchable SearchResult
     */
    @SuppressWarnings("unused")
    public void removeSearchable(final SearchResult searchable) {
        if (searchables.contains(searchable))
            searchables.remove(new SearchResult(mEditText.getText().toString(), null));
    }

    /**
     * Clear all searchable items
     */
    @SuppressWarnings("unused")
    public void clearSearchables() {
        searchables.clear();
    }

    /**
     * Sets the input type for the edit text.
     *
     * @param method {@link Integer} The new input type.
     */
    @SuppressWarnings("unused")
    public void setInputType(final Integer method) {
        mEditText.setInputType(method);
    }

    /**
     * Get all searchable items
     *
     * @return ArrayList of SearchResults
     */
    @SuppressWarnings("unused")
    public ArrayList<SearchResult> getSearchables() {
        return searchables;
    }

    private void revealFrom(Activity a, SearchBox s) {
        FrameLayout layout = (FrameLayout) a.getWindow().getDecorView()
                .findViewById(android.R.id.content);
        RelativeLayout root = (RelativeLayout) s.findViewById(R.id.search_root);
        Resources r = getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 96,
                r.getDisplayMetrics());
        int cx = layout.getLeft() + layout.getRight();
        int cy = layout.getTop();

        int finalRadius = (int) Math.max(layout.getWidth(), px);

        SupportAnimator animator = ViewAnimationUtils.createCircularReveal(
                root, cx, cy, 0, finalRadius);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(500);
        animator.addListener(new SupportAnimator.AnimatorListener() {

            @Override
            public void onAnimationCancel() {

            }

            @Override
            public void onAnimationEnd() {
                toggleSearch();
            }

            @Override
            public void onAnimationRepeat() {

            }

            @Override
            public void onAnimationStart() {

            }

        });
        animator.start();
    }

    private void search(final SearchResult result) {
        if (!searchWithoutSuggestions && getNumberOfResults() == 0) return;
        setSearchText(result.mTitle);
        if (!TextUtils.isEmpty(getSearchText())) {
            if (listener != null)
                listener.onSearch(result.mTitle);
        }
        toggleSearch();
    }


    public void openSearch(final Boolean openKeyboard) {
        this.materialMenu.animateState(IconState.ARROW);
        this.logo.setVisibility(View.GONE);
        this.drawerLogo.setVisibility(View.GONE);
        mEditText.setVisibility(View.VISIBLE);
        this.results.setVisibility(View.VISIBLE);
        animate = Boolean.TRUE;
        results.setAdapter(new SearchAdapter(context, resultList));
        mEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    micStateChanged(Boolean.FALSE);
                    //noinspection deprecation
                    mic.setImageDrawable(context.getResources().getDrawable(
                            R.drawable.ic_clear));
                    updateResults();
                } else {
                    micStateChanged(Boolean.TRUE);
                    //noinspection deprecation
                    mic.setImageDrawable(context.getResources().getDrawable(
                            R.drawable.ic_action_mic));
                    if (initialResults != null) {
                        setInitialResults();
                    } else {
                        updateResults();
                    }
                }

                if (listener != null)
                    listener.onSearchTermChanged();
            }

            @Override
            public void beforeTextChanged(final CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void onTextChanged(final CharSequence s, int start, int before,
                                      int count) {

            }

        });
        results.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SearchResult result = resultList.get(position);
                search(result);
            }
        });
        if (initialResults != null) {
            setInitialResults();
        } else {
            updateResults();
        }

        if (listener != null)
            listener.onSearchOpened();
        if (getSearchText().length() > 0) {
            micStateChanged(Boolean.FALSE);
            //noinspection deprecation
            mic.setImageDrawable(context.getResources().getDrawable(
                    R.drawable.ic_clear));
        }
        if (openKeyboard) {
            InputMethodManager inputMethodManager = (InputMethodManager) context
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.toggleSoftInputFromWindow(
                    getApplicationWindowToken(),
                    InputMethodManager.SHOW_IMPLICIT, 0);
        }
        mEditText.post(new Runnable() {
            @Override
            public void run() {
                mEditText.requestFocus();
            }
        });
        searchOpen = Boolean.TRUE;
    }

    private void setInitialResults() {
        resultList.clear();
        int count = 0;
        for (int x = 0; x < initialResults.size(); x++) {
            if (count < 5) {
                addResult(initialResults.get(x));
                count++;
            }
        }
        if (resultList.size() == 0) {
            results.setVisibility(View.GONE);
        } else {
            results.setVisibility(View.VISIBLE);
        }
    }


    public void closeSearch() {
        this.materialMenu.animateState(IconState.BURGER);
        this.logo.setVisibility(View.VISIBLE);
        this.mEditText.setVisibility(View.GONE);
        logo.setHint(mEditText.getText());
        this.drawerLogo.setVisibility(View.VISIBLE);
        this.results.setVisibility(View.GONE);
        if (listener != null)
            listener.onSearchClosed();
        micStateChanged(Boolean.TRUE);
        //noinspection deprecation
        mic.setImageDrawable(context.getResources().getDrawable(
                R.drawable.ic_action_mic));
        InputMethodManager inputMethodManager = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getApplicationWindowToken(),
                0);
        searchOpen = Boolean.FALSE;
    }

    private void search(String text) {
        SearchResult option = new SearchResult(text, null);
        search(option);
    }

    public void mockSearch() {
        final String text = getSearchText();
        final SearchResult option = new SearchResult(text, null);
        if (!searchWithoutSuggestions && getNumberOfResults() == 0) return;
        setSearchText(option.mTitle);
        logo.setHint(option.mTitle);
    }

    public void setSearchText(final String text) {
        mEditText.setText(text);
        invalidate();
        requestLayout();
    }

    public Boolean isOpen() {
        return searchOpen;
    }

    public final ArrayList<String> getSearchableNames() {
        final ArrayList<String> ret = new ArrayList<>();

        for (final SearchResult x : searchables) {
            ret.add(x.mTitle);
        }

        return ret;
    }


    class SearchAdapter extends ArrayAdapter<SearchResult> {
        public SearchAdapter(Context context, ArrayList<SearchResult> options) {
            super(context, 0, options);
        }

        int count = 0;

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            SearchResult option = getItem(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(
                        R.layout.search_option, parent, Boolean.FALSE);

                if (animate) {
                    Animation anim = AnimationUtils.loadAnimation(context,
                            R.anim.anim_down);
                    anim.setDuration(400);
                    convertView.startAnimation(anim);
                    if (count == this.getCount()) {
                        animate = Boolean.FALSE;
                    }
                    count++;
                }
            }

            View border = convertView.findViewById(R.id.border);
            if (position == 0) {
                border.setVisibility(View.VISIBLE);
            } else {
                border.setVisibility(View.GONE);
            }
            final TextView title = (TextView) convertView
                    .findViewById(R.id.title);
            title.setText(option.mTitle);
            ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
            icon.setImageDrawable(option.mIcon);
            ImageView up = (ImageView) convertView.findViewById(R.id.up);
            up.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    setSearchText(title.getText().toString());
                    mEditText.setSelection(mEditText.getText().length());
                }
            });

            return convertView;
        }
    }

    public interface SearchListener {
        /**
         * Called when the searchbox is opened
         */
        void onSearchOpened();

        /**
         * Called when the clear button is pressed
         */
        void onSearchCleared();

        /**
         * Called when the searchbox is closed
         */
        void onSearchClosed();

        /**
         * Called when the searchbox's edittext changes
         */
        void onSearchTermChanged();

        /**
         * Called when a mEditText happens, with a result
         *
         * @param result {@link String} The result of the mEditText
         */
        void onSearch(final String result);
    }

    public interface MenuListener {
        /**
         * Called when the menu button is pressed
         */
        void onMenuClick();
    }
}
