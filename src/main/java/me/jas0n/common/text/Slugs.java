package me.jas0n.common.text;

import cn.hutool.extra.pinyin.PinyinUtil;

public final class Slugs {

    private Slugs() {
    }

    public static String slugify(String input) {
        if (input == null || input.isBlank()) {
            return "project";
        }

        String text = input.trim().toLowerCase();
        text = PinyinUtil.getPinyin(text, "");
        text = text.replaceAll("[^a-z0-9]+", "-");
        text = text.replaceAll("^-+|-+$", "");
        text = text.replaceAll("-{2,}", "-");

        return text.isBlank() ? "project" : text;
    }
}