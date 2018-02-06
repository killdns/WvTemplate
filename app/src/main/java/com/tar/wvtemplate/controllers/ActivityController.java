package com.tar.wvtemplate.controllers;

import android.support.v7.app.AppCompatActivity;



public final class ActivityController {
    /**
     * Главное Activity приложения
     */
    private static AppCompatActivity baseActivity;


    /**
     * Геттер ссылки на изображение баннера
     */
    public static String getBannerImageUrl() {
        return bannerImageUrl;
    }

    /**
     * Сеттер ссылки на изображение баннера
     */
    public static void setBannerImageUrl(String bannerImageUrl) {
        ActivityController.bannerImageUrl = bannerImageUrl;
    }

    /**
     * Геттер ссылки перехода по клику на баннере
     */
    public static String getBannerUrlTo() {
        return bannerUrlTo;
    }

    /**
     * Сеттер ссылки перехода по клику на баннере
     */
    public static void setBannerUrlTo(String bannerUrlTo) {
        ActivityController.bannerUrlTo = bannerUrlTo;
    }

    /**
     * Геттер значения автоматического открывания ссылки при получении
     */
    public static boolean getBannerIstaOpen() {
        return bannerIstaOpen;
    }

    /**
     * Сеттер значения автоматического открывания ссылки при получении
     */
    public static void setBannerIstaOpen(boolean bannerIstaOpen) {
        ActivityController.bannerIstaOpen = bannerIstaOpen;
    }

    /**
     * Геттер значения открытия ссылки в стандартном браузере
     */
    public static boolean getBannerOpenInDefaultBrowser() {
        return bannerOpenInDefaultBrowser;
    }
    /**
     * Сеттер значения открытия ссылки в стандартном браузере
     */
    public static void setBannerOpenInDefaultBrowser(boolean bannerOpenInDefaultBrowser) {
        ActivityController.bannerOpenInDefaultBrowser = bannerOpenInDefaultBrowser;
    }

    /**
     * Геттер значения того, что открыт фрейм с WebView
     */
    public static boolean getWebViewIsShown() {
        return webViewIsShown;
    }

    /**
     * Сеттер значения того, что открыт фрейм с WebView
     */
    public static void setWebViewIsShown(boolean webViewIsShown) {
        ActivityController.webViewIsShown = webViewIsShown;
    }

    /**
     * Геттер значения того, что открыт фрейм с баннером
     */
    public static boolean getBannerIsShown() {
        return bannerIsShown;
    }

    /**
     * Сеттер значения того, что открыт фрейм с баннером
     */
    public static void setBannerIsShown(boolean bannerIsShown) {
        ActivityController.bannerIsShown = bannerIsShown;
    }


    /**
     * Ссылка на изображение баннера
     */
    private static String bannerImageUrl;
    /**
     * Ссылка перехода при клике на баннер
     */
    private static String bannerUrlTo;

    /**
     * Автоматическое открытие ссылки
     */
    private static boolean bannerIstaOpen;
    /**
     * Открытие ссылки в стандартном браузере
     */
    private static boolean bannerOpenInDefaultBrowser;

    /**
     * Фрейм с webView открыт/закрыт
     */
    private static boolean webViewIsShown;
    /**
     * Фрейм с баннером открыт/закрыт
     */
    private static boolean bannerIsShown;

    /**
     * Геттер главного Activity приложения
     */
    public static AppCompatActivity getBaseActivity() {
        return baseActivity;
    }


    /**
     * Сеттер главного Activity приложения
     */
    public static void setBaseActivity(AppCompatActivity baseActivity) {
        ActivityController.baseActivity = baseActivity;
    }


}
