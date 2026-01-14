package com.hook.xphooktemplet;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;


public class MainHook implements IXposedHookLoadPackage {
    private static final String TARGET_PACKAGE = "com.tgc.sky.android";
    private static final String TAG = "HOOK";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        try {
            Log.d(TAG, "Hooking:" + TARGET_PACKAGE);
            if (!lpparam.packageName.equals(TARGET_PACKAGE)) {
                Log.d(TAG, "handleLoadPackage: not found!!");
                return;
            }
            XposedHelpers.findAndHookMethod(Application.class, "attach", Context.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    try {
                        super.beforeHookedMethod(param);
                        if (param.args == null || param.args.length == 0) {
                            Log.e(TAG, "Application.attach: param.args is null or empty");
                            return;
                        }
                        Context context = (Context) param.args[0];
                        if (context == null) {
                            Log.e(TAG, "Application.attach: context is null");
                            return;
                        }
                        ClassLoader classLoader = context.getClassLoader();
                        if (classLoader == null) {
                            Log.e(TAG, "Application.attach: classLoader is null");
                            return;
                        }
                        Class<?> clazz = XposedHelpers.findClass("java.lang.System", classLoader);
                        Log.d(TAG, "System class found: " + clazz);
                        XposedHelpers.findAndHookMethod(clazz, "loadLibrary",
                                String.class, new XC_MethodHook() {
                                    @Override
                                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                        Log.d(TAG, "---------------start----------------");
                                        StackTraceElement[] stackElements = new Throwable().getStackTrace();
                                        if (stackElements != null) {
                                            for (int i = 0; i < stackElements.length; i++) {

                                                Log.d(TAG, stackElements[i].getClassName() + "." + stackElements[i].getMethodName()
                                                        + "(" + stackElements[i].getFileName()
                                                        + ":" + stackElements[i].getLineNumber())
                                                ;
                                            }
                                        }
                                        Log.d(TAG, "---------------over----------------");

                                        try {
                                            super.beforeHookedMethod(param);

                                            if (param.args != null && param.args.length > 0) {
                                                String libName = (String) param.args[0];
                                                String threadName = Thread.currentThread().getName();
                                                String className = "java.lang.System"; // 默认使用类名，因为这是静态方法

                                                // 只有当thisObject不为null时才调用getClass()
                                                if (param.thisObject != null) {
                                                    className = param.thisObject.getClass().getName();
                                                }

                                                Log.d(TAG, "[System.loadLibrary] Thread: " + threadName);
                                                Log.d(TAG, "[System.loadLibrary] Class: " + className);
                                                Log.d(TAG, "[System.loadLibrary] Library Name: " + libName);
                                                Log.d(TAG, "[System.loadLibrary] Full Method: System.loadLibrary(\"" + libName + "\")");
                                                Log.d(TAG, "[System.loadLibrary] ----------------------------");
                                            }
                                        } catch (Throwable e) {
                                            Log.e(TAG, "Error in loadLibrary hook: " + e.getMessage(), e);
                                            // 捕获所有异常，防止应用崩溃
                                        }
                                    }

                                    ;
                                });

                    } catch (Throwable e) {
                        Log.e(TAG, "Error in Application.attach hook: " + e.getMessage(), e);
                        // 捕获所有异常，防止应用崩溃
                    }
                }
            });
        } catch (Throwable e) {
            Log.e(TAG, "Error in handleLoadPackage: " + e.getMessage(), e);
            // 捕获所有异常，防止应用崩溃
        }
    }
}

