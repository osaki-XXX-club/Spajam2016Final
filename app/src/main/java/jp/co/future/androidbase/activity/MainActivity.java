package jp.co.future.androidbase.activity;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.nightonke.boommenu.BoomMenuButton;
import com.nightonke.boommenu.Types.BoomType;
import com.nightonke.boommenu.Types.ButtonType;
import com.nightonke.boommenu.Types.PlaceType;
import com.nightonke.boommenu.Util;
import com.skyfishjy.library.RippleBackground;

import java.util.ArrayList;
import java.util.Random;

import jp.co.future.androidbase.R;
import jp.co.future.androidbase.fragment.MainActivityFragment;

public class MainActivity extends AppCompatActivity implements MainActivityFragment.OnFragmentInteractionListener {


    /**
     * ログ出力用タグ
     */
    private static final String TAG = MainActivity.class.getSimpleName();

    /**
     * クラス名
     */
    private static final String className = MainActivity.class.getName().toString();

    /**
     * バインドするフラグメント
     */
    private MainActivityFragment fragment;

    private ImageView foundDevice;

    private BoomMenuButton boomMenuButton;

    private boolean blesearch;

    private RippleBackground rippleBackground;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            // フラグメントを生成
            fragment = new MainActivityFragment();
            // フラグメントをアクティビティに追加する FragmentTransaction を利用する
            FragmentManager manager = getFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.add(R.id.container, fragment, "fragment");
            transaction.commit();
        }


    }

    @Override
    protected void onResume() {

        super.onResume();

        //メニューボタン
        boomMenuButton = (BoomMenuButton) findViewById(R.id.boom);

        //各メニューが押された時の処理
        boomMenuButton.setOnSubButtonClickListener(new BoomMenuButton.OnSubButtonClickListener() {
            @Override
            public void onClick(int buttonIndex) {
                // return the index of the sub button clicked
                if (0 == buttonIndex) {
                    //BLE切り替え
                    if (blesearch) {
                        rippleBackground.stopRippleAnimation();
                        blesearch = false;
                        foundDevice.setVisibility(View.INVISIBLE);

                        //TODO BLE止める
                    } else {
                        rippleBackground.startRippleAnimation();
                        blesearch = true;

                        //TODO BLEスタート
                    }


                } else if (1 == buttonIndex) {
                    //マッチング画面遷移
                } else if (2 == buttonIndex) {
                    //設定画面遷移
                }
            }
        });


        //BLEサーチ
        rippleBackground = (RippleBackground) findViewById(R.id.content);

        final Handler handler = new Handler();

        foundDevice = (ImageView) findViewById(R.id.foundDevice);

        ImageView button = (ImageView) findViewById(R.id.centerImage);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (blesearch) {
                    rippleBackground.stopRippleAnimation();
                    blesearch = false;
                    foundDevice.setVisibility(View.INVISIBLE);

                    //TODO BLE止める

                } else {

                    blesearch = true;

                    //TODO BLEスタート
                    rippleBackground.startRippleAnimation();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            foundDevice();
                        }
                    }, 3000);

                }

            }
        });


    }

    private void foundDevice() {
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(400);
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        ArrayList<Animator> animatorList = new ArrayList<Animator>();
        ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(foundDevice, "ScaleX", 0f, 1.2f, 1f);
        animatorList.add(scaleXAnimator);
        ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(foundDevice, "ScaleY", 0f, 1.2f, 1f);
        animatorList.add(scaleYAnimator);
        animatorSet.playTogether(animatorList);
        foundDevice.setVisibility(View.VISIBLE);

        RelativeLayout.LayoutParams param = (RelativeLayout.LayoutParams) foundDevice.getLayoutParams();
        Random rnd = new Random();

        int leftDp = 0;
        int topDp = 0;
        int rightDp = 0;
        int bottomDp = 0;

        rightDp = rnd.nextInt(250) - 50;
        bottomDp = rnd.nextInt(250) - 50;
        leftDp = rnd.nextInt(500) + 50;
        topDp = rnd.nextInt(800);

        Log.d(TAG, "左：" + leftDp);
        Log.d(TAG, "上：" + topDp);

        if (400 <= topDp && topDp <= 500) {
            topDp += 250;
        } else if (500 < topDp && topDp <= 550) {
            topDp += 150;
        } else if (550 < topDp && topDp <= 650) {
            topDp += 100;
        }

        param.setMargins(leftDp, topDp, 0, 0);


        foundDevice.setLayoutParams(param);
        animatorSet.start();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        //メニューボタン作成
        Drawable[] subButtonDrawables = new Drawable[3];
        int[] drawablesResource = new int[]{
                R.drawable.ic_bluetooth_searching_white_48dp,
                R.drawable.ic_people_white_48dp,
                R.drawable.ic_settings_white_48dp
        };
        for (int i = 0; i < 3; i++)
            subButtonDrawables[i] = ContextCompat.getDrawable(this, drawablesResource[i]);

        String[] subButtonTexts = new String[]{"検索切替", "マッチング", "設定"};

        int[][] subButtonColors = new int[3][2];
        for (int i = 0; i < 3; i++) {
            subButtonColors[i][1] = ContextCompat.getColor(this, R.color.colorPrimary);
            subButtonColors[i][0] = Util.getInstance().getPressedColor(subButtonColors[i][1]);
        }

        boomMenuButton.init(
                subButtonDrawables, // The drawables of images of sub buttons. Can not be null.
                subButtonTexts,     // The texts of sub buttons, ok to be null.
                subButtonColors,    // The colors of sub buttons, including pressed-state and normal-state.
                ButtonType.CIRCLE,     // The button type.
                BoomType.PARABOLA_2,  // The boom type.
                PlaceType.CIRCLE_3_4,  // The place type.
                null,               // Ease type to move the sub buttons when showing.
                null,               // Ease type to scale the sub buttons when showing.
                null,               // Ease type to rotate the sub buttons when showing.
                null,               // Ease type to move the sub buttons when dismissing.
                null,               // Ease type to scale the sub buttons when dismissing.
                null,               // Ease type to rotate the sub buttons when dismissing.
                null                // Rotation degree.
        );

        boomMenuButton.setTextViewColor(ContextCompat.getColor(this, R.color.black));
        boomMenuButton.setSubButtonShadowOffset(Util.getInstance().dp2px(2), Util.getInstance().dp2px(2));

    }


    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onFoundDeviceClicked(View v) {
        //IDごとに遷移
        // 画面へ遷移
        // インテントのインスタンス生成
        Intent intent = new Intent(this, UserDetailActivity.class);
        intent.putExtra("id", "ogawatachi");
//        intent.putExtra("subMode", CommonKbnConst.VAL_MODE_HAITATU);
//        intent.putExtra("clear", true);
        // 次画面のアクティビティ起動
        startActivity(intent);

    }

    //    @Override
//    public void onAaClicked(View v) {
//        // 画面へ遷移
//        // インテントのインスタンス生成
//        Intent intent = new Intent(this, UserDetailActivity.class);
////        intent.putExtra("mode", CommonKbnConst.VAL_MODE_HAITATU);
////        intent.putExtra("subMode", CommonKbnConst.VAL_MODE_HAITATU);
////        intent.putExtra("clear", true);
//        // 次画面のアクティビティ起動
//        startActivity(intent);
//
//    }
//
//
//
//    @Override
//    public void onSetteiClicked(View v) {
//        Intent intent = new Intent(this, BleActivity.class);
//        startActivity(intent);
////        // インテントのインスタンス生成
////        Intent intent = new Intent(this, FugaActivity.class);
////        // インテントに値を設定
////        Bundle bundle = new Bundle();
////        bundle.putString("displayMode", "normal");
////        intent.putExtras(bundle);
////        // 次画面のアクティビティ起動
////        startActivity(intent);
//    }
//
//    @Override
//    public void onKaijoClicked(View v) {
//
//    }

}
