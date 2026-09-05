package com.guesstheword;

public class Word {
    private long id;
    private String text;
    private long categoryId;
    private boolean isMultiPart;

    public Word() {}

    public Word(String text, long categoryId, boolean isMultiPart) {
        this.text = text.toUpperCase();
        this.categoryId = categoryId;
        this.isMultiPart = isMultiPart;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text.toUpperCase(); }

    public long getCategoryId() { return categoryId; }
    public void setCategoryId(long categoryId) { this.categoryId = categoryId; }

    public boolean isMultiPart() { return isMultiPart; }
    public void setMultiPart(boolean multiPart) { isMultiPart = multiPart; }

    public int getLength() {
        if (isMultiPart) {
            return text.replace(" ", "").length();
        }
        return text.length();
    }

    public String getDisplayText() {
        return text;
    }
}