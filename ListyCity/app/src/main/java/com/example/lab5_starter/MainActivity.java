package com.example.lab5_starter;

import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements CityDialogFragment.CityDialogListener {

    private Button addCityButton;

    private Button deleteButton;
    private ListView cityListView;

    private ArrayList<City> cityArrayList;
    private ArrayAdapter<City> cityArrayAdapter;

    private boolean deleteMode = false;
    private GestureDetector gestureDetector;
    private GestureListener gestureListener;

    private FirebaseFirestore db;
    private CollectionReference citiesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        citiesRef = db.collection("cities");


        // Set views
        addCityButton = findViewById(R.id.buttonAddCity);
        cityListView = findViewById(R.id.listviewCities);

        deleteButton = findViewById(R.id.delete);

        // create city array
        cityArrayList = new ArrayList<>();
        cityArrayAdapter = new CityArrayAdapter(this, cityArrayList);
        cityListView.setAdapter(cityArrayAdapter);

        //addDummyData();

        // set listeners
        addCityButton.setOnClickListener(view -> {
            CityDialogFragment cityDialogFragment = new CityDialogFragment();
            cityDialogFragment.show(getSupportFragmentManager(),"Add City");
        });

        cityListView.setOnItemClickListener((adapterView, view, i, l) -> {
            if (deleteMode == false) {
                City city = cityArrayAdapter.getItem(i);
                CityDialogFragment cityDialogFragment = CityDialogFragment.newInstance(city);
                cityDialogFragment.show(getSupportFragmentManager(), "City Details");
            }
        });




        gestureListener = new GestureListener(cityListView, cityArrayList, cityArrayAdapter, deleteMode, citiesRef);
        gestureDetector = new GestureDetector(this, gestureListener);

        deleteButton.setOnClickListener(view ->
        {deleteMode = activeDelete(deleteMode);
            gestureListener.setDeleteMode(deleteMode);
        });

        cityListView.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));

        citiesRef.addSnapshotListener((value, error) ->
        {
            if (error != null) {
                Log.e("Firestore",error.toString());
            }
            if (value!=null && !value.isEmpty()){
                cityArrayList.clear();
                for (QueryDocumentSnapshot snapshot: value){
                    String name = snapshot.getString("name");
                    String province = snapshot.getString("province");

                    cityArrayList.add(new City(name,province));

                }
                cityArrayAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void updateCity(City city, String title, String year) {
        DocumentReference docRef = citiesRef.document(city.getName());
        docRef.delete();

        city.setName(title);
        city.setProvince(year);


        docRef = citiesRef.document(city.getName());
        docRef.set(city);


        cityArrayAdapter.notifyDataSetChanged();

        // Updating the database using delete + addition
    }

    @Override
    public void addCity(City city){
        cityArrayList.add(city);
        cityArrayAdapter.notifyDataSetChanged();

        DocumentReference docRef = citiesRef.document(city.getName());
        docRef.set(city);

    }

//    public void addDummyData(){
//        City m1 = new City("Edmonton", "AB");
//        City m2 = new City("Vancouver", "BC");
//        cityArrayList.add(m1);
//        cityArrayList.add(m2);
//        cityArrayAdapter.notifyDataSetChanged();
//    }

    private Boolean activeDelete(Boolean deleteMode){

        deleteMode = !deleteMode;

        if (deleteMode){
            addCityButton.setVisibility(View.GONE);
            deleteButton.setText("SWIPE TO DELETE\n(click again to disable delete)");
            
        }else{
            addCityButton.setVisibility(View.VISIBLE);
            deleteButton.setText("Delete");
        }

        return deleteMode;
    }
}