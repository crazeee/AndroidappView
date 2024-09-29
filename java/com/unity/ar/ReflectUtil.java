package com.unity.ar;

import android.view.InputEvent;
import android.view.MotionEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectUtil {


    public static void callMethod(Object classOb,String methodName,int param){
        try {
            Method m = classOb.getClass().getDeclaredMethod(methodName,int.class);
            m.setAccessible(true);
            m.invoke(classOb,param);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static void callMethod(Object classOb,String methodName,long param){
        try {
            Method m = classOb.getClass().getDeclaredMethod(methodName,long.class);
            m.setAccessible(true);
            m.invoke(classOb,param);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static void callMethod(Object classOb,String methodName,Object param,Object param1){
        try {
            Method m = classOb.getClass().getDeclaredMethod(methodName, InputEvent.class,int.class);
            m.setAccessible(true);
            m.invoke(classOb,param,param1);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }


    public static void callStaticMethod(Class clazz,String methodName,int param){
        try {
            Method m = clazz.getDeclaredMethod(methodName,int.class);
            m.setAccessible(true);
            m.invoke(null,param);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static Object callStaticMethodR(Class clazz,String methodName){
        try {
            Method m = clazz.getDeclaredMethod(methodName);
            m.setAccessible(true);
            return m.invoke(null);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

}
