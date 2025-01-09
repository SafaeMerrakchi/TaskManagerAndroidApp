package com.example.taskmanager;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ManageCategoriesActivity extends AppCompatActivity {

    private EditText editCategoryName;
    private RecyclerView recyclerView;
    private CategoryAdapter adapter;
    private TaskDBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_categories);

        getSupportActionBar().hide();


        // Initialisation des vues
        editCategoryName = findViewById(R.id.edit_category_name);
        Button addCategoryButton = findViewById(R.id.button_add_category);
        recyclerView = findViewById(R.id.recycler_view_categories);

        // Initialisation de la base de données
        dbHelper = new TaskDBHelper(this);

        // Configuration du RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CategoryAdapter(loadCategoriesFromDB(), this::confirmDeleteCategory);
        recyclerView.setAdapter(adapter);

        // Gestion du clic sur le bouton "Add Category"
        addCategoryButton.setOnClickListener(v -> {
            String categoryName = editCategoryName.getText().toString().trim();
            if (!categoryName.isEmpty()) {
                if (addCategory(categoryName)) {
                    adapter.updateCategories(loadCategoriesFromDB());
                    editCategoryName.setText(""); // Réinitialiser le champ de saisie
                    Toast.makeText(this, "Catégorie ajoutée", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "La catégorie existe déjà", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Le nom de la catégorie ne peut pas être vide", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean addCategory(String name) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);

        // Insérer la catégorie et vérifier si elle existe déjà
        long result = db.insert("categories", null, values);
        db.close();
        return result != -1;
    }

    private void confirmDeleteCategory(String categoryName) {
        // Affiche une boîte de dialogue de confirmation
        new AlertDialog.Builder(this)
                .setTitle("Supprimer la catégorie")
                .setMessage("Êtes-vous sûr de vouloir supprimer la catégorie \"" + categoryName + "\" ?")
                .setPositiveButton("Oui", (dialog, which) -> {
                    deleteCategory(categoryName);
                    adapter.updateCategories(loadCategoriesFromDB());
                    Toast.makeText(this, "Catégorie supprimée", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Non", null)
                .show();
    }

    private void deleteCategory(String name) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("categories", "name=?", new String[]{name});
        db.close();
    }

    private List<String> loadCategoriesFromDB() {
        List<String> categories = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT name FROM categories", null);

        while (cursor.moveToNext()) {
            categories.add(cursor.getString(0));
        }
        cursor.close();
        db.close();
        return categories;
    }
}
