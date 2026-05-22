package com.hytale.extendedteleport.i18n;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.ui.LocalizableString;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public final class TranslationManager
{
    private static final HytaleLogger logger = HytaleLogger.getLogger().getSubLogger("ExtendedTeleport-i18n");

    private static final String LANG_FILE_PATH = "Server/Languages/%s/hytale.extendedteleport.lang";
    private static final String DEFAULT_LANGUAGE = "en-US";
    private static final String KEY_PREFIX = "";
    private static final Pattern PARAM_PATTERN = Pattern.compile("\\{([^}]+)}");

    private static TranslationManager instance;

    private final Map<String, Map<String, String>> translations = new ConcurrentHashMap<>();
    private String currentLanguage = "en-US";

    private TranslationManager() {
        loadLanguage("en-US");
    }


    public static TranslationManager getInstance() {
        if (instance == null) {
            synchronized (TranslationManager.class) {
                if (instance == null) {
                    instance = new TranslationManager();
                }
            }
        }
        return instance;
    }


    public void loadLanguage(String language) {
        String path = "Server/Languages/%s/hytale.extendedteleport.lang".formatted(new Object[] { language });
        Map<String, String> langMap = new HashMap<>();

        try { InputStream is = getClass().getClassLoader().getResourceAsStream(path);
            try { if (is == null)
                { if (!language.equals("en-US"))
                    { logger.at(Level.WARNING).log("Translation file not found for language: " + language + ", falling back to en-US");


                if (is != null) is.close();  return; }  logger.at(Level.SEVERE).log("Default translation file not found: " + path); if (is != null) is.close();  return; }  BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8)); try { int lineNum = 0; String line; while ((line = reader.readLine()) != null) { lineNum++; line = line.trim(); if (line.isEmpty() || line.startsWith("#")) continue;  int equalsIndex = line.indexOf('='); if (equalsIndex == -1) { logger.at(Level.WARNING).log("Invalid translation line %d in %s: %s".formatted(new Object[] { Integer.valueOf(lineNum), path, line })); continue; }  String key = line.substring(0, equalsIndex).trim(); String value = line.substring(equalsIndex + 1).trim(); langMap.put(key, value); }  reader.close(); } catch (Throwable throwable) { try { reader.close(); } catch (Throwable throwable1) { throwable.addSuppressed(throwable1); }  throw throwable; }  this.translations.put(language, langMap); logger.at(Level.INFO).log("Loaded %d translations for language: %s".formatted(new Object[] { Integer.valueOf(langMap.size()), language })); if (is != null) is.close();  } catch (Throwable throwable) { if (is != null) try { is.close(); } catch (Throwable throwable1) { throwable.addSuppressed(throwable1); }   throw throwable; }  } catch (IOException e)
        { logger.at(Level.SEVERE).log("Error loading translations for " + language + ": " + e.getMessage()); }

    }


    public void setCurrentLanguage(String language) {
        if (!this.translations.containsKey(language)) {
            loadLanguage(language);
        }
        this.currentLanguage = language;
    }


    public String getRaw(String key) {
        String fullKey = key.startsWith("") ? key : key;


        Map<String, String> currentLang = this.translations.get(this.currentLanguage);
        if (currentLang != null && currentLang.containsKey(fullKey)) {
            return currentLang.get(fullKey);
        }


        if (!this.currentLanguage.equals("en-US")) {
            Map<String, String> defaultLang = this.translations.get("en-US");
            if (defaultLang != null && defaultLang.containsKey(fullKey)) {
                return defaultLang.get(fullKey);
            }
        }


        logger.at(Level.WARNING).log("Missing translation key: " + fullKey);
        return key;
    }


    public String get(String key, Object... params) {
        String raw = getRaw(key);

        if (params.length == 0) {
            return raw;
        }


        Map<String, Object> paramMap = new HashMap<>();
        for (int i = 0; i + 1 < params.length; i += 2) {
            paramMap.put(String.valueOf(params[i]), params[i + 1]);
        }

        return format(raw, paramMap);
    }


    public String get(String key, Map<String, Object> params) {
        String raw = getRaw(key);
        return format(raw, params);
    }


    private String format(String text, Map<String, Object> params) {
        if (params == null || params.isEmpty()) {
            return text;
        }

        Matcher matcher = PARAM_PATTERN.matcher(text);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            String paramName = matcher.group(1);
            Object value = params.get(paramName);
            String replacement = (value != null) ? String.valueOf(value) : matcher.group(0);
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);

        return result.toString();
    }


    public Message message(String key, Object... params) {
        return Message.raw(get(key, params));
    }


    public Message message(String key, Color color, Object... params) {
        return Message.raw(get(key, params)).color(color);
    }


    public LocalizableString localizable(String key, Object... params) {
        return LocalizableString.fromString(get(key, params));
    }


    public Set<String> getLoadedLanguages() {
        return this.translations.keySet();
    }


    public boolean hasKey(String key) {
        String fullKey = key.startsWith("") ? key : key;
        Map<String, String> currentLang = this.translations.get(this.currentLanguage);
        if (currentLang != null && currentLang.containsKey(fullKey)) {
            return true;
        }
        Map<String, String> defaultLang = this.translations.get("en-US");
        return (defaultLang != null && defaultLang.containsKey(fullKey));
    }


    public void reload() {
        for (String lang : new ArrayList<>(this.translations.keySet()))
        loadLanguage(lang);
    }
}