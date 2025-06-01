//ーーーー>ーー>ー>ーーーーーーーーーーーーーーーーーーーーーー(
//
//
// _______                    _                 
//|__   __|                  (_)                
//   | |      __ _     _ __    _ 
//   | |    / _`  |   | '_ \  | |
//   | |   | (_|  |   |  |_)  | | 
//   |_|    \__,_ \   | .__/  |_|
//                    | |                      
//                    |_|
//
//               ★彡[tapimod]彡★
//         TAPI製ModMenuテンプレ(ぷにぷに)
//         Discord サーバー⬇
//         https://discord.gg/6FQPgbBWGR
//ーーーー>ーー>ー>ーーーーーーーーーーーーーーーーーーーーーー(

package uk.lgl.modmenu;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Process;
import android.system.Os;
import android.text.Html;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.DigitsKeyListener;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.app.ActivityManager.RunningAppProcessInfo;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static android.widget.RelativeLayout.ALIGN_PARENT_LEFT;
import static android.widget.RelativeLayout.ALIGN_PARENT_RIGHT;
import static android.widget.RelativeLayout.CENTER_IN_PARENT;

public class FloatingModMenuService extends Service {
    //********** Here you can easly change the menu appearance **********//
    public static final String TAG = "Mod_Menu"; //Tag for logcat
    int TEXT_COLOR = Color.parseColor("#FFFFFF");
    int TEXT_COLOR_2 = Color.parseColor("#FFFFFF");
    int BTN_COLOR = Color.parseColor("#C1FFFFFF");
    int MENU_BG_COLOR = Color.parseColor("#000000"); //#AARRGGBB
    int MENU_FEATURE_BG_COLOR = Color.parseColor("#000000"); //#AARRGGBB
    int MENU_WIDTH = 250;
    int MENU_HEIGHT = 210;
    float MENU_CORNER = 4f;
    int ICON_SIZE = 70; //アイコンサイズ
    float ICON_ALPHA = 1f;
    int ToggleON = Color.GREEN;
    int ToggleOFF = Color.WHITE;
    int BtnON = Color.parseColor("#63FF0000");
    int BtnOFF = Color.parseColor("#630D00FF");
    int CategoryBG = Color.parseColor("#2F3D4C");
    int SeekBarColor = Color.parseColor("#000000");
    int SeekBarProgressColor = Color.parseColor("#ffffff");
    int CheckBoxColor = Color.parseColor("#FFFFFF");
    int RadioColor = Color.parseColor("#FFFFFF");
    String NumberTxtColor = "#41c300";
    //********************************************************************//
    RelativeLayout mCollapsed, mRootContainer;
    LinearLayout mExpanded, patches, mSettings, mCollapse;
    LinearLayout.LayoutParams scrlLLExpanded, scrlLL;
    WindowManager mWindowManager;
    WindowManager.LayoutParams params;
    ImageView startimage;
    FrameLayout rootFrame;
    ScrollView scrollView;

    boolean stopChecking;

    //initialize methods from the native library
    native void setTitleText(TextView textView);

    native void setHeadingText(TextView textView);

    native String Icon();

    native String IconWebViewData();

    native String[] getFeatureList();

    native String[] settingsList();

    native boolean isGameLibLoaded();

    //When this Class is called the code in this function will be executed
    @Override
    public void onCreate() {
        super.onCreate();
        Preferences.context = this;

        //Create the menu
        initFloating();

        //Create a handler for this Class
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            public void run() {
                Thread();
                handler.postDelayed(this, 1000);
            }
        });
    }

    //Here we write the code for our Menu
    // Reference: https://www.androidhive.info/2016/11/android-floating-widget-like-facebook-chat-head/
    private void initFloating() {
        rootFrame = new FrameLayout(this); // Global markup
        rootFrame.setOnTouchListener(onTouchListener());
        mRootContainer = new RelativeLayout(this); // Markup on which two markups of the icon and the menu itself will be placed
        mCollapsed = new RelativeLayout(this); // Markup of the icon (when the menu is minimized)
        mCollapsed.setVisibility(View.VISIBLE);
        mCollapsed.setAlpha(ICON_ALPHA);

        //********** The box of the mod menu **********
        mExpanded = new LinearLayout(this); // Menu markup (when the menu is expanded)
        mExpanded.setVisibility(View.GONE);
        mExpanded.setBackgroundColor(MENU_BG_COLOR);
        mExpanded.setOrientation(LinearLayout.VERTICAL);
        mExpanded.setPadding(4, 4, 4, 4); //枠の余白
        mExpanded.setLayoutParams(new LinearLayout.LayoutParams(dp(MENU_WIDTH), WRAP_CONTENT));
        GradientDrawable gdMenuBody = new GradientDrawable();
        gdMenuBody.setCornerRadius(MENU_CORNER); //Set corner
        gdMenuBody.setColor(MENU_BG_COLOR); //Set background color
        gdMenuBody.setStroke(4, Color.parseColor("#FFFFFF")); //メニューの枠の色
        gdMenuBody.setCornerRadius(50); //メニューの丸み
        mExpanded.setBackground(gdMenuBody); //Apply GradientDrawable to it

        //********** The icon to open mod menu **********
        startimage = new ImageView(this);
        startimage.setLayoutParams(new RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
        int applyDimension = (int) TypedValue.applyDimension(1, ICON_SIZE, getResources().getDisplayMetrics()); //Icon size
        startimage.getLayoutParams().height = applyDimension;
        startimage.getLayoutParams().width = applyDimension;
        //startimage.requestLayout();
        startimage.setScaleType(ImageView.ScaleType.FIT_XY);
        byte[] decode = Base64.decode(Icon(), 0);
        startimage.setImageBitmap(BitmapFactory.decodeByteArray(decode, 0, decode.length));
        ((ViewGroup.MarginLayoutParams) startimage.getLayoutParams()).topMargin = convertDipToPixels(10);
        //Initialize event handlers for buttons, etc.
        startimage.setOnTouchListener(onTouchListener());
        startimage.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                mCollapsed.setVisibility(View.GONE);
                mExpanded.setVisibility(View.VISIBLE);
            }
        });

        //********** The icon in Webview to open mod menu **********
        WebView wView = new WebView(this); //Icon size width=\"50\" height=\"50\"
        wView.setLayoutParams(new RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
        int applyDimension2 = (int) TypedValue.applyDimension(1, ICON_SIZE, getResources().getDisplayMetrics()); //Icon size
        wView.getLayoutParams().height = applyDimension2;
        wView.getLayoutParams().width = applyDimension2;
        wView.loadData("<html>" +
                "<head></head>" +
                "<body style=\"margin: 0; padding: 0\">" +
                "<img src=\"" + IconWebViewData() + "\" width=\"" + ICON_SIZE + "\" height=\"" + ICON_SIZE + "\" >" +
                "</body>" +
                "</html>", "text/html", "utf-8");
        wView.setBackgroundColor(0x00000000); //Transparent
        wView.setAlpha(ICON_ALPHA);
        wView.getSettings().setAppCacheEnabled(true);
        wView.setOnTouchListener(onTouchListener());

        //********** Settings icon **********
        TextView settings = new TextView(this); //Android 5 can't show ⚙, instead show other icon instead
        settings.setText(Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M ? "⭐" : "\uD83D\uDD27");
        settings.setTextColor(TEXT_COLOR);
        settings.setTypeface(Typeface.DEFAULT_BOLD);
        settings.setTextSize(20.0f);
        RelativeLayout.LayoutParams rlsettings = new RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        rlsettings.addRule(ALIGN_PARENT_RIGHT);
        settings.setLayoutParams(rlsettings);
        settings.setOnClickListener(new View.OnClickListener() {
            boolean settingsOpen;

            @Override
            public void onClick(View v) {
                try {
                    settingsOpen = !settingsOpen;
                    if (settingsOpen) {
                        scrollView.removeView(patches);
                        scrollView.addView(mSettings);
                        scrollView.scrollTo(0, 0);
                    } else {
                        scrollView.removeView(mSettings);
                        scrollView.addView(patches);
                    }
                } catch (IllegalStateException e) {
                }
            }
        });

        //********** Settings **********
        mSettings = new LinearLayout(this);
        mSettings.setOrientation(LinearLayout.VERTICAL);
        featureList(settingsList(), mSettings);

        //********** Title text **********
        RelativeLayout titleText = new RelativeLayout(this);
        titleText.setPadding(10, 5, 10, 5);
        titleText.setVerticalGravity(16);

        TextView title = new TextView(this);
        title.setTextColor(TEXT_COLOR);
        title.setTextSize(18.0f);
        title.setGravity(Gravity.CENTER);
        RelativeLayout.LayoutParams rl = new RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        rl.addRule(RelativeLayout.CENTER_HORIZONTAL);
        title.setLayoutParams(rl);
        setTitleText(title);

        //********** Heading text **********
        TextView heading = new TextView(this);
        //heading.setText(Html.fromHtml(Heading()));
        heading.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        heading.setMarqueeRepeatLimit(-1);
        heading.setSingleLine(true);
        heading.setSelected(true);
        heading.setTextColor(TEXT_COLOR);
        heading.setTextSize(10.0f);
        heading.setGravity(Gravity.CENTER);
        heading.setPadding(0, 0, 0, 5);
        setHeadingText(heading);

        //********** Mod menu feature list **********
        scrollView = new ScrollView(this);
        //Auto size. To set size manually, change the width and height example 500, 500
        scrlLL = new LinearLayout.LayoutParams(MATCH_PARENT, dp(MENU_HEIGHT));
        scrlLLExpanded = new LinearLayout.LayoutParams(mExpanded.getLayoutParams());
        scrlLLExpanded.weight = 1.0f;
        scrollView.setLayoutParams(Preferences.isExpanded ? scrlLLExpanded : scrlLL);
        scrollView.setBackgroundColor(MENU_FEATURE_BG_COLOR);
        patches = new LinearLayout(this);
        patches.setOrientation(LinearLayout.VERTICAL);

        //********** RelativeLayout for buttons **********
        RelativeLayout relativeLayout = new RelativeLayout(this);
        relativeLayout.setPadding(10, 3, 10, 3);
        relativeLayout.setVerticalGravity(Gravity.CENTER);

        //**********  Hide/Kill button **********
        RelativeLayout.LayoutParams lParamsHideBtn = new RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        lParamsHideBtn.addRule(ALIGN_PARENT_LEFT);

        Button hideBtn = new Button(this);
        hideBtn.setLayoutParams(lParamsHideBtn);
        hideBtn.setBackgroundColor(Color.parseColor("#00FFFFFF"));
        hideBtn.setText("《𝐊𝐈𝐋𝐋》");
        hideBtn.setTextColor(TEXT_COLOR);
        hideBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    mCollapsed.setVisibility(View.VISIBLE);
                    mCollapsed.setAlpha(0);
                    mExpanded.setVisibility(View.GONE);
                    Toast.makeText(view.getContext(), "アイコンを非表示にしました", Toast.LENGTH_LONG).show();
                }
            });
        hideBtn.setOnLongClickListener(new View.OnLongClickListener() {
                public boolean onLongClick(View view) {
                    Toast.makeText(view.getContext(), "メニューを終了しました✨", Toast.LENGTH_LONG).show();
                    FloatingModMenuService.this.stopSelf();
                    return false;
                }
            });

        //********** Close button **********
        RelativeLayout.LayoutParams lParamsCloseBtn = new RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        lParamsCloseBtn.addRule(ALIGN_PARENT_RIGHT);

        Button closeBtn = new Button(this);
        closeBtn.setLayoutParams(lParamsCloseBtn);
        closeBtn.setBackgroundColor(Color.parseColor("#00FFFFFF"));
        closeBtn.setText("《𝐂𝐋𝐎𝐒𝐄》");
        closeBtn.setTextColor(TEXT_COLOR);
        closeBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    mCollapsed.setVisibility(View.VISIBLE);
                    mCollapsed.setAlpha(ICON_ALPHA);
                    mExpanded.setVisibility(View.GONE);
                    Toast.makeText(view.getContext(), "TAPIs team", Toast.LENGTH_LONG).show();
                }
            });

        closeBtn.setOnLongClickListener(new View.OnLongClickListener() {
                public boolean onLongClick(View view) {
                    Toast.makeText(view.getContext(), "TAPIによって制作されたModmenuです", Toast.LENGTH_LONG).show();
                    //FloatingModMenuService.this.stopSelf();
                    return false;
                }
			});
        
        RelativeLayout.LayoutParams lParamsKillBtn = new RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        lParamsKillBtn.addRule(CENTER_IN_PARENT);

        Button KillBtn = new Button(this);
        KillBtn.setLayoutParams(lParamsKillBtn);
        KillBtn.setBackgroundColor(Color.parseColor("#00FFFFFF"));
        KillBtn.setText("《𝐄𝐍𝐃》");
        KillBtn.setTextColor(TEXT_COLOR);
        KillBtn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    Process.killProcess(Process.myPid()); 

                }
			});
        //********** Params **********
        //Variable to check later if the phone supports Draw over other apps permission
        int iparams = Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O ? 2038 : 2002;
        params = new WindowManager.LayoutParams(WRAP_CONTENT, WRAP_CONTENT, iparams, 8, -3);
        params.gravity = 51;
        params.x = 0;
        params.y = 100;

        //********** Adding view components **********
        rootFrame.addView(mRootContainer);
        mRootContainer.addView(mCollapsed);
        mRootContainer.addView(mExpanded);
        if (IconWebViewData() != null) {
            mCollapsed.addView(wView);
        } else {
            mCollapsed.addView(startimage);
        }
        titleText.addView(title);
        titleText.addView(settings);
        mExpanded.addView(titleText);
        mExpanded.addView(heading);
        scrollView.addView(patches);
        mExpanded.addView(scrollView);
        relativeLayout.addView(hideBtn);
        relativeLayout.addView(closeBtn);
        relativeLayout.addView(KillBtn);
        mExpanded.addView(relativeLayout);
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mWindowManager.addView(rootFrame, params);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            boolean viewLoaded = false;

            @Override
            public void run() {
                //If the save preferences is enabled, it will check if game lib is loaded before starting menu
                //Comment the if-else code out except startService if you want to run the app and test preferences
                if (Preferences.loadPref && !isGameLibLoaded() && !stopChecking) {
                    if (!viewLoaded) {
                        //patches.addView(Category("「機能設定の保存」が有効になっています。下記の「読み込む」を押して下さい"));
                        patches.addView(Button(-100, "読み込む"));
                        viewLoaded = true;
                    }
                    handler.postDelayed(this, 600);
                } else {
                    patches.removeAllViews();
                    featureList(getFeatureList(), patches);
                }
            }
        }, 500);
    }

    private View.OnTouchListener onTouchListener() {
        return new View.OnTouchListener() {
            final View collapsedView = mCollapsed;
            final View expandedView = mExpanded;
            private float initialTouchX, initialTouchY;
            private int initialX, initialY;

            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = motionEvent.getRawX();
                        initialTouchY = motionEvent.getRawY();
                        return true;
                    case MotionEvent.ACTION_UP:
                        int rawX = (int) (motionEvent.getRawX() - initialTouchX);
                        int rawY = (int) (motionEvent.getRawY() - initialTouchY);
                        mExpanded.setAlpha(1f);
                        mCollapsed.setAlpha(1f);
                        //The check for Xdiff <10 && YDiff< 10 because sometime elements moves a little while clicking.
                        //So that is click event.
                        if (rawX < 10 && rawY < 10 && isViewCollapsed()) {
                            //When user clicks on the image view of the collapsed layout,
                            //visibility of the collapsed layout will be changed to "View.GONE"
                            //and expanded view will become visible.
                            try {
                                collapsedView.setVisibility(View.GONE);
                                expandedView.setVisibility(View.VISIBLE);
                            } catch (NullPointerException e) {

                            }
                        }
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        mExpanded.setAlpha(0.5f);
                        mCollapsed.setAlpha(0.5f);
                        //Calculate the X and Y coordinates of the view.
                        params.x = initialX + ((int) (motionEvent.getRawX() - initialTouchX));
                        params.y = initialY + ((int) (motionEvent.getRawY() - initialTouchY));
                        //Update the layout with new X & Y coordinate
                        mWindowManager.updateViewLayout(rootFrame, params);
                        return true;
                    default:
                        return false;
                }
            }
        };
    }

    private void featureList(String[] listFT, LinearLayout linearLayout) {
        //Currently looks messy right now. Let me know if you have improvements
        int featNum, subFeat = 0;
        LinearLayout llBak = linearLayout;

        for (int i = 0; i < listFT.length; i++) {
            boolean switchedOn = false;
            //Log.i("featureList", listFT[i]);
            String feature = listFT[i];
            if (feature.contains("True_")) {
                switchedOn = true;
                feature = feature.replaceFirst("True_", "");
            }

            linearLayout = llBak;
            if (feature.contains("CollapseAdd_")) {
                //if (collapse != null)
                linearLayout = mCollapse;
                feature = feature.replaceFirst("CollapseAdd_", "");
            }
            String[] str = feature.split("_");

            //Assign feature number
            if (TextUtils.isDigitsOnly(str[0]) || str[0].matches("-[0-9]*")) {
                featNum = Integer.parseInt(str[0]);
                feature = feature.replaceFirst(str[0] + "_", "");
                subFeat++;
            } else {
                //Subtract feature number. We don't want to count ButtonLink, Category, RichTextView and RichWebView
                featNum = i - subFeat;
            }
            String[] strSplit = feature.split("_");
            switch (strSplit[0]) {
                case "Toggle":
                    linearLayout.addView(Switch(featNum, strSplit[1], switchedOn));
                    break;
                case "SeekBar":
                    linearLayout.addView(SeekBar(featNum, strSplit[1], Integer.parseInt(strSplit[2]), Integer.parseInt(strSplit[3])));
                    break;
                case "Button":
                    linearLayout.addView(Button(featNum, strSplit[1]));
                    break;
                case "ButtonOnOff":
                    linearLayout.addView(ButtonOnOff(featNum, strSplit[1], switchedOn));
                    break;
                case "Spinner":
                    linearLayout.addView(RichTextView(strSplit[1]));
                    linearLayout.addView(Spinner(featNum, strSplit[1], strSplit[2]));
                    break;
                case "InputText":
                    linearLayout.addView(TextField(featNum, strSplit[1], false, 0));
                    break;
                case "InputValue":
                    if (strSplit.length == 3)
                        linearLayout.addView(TextField(featNum, strSplit[2], true, Integer.parseInt(strSplit[1])));
                    if (strSplit.length == 2)
                        linearLayout.addView(TextField(featNum, strSplit[1], true, 0));
                    break;
                case "CheckBox":
                    linearLayout.addView(CheckBox(featNum, strSplit[1], switchedOn));
                    break;
                case "RadioButton":
                    linearLayout.addView(RadioButton(featNum, strSplit[1], strSplit[2]));
                    break;
                case "Collapse":
                    // strSplit[1] = タイトル, strSplit[2] = 色コード, strSplit[3] = 角丸
                    String colorHex = strSplit.length > 2 ? strSplit[2] : "#FFFFFF";
                    float radiusDp = strSplit.length > 3 ? Float.parseFloat(strSplit[3]) : 16f;
                    Collapse(linearLayout, strSplit[1], colorHex, radiusDp);
                    subFeat++;
					break;
                case "ButtonLink":
                    subFeat++;
                    linearLayout.addView(ButtonLink(strSplit[1], strSplit[2]));
                    break;
                case "Category":
                    subFeat++;
                    Category(linearLayout, strSplit[1]);  // ← これが正しい！
					break;
                case "RichTextView":
                    subFeat++;
                    linearLayout.addView(RichTextView(strSplit[1]));
                    break;
                case "RichWebView":
                    subFeat++;
                    linearLayout.addView(RichWebView(strSplit[1]));
                    break;
                case "LogOut":
                    if (strSplit.length == 3)
                        Logout(linearLayout, featNum, strSplit[2], Integer.parseInt(strSplit[1]));
                    if (strSplit.length == 2)
                        Logout(linearLayout, featNum, strSplit[1], 0);
                    break;  
                case "EndApp":
                    if (strSplit.length == 3) {
                        Endapp(linearLayout, featNum, strSplit[2], Integer.parseInt(strSplit[1]));
                    }
                    if (strSplit.length == 2) {
                        Endapp(linearLayout, featNum, strSplit[1], 0);
                    }
                    break;
                case "IconSize":
                    IconSize(linearLayout, featNum, strSplit[1], Integer.parseInt(strSplit[2]), Integer.parseInt(strSplit[3]));
                    break;  
                case "ICONALPHA":
                    ICONALPHA(linearLayout, featNum, strSplit[1], Integer.parseInt(strSplit[2]), Integer.parseInt(strSplit[3]));
                    break;      
                case "MenuColor":
                    MenuColor(linearLayout, featNum, strSplit[1], 0, 0);
                    break;
                case "MenuSize":
                    MenuSizeAdjust(linearLayout, featNum, strSplit[1]);
                    break;
            }
        }
    }
    
    private void MenuSizeAdjust(final LinearLayout linLayout, final int featNum, final String featName) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(10, 10, 10, 10);

        final Button sizeBtn = new Button(this);
        sizeBtn.setText("メニューサイズ変更");
        sizeBtn.setAllCaps(false);
        sizeBtn.setTextColor(Color.WHITE);

        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(20);
        bg.setStroke(2, Color.WHITE);
        bg.setColor(Color.BLACK);
        sizeBtn.setBackground(bg);

        sizeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    LinearLayout container = new LinearLayout(FloatingModMenuService.this);
                    container.setOrientation(LinearLayout.VERTICAL);
                    container.setPadding(30, 20, 30, 10);

                    final EditText widthInput = new EditText(FloatingModMenuService.this);
                    widthInput.setHint("幅 (例: 250)");
                    widthInput.setInputType(InputType.TYPE_CLASS_NUMBER);
                    container.addView(widthInput);

                    final EditText heightInput = new EditText(FloatingModMenuService.this);
                    heightInput.setHint("高さ (例: 210)");
                    heightInput.setInputType(InputType.TYPE_CLASS_NUMBER);
                    container.addView(heightInput);

                    AlertDialog.Builder builder = new AlertDialog.Builder(FloatingModMenuService.this);
                    builder.setTitle("メニューサイズ変更");
                    builder.setView(container);

                    builder.setPositiveButton("適用", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                try {
                                    int newWidth = Integer.parseInt(widthInput.getText().toString().trim());
                                    int newHeight = Integer.parseInt(heightInput.getText().toString().trim());

                                    if (newWidth < 100 || newHeight < 100) {
                                        Toast.makeText(FloatingModMenuService.this, "サイズが小さすぎます", Toast.LENGTH_SHORT).show();
                                        return;
                                    }

                                    // dp変換
                                    int widthPx = dp(newWidth);
                                    int heightPx = dp(newHeight);

                                    if (mExpanded != null) {
                                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(widthPx, WRAP_CONTENT);
                                        mExpanded.setLayoutParams(layoutParams);
                                    }

                                    if (scrollView != null) {
                                        scrollView.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, heightPx));
                                    }

                                    Toast.makeText(FloatingModMenuService.this, "サイズ変更完了", Toast.LENGTH_SHORT).show();
                                } catch (Exception e) {
                                    Toast.makeText(FloatingModMenuService.this, "数値を正しく入力してください", Toast.LENGTH_SHORT).show();
                                    e.printStackTrace();
                                }
                            }
                        });

                    builder.setNegativeButton("キャンセル", null);

                    AlertDialog dialog = builder.create();
                    if (Build.VERSION.SDK_INT >= 26) {
                        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
                    } else {
                        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_PHONE);
                    }
                    dialog.show();
                }
            });

        layout.addView(sizeBtn);
        linLayout.addView(layout);
    }

    private void MenuColor(final LinearLayout linLayout, final int featNum, final String featName, int min, int max) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(10, 10, 10, 10);

        final TextView label = new TextView(this);
        label.setText(Html.fromHtml(featName + ": <font color='" + NumberTxtColor + "'>現在：" + Integer.toHexString(BTN_COLOR).toUpperCase() + "</font>"));
        label.setTextColor(TEXT_COLOR_2);
        layout.addView(label);

        final Button colorButton = new Button(this);
        colorButton.setText("枠色を選択");
        colorButton.setAllCaps(false);
        colorButton.setTextColor(Color.WHITE);

        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(20);
        bg.setStroke(2, Color.WHITE);
        bg.setColor(Color.BLACK);
        colorButton.setBackground(bg);

        colorButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final EditText input = new EditText(FloatingModMenuService.this);
                    input.setHint("#FFFFFFなど");
                    input.setTextColor(Color.BLACK);

                    AlertDialog.Builder builder = new AlertDialog.Builder(FloatingModMenuService.this);
                    builder.setTitle("カラーコード入力");
                    builder.setMessage("例:#FFFFFF");
                    builder.setView(input);

                    builder.setNeutralButton("初期値に戻す", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                int defaultColor = Color.parseColor("#41F0F8"); // ← 初期値（必要に応じて変更）
                                float radius = 50f; // ← もともと使っている角丸

                                GradientDrawable resetBg = new GradientDrawable();
                                resetBg.setColor(MENU_BG_COLOR);
                                resetBg.setCornerRadius(radius);
                                resetBg.setStroke(4, defaultColor);
                                mExpanded.setBackground(resetBg);

                                Preferences.changeFeatureString("MenuBorderColor", featNum, "#41F0F8"); // 保存も初期化
                                label.setText(Html.fromHtml(featName + ": <font color='" + NumberTxtColor + "'>DEFAULT</font>"));
                            }
                        });

                    builder.setPositiveButton("適用", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String colorStr = input.getText().toString().trim();
                                try {
                                    int newColor = Color.parseColor(colorStr);

                                    // 元の角丸を維持（ここで mExpanded の background を GradientDrawable にキャスト）
                                    GradientDrawable currentBg = (GradientDrawable) mExpanded.getBackground();
                                    float radius = 0f;
                                    try {
                                        // Androidは getCornerRadius() を直接サポートしないため再取得できないが、
                                        // 初期化時に使った値（例: 50）を保持しているなら使う（例: MENU_CORNER or 50f）
                                        radius = 50f; // 元の角丸半径をここに再指定
                                    } catch (Exception e) {
                                        // fallback
                                        radius = 16f;
                                    }

                                    GradientDrawable newBg = new GradientDrawable();
                                    newBg.setColor(MENU_BG_COLOR);
                                    newBg.setCornerRadius(radius);
                                    newBg.setStroke(4, newColor);

                                    mExpanded.setBackground(newBg);

                                    Preferences.changeFeatureString("MenuBorderColor", featNum, colorStr);
                                    label.setText(Html.fromHtml(featName + ": <font color='" + NumberTxtColor + "'>" + colorStr.toUpperCase() + "</font>"));
                                } catch (Exception e) {
                                    Toast.makeText(FloatingModMenuService.this, "カラーコードの形式が正しくありません", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    builder.setNegativeButton("キャンセル", null);

                    AlertDialog dialog = builder.create();
                    if (Build.VERSION.SDK_INT >= 26) {
                        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
                    } else {
                        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_PHONE);
                    }
                    dialog.show();
                }
            });

        layout.addView(colorButton);
        linLayout.addView(layout);
    }

    private Drawable createCustomThumb() {
        GradientDrawable thumb = new GradientDrawable();
        thumb.setColor(Color.BLACK); // 中の色
        thumb.setStroke(2, Color.WHITE); // 枠の色

        float radius = dpToPx(10);
        thumb.setCornerRadii(new float[]{
                                 radius, radius,  // 左上（丸）
                                 0f, 0f,          // 右上（四角）
                                 radius, radius,  // 右下（丸）
                                 0f, 0f           // 左下（四角）
                             });

        thumb.setSize(40, 40);
        return thumb;
    }

    private void IconSize(LinearLayout linLayout, final int featNum, final String featName, final int min, int max) {
        int loadedProg = Preferences.loadPrefInt(featName, featNum);
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setPadding(10, 5, 0, 5);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setGravity(Gravity.CENTER);

        int applyDimension = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, ICON_SIZE, getResources().getDisplayMetrics());
        startimage.getLayoutParams().height = applyDimension;
        startimage.getLayoutParams().width = applyDimension;

        final TextView textView = new TextView(this);
        String displayValue = (loadedProg == 0 || loadedProg < min) ? "初期値" : String.valueOf(loadedProg);
        textView.setText(Html.fromHtml(featName + " -> <font color='" + NumberTxtColor + "'>" + displayValue));
        textView.setTextColor(TEXT_COLOR_2);

        SeekBar seekBar = new SeekBar(this);
        seekBar.setPadding(25, 10, 35, 10);
        seekBar.setMax(max);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            seekBar.setMin(min);
        seekBar.setProgress((loadedProg == 0 || loadedProg < min) ? min : loadedProg);

        seekBar.setThumb(createCustomThumb());
        seekBar.getProgressDrawable().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                public void onStartTrackingTouch(SeekBar seekBar) {}
                public void onStopTrackingTouch(SeekBar seekBar) {}

                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    int newProgress = Math.max(progress, min);
                    seekBar.setProgress(newProgress);
                    int newIconSize = ICON_SIZE + newProgress;
                    int applyDimension = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, newIconSize, getResources().getDisplayMetrics());
                    startimage.getLayoutParams().height = applyDimension;
                    startimage.getLayoutParams().width = applyDimension;
                    String displayValue = (newProgress == min) ? "初期値" : String.valueOf(newProgress);
                    textView.setText(Html.fromHtml(featName + " -> <font color='" + NumberTxtColor + "'>" + displayValue));
                }
            });

        linearLayout.addView(textView);
        linearLayout.addView(seekBar);
        linLayout.addView(linearLayout);
    }

    private void ICONALPHA(LinearLayout linLayout, final int featNum, final String featName, final int min, int max) {
        int loadedProg = Preferences.loadPrefInt(featName, featNum);
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setPadding(10, 5, 0, 5);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setGravity(Gravity.CENTER);

        int applyDimension = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, ICON_SIZE, getResources().getDisplayMetrics());
        startimage.getLayoutParams().height = applyDimension;
        startimage.getLayoutParams().width = applyDimension;

        final TextView textView = new TextView(this);
        String displayValue = (loadedProg == 0 || loadedProg < min) ? "初期値" : String.valueOf(loadedProg);
        textView.setText(Html.fromHtml(featName + " -> <font color='" + NumberTxtColor + "'>" + displayValue));
        textView.setTextColor(TEXT_COLOR_2);

        SeekBar seekBar = new SeekBar(this);
        seekBar.setPadding(25, 10, 35, 10);
        seekBar.setMax(max);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            seekBar.setMin(min);
        seekBar.setProgress((loadedProg == 0 || loadedProg < min) ? min : loadedProg);

        seekBar.setThumb(createCustomThumb());
        seekBar.getProgressDrawable().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                public void onStartTrackingTouch(SeekBar seekBar) {}
                public void onStopTrackingTouch(SeekBar seekBar) {}

                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    int newProgress = Math.max(progress, min);
                    seekBar.setProgress(newProgress);

                    float newAlpha = 1.0f - (newProgress * 0.01f);
                    startimage.setAlpha(newAlpha);

                    String displayValue = (newProgress == min) ? "初期値" : String.valueOf(newProgress);
                    textView.setText(Html.fromHtml(featName + " -> <font color='" + NumberTxtColor + "'>" + displayValue));
                }
            });

        linearLayout.addView(textView);
        linearLayout.addView(seekBar);
        linLayout.addView(linearLayout);
    }

    private View Switch(final int featNum, final String featName, boolean swiOn) {
        final Switch switchR = new Switch(this);
        ColorStateList buttonStates = new ColorStateList(
                new int[][]{
                        new int[]{-android.R.attr.state_enabled},
                        new int[]{android.R.attr.state_checked},
                        new int[]{}
                },
                new int[]{
                        Color.BLUE,
                        ToggleON, // ON
                        ToggleOFF // OFF
                }
        );
        //Set colors of the switch. Comment out if you don't like it
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            switchR.getThumbDrawable().setTintList(buttonStates);
            switchR.getTrackDrawable().setTintList(buttonStates);
        }
        switchR.setText(featName);
        switchR.setTextColor(TEXT_COLOR_2);
        switchR.setPadding(10, 5, 0, 5);
        switchR.setChecked(Preferences.loadPrefBool(featName, featNum, swiOn));
        switchR.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton compoundButton, boolean bool) {
                Preferences.changeFeatureBool(featName, featNum, bool);
                switch (featNum) {
                    case -1: //Save perferences
                        Preferences.with(switchR.getContext()).writeBoolean(-1, bool);
                        if (bool == false)
                            Preferences.with(switchR.getContext()).clear(); //Clear perferences if switched off
                        break;
                    case -3:
                        Preferences.isExpanded = bool;
                        scrollView.setLayoutParams(bool ? scrlLLExpanded : scrlLL);
                        break;
                }
            }
        });
        return switchR;
    }
    
    int ThumbBorderColor = Color.parseColor("#ffffff"); // ピンク

    private View SeekBar(final int featNum, final String featName, final int min, int max) {
        int loadedProg = Preferences.loadPrefInt(featName, featNum);
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setPadding(10, 5, 0, 5);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setGravity(Gravity.CENTER);

        final TextView textView = new TextView(this);
        textView.setText(Html.fromHtml(featName + ": <font color='" + NumberTxtColor + "'>" + ((loadedProg == 0) ? min : loadedProg)));
        textView.setTextColor(TEXT_COLOR_2);

        SeekBar seekBar = new SeekBar(this);
        seekBar.setPadding(25, 10, 35, 10);
        seekBar.setMax(max);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            seekBar.setMin(min);
        seekBar.setProgress((loadedProg == 0) ? min : loadedProg);
        seekBar.getProgressDrawable().setColorFilter(SeekBarProgressColor, PorterDuff.Mode.SRC_ATOP);

        // 四角いThumb（左上＆右下角丸、枠あり）
        GradientDrawable thumb = new GradientDrawable();
        thumb.setColor(SeekBarColor);
        thumb.setSize(40, 40);
        thumb.setStroke(2, ThumbBorderColor); // 枠線を追加

        float radius = dpToPx(10);
        thumb.setCornerRadii(new float[]{
                                 radius, radius, // 左上
                                 0f, 0f,         // 右上
                                 radius, radius, // 右下
                                 0f, 0f          // 左下
                             });

        seekBar.setThumb(thumb);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                public void onStartTrackingTouch(SeekBar seekBar) {}
                public void onStopTrackingTouch(SeekBar seekBar) {}

                public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
                    int value = Math.max(i, min);
                    seekBar.setProgress(value);
                    Preferences.changeFeatureInt(featName, featNum, value);
                    textView.setText(Html.fromHtml(featName + ": <font color='" + NumberTxtColor + "'>" + value));
                }
            });

        linearLayout.addView(textView);
        linearLayout.addView(seekBar);
        return linearLayout;
    }

    private View Button(final int featNum, final String featName) {
        final Button button = new Button(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);
        layoutParams.setMargins(7, 5, 7, 5);
        button.setLayoutParams(layoutParams);
        button.setTextColor(TEXT_COLOR_2);
        button.setAllCaps(false); //Disable caps to support html
        button.setText(Html.fromHtml(featName));
        button.setBackgroundColor(BTN_COLOR);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                switch (featNum) {
                    case -4:
                        Logcat.Save(getApplicationContext());
                        break;
                    case -5:
                        Logcat.Clear(getApplicationContext());
                        break;
                    case -6:
                        scrollView.removeView(mSettings);
                        scrollView.addView(patches);
                        break;
                    case -100:
                        stopChecking = true;
                        break;
                }
                Preferences.changeFeatureInt(featName, featNum, 0);
            }
        });

        return button;
    }

    private View ButtonLink(final String featName, final String url) {
        final Button button = new Button(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(7, 5, 7, 5);
        button.setLayoutParams(layoutParams);
        button.setAllCaps(false);
        button.setTextColor(TEXT_COLOR_2);
        button.setText(Html.fromHtml(featName));

        // カスタム背景（黒背景・白枠）
        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.BLACK); // 背景：黒
        background.setCornerRadius(dpToPx(12));
        background.setStroke(2, Color.WHITE); // 枠線：白
        button.setBackground(background);

        // クリック時の確認ダイアログ
        button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(FloatingModMenuService.this);
                    builder.setTitle("リンクを開きますか？");
                    builder.setMessage("このリンクを開きます:\n" + url);
                    builder.setPositiveButton("開く", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.setData(Uri.parse(url));
                                startActivity(intent);
                            }
                        });
                    builder.setNegativeButton("キャンセル", null);
                    AlertDialog dialog = builder.create();
                    if (Build.VERSION.SDK_INT >= 26) {
                        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
                    } else {
                        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_PHONE);
                    }
                    dialog.show();
                }
            });

        return button;
    }

    private View ButtonOnOff(final int featNum, String featName, boolean switchedOn) {
        final Button button = new Button(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);
        layoutParams.setMargins(7, 5, 7, 5);
        button.setLayoutParams(layoutParams);
        button.setTextColor(TEXT_COLOR_2);
        button.setAllCaps(false); //Disable caps to support html

        final String finalfeatName = featName.replace("OnOff_", "");
        boolean isOn = Preferences.loadPrefBool(featName, featNum, switchedOn);
        if (isOn) {
			GradientDrawable gradientdrawable7 = new GradientDrawable();
			gradientdrawable7.setCornerRadius(20); //Set corner
			gradientdrawable7.setStroke(2, Color.WHITE); //Set border
			button.setBackground(gradientdrawable7);
            button.setText(Html.fromHtml(finalfeatName + " ON"));
			button.setTextColor(Color.RED);
            //button.setBackgroundColor(BtnON);
            isOn = false;
        } else {
			GradientDrawable gradientdrawable7 = new GradientDrawable();
			gradientdrawable7.setCornerRadius(20); //Set corner
			//gradientdrawable6.setColor(Color.TRANSPARENT); //Set background color
			gradientdrawable7.setStroke(2, Color.WHITE); //Set border
			//gradientdrawable.setSize(MENU_WIDTH, MENU_HEIGHT);
			button.setBackground(gradientdrawable7);
            button.setText(Html.fromHtml(finalfeatName + " OFF"));
			button.setTextColor(Color.WHITE);
            //button.setBackgroundColor(BtnOFF);
            isOn = true;
        }
        final boolean finalIsOn = isOn;
        button.setOnClickListener(new View.OnClickListener() {
				boolean isOn = finalIsOn;

				public void onClick(View v) {
					Preferences.changeFeatureBool(finalfeatName, featNum, isOn);
					//Log.d(TAG, finalfeatName + " " + featNum + " " + isActive2);
					if (isOn) {
						GradientDrawable gradientdrawable7 = new GradientDrawable();
						gradientdrawable7.setCornerRadius(20); //Set corner
						gradientdrawable7.setStroke(2, Color.WHITE); //Set border
						button.setBackground(gradientdrawable7);
						button.setText(Html.fromHtml(finalfeatName + " ON"));
						button.setTextColor(Color.RED);
						//button.setBackgroundColor(BtnON);
						isOn = false;
					} else {
						GradientDrawable gradientdrawable7 = new GradientDrawable();
						gradientdrawable7.setCornerRadius(20); //Set corner
						gradientdrawable7.setStroke(2, Color.WHITE); //Set border
						button.setBackground(gradientdrawable7);
						button.setText(Html.fromHtml(finalfeatName + " OFF"));
						button.setTextColor(Color.WHITE);
						//button.setBackgroundColor(BtnOFF);
						isOn = true;
					}
				}
			});
        return button;
    }

    private View Spinner(final int featNum, final String featName, final String list) {
        Log.d(TAG, "spinner " + featNum + " " + featName + " " + list);
        final List<String> lists = new LinkedList<>(Arrays.asList(list.split(",")));

        // Create another LinearLayout as a workaround to use it as a background
        // to keep the down arrow symbol. No arrow symbol if setBackgroundColor set
        LinearLayout linearLayout2 = new LinearLayout(this);
        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);
        layoutParams2.setMargins(10, 2, 10, 5);
        linearLayout2.setOrientation(LinearLayout.VERTICAL);
        linearLayout2.setBackgroundColor(BTN_COLOR);
        linearLayout2.setLayoutParams(layoutParams2);

        final Spinner spinner = new Spinner(this, Spinner.MODE_DROPDOWN);
        spinner.setPadding(5, 10, 5, 8);
        spinner.setLayoutParams(layoutParams2);
        spinner.getBackground().setColorFilter(1, PorterDuff.Mode.SRC_ATOP); //trick to show white down arrow color
        //Creating the ArrayAdapter instance having the list
        ArrayAdapter aa = new ArrayAdapter(this, android.R.layout.simple_spinner_item, lists);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner'
        spinner.setAdapter(aa);
        spinner.setSelection(Preferences.loadPrefInt(featName, featNum));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                Preferences.changeFeatureInt(spinner.getSelectedItem().toString(), featNum, position);
                ((TextView) parentView.getChildAt(0)).setTextColor(TEXT_COLOR_2);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        linearLayout2.addView(spinner);
        return linearLayout2;
    }

    private View TextField(final int feature, final String featName, final boolean numOnly, final int maxValue) {
        final EditTextString edittextstring = new EditTextString();
        final EditTextNum edittextnum = new EditTextNum();
        LinearLayout linearLayout = new LinearLayout(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);
        layoutParams.setMargins(7, 5, 7, 5);

        final Button button = new Button(this);
        if (numOnly) {
            int num = Preferences.loadPrefInt(featName, feature);
            edittextnum.setNum((num == 0) ? 1 : num);
            button.setText(Html.fromHtml(featName + ": <font color='" + NumberTxtColor + "'>" + ((num == 0) ? 1 : num) + "</font>"));
        } else {
            String string = Preferences.loadPrefString(featName, feature);
            edittextstring.setString((string == "") ? "" : string);
            button.setText(Html.fromHtml(featName + ": <font color='" + NumberTxtColor + "'>" + string + "</font>"));
        }
        button.setAllCaps(false);
        button.setLayoutParams(layoutParams);
        button.setBackgroundColor(BTN_COLOR);
        button.setTextColor(TEXT_COLOR_2);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog alert = new AlertDialog.Builder(getApplicationContext(), 2).create();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    Objects.requireNonNull(alert.getWindow()).setType(Build.VERSION.SDK_INT >= 26 ? 2038 : 2002);
                }
                alert.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    public void onCancel(DialogInterface dialog) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(getApplicationContext().INPUT_METHOD_SERVICE);
                        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                    }
                });

                //LinearLayout
                LinearLayout linearLayout1 = new LinearLayout(getApplicationContext());
                linearLayout1.setPadding(5, 5, 5, 5);
                linearLayout1.setOrientation(LinearLayout.VERTICAL);
                linearLayout1.setBackgroundColor(MENU_FEATURE_BG_COLOR);

                //TextView
                final TextView TextViewNote = new TextView(getApplicationContext());
                TextViewNote.setText("Tap OK to apply changes. Tap outside to cancel");
                if (maxValue != 0)
                TextViewNote.setText("Tap OK to apply changes. Tap outside to cancel\nMax value: " + maxValue);
                TextViewNote.setTextColor(TEXT_COLOR_2);

                //Edit text
                final EditText edittext = new EditText(getApplicationContext());
                edittext.setMaxLines(1);
                edittext.setWidth(convertDipToPixels(300));
                edittext.setTextColor(TEXT_COLOR_2);
                if (numOnly) {
                    edittext.setInputType(InputType.TYPE_CLASS_NUMBER);
                    edittext.setKeyListener(DigitsKeyListener.getInstance("0123456789-"));
                    InputFilter[] FilterArray = new InputFilter[1];
                    FilterArray[0] = new InputFilter.LengthFilter(10);
                    edittext.setFilters(FilterArray);
                } else {
                    edittext.setText(edittextstring.getString());
                }
                edittext.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(getApplicationContext().INPUT_METHOD_SERVICE);
                        if (hasFocus) {
                            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                        } else {
                            imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                        }
                    }
                });
                edittext.requestFocus();

                //Button
                Button btndialog = new Button(getApplicationContext());
                btndialog.setBackgroundColor(BTN_COLOR);
                btndialog.setTextColor(TEXT_COLOR_2);
                btndialog.setText("OK");
                btndialog.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (numOnly) {
                            int num;
                            try {
                                num = Integer.parseInt(TextUtils.isEmpty(edittext.getText().toString()) ? "0" : edittext.getText().toString());
                                if (maxValue != 0 &&  num >= maxValue)
                                    num = maxValue;
                            } catch (NumberFormatException ex) {
                                num = 2147483640;
                            }
                            edittextnum.setNum(num);
                            button.setText(Html.fromHtml(featName + ": <font color='#41c300'>" + num + "</font>"));
                            alert.dismiss();
                            Preferences.changeFeatureInt(featName, feature, num);
                        } else {
                            String str = edittext.getText().toString();
                            edittextstring.setString(edittext.getText().toString());
                            button.setText(Html.fromHtml(featName + ": <font color='#41c300'>" + str + "</font>"));
                            alert.dismiss();
                            Preferences.changeFeatureString(featName, feature, str);
                        }
                        edittext.setFocusable(false);
                    }
                });

                linearLayout1.addView(TextViewNote);
                linearLayout1.addView(edittext);
                linearLayout1.addView(btndialog);
                alert.setView(linearLayout1);
                alert.show();
            }
        });

        linearLayout.addView(button);
        return linearLayout;
    }

    private View CheckBox(final int featNum, final String featName, boolean switchedOn) {
        final CheckBox checkBox = new CheckBox(this);
        checkBox.setText(featName);
        checkBox.setTextColor(TEXT_COLOR_2);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            checkBox.setButtonTintList(ColorStateList.valueOf(CheckBoxColor));
        checkBox.setChecked(Preferences.loadPrefBool(featName, featNum, switchedOn));
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (checkBox.isChecked()) {
                    Preferences.changeFeatureBool(featName, featNum, isChecked);
                } else {
                    Preferences.changeFeatureBool(featName, featNum, isChecked);
                }
            }
        });
        return checkBox;
    }

    private View RadioButton(final int featNum, String featName, final String list) {
        //Credit: LoraZalora
        final List<String> lists = new LinkedList<>(Arrays.asList(list.split(",")));

        final TextView textView = new TextView(this);
        textView.setText(featName + ":");
        textView.setTextColor(TEXT_COLOR_2);

        final RadioGroup radioGroup = new RadioGroup(this);
        radioGroup.setPadding(10, 5, 10, 5);
        radioGroup.setOrientation(LinearLayout.VERTICAL);
        radioGroup.addView(textView);

        for (int i = 0; i < lists.size(); i++) {
            final RadioButton Radioo = new RadioButton(this);
            final String finalfeatName = featName, radioName = lists.get(i);
            View.OnClickListener first_radio_listener = new View.OnClickListener() {
                public void onClick(View v) {
                    textView.setText(Html.fromHtml(finalfeatName + ": <font color='" + NumberTxtColor + "'>" + radioName));
                    Preferences.changeFeatureInt(finalfeatName, featNum, radioGroup.indexOfChild(Radioo));
                }
            };
            System.out.println(lists.get(i));
            Radioo.setText(lists.get(i));
            Radioo.setTextColor(Color.LTGRAY);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                Radioo.setButtonTintList(ColorStateList.valueOf(RadioColor));
            Radioo.setOnClickListener(first_radio_listener);
            radioGroup.addView(Radioo);
        }

        int index = Preferences.loadPrefInt(featName, featNum);
        if (index > 0) { //Preventing it to get an index less than 1. below 1 = null = crash
            textView.setText(Html.fromHtml(featName + ": <font color='" + NumberTxtColor + "'>" + lists.get(index - 1)));
            ((RadioButton) radioGroup.getChildAt(index)).setChecked(true);
        }

        return radioGroup;
    }


    private void Collapse(LinearLayout linLayout, final String text, final String borderColorHex, final float cornerRadiusDp) {
        LinearLayout.LayoutParams layoutParamsLL = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        layoutParamsLL.setMargins(0, 5, 0, 0);

        LinearLayout collapse = new LinearLayout(this);
        collapse.setLayoutParams(layoutParamsLL);
        collapse.setOrientation(LinearLayout.VERTICAL);

        float cornerRadius = dpToPx(cornerRadiusDp);

        // 枠線 + 角丸の共通背景
        GradientDrawable border = new GradientDrawable();
        border.setColor(Color.TRANSPARENT);
        border.setStroke(4, Color.parseColor(borderColorHex)); // 緑など
        border.setCornerRadius(cornerRadius);
        collapse.setBackground(border); // collapse全体に適用

        // 折りたたみ部分（中身）
        final LinearLayout collapseSub = new LinearLayout(this);
        collapseSub.setVerticalGravity(Gravity.CENTER);
        collapseSub.setPadding(0, 5, 0, 5);
        collapseSub.setOrientation(LinearLayout.VERTICAL);
        collapseSub.setVisibility(View.GONE);

        mCollapse = collapseSub;

        // タイトル部分
        final TextView textView = new TextView(this);
        textView.setText("▽ " + text + " ▽");
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(TEXT_COLOR_2);
        textView.setTypeface(null, Typeface.BOLD);
        textView.setPadding(0, 20, 0, 20);
        textView.setBackgroundColor(Color.TRANSPARENT); // 背景色なしにする（枠の中に収める）

        // トグル機能
        textView.setOnClickListener(new View.OnClickListener() {
                boolean isChecked;

                @Override
                public void onClick(View v) {
                    isChecked = !isChecked;
                    if (isChecked) {
                        collapseSub.setVisibility(View.VISIBLE);
                        textView.setText("△ " + text + " △");
                    } else {
                        collapseSub.setVisibility(View.GONE);
                        textView.setText("▽ " + text + " ▽");
                    }
                }
            });

        // レイアウトに追加
        collapse.addView(textView);
        collapse.addView(collapseSub);
        linLayout.addView(collapse);
    }

    private void Category(LinearLayout linLayout, String text) {
        TextView textView = new TextView(this);

        // 角丸背景（半透明黒＋白い枠線）
        float cornerRadius = dpToPx(28);
        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.parseColor("#69000000")); // 背景色（半透明黒）
        background.setCornerRadius(cornerRadius);
        background.setStroke(2, Color.WHITE); // ← 白い枠線を追加（太さ 2px）

        textView.setBackground(background);

        // テキストスタイル
        textView.setText(Html.fromHtml(text));
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(Color.WHITE);
        textView.setTypeface(Typeface.SERIF, Typeface.BOLD);

        // 文字サイズを変更
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);

        // パディング
        int paddingVertical = (int) dpToPx(10);
        int paddingHorizontal = (int) dpToPx(19);
        textView.setPadding(paddingHorizontal, paddingVertical, paddingHorizontal, paddingVertical);

        // サイズとマージン
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            (int) dpToPx(231),
            (int) dpToPx(35)
        );
        int margin = (int) dpToPx(8);
        params.setMargins(margin, margin, margin, margin);
        textView.setLayoutParams(params);

        linLayout.addView(textView);
    }

    // dp → px 変換（Context不要、thisで使える）
    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    private void Logout(LinearLayout linLayout, final int featNum, final String featName, final int maxValue) {
        LinearLayout linearLayout = new LinearLayout(this);

        int verticalMarginInPixels = (int) dpToPx(2);
        int horizontalMarginInPixels = (int) dpToPx(6);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(horizontalMarginInPixels, verticalMarginInPixels, horizontalMarginInPixels, verticalMarginInPixels);

        final Button button = new Button(this);
        button.setText(Html.fromHtml(featName));
        button.setAllCaps(false);
        button.setLayoutParams(layoutParams);
        button.setTextColor(TEXT_COLOR_2);

        float cornerRadius = dpToPx(20);
        float[] radii = { cornerRadius, cornerRadius, 0, 0, cornerRadius, cornerRadius, 0, 0 };

        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.BLACK); // 黒背景
        background.setCornerRadii(radii);
        background.setStroke(2, Color.WHITE); // 白枠
        button.setBackground(background);

        button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder alertName = new AlertDialog.Builder(FloatingModMenuService.this);
                    alertName.setTitle("ログアウト");
                    alertName.setMessage("ログアウトしますか?");

                    alertName.setPositiveButton("はい", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                removeFile("/data/data/com.Level5.YWP/files/.library/ywp_cud/data01.cud");
                                removeFile("/data/data/com.Level5.YWP/files/.library/ywp_cud/data00.cud");
                                removeFile("/data/user/0/com.Level5.YWP/files/.library/ywp_cud/data01.cud");
                                removeFile("/data/user/0/com.Level5.YWP/files/.library/ywp_cud/data00.cud");
                                Preferences.changeFeatureInt(featName, featNum, maxValue);
                                button.setText(Html.fromHtml("ログアウト完了"));
                            }
                        });

                    alertName.setNegativeButton("いいえ", null);
                    AlertDialog dialog = alertName.create();
                    if (Build.VERSION.SDK_INT >= 26) {
                        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
                    } else {
                        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_PHONE);
                    }
                    dialog.show();
                }
            });

        linearLayout.addView(button);
        linLayout.addView(linearLayout);
    }

    private String getAppName(String packageName) {
        try {
            ApplicationInfo ai = getPackageManager().getApplicationInfo(packageName, 0);
            return (String) getPackageManager().getApplicationLabel(ai);
        } catch (PackageManager.NameNotFoundException e) {
            return packageName;
        }
    }

    private void Endapp(LinearLayout linLayout, final int featNum, final String featName, final int maxValue) {
        LinearLayout linearLayout = new LinearLayout(this);

        int verticalMarginInPixels = (int) dpToPx(2);
        int horizontalMarginInPixels = (int) dpToPx(6);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(horizontalMarginInPixels, verticalMarginInPixels, horizontalMarginInPixels, verticalMarginInPixels);

        final Button button = new Button(this);
        button.setText(Html.fromHtml(featName));
        button.setAllCaps(false);
        button.setLayoutParams(layoutParams);
        button.setTextColor(TEXT_COLOR_2);

        float cornerRadius = dpToPx(20);
        float[] radii = { cornerRadius, cornerRadius, 0, 0, cornerRadius, cornerRadius, 0, 0 };

        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.BLACK); // 黒背景
        background.setCornerRadii(radii);
        background.setStroke(2, Color.WHITE); // 白枠
        button.setBackground(background);

        button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String packageName = getPackageName();
                    String appName = getAppName(packageName);

                    AlertDialog.Builder alertName = new AlertDialog.Builder(FloatingModMenuService.this);
                    alertName.setTitle(appName + "の終了");
                    alertName.setMessage(appName + "を終了しますか？");

                    alertName.setPositiveButton("はい", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                Preferences.changeFeatureInt(featName, featNum, maxValue);
                                Process.killProcess(Process.myPid());
                            }
                        });

                    alertName.setNegativeButton("いいえ", null);

                    AlertDialog dialog = alertName.create();
                    if (Build.VERSION.SDK_INT >= 26) {
                        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
                    } else {
                        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_PHONE);
                    }
                    dialog.show();
                }
            });

        linearLayout.addView(button);
        linLayout.addView(linearLayout);
    }


    private void removeFile(String path) {
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
    }
    
    private View RichTextView(String text) {
        TextView textView = new TextView(this);
        textView.setText(Html.fromHtml(text));
        textView.setTextColor(TEXT_COLOR_2);
        textView.setPadding(10, 5, 10, 5);
        return textView;
    }

    private View RichWebView(String text) {
        WebView wView = new WebView(this);
        wView.loadData(text, "text/html", "utf-8");
        wView.setBackgroundColor(0x00000000); //Transparent
        wView.setPadding(0, 5, 0, 5);
        wView.getSettings().setAppCacheEnabled(false);
        return wView;
    }

    //Override our Start Command so the Service doesnt try to recreate itself when the App is closed
    public int onStartCommand(Intent intent, int i, int i2) {
        return Service.START_NOT_STICKY;
    }

    private boolean isViewCollapsed() {
        return rootFrame == null || mCollapsed.getVisibility() == View.VISIBLE;
    }

    //For our image a little converter
    private int convertDipToPixels(int i) {
        return (int) ((((float) i) * getResources().getDisplayMetrics().density) + 0.5f);
    }

    private int dp(int i) {
        return (int) TypedValue.applyDimension(1, (float) i, getResources().getDisplayMetrics());
    }

    //Check if we are still in the game. If now our menu and menu button will dissapear
    private boolean isNotInGame() {
        RunningAppProcessInfo runningAppProcessInfo = new RunningAppProcessInfo();
        ActivityManager.getMyMemoryState(runningAppProcessInfo);
        return runningAppProcessInfo.importance != 100;
    }

    //Destroy our View
    public void onDestroy() {
        super.onDestroy();
        if (rootFrame != null) {
            mWindowManager.removeView(rootFrame);
        }
    }

    //Same as above so it wont crash in the background and therefore use alot of Battery life
    public void onTaskRemoved(Intent intent) {
        super.onTaskRemoved(intent);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        stopSelf();
    }

    private void Thread() {
        if (rootFrame == null) {
            return;
        }
        if (isNotInGame()) {
            rootFrame.setVisibility(View.INVISIBLE);
        } else {
            rootFrame.setVisibility(View.VISIBLE);
        }
    }

    private class EditTextString {
        private String text;

        public void setString(String s) {
            text = s;
        }

        public String getString() {
            return text;
        }
    }

    private class EditTextNum {
        private int val;

        public void setNum(int i) {
            val = i;
        }

        public int getNum() {
            return val;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
