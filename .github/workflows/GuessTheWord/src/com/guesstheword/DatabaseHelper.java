package com.guesstheword;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "guesstheword.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TAG = "DatabaseHelper";

    // Tables
    private static final String TABLE_CATEGORIES = "categories";
    private static final String TABLE_WORDS = "words";

    // Category columns
    private static final String COL_CAT_ID = "id";
    private static final String COL_CAT_NAME = "name";
    private static final String COL_CAT_BUILTIN = "is_builtin";

    // Word columns
    private static final String COL_WORD_ID = "id";
    private static final String COL_WORD_TEXT = "text";
    private static final String COL_WORD_CAT_ID = "category_id";
    private static final String COL_WORD_MULTIPART = "is_multipart";

    private Context context;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createCategories = "CREATE TABLE " + TABLE_CATEGORIES + " (" +
                COL_CAT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_CAT_NAME + " TEXT UNIQUE NOT NULL, " +
                COL_CAT_BUILTIN + " INTEGER DEFAULT 0)";

        String createWords = "CREATE TABLE " + TABLE_WORDS + " (" +
                COL_WORD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_WORD_TEXT + " TEXT NOT NULL, " +
                COL_WORD_CAT_ID + " INTEGER NOT NULL, " +
                COL_WORD_MULTIPART + " INTEGER DEFAULT 0, " +
                "FOREIGN KEY(" + COL_WORD_CAT_ID + ") REFERENCES " + TABLE_CATEGORIES + "(" + COL_CAT_ID + "))";

        db.execSQL(createCategories);
        db.execSQL(createWords);

        // Insert default categories and words
        insertDefaultData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WORDS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
        onCreate(db);
    }

    private void insertDefaultData(SQLiteDatabase db) {
        // Built-in categories
        String[] categories = {"All", "Name", "Game", "Artist", "Music", "Item", "Animal", "Food", "Place", "Brand", "Movie", "Sport", "Color"};
        long[] catIds = new long[categories.length];

        for (int i = 0; i < categories.length; i++) {
            ContentValues cv = new ContentValues();
            cv.put(COL_CAT_NAME, categories[i]);
            cv.put(COL_CAT_BUILTIN, 1);
            catIds[i] = db.insert(TABLE_CATEGORIES, null, cv);
        }

        // Default words for each category (3-7 letters, some multi-part)
        addDefaultWords(db, catIds);
    }

    private void addDefaultWords(SQLiteDatabase db, long[] catIds) {
        // All words will be added to "All" category (index 0) and their specific category
        String[][] categoryWords = {
            // Name (3-7 letters)
            {"JOHN", "MARY", "BOB", "ALICE", "TOM", "JANE", "MIKE", "LISA", "DAVE", "ANNA",
             "CHRIS", "PETER", "PAUL", "MARK", "LUKE", "MATT", "NICK", "ERIC", "ADAM", "BEN"},
            // Game
            {"CHESS", "POKER", "GO", "TAG", "HIDE", "SEEK", "MARIO", "SONIC", "ZELDA", "TETRIS",
             "PACMAN", "MINES", "SUDOKU", "CANDY", "ANGRY", "BIRDS", "PLANTS", "ZOMBIES"},
            // Artist
            {"PICASSO", "MONET", "VAN GOGH", "DA VINCI", "REMBRANDT", "MICHELANGELO", "DALI", "WARHOL",
             "KAHLO", "POLLOCK", "OKEEFFE", "MONDRIAN", "KANDINSKY", "MATISSE", "CEZANNE"},
            // Music
            {"ROCK", "JAZZ", "BLUES", "POP", "RAP", "FUNK", "SOUL", "METAL", "INDIE", "FOLK",
             "REGGAE", "TECHNO", "HOUSE", "TRANCE", "AMBIENT", "CLASSICAL", "OPERA", "SYMPHONY"},
            // Item
            {"BOOK", "PEN", "PHONE", "KEY", "BAG", "WATCH", "RING", "COIN", "CARD", "NOTE",
             "TABLE", "CHAIR", "LAMP", "DOOR", "WINDOW", "FLOOR", "WALL", "ROOF"},
            // Animal
            {"CAT", "DOG", "BIRD", "FISH", "LION", "TIGER", "BEAR", "WOLF", "FOX", "DEER",
             "ELEPHANT", "GIRAFFE", "MONKEY", "PANDA", "KOALA", "KANGAROO", "PENGUIN", "DOLPHIN"},
            // Food
            {"PIE", "CAKE", "BREAD", "MEAT", "FISH", "RICE", "BEAN", "EGG", "MILK", "CHEESE",
             "APPLE", "BANANA", "ORANGE", "GRAPE", "MELON", "PEACH", "PEAR", "PLUM"},
            // Place
            {"HOME", "WORK", "SCHOOL", "PARK", "BEACH", "CITY", "TOWN", "FARM", "ZOO", "MALL",
             "PARIS", "LONDON", "TOKYO", "NEW YORK", "ROME", "BERLIN", "MOSCOW", "SYDNEY"},
            // Brand
            {"NIKE", "ADIDAS", "PUMA", "REEBOK", "APPLE", "GOOGLE", "MICROSOFT", "AMAZON", "META",
             "TESLA", "BMW", "AUDI", "MERCEDES", "TOYOTA", "HONDA", "FORD", "SONY", "LG"},
            // Movie
            {"STAR WARS", "AVATAR", "TITANIC", "JAWS", "ROCKY", "ALIEN", "TERMINATOR", "MATRIX",
             "INCEPTION", "INTERSTELLAR", "GLADIATOR", "BRAVEHEART", "FORREST GUMP", "GODFATHER"},
            // Sport
            {"SOCCER", "TENNIS", "GOLF", "SWIM", "RUN", "BOXING", "SKI", "SURF", "BIKE", "HIKE",
             "FOOTBALL", "BASKETBALL", "BASEBALL", "HOCKEY", "VOLLEYBALL", "CRICKET", "RUGBY"},
            // Color
            {"RED", "BLUE", "GREEN", "YELLOW", "BLACK", "WHITE", "PINK", "BROWN", "GRAY", "PURPLE",
             "ORANGE", "CYAN", "MAGENTA", "MAROON", "TEAL", "NAVY", "OLIVE", "SILVER"}
        };

        for (int catIdx = 1; catIdx < categoryWords.length; catIdx++) {
            for (String word : categoryWords[catIdx]) {
                boolean isMulti = word.contains(" ");
                // Add to specific category
                addWord(db, word, catIds[catIdx], isMulti);
                // Add to "All" category
                addWord(db, word, catIds[0], isMulti);
            }
        }
    }

    private void addWord(SQLiteDatabase db, String text, long catId, boolean isMultiPart) {
        ContentValues cv = new ContentValues();
        cv.put(COL_WORD_TEXT, text.toUpperCase());
        cv.put(COL_WORD_CAT_ID, catId);
        cv.put(COL_WORD_MULTIPART, isMultiPart ? 1 : 0);
        db.insert(TABLE_WORDS, null, cv);
    }

    // Public methods
    public List<Category> getAllCategories() {
        List<Category> categories = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_CATEGORIES, null, null, null, null, null, COL_CAT_NAME);
        while (cursor.moveToNext()) {
            Category cat = new Category();
            cat.setId(cursor.getLong(cursor.getColumnIndex(COL_CAT_ID)));
            cat.setName(cursor.getString(cursor.getColumnIndex(COL_CAT_NAME)));
            cat.setBuiltIn(cursor.getInt(cursor.getColumnIndex(COL_CAT_BUILTIN)) == 1);
            categories.add(cat);
        }
        cursor.close();
        return categories;
    }

    public List<Word> getWordsByCategory(long categoryId, int minLength, int maxLength, boolean allowMultiPart) {
        List<Word> words = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        StringBuilder selection = new StringBuilder(COL_WORD_CAT_ID + " = ?");
        String[] selectionArgs = new String[]{String.valueOf(categoryId)};

        if (!allowMultiPart) {
            selection.append(" AND ").append(COL_WORD_MULTIPART).append(" = 0");
        }

        Cursor cursor = db.query(TABLE_WORDS, null, selection.toString(), selectionArgs, null, null, null);
        while (cursor.moveToNext()) {
            Word word = new Word();
            word.setId(cursor.getLong(cursor.getColumnIndex(COL_WORD_ID)));
            word.setText(cursor.getString(cursor.getColumnIndex(COL_WORD_TEXT)));
            word.setCategoryId(cursor.getLong(cursor.getColumnIndex(COL_WORD_CAT_ID)));
            word.setMultiPart(cursor.getInt(cursor.getColumnIndex(COL_WORD_MULTIPART)) == 1);

            int len = word.getLength();
            if (len >= minLength && len <= maxLength) {
                words.add(word);
            }
        }
        cursor.close();
        return words;
    }

    public Word getRandomWord(long categoryId, int minLength, int maxLength, boolean allowMultiPart) {
        List<Word> words = getWordsByCategory(categoryId, minLength, maxLength, allowMultiPart);
        if (words.isEmpty()) return null;
        return words.get((int) (Math.random() * words.size()));
    }

    public long addCategory(String name) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_CAT_NAME, name);
        cv.put(COL_CAT_BUILTIN, 0);
        return db.insert(TABLE_CATEGORIES, null, cv);
    }

    public long addWord(String text, long categoryId, boolean isMultiPart) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_WORD_TEXT, text.toUpperCase());
        cv.put(COL_WORD_CAT_ID, categoryId);
        cv.put(COL_WORD_MULTIPART, isMultiPart ? 1 : 0);
        long id = db.insert(TABLE_WORDS, null, cv);

        // Also add to "All" category (id=1)
        if (categoryId != 1) {
            cv.put(COL_WORD_CAT_ID, 1);
            db.insert(TABLE_WORDS, null, cv);
        }
        return id;
    }

    public boolean wordExists(String text, long categoryId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_WORDS,
                new String[]{COL_WORD_ID},
                COL_WORD_TEXT + " = ? AND " + COL_WORD_CAT_ID + " = ?",
                new String[]{text.toUpperCase(), String.valueOf(categoryId)},
                null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public int getWordCount(long categoryId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_WORDS + " WHERE " + COL_WORD_CAT_ID + " = ?", new String[]{String.valueOf(categoryId)});
        int count = 0;
        if (cursor.moveToFirst()) count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    // Export/Import
    public boolean exportWords(File file, long categoryId) {
        try {
            List<Word> words = getWordsByCategory(categoryId, 3, 7, true);
            JSONObject json = new JSONObject();
            json.put("category_id", categoryId);
            json.put("export_date", System.currentTimeMillis());

            JSONArray array = new JSONArray();
            for (Word w : words) {
                JSONObject obj = new JSONObject();
                obj.put("text", w.getText());
                obj.put("multipart", w.isMultiPart());
                array.put(obj);
            }
            json.put("words", array);

            FileWriter writer = new FileWriter(file);
            writer.write(json.toString(2));
            writer.close();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Export words failed", e);
            return false;
        }
    }

    public boolean exportCategories(File file) {
        try {
            List<Category> cats = getAllCategories();
            JSONObject json = new JSONObject();
            json.put("export_date", System.currentTimeMillis());

            JSONArray array = new JSONArray();
            for (Category c : cats) {
                JSONObject obj = new JSONObject();
                obj.put("name", c.getName());
                obj.put("builtin", c.isBuiltIn());
                array.put(obj);
            }
            json.put("categories", array);

            FileWriter writer = new FileWriter(file);
            writer.write(json.toString(2));
            writer.close();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Export categories failed", e);
            return false;
        }
    }

    public boolean exportAll(File file) {
        try {
            JSONObject json = new JSONObject();
            json.put("export_date", System.currentTimeMillis());

            // Categories
            List<Category> cats = getAllCategories();
            JSONArray catArray = new JSONArray();
            for (Category c : cats) {
                JSONObject obj = new JSONObject();
                obj.put("name", c.getName());
                obj.put("builtin", c.isBuiltIn());
                catArray.put(obj);
            }
            json.put("categories", catArray);

            // Words per category
            JSONArray wordArray = new JSONArray();
            for (Category c : cats) {
                List<Word> words = getWordsByCategory(c.getId(), 3, 7, true);
                for (Word w : words) {
                    JSONObject obj = new JSONObject();
                    obj.put("text", w.getText());
                    obj.put("category", c.getName());
                    obj.put("multipart", w.isMultiPart());
                    wordArray.put(obj);
                }
            }
            json.put("words", wordArray);

            FileWriter writer = new FileWriter(file);
            writer.write(json.toString(2));
            writer.close();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Export all failed", e);
            return false;
        }
    }

    public boolean importWords(File file, long categoryId) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            reader.close();

            JSONObject json = new JSONObject(sb.toString());
            JSONArray array = json.getJSONArray("words");

            int imported = 0;
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                String text = obj.getString("text").toUpperCase();
                boolean multi = obj.optBoolean("multipart", false);

                if (!wordExists(text, categoryId)) {
                    addWord(text, categoryId, multi);
                    imported++;
                }
            }
            return imported > 0;
        } catch (Exception e) {
            Log.e(TAG, "Import words failed", e);
            return false;
        }
    }

    public boolean importCategories(File file) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            reader.close();

            JSONObject json = new JSONObject(sb.toString());
            JSONArray array = json.getJSONArray("categories");

            int imported = 0;
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                String name = obj.getString("name");
                boolean builtin = obj.optBoolean("builtin", false);

                // Check if exists
                SQLiteDatabase db = getReadableDatabase();
                Cursor cursor = db.query(TABLE_CATEGORIES, new String[]{COL_CAT_ID},
                        COL_CAT_NAME + " = ?", new String[]{name}, null, null, null);
                if (cursor.getCount() == 0) {
                    cursor.close();
                    addCategory(name);
                    imported++;
                } else {
                    cursor.close();
                }
            }
            return imported > 0;
        } catch (Exception e) {
            Log.e(TAG, "Import categories failed", e);
            return false;
        }
    }

    public boolean importAll(File file) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            reader.close();

            JSONObject json = new JSONObject(sb.toString());

            // Import categories first
            if (json.has("categories")) {
                importCategories(file);
            }

            // Import words
            if (json.has("words")) {
                JSONArray array = json.getJSONArray("words");
                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    String text = obj.getString("text").toUpperCase();
                    String catName = obj.getString("category");
                    boolean multi = obj.optBoolean("multipart", false);

                    // Find category ID
                    SQLiteDatabase db = getReadableDatabase();
                    Cursor cursor = db.query(TABLE_CATEGORIES, new String[]{COL_CAT_ID},
                            COL_CAT_NAME + " = ?", new String[]{catName}, null, null, null);
                    if (cursor.moveToFirst()) {
                        long catId = cursor.getLong(0);
                        if (!wordExists(text, catId)) {
                            addWord(text, catId, multi);
                        }
                    }
                    cursor.close();
                }
            }
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Import all failed", e);
            return false;
        }
    }

    public boolean updateWordList(File file) {
        // This replaces all words with the ones from the file
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            reader.close();

            JSONObject json = new JSONObject(sb.toString());
            JSONArray array = json.getJSONArray("words");

            SQLiteDatabase db = getWritableDatabase();
            db.delete(TABLE_WORDS, null, null);

            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                String text = obj.getString("text").toUpperCase();
                String catName = obj.getString("category");
                boolean multi = obj.optBoolean("multipart", false);

                Cursor cursor = db.query(TABLE_CATEGORIES, new String[]{COL_CAT_ID},
                        COL_CAT_NAME + " = ?", new String[]{catName}, null, null, null);
                if (cursor.moveToFirst()) {
                    long catId = cursor.getLong(0);
                    ContentValues cv = new ContentValues();
                    cv.put(COL_WORD_TEXT, text);
                    cv.put(COL_WORD_CAT_ID, catId);
                    cv.put(COL_WORD_MULTIPART, multi ? 1 : 0);
                    db.insert(TABLE_WORDS, null, cv);
                }
                cursor.close();
            }
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Update word list failed", e);
            return false;
        }
    }
}