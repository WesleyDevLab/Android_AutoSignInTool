package com.pyy.signin;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.pyy.signin.Utils.delay;
import static com.pyy.signin.Utils.prt;

/**
 * Created by pyy on 2017/8/4.
 */

public class SignInService extends AccessibilityService {
    final static String logTag = "[SignInService]";
    String fgPackageName;
    static Lock autoLock = new ReentrantLock();
    static Condition autoCondition = autoLock.newCondition();
    autoSignInJD jd = new autoSignInJD();
    autoSignInSMZDM smzdm = new autoSignInSMZDM();
    autoSignInJDJR jdjr = new autoSignInJDJR();
    autoSignInTXDM txdm = new autoSignInTXDM();

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        fgPackageName = accessibilityEvent.getPackageName().toString();
        //prt("Event: " + accessibilityEvent);
        if (accessibilityEvent.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED) {
            //Log.i(logTag, "CINDY " + accessibilityEvent);
            if ("com.jd.jrapp".equals(fgPackageName) && (accessibilityEvent.getText().equals("京豆明细")
                 || !accessibilityEvent.getText().toString().contains("签到"))) {
                autoLock.lock();
                autoCondition.signal();
                autoLock.unlock();
            }
            if ("com.jingdong.app.mall".equals(fgPackageName)
                    && !(accessibilityEvent.getText().toString().contains("领京豆")
                    || accessibilityEvent.getText().toString().contains("我的")
                    || accessibilityEvent.getText().toString().contains("会员")
                    || accessibilityEvent.getText().toString().contains("惠赚钱")
                    || accessibilityEvent.getText().toString().contains("签到")
                    || accessibilityEvent.getText().toString().contains("领券"))) {
                autoLock.lock();
                autoCondition.signal();
                autoLock.unlock();
            }
        }

        if (accessibilityEvent.getEventType() == AccessibilityEvent.TYPE_VIEW_SCROLLED
                && "com.jd.jrapp".equals(fgPackageName) && jdjr.gestureLockFlag) {
            jdjr.gestureLockFlag = false;
            autoLock.lock();
            autoCondition.signal();
            autoLock.unlock();
        }

        new autoSignThread(accessibilityEvent).start();
    }

    class autoSignThread extends Thread {
        private AccessibilityEvent event;
        autoSignThread(AccessibilityEvent arg) {
            event = arg;
        }

        @Override
        public void run() {
            super.run();
            autoSign(event);
        }
    }

    private void autoSign(AccessibilityEvent envent) {
        if (MainPage.flag) {
            MainPage.lock.lock();
            MainPage.flag = false;
            switch (fgPackageName) {
                case "com.jingdong.app.mall":
                    jd.doJD(this);
                    break;
                case "com.jd.jrapp":
                    jdjr.doJDJR(this);
                    break;
                case "com.smzdm.client.android":
                    smzdm.doSMZDM(this);
                    break;
                case "com.qq.ac.android":
                    txdm.doTXDM(this);
                    break;
                default:
                    break;
            }
            MainPage.lock.unlock();
        }
    }


    @Override
    public void onInterrupt() {

    }

    @Override
    protected  void onServiceConnected() {
        super.onServiceConnected();
    }
}
