package com.guesstheword;

public class Category {
    private long id;
    private String name;
    private boolean isBuiltIn;

    public Category() {}

    public Category(String name, boolean isBuiltIn) {
        this.name = name;
        this.isBuiltIn = isBuiltIn;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public boolean isBuiltIn() { return isBuiltIn; }
    public void setBuiltIn(boolean builtIn) { isBuiltIn = builtIn; }
}