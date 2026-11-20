package com.jbrowser.tv;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.mozilla.geckoview.AllowOrDeny;
import org.mozilla.geckoview.GeckoResult;
import org.mozilla.geckoview.GeckoRuntime;
import org.mozilla.geckoview.GeckoRuntimeSettings;
import org.mozilla.geckoview.GeckoSession;
import org.mozilla.geckoview.GeckoSessionSettings;
import org.mozilla.geckoview.GeckoView;
import org.mozilla.geckoview.WebExtension;
import org.mozilla.geckoview.WebExtensionController;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** JBrowser 2.0: GeckoView-powered, desktop-first browser designed for Android TV. */
public class MainActivity extends Activity {
    private static final int VERSION_CODE = 7;
    private static final String VERSION_NAME = "2.0.0";
    private static final int MAX_TABS = 6;
    private static final int REQUEST_EXTENSION_FILE = 2401;

    private static final int BG = Color.rgb(8, 13, 25);
    private static final int PANEL = Color.rgb(17, 28, 45);
    private static final int PANEL_HI = Color.rgb(27, 43, 65);
    private static final int ACCENT = Color.rgb(86, 224, 205);
    private static final int TEXT = Color.rgb(241, 247, 255);
    private static final int MUTED = Color.rgb(151, 171, 194);

    private static GeckoRuntime runtime;

    private final ArrayList<Tab> tabs = new ArrayList<>();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private BookmarkStore bookmarkStore;

    private LinearLayout root;
    private LinearLayout topBar;
    private LinearLayout tabStrip;
    private HorizontalScrollView tabScroller;
    private FrameLayout pageHost;
    private GeckoView geckoView;
    private PointerOverlay pointerOverlay;
    private View homeView;
    private EditText addressBar;
    private TextView statusView;
    private TextView pointerButton;
    private TextView desktopButton;
    private TextView tabButton;
    private TextView backButton;
    private TextView forwardButton;
    private TextView reloadButton;

    private int activeTab = -1;
    private boolean pointerMode = true;
    private boolean desktopMode = true;
    private float pointerX;
    private float pointerY;
    private String pendingInstallFileName;

    private static final class Tab {
        final GeckoSession session;
        final boolean privateTab;
        String url = "";
        String title = "New tab";
        boolean canGoBack;
        boolean canGoForward;
        boolean loading;

        Tab(GeckoSession session, boolean privateTab) {
            this.session = session;
            this.privateTab = privateTab;
        }
    }

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        applyImmersive();

        bookmarkStore = new BookmarkStore(this);
        desktopMode = getPreferences(MODE_PRIVATE).getBoolean("desktop_mode", true);
        pointerMode = getPreferences(MODE_PRIVATE).getBoolean("pointer_mode", true);
        ensureRuntime();
        buildBrowserUi();

        Tab first = createTab(false);
        activateTab(tabs.indexOf(first));

        Uri incoming = getIntent() == null ? null : getIntent().getData();
        if (incoming != null) navigate(incoming.toString());
        else showHome();
    }

    private void ensureRuntime() {
        if (runtime != null) return;
        GeckoRuntimeSettings settings = new GeckoRuntimeSettings.Builder()
                .extensionsProcessEnabled(true)
                .extensionsWebAPIEnabled(true)
                .forceUserScalableEnabled(true)
                .build();
        runtime = GeckoRuntime.create(getApplicationContext(), settings);
        runtime.getWebExtensionController().setPromptDelegate(new WebExtensionController.PromptDelegate() {
            @Override
            public GeckoResult<WebExtension.PermissionPromptResponse> onInstallPromptRequest(
                    WebExtension extension, String[] permissions, String[] origins, String[] dataPermissions) {
                return promptExtensionPermissions(extension, permissions, origins, dataPermissions);
            }

            @Override
            public GeckoResult<AllowOrDeny> onOptionalPrompt(WebExtension extension, String[] permissions,
                                                              String[] origins, String[] dataPermissions) {
                return promptOptionalPermissions(extension, permissions, origins, dataPermissions);
            }

            @Override
            public GeckoResult<AllowOrDeny> onUpdatePrompt(WebExtension extension, String[] permissions,
                                                            String[] origins, String[] dataPermissions) {
                return promptUpdatePermissions(extension, permissions, origins, dataPermissions);
            }
        });
    }

    private void applyImmersive() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    private void buildBrowserUi() {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);
        root.setPadding(dp(20), dp(9), dp(20), dp(7));

        topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        root.addView(topBar, new LinearLayout.LayoutParams(-1, dp(58)));

        TextView brand = label("J", 28, ACCENT, true);
        brand.setGravity(Gravity.CENTER);
        brand.setBackground(round(PANEL_HI, 18));
        topBar.addView(brand, new LinearLayout.LayoutParams(dp(48), dp(44)));

        TextView wordmark = label("JBrowser", 22, TEXT, true);
        wordmark.setPadding(dp(12), 0, dp(14), 0);
        topBar.addView(wordmark, new LinearLayout.LayoutParams(dp(132), -2));

        backButton = toolbarButton("‹", "Back", this::goBack);
        topBar.addView(backButton);
        forwardButton = toolbarButton("›", "Forward", this::goForward);
        topBar.addView(forwardButton);
        reloadButton = toolbarButton("↻", "Reload", this::reloadPage);
        topBar.addView(reloadButton);
        TextView homeButton = toolbarButton("⌂", "Home", this::showHome);
        topBar.addView(homeButton);

        addressBar = new EditText(this);
        addressBar.setSingleLine(true);
        addressBar.setTextSize(16);
        addressBar.setTextColor(TEXT);
        addressBar.setHintTextColor(MUTED);
        addressBar.setHint("Search or enter a website address");
        addressBar.setSelectAllOnFocus(true);
        addressBar.setImeOptions(EditorInfo.IME_ACTION_GO);
        addressBar.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        addressBar.setPadding(dp(16), 0, dp(16), 0);
        addressBar.setBackground(round(PANEL_HI, 15));
        addressBar.setSingleLine(true);
        addressBar.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_DONE
                    || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                    && event.getAction() == KeyEvent.ACTION_DOWN)) {
                navigate(addressBar.getText().toString());
                hideKeyboard(addressBar);
                return true;
            }
            return false;
        });
        LinearLayout.LayoutParams addressParams = new LinearLayout.LayoutParams(0, dp(44), 1f);
        addressParams.setMargins(dp(8), 0, dp(8), 0);
        topBar.addView(addressBar, addressParams);

        tabButton = toolbarButton("▣", "Tabs", this::showTabs);
        topBar.addView(tabButton);
        pointerButton = toolbarButton("⌖", "Pointer", this::togglePointerMode);
        topBar.addView(pointerButton);
        desktopButton = toolbarButton("PC", "Desktop mode", this::toggleDesktopMode);
        topBar.addView(desktopButton);
        TextView extensions = toolbarButton("🧩", "Extensions", this::showExtensions);
        topBar.addView(extensions);
        TextView bookmark = toolbarButton("☆", "Bookmark", this::addBookmark);
        topBar.addView(bookmark);
        TextView menu = toolbarButton("⋯", "More", this::showMoreMenu);
        topBar.addView(menu);

        tabScroller = new HorizontalScrollView(this);
        tabScroller.setHorizontalScrollBarEnabled(false);
        tabStrip = new LinearLayout(this);
        tabStrip.setOrientation(LinearLayout.HORIZONTAL);
        tabStrip.setGravity(Gravity.CENTER_VERTICAL);
        tabScroller.addView(tabStrip, new HorizontalScrollView.LayoutParams(-2, -1));
        root.addView(tabScroller, new LinearLayout.LayoutParams(-1, dp(44)));

        statusView = label("Ready · Desktop browsing enabled", 12, MUTED, false);
        statusView.setGravity(Gravity.CENTER_VERTICAL);
        statusView.setPadding(dp(3), 0, 0, dp(3));
        root.addView(statusView, new LinearLayout.LayoutParams(-1, dp(24)));

        pageHost = new FrameLayout(this);
        pageHost.setBackgroundColor(Color.rgb(10, 17, 29));
        root.addView(pageHost, new LinearLayout.LayoutParams(-1, 0, 1f));

        geckoView = new GeckoView(this);
        geckoView.setFocusable(true);
        geckoView.setFocusableInTouchMode(true);
        pageHost.addView(geckoView, new FrameLayout.LayoutParams(-1, -1));

        pointerOverlay = new PointerOverlay(this);
        pointerOverlay.setClickable(false);
        pointerOverlay.setFocusable(false);
        pageHost.addView(pointerOverlay, new FrameLayout.LayoutParams(-1, -1));
        pointerOverlay.setVisibility(View.GONE);

        setContentView(root);
        updateToolbar();
    }

    private TextView toolbarButton(String caption, String description, Runnable action) {
        TextView button = label(caption, 18, TEXT, true);
        button.setGravity(Gravity.CENTER);
        button.setContentDescription(description);
        button.setFocusable(true);
        button.setBackground(focusableRound(PANEL, PANEL_HI, 12));
        button.setOnClickListener(v -> action.run());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(43), dp(42));
        lp.setMargins(dp(3), 0, dp(3), 0);
        button.setLayoutParams(lp);
        return button;
    }

    private Tab createTab(boolean privateTab) {
        if (tabs.size() >= MAX_TABS) {
            toast("JBrowser supports up to " + MAX_TABS + " open tabs");
            return null;
        }
        GeckoSessionSettings sessionSettings = new GeckoSessionSettings.Builder()
                .userAgentMode(desktopMode ? GeckoSessionSettings.USER_AGENT_MODE_DESKTOP
                        : GeckoSessionSettings.USER_AGENT_MODE_MOBILE)
                .viewportMode(desktopMode ? GeckoSessionSettings.VIEWPORT_MODE_DESKTOP
                        : GeckoSessionSettings.VIEWPORT_MODE_MOBILE)
                .usePrivateMode(privateTab)
                .suspendMediaWhenInactive(true)
                .build();
        GeckoSession session = new GeckoSession(sessionSettings);
        Tab tab = new Tab(session, privateTab);
        session.setNavigationDelegate(new GeckoSession.NavigationDelegate() {
            @Override
            public void onLocationChange(GeckoSession changed, String url,
                    List<GeckoSession.PermissionDelegate.ContentPermission> permissions, Boolean hasUserGesture) {
                tab.url = url == null ? "" : url;
                if (tabs.indexOf(tab) == activeTab) {
                    addressBar.setText(tab.url);
                    addressBar.setSelection(addressBar.length());
                }
                if (!tab.privateTab && url != null
                        && (url.startsWith("http://") || url.startsWith("https://"))) {
                    saveHistory(tab.title, url);
                }
            }

            @Override
            public void onCanGoBack(GeckoSession changed, boolean canGoBack) {
                tab.canGoBack = canGoBack;
                if (tabs.indexOf(tab) == activeTab) updateToolbar();
            }

            @Override
            public void onCanGoForward(GeckoSession changed, boolean canGoForward) {
                tab.canGoForward = canGoForward;
                if (tabs.indexOf(tab) == activeTab) updateToolbar();
            }

            @Override
            public GeckoResult<GeckoSession> onNewSession(GeckoSession source, String uri) {
                Tab created = createTab(false);
                if (created == null) return GeckoResult.fromValue(null);
                // The URI is informational here; Gecko loads it after this callback returns.
                // Marking it keeps activateTab from replacing the new window with the homepage.
                created.url = uri == null ? "" : uri;
                activateTab(tabs.indexOf(created));
                showBrowserSurface(true);
                return GeckoResult.fromValue(created.session);
            }
        });
        session.setProgressDelegate(new GeckoSession.ProgressDelegate() {
            @Override
            public void onPageStart(GeckoSession changed, String url) {
                tab.loading = true;
                if (tabs.indexOf(tab) == activeTab) statusView.setText("Loading website…");
            }

            @Override
            public void onPageStop(GeckoSession changed, boolean success) {
                tab.loading = false;
                if (tabs.indexOf(tab) == activeTab) {
                    statusView.setText(success ? "Page loaded · Desktop layout" : "Page load finished with an error");
                    updateToolbar();
                }
            }
        });
        session.setContentDelegate(new GeckoSession.ContentDelegate() {
            @Override
            public void onTitleChange(GeckoSession changed, String title) {
                if (title != null && !title.trim().isEmpty()) tab.title = title.trim();
                if (tabs.indexOf(tab) == activeTab) refreshTabStrip();
            }

            @Override
            public void onFullScreen(GeckoSession changed, boolean fullScreen) {
                if (fullScreen) {
                    topBar.setVisibility(View.GONE);
                    tabScroller.setVisibility(View.GONE);
                    statusView.setVisibility(View.GONE);
                    pointerOverlay.setVisibility(pointerMode ? View.VISIBLE : View.GONE);
                } else {
                    topBar.setVisibility(View.VISIBLE);
                    tabScroller.setVisibility(View.VISIBLE);
                    statusView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCrash(GeckoSession changed) {
                toast("This tab crashed. Reload the page to try again.");
                tab.loading = false;
            }
        });
        session.open(runtime);
        session.setActive(false);
        tabs.add(tab);
        refreshTabStrip();
        return tab;
    }

    private void activateTab(int index) {
        if (index < 0 || index >= tabs.size()) return;
        if (activeTab >= 0 && activeTab < tabs.size()) {
            Tab old = tabs.get(activeTab);
            old.session.setActive(false);
            old.session.setFocused(false);
            runtime.getWebExtensionController().setTabActive(old.session, false);
        }
        activeTab = index;
        Tab selected = tabs.get(index);
        selected.session.setActive(true);
        selected.session.setFocused(true);
        geckoView.setSession(selected.session);
        runtime.getWebExtensionController().setTabActive(selected.session, true);
        addressBar.setText(selected.url);
        showBrowserSurface(!selected.url.isEmpty());
        refreshTabStrip();
        updateToolbar();
        if (selected.url.isEmpty()) showHome();
        else geckoView.requestFocus();
    }

    private void showHome() {
        if (activeTab >= 0 && activeTab < tabs.size()) tabs.get(activeTab).session.setActive(false);
        if (homeView != null) pageHost.removeView(homeView);
        homeView = buildHomeView();
        pageHost.addView(homeView, new FrameLayout.LayoutParams(-1, -1));
        geckoView.setVisibility(View.GONE);
        pointerOverlay.setVisibility(View.GONE);
        addressBar.setText("");
        statusView.setText("Ready · Desktop browsing enabled · No built-in ad blocker");
        updateToolbar();
    }

    private View buildHomeView() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BG);
        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(30), dp(28), dp(30), dp(30));
        scroll.addView(body, new ScrollView.LayoutParams(-1, -2));

        LinearLayout hero = new LinearLayout(this);
        hero.setOrientation(LinearLayout.VERTICAL);
        hero.setPadding(dp(28), dp(24), dp(28), dp(24));
        hero.setBackground(roundGradient(new int[]{Color.rgb(23, 61, 76), Color.rgb(18, 35, 57)}, 24));
        body.addView(hero, new LinearLayout.LayoutParams(-1, dp(164)));

        TextView eyebrow = label("J B R O W S E R   •   ANDROID TV", 12, ACCENT, true);
        hero.addView(eyebrow);
        TextView title = label("The web, on the big screen.", 31, TEXT, true);
        LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(-1, -2);
        titleLp.topMargin = dp(12);
        hero.addView(title, titleLp);
        TextView subtitle = label("Desktop websites. A remote-friendly pointer. Your choice of extensions.", 15, MUTED, false);
        LinearLayout.LayoutParams subLp = new LinearLayout.LayoutParams(-1, -2);
        subLp.topMargin = dp(7);
        hero.addView(subtitle, subLp);

        TextView section = label("QUICK ACCESS", 13, MUTED, true);
        LinearLayout.LayoutParams sectionLp = new LinearLayout.LayoutParams(-1, -2);
        sectionLp.topMargin = dp(24);
        sectionLp.bottomMargin = dp(12);
        body.addView(section, sectionLp);

        String[][] sites = {
                {"YouTube", "▶", "https://www.youtube.com/"},
                {"Google", "G", "https://www.google.com/"},
                {"Netflix", "N", "https://www.netflix.com/"},
                {"Twitch", "◉", "https://www.twitch.tv/"},
                {"Wikipedia", "W", "https://www.wikipedia.org/"},
                {"Prime Video", "P", "https://www.primevideo.com/"},
                {"Reddit", "r", "https://www.reddit.com/"},
                {"Firefox Add-ons", "🧩", "https://addons.mozilla.org/en-US/android/"}
        };
        for (int start = 0; start < sites.length; start += 4) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(-1, dp(112));
            rowLp.bottomMargin = dp(12);
            body.addView(row, rowLp);
            for (int j = start; j < Math.min(start + 4, sites.length); j++) {
                final String[] site = sites[j];
                LinearLayout tile = new LinearLayout(this);
                tile.setOrientation(LinearLayout.VERTICAL);
                tile.setGravity(Gravity.CENTER);
                tile.setBackground(focusableRound(PANEL, PANEL_HI, 18));
                tile.setFocusable(true);
                TextView icon = label(site[1], 27, ACCENT, true);
                icon.setGravity(Gravity.CENTER);
                TextView name = label(site[0], 14, TEXT, true);
                name.setGravity(Gravity.CENTER);
                tile.addView(icon, new LinearLayout.LayoutParams(-1, dp(42)));
                tile.addView(name, new LinearLayout.LayoutParams(-1, dp(28)));
                tile.setOnClickListener(v -> navigate(site[2]));
                LinearLayout.LayoutParams tileLp = new LinearLayout.LayoutParams(0, -1, 1f);
                tileLp.setMargins(dp(5), 0, dp(5), 0);
                row.addView(tile, tileLp);
            }
        }

        LinearLayout bottomRow = new LinearLayout(this);
        bottomRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams bottomLp = new LinearLayout.LayoutParams(-1, dp(78));
        bottomLp.topMargin = dp(8);
        body.addView(bottomRow, bottomLp);
        TextView addons = homeAction("🧩   Manage extensions", "Install compatible Firefox WebExtensions", this::showExtensions);
        TextView bookmarks = homeAction("☆   Bookmarks", "Open saved websites", this::showBookmarks);
        LinearLayout.LayoutParams actionLp = new LinearLayout.LayoutParams(0, -1, 1f);
        actionLp.setMargins(dp(5), 0, dp(5), 0);
        bottomRow.addView(addons, actionLp);
        bottomRow.addView(bookmarks, new LinearLayout.LayoutParams(0, -1, 1f));

        TextView footer = label("No built-in ad blocker  ·  GeckoView engine  ·  Desktop mode by default", 12, MUTED, false);
        footer.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams footerLp = new LinearLayout.LayoutParams(-1, dp(36));
        footerLp.topMargin = dp(12);
        body.addView(footer, footerLp);
        return scroll;
    }

    private TextView homeAction(String title, String description, Runnable action) {
        TextView v = label(title + "\n" + description, 16, TEXT, true);
        v.setGravity(Gravity.CENTER_VERTICAL);
        v.setPadding(dp(18), 0, dp(18), 0);
        v.setBackground(focusableRound(PANEL, PANEL_HI, 16));
        v.setFocusable(true);
        v.setOnClickListener(view -> action.run());
        return v;
    }

    private void showBrowserSurface(boolean show) {
        if (homeView != null) {
            pageHost.removeView(homeView);
            homeView = null;
        }
        geckoView.setVisibility(show ? View.VISIBLE : View.GONE);
        pointerOverlay.setVisibility(show && pointerMode ? View.VISIBLE : View.GONE);
        if (show) {
            if (pointerX <= 0 || pointerY <= 0) resetPointer();
            geckoView.requestFocus();
        }
    }

    private void navigate(String input) {
        if (input == null) return;
        String value = input.trim();
        if (value.isEmpty()) return;
        String url;
        if (value.matches("(?i)^https?://.*")) url = value;
        else if (value.contains(".") && !value.contains(" ")) url = "https://" + value;
        else url = "https://www.google.com/search?q=" + Uri.encode(value);

        if (activeTab < 0 || activeTab >= tabs.size()) {
            Tab tab = createTab(false);
            if (tab == null) return;
            activateTab(tabs.indexOf(tab));
        }
        Tab tab = tabs.get(activeTab);
        tab.url = url;
        addressBar.setText(url);
        showBrowserSurface(true);
        tab.session.setActive(true);
        tab.session.loadUri(url);
        updateToolbar();
    }

    private void goBack() {
        Tab tab = activeTab();
        if (tab != null && tab.canGoBack) tab.session.goBack();
        else showHome();
    }

    private void goForward() {
        Tab tab = activeTab();
        if (tab != null && tab.canGoForward) tab.session.goForward();
    }

    private void reloadPage() {
        Tab tab = activeTab();
        if (tab != null && !tab.url.isEmpty()) tab.session.reload();
    }

    private void addBookmark() {
        Tab tab = activeTab();
        if (tab == null || tab.url.isEmpty()) {
            toast("Open a website before bookmarking it");
            return;
        }
        try {
            bookmarkStore.add(tab.title, tab.url, false);
            toast("Bookmark saved");
        } catch (Exception e) {
            toast("Could not save bookmark");
        }
    }

    private void showBookmarks() {
        List<BookmarkStore.Bookmark> list = bookmarkStore.getPublicBookmarks();
        if (list.isEmpty()) {
            toast("No bookmarks yet");
            return;
        }
        String[] names = new String[list.size()];
        for (int i = 0; i < list.size(); i++) names[i] = list.get(i).title + "\n" + list.get(i).url;
        new AlertDialog.Builder(this).setTitle("Bookmarks").setItems(names, (d, which) -> navigate(list.get(which).url))
                .setNegativeButton("Close", null).show();
    }

    private void showTabs() {
        if (tabs.isEmpty()) return;
        String[] names = new String[tabs.size() + 1];
        for (int i = 0; i < tabs.size(); i++) {
            Tab tab = tabs.get(i);
            names[i] = (i == activeTab ? "✓  " : "    ") + (tab.title.isEmpty() ? "New tab" : tab.title);
        }
        names[tabs.size()] = "+  New tab";
        new AlertDialog.Builder(this).setTitle("Open tabs").setItems(names, (dialog, which) -> {
            if (which == tabs.size()) {
                Tab newTab = createTab(false);
                if (newTab != null) activateTab(tabs.indexOf(newTab));
            } else activateTab(which);
        }).setNegativeButton("Close current tab", (d, w) -> closeActiveTab())
                .setNeutralButton("Done", null).show();
    }

    private void closeActiveTab() {
        if (activeTab < 0 || activeTab >= tabs.size()) return;
        Tab removed = tabs.remove(activeTab);
        removed.session.close();
        if (tabs.isEmpty()) {
            Tab tab = createTab(false);
            if (tab != null) activateTab(0);
        } else activateTab(Math.min(activeTab, tabs.size() - 1));
        refreshTabStrip();
    }

    private void refreshTabStrip() {
        if (tabStrip == null) return;
        tabStrip.removeAllViews();
        for (int i = 0; i < tabs.size(); i++) {
            final int index = i;
            Tab tab = tabs.get(i);
            TextView chip = label((i == activeTab ? "●  " : "") + shortTitle(tab.title) + "   ×", 13,
                    i == activeTab ? ACCENT : TEXT, true);
            chip.setGravity(Gravity.CENTER);
            chip.setPadding(dp(14), 0, dp(14), 0);
            chip.setFocusable(true);
            chip.setBackground(focusableRound(i == activeTab ? PANEL_HI : PANEL, Color.rgb(43, 67, 93), 12));
            chip.setOnClickListener(v -> {
                if (index == activeTab) closeActiveTab();
                else activateTab(index);
            });
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, dp(34));
            lp.setMargins(0, dp(4), dp(8), dp(4));
            tabStrip.addView(chip, lp);
        }
        TextView add = label("+  New tab", 13, ACCENT, true);
        add.setGravity(Gravity.CENTER);
        add.setPadding(dp(15), 0, dp(15), 0);
        add.setBackground(focusableRound(PANEL, PANEL_HI, 12));
        add.setFocusable(true);
        add.setOnClickListener(v -> {
            Tab tab = createTab(false);
            if (tab != null) activateTab(tabs.indexOf(tab));
        });
        LinearLayout.LayoutParams addLp = new LinearLayout.LayoutParams(-2, dp(34));
        addLp.setMargins(0, dp(4), dp(8), dp(4));
        tabStrip.addView(add, addLp);
    }

    private void showExtensions() {
        String[] actions = {"Get Firefox Add-ons", "Install from .xpi file", "Install from .xpi URL", "Manage installed extensions"};
        new AlertDialog.Builder(this).setTitle("JBrowser extensions")
                .setMessage("JBrowser supports Mozilla-signed Firefox WebExtensions that are compatible with GeckoView. Not every Firefox add-on and Chrome extension is supported.")
                .setItems(actions, (dialog, which) -> {
                    if (which == 0) navigate("https://addons.mozilla.org/en-US/android/");
                    else if (which == 1) pickExtensionFile();
                    else if (which == 2) promptExtensionUrl();
                    else listExtensions();
                }).setNegativeButton("Close", null).show();
    }

    private void pickExtensionFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/x-xpinstall");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"application/x-xpinstall", "application/zip", "application/octet-stream"});
        try { startActivityForResult(intent, REQUEST_EXTENSION_FILE); }
        catch (Exception e) { toast("No file picker is available on this device"); }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQUEST_EXTENSION_FILE || resultCode != RESULT_OK || data == null || data.getData() == null) return;
        Uri selected = data.getData();
        installExtensionFromUri(selected);
    }

    private void installExtensionFromUri(Uri uri) {
        statusView.setText("Preparing extension package…");
        new Thread(() -> {
            File copied = null;
            try (InputStream input = getContentResolver().openInputStream(uri)) {
                if (input == null) throw new IllegalStateException("Could not read selected file");
                File dir = new File(getCacheDir(), "extension-installs");
                if (!dir.exists() && !dir.mkdirs()) throw new IllegalStateException("Could not create cache folder");
                copied = new File(dir, "extension-" + System.currentTimeMillis() + ".xpi");
                try (FileOutputStream output = new FileOutputStream(copied)) {
                    byte[] buffer = new byte[8192];
                    int count;
                    while ((count = input.read(buffer)) != -1) output.write(buffer, 0, count);
                }
            } catch (Exception e) {
                File finalCopied = copied;
                mainHandler.post(() -> toast("Could not read XPI: " + e.getMessage()));
                return;
            }
            File finalCopied = copied;
            mainHandler.post(() -> installExtensionUri(Uri.fromFile(finalCopied).toString(), WebExtensionController.INSTALLATION_METHOD_FROM_FILE));
        }).start();
    }

    private void promptExtensionUrl() {
        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setTextSize(16);
        input.setHint("https://…/addon.xpi");
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        input.setPadding(dp(14), dp(10), dp(14), dp(10));
        new AlertDialog.Builder(this).setTitle("Install signed XPI URL")
                .setMessage("Paste a direct HTTPS link to a Mozilla-signed .xpi package.")
                .setView(input).setPositiveButton("Install", (d, w) -> {
                    String url = input.getText().toString().trim();
                    if (!url.toLowerCase(Locale.ROOT).startsWith("https://") || !url.toLowerCase(Locale.ROOT).contains(".xpi")) {
                        toast("Enter a direct HTTPS .xpi URL");
                        return;
                    }
                    installExtensionUri(url, WebExtensionController.INSTALLATION_METHOD_MANAGER);
                }).setNegativeButton("Cancel", null).show();
    }

    private void installExtensionUri(String uri, String method) {
        toast("Checking extension signature and permissions…");
        runtime.getWebExtensionController().install(uri, method).accept(extension -> mainHandler.post(() -> {
            toast("Installed: " + extensionName(extension));
            listExtensions();
        }), error -> mainHandler.post(() -> toast("Extension install failed: " + shortError(error))));
    }

    private void listExtensions() {
        runtime.getWebExtensionController().list().accept(extensions -> mainHandler.post(() -> {
            if (extensions == null || extensions.isEmpty()) {
                new AlertDialog.Builder(this).setTitle("Installed extensions")
                        .setMessage("No extensions are installed yet. Use Get Firefox Add-ons or install a signed .xpi file.")
                        .setPositiveButton("Get add-ons", (d, w) -> navigate("https://addons.mozilla.org/en-US/android/"))
                        .setNegativeButton("Close", null).show();
                return;
            }
            String[] names = new String[extensions.size()];
            for (int i = 0; i < extensions.size(); i++) {
                WebExtension.MetaData meta = extensions.get(i).metaData;
                names[i] = extensionName(extensions.get(i)) + "  ·  " + ((meta != null && meta.enabled) ? "Enabled" : "Disabled");
            }
            new AlertDialog.Builder(this).setTitle("Installed extensions").setItems(names,
                    (d, which) -> showExtensionActions(extensions.get(which))).setNegativeButton("Close", null).show();
        }), error -> mainHandler.post(() -> toast("Could not read installed extensions: " + shortError(error))));
    }

    private void showExtensionActions(WebExtension extension) {
        WebExtension.MetaData meta = extension.metaData;
        boolean enabled = meta != null && meta.enabled;
        String[] actions = {enabled ? "Disable" : "Enable", "Open extension options", "Uninstall"};
        new AlertDialog.Builder(this).setTitle(extensionName(extension))
                .setMessage((meta == null ? "" : safe(meta.description) + "\nVersion " + safe(meta.version)))
                .setItems(actions, (d, which) -> {
                    WebExtensionController controller = runtime.getWebExtensionController();
                    if (which == 0) {
                        if (enabled) controller.disable(extension, WebExtensionController.EnableSource.USER)
                                .accept(updated -> mainHandler.post(() -> { toast("Extension disabled"); listExtensions(); }),
                                        error -> mainHandler.post(() -> toast(shortError(error))));
                        else controller.enable(extension, WebExtensionController.EnableSource.USER)
                                .accept(updated -> mainHandler.post(() -> { toast("Extension enabled"); listExtensions(); }),
                                        error -> mainHandler.post(() -> toast(shortError(error))));
                    } else if (which == 1) {
                        if (meta != null && meta.optionsPageUrl != null && !meta.optionsPageUrl.isEmpty()) navigate(meta.optionsPageUrl);
                        else toast("This extension does not provide an options page");
                    } else {
                        new AlertDialog.Builder(this).setTitle("Uninstall extension?")
                                .setMessage("Remove " + extensionName(extension) + " and its stored data?")
                                .setNegativeButton("Cancel", null)
                                .setPositiveButton("Uninstall", (confirm, w) -> controller.uninstall(extension)
                                        .accept(unused -> mainHandler.post(() -> { toast("Extension removed"); listExtensions(); }),
                                                error -> mainHandler.post(() -> toast(shortError(error)))))
                                .show();
                    }
                }).setNegativeButton("Back", (d, w) -> listExtensions()).show();
    }

    private GeckoResult<WebExtension.PermissionPromptResponse> promptExtensionPermissions(
            WebExtension extension, String[] permissions, String[] origins, String[] dataPermissions) {
        GeckoResult<WebExtension.PermissionPromptResponse> result = new GeckoResult<>();
        String message = buildPermissionMessage(permissions, origins, dataPermissions);
        new AlertDialog.Builder(this).setTitle("Install " + extensionName(extension) + "?")
                .setMessage(message + "\n\nOnly approve an extension if you trust its developer.")
                .setNegativeButton("Cancel", (d, w) -> result.complete(new WebExtension.PermissionPromptResponse(false, false, false)))
                .setPositiveButton("Approve permissions", (d, w) -> result.complete(new WebExtension.PermissionPromptResponse(true, false, false)))
                .setOnCancelListener(d -> result.complete(new WebExtension.PermissionPromptResponse(false, false, false)))
                .show();
        return result;
    }

    private GeckoResult<AllowOrDeny> promptOptionalPermissions(WebExtension extension, String[] permissions,
                                                                String[] origins, String[] dataPermissions) {
        GeckoResult<AllowOrDeny> result = new GeckoResult<>();
        new AlertDialog.Builder(this).setTitle("Additional permissions")
                .setMessage(extensionName(extension) + " requests:\n\n" + buildPermissionMessage(permissions, origins, dataPermissions))
                .setNegativeButton("Deny", (d, w) -> result.complete(AllowOrDeny.DENY))
                .setPositiveButton("Allow", (d, w) -> result.complete(AllowOrDeny.ALLOW))
                .setOnCancelListener(d -> result.complete(AllowOrDeny.DENY)).show();
        return result;
    }

    private GeckoResult<AllowOrDeny> promptUpdatePermissions(WebExtension extension, String[] permissions,
                                                              String[] origins, String[] dataPermissions) {
        GeckoResult<AllowOrDeny> result = new GeckoResult<>();
        new AlertDialog.Builder(this).setTitle("Extension update permissions")
                .setMessage(extensionName(extension) + " requests new permissions:\n\n" + buildPermissionMessage(permissions, origins, dataPermissions))
                .setNegativeButton("Do not update", (d, w) -> result.complete(AllowOrDeny.DENY))
                .setPositiveButton("Approve update", (d, w) -> result.complete(AllowOrDeny.ALLOW))
                .setOnCancelListener(d -> result.complete(AllowOrDeny.DENY)).show();
        return result;
    }

    private String buildPermissionMessage(String[] permissions, String[] origins, String[] dataPermissions) {
        StringBuilder out = new StringBuilder();
        appendPermissionGroup(out, "Permissions", permissions);
        appendPermissionGroup(out, "Website access", origins);
        appendPermissionGroup(out, "Data collection", dataPermissions);
        return out.length() == 0 ? "This extension did not list additional permissions." : out.toString();
    }

    private void appendPermissionGroup(StringBuilder out, String title, String[] values) {
        if (values == null || values.length == 0) return;
        out.append(title).append(":\n");
        for (String value : values) out.append(" • ").append(value).append('\n');
        out.append('\n');
    }

    private String extensionName(WebExtension extension) {
        return extension.metaData != null && extension.metaData.name != null
                ? extension.metaData.name : extension.id;
    }

    private String shortError(Throwable error) {
        if (error == null || error.getMessage() == null) return "Unknown error";
        String message = error.getMessage();
        return message.length() > 150 ? message.substring(0, 150) : message;
    }

    private void togglePointerMode() {
        pointerMode = !pointerMode;
        getPreferences(MODE_PRIVATE).edit().putBoolean("pointer_mode", pointerMode).apply();
        pointerOverlay.setVisibility(pointerMode && geckoView.getVisibility() == View.VISIBLE ? View.VISIBLE : View.GONE);
        updateToolbar();
        toast(pointerMode ? "Remote pointer enabled · arrows move · OK clicks" : "Remote pointer off · use normal page navigation");
        if (pointerMode) geckoView.requestFocus();
    }

    private void toggleDesktopMode() {
        desktopMode = !desktopMode;
        getPreferences(MODE_PRIVATE).edit().putBoolean("desktop_mode", desktopMode).apply();
        for (Tab tab : tabs) {
            GeckoSessionSettings settings = tab.session.getSettings();
            settings.setUserAgentMode(desktopMode ? GeckoSessionSettings.USER_AGENT_MODE_DESKTOP
                    : GeckoSessionSettings.USER_AGENT_MODE_MOBILE);
            settings.setViewportMode(desktopMode ? GeckoSessionSettings.VIEWPORT_MODE_DESKTOP
                    : GeckoSessionSettings.VIEWPORT_MODE_MOBILE);
            if (!tab.url.isEmpty()) tab.session.reload();
        }
        updateToolbar();
        toast(desktopMode ? "Desktop websites enabled" : "Mobile websites enabled");
    }

    private void showMoreMenu() {
        String[] items = {"Extensions", "Bookmarks", "Pointer: " + (pointerMode ? "On" : "Off"),
                "Website layout: " + (desktopMode ? "Desktop" : "Mobile"), "New private tab", "About JBrowser"};
        new AlertDialog.Builder(this).setTitle("JBrowser menu").setItems(items, (d, which) -> {
            if (which == 0) showExtensions();
            else if (which == 1) showBookmarks();
            else if (which == 2) togglePointerMode();
            else if (which == 3) toggleDesktopMode();
            else if (which == 4) {
                Tab privateTab = createTab(true);
                if (privateTab != null) activateTab(tabs.indexOf(privateTab));
            } else new AlertDialog.Builder(this).setTitle("JBrowser " + VERSION_NAME)
                    .setMessage("A desktop-first Android TV browser powered by Mozilla GeckoView. Supports compatible, Mozilla-signed Firefox WebExtensions. Built-in ad blocking has been removed.\n\nMinimum Android version: 8.0 (API 26).")
                    .setPositiveButton("OK", null).show();
        }).setNegativeButton("Close", null).show();
    }

    private void updateToolbar() {
        if (pointerButton != null) pointerButton.setText(pointerMode ? "⌖" : "＋");
        if (desktopButton != null) desktopButton.setText(desktopMode ? "PC" : "MB");
        Tab tab = activeTab();
        if (backButton != null) backButton.setAlpha(tab != null && tab.canGoBack ? 1f : 0.45f);
        if (forwardButton != null) forwardButton.setAlpha(tab != null && tab.canGoForward ? 1f : 0.45f);
        if (tabButton != null) tabButton.setText("▣ " + tabs.size());
        if (tab != null && tab.url.length() > 0 && geckoView.getVisibility() == View.VISIBLE) {
            addressBar.setText(tab.url);
            addressBar.setSelection(addressBar.length());
        }
    }

    private void resetPointer() {
        pointerX = Math.max(dp(30), pageHost.getWidth() / 2f);
        pointerY = Math.max(dp(30), pageHost.getHeight() / 2f);
        updatePointer();
    }

    private void movePointer(int keyCode, int repeat) {
        if (pointerX <= 0 || pointerY <= 0) resetPointer();
        float step = dp(repeat > 0 ? 34 : 22);
        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) pointerX -= step;
        else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) pointerX += step;
        else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) pointerY -= step;
        else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) pointerY += step;
        pointerX = Math.max(dp(2), Math.min(pageHost.getWidth() - dp(2), pointerX));
        pointerY = Math.max(dp(2), Math.min(pageHost.getHeight() - dp(2), pointerY));
        updatePointer();
        sendMouseEvent(MotionEvent.ACTION_HOVER_MOVE, 0);
    }

    private void updatePointer() {
        if (pointerOverlay != null) {
            pointerOverlay.setPointer(pointerX, pointerY);
            pointerOverlay.invalidate();
        }
    }

    private void pointerClick() {
        sendMouseEvent(MotionEvent.ACTION_DOWN, MotionEvent.BUTTON_PRIMARY);
        sendMouseEvent(MotionEvent.ACTION_UP, 0);
    }

    private void scrollPage(boolean pageUp) {
        Tab tab = activeTab();
        if (tab == null || geckoView.getVisibility() != View.VISIBLE || !tab.session.isOpen()) return;
        if (pointerX <= 0 || pointerY <= 0) resetPointer();
        long now = android.os.SystemClock.uptimeMillis();
        MotionEvent.PointerProperties properties = new MotionEvent.PointerProperties();
        properties.id = 0;
        properties.toolType = MotionEvent.TOOL_TYPE_MOUSE;
        MotionEvent.PointerCoords coordinates = new MotionEvent.PointerCoords();
        coordinates.x = Math.max(0, pointerX);
        coordinates.y = Math.max(0, pointerY);
        coordinates.pressure = 0f;
        coordinates.size = 1f;
        coordinates.setAxisValue(MotionEvent.AXIS_VSCROLL, pageUp ? 1f : -1f);
        MotionEvent event = MotionEvent.obtain(now, now, MotionEvent.ACTION_SCROLL, 1,
                new MotionEvent.PointerProperties[]{properties}, new MotionEvent.PointerCoords[]{coordinates},
                0, 0, 1f, 1f, 0, 0, android.view.InputDevice.SOURCE_MOUSE, 0);
        tab.session.getPanZoomController().onMotionEvent(event);
        event.recycle();
    }

    private void sendMouseEvent(int action, int buttons) {
        Tab tab = activeTab();
        if (tab == null || geckoView.getVisibility() != View.VISIBLE || !tab.session.isOpen()) return;
        long now = android.os.SystemClock.uptimeMillis();
        MotionEvent.PointerProperties properties = new MotionEvent.PointerProperties();
        properties.id = 0;
        properties.toolType = MotionEvent.TOOL_TYPE_MOUSE;
        MotionEvent.PointerCoords coordinates = new MotionEvent.PointerCoords();
        coordinates.x = Math.max(0, pointerX);
        coordinates.y = Math.max(0, pointerY);
        coordinates.pressure = action == MotionEvent.ACTION_UP ? 0f : 1f;
        coordinates.size = 1f;
        MotionEvent event = MotionEvent.obtain(now, now, action, 1,
                new MotionEvent.PointerProperties[]{properties}, new MotionEvent.PointerCoords[]{coordinates},
                0, buttons, 1f, 1f, 0, 0, android.view.InputDevice.SOURCE_MOUSE, 0);
        if (action == MotionEvent.ACTION_HOVER_MOVE) tab.session.getPanZoomController().onMotionEvent(event);
        else tab.session.getPanZoomController().onMouseEvent(event);
        event.recycle();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN && event.isCtrlPressed()) {
            int key = event.getKeyCode();
            if (key == KeyEvent.KEYCODE_T) {
                Tab tab = createTab(false);
                if (tab != null) activateTab(tabs.indexOf(tab));
                return true;
            }
            if (key == KeyEvent.KEYCODE_W) { closeActiveTab(); return true; }
            if (key == KeyEvent.KEYCODE_L) { addressBar.requestFocus(); addressBar.selectAll(); return true; }
            if (key == KeyEvent.KEYCODE_R) { reloadPage(); return true; }
        }
        if (pointerMode && geckoView != null && geckoView.getVisibility() == View.VISIBLE
                && !addressBar.hasFocus() && event.getAction() == KeyEvent.ACTION_DOWN) {
            int key = event.getKeyCode();
            if (key == KeyEvent.KEYCODE_DPAD_LEFT || key == KeyEvent.KEYCODE_DPAD_RIGHT
                    || key == KeyEvent.KEYCODE_DPAD_UP || key == KeyEvent.KEYCODE_DPAD_DOWN) {
                movePointer(key, event.getRepeatCount());
                return true;
            }
            if (key == KeyEvent.KEYCODE_DPAD_CENTER || key == KeyEvent.KEYCODE_ENTER) {
                pointerClick();
                return true;
            }
            if (key == KeyEvent.KEYCODE_PAGE_UP || key == KeyEvent.KEYCODE_CHANNEL_UP) {
                scrollPage(true);
                return true;
            }
            if (key == KeyEvent.KEYCODE_PAGE_DOWN || key == KeyEvent.KEYCODE_CHANNEL_DOWN) {
                scrollPage(false);
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onBackPressed() {
        Tab tab = activeTab();
        if (tab != null && tab.canGoBack && geckoView.getVisibility() == View.VISIBLE) goBack();
        else if (geckoView.getVisibility() == View.VISIBLE) showHome();
        else super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        applyImmersive();
        Tab tab = activeTab();
        if (tab != null) tab.session.setActive(true);
    }

    @Override
    protected void onPause() {
        Tab tab = activeTab();
        if (tab != null) tab.session.setFocused(false);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        for (Tab tab : tabs) {
            try { tab.session.close(); } catch (Exception ignored) { }
        }
        tabs.clear();
        super.onDestroy();
    }

    private void saveHistory(String title, String url) {
        if (url == null || url.isEmpty()) return;
        String json = getPreferences(MODE_PRIVATE).getString("history", "");
        org.json.JSONArray list;
        try { list = json.isEmpty() ? new org.json.JSONArray() : new org.json.JSONArray(json); }
        catch (Exception e) { list = new org.json.JSONArray(); }
        org.json.JSONObject entry = new org.json.JSONObject();
        try {
            entry.put("title", title == null ? url : title);
            entry.put("url", url);
            entry.put("time", System.currentTimeMillis());
            org.json.JSONArray newList = new org.json.JSONArray();
            newList.put(entry);
            for (int i = 0; i < list.length() && i < 199; i++) {
                org.json.JSONObject old = list.optJSONObject(i);
                if (old != null && !url.equals(old.optString("url"))) newList.put(old);
            }
            getPreferences(MODE_PRIVATE).edit().putString("history", newList.toString()).apply();
        } catch (Exception ignored) { }
    }

    private void hideKeyboard(View view) {
        try { ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(view.getWindowToken(), 0); }
        catch (Exception ignored) { }
    }

    private Tab activeTab() {
        return activeTab >= 0 && activeTab < tabs.size() ? tabs.get(activeTab) : null;
    }

    private void toast(String message) { Toast.makeText(this, message, Toast.LENGTH_LONG).show(); }
    private int dp(float value) { return Math.round(value * getResources().getDisplayMetrics().density); }
    private String shortTitle(String title) {
        if (title == null || title.trim().isEmpty()) return "New tab";
        return title.length() > 22 ? title.substring(0, 19) + "…" : title;
    }
    private String safe(String value) { return value == null ? "" : value; }

    private TextView label(String value, float size, int color, boolean bold) {
        TextView text = new TextView(this);
        text.setText(value);
        text.setTextSize(size);
        text.setTextColor(color);
        if (bold) text.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        text.setIncludeFontPadding(true);
        return text;
    }

    private GradientDrawable round(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radius));
        return d;
    }

    private GradientDrawable roundGradient(int[] colors, int radius) {
        GradientDrawable d = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, colors);
        d.setCornerRadius(dp(radius));
        return d;
    }

    private Drawable focusableRound(int normal, int focused, int radius) {
        GradientDrawable normalDrawable = round(normal, radius);
        GradientDrawable focusedDrawable = round(focused, radius);
        android.graphics.drawable.StateListDrawable states = new android.graphics.drawable.StateListDrawable();
        states.addState(new int[]{android.R.attr.state_focused}, focusedDrawable);
        states.addState(new int[]{}, normalDrawable);
        return states;
    }

    private final class PointerOverlay extends View {
        private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
        private float x;
        private float y;

        PointerOverlay(Context context) {
            super(context);
            fill.setColor(Color.WHITE);
            stroke.setColor(Color.BLACK);
            stroke.setStyle(Paint.Style.STROKE);
            stroke.setStrokeWidth(dp(2));
        }

        void setPointer(float x, float y) { this.x = x; this.y = y; }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            Path pointer = new Path();
            pointer.moveTo(x, y);
            pointer.lineTo(x, y + dp(25));
            pointer.lineTo(x + dp(7), y + dp(19));
            pointer.lineTo(x + dp(13), y + dp(32));
            pointer.lineTo(x + dp(18), y + dp(29));
            pointer.lineTo(x + dp(12), y + dp(17));
            pointer.lineTo(x + dp(23), y + dp(17));
            pointer.close();
            canvas.drawPath(pointer, stroke);
            canvas.drawPath(pointer, fill);
        }
    }
}
