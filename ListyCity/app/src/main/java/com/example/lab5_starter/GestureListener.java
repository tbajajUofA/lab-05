package com.example.lab5_starter;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;

public class GestureListener extends GestureDetector.SimpleOnGestureListener {

    private static final int SWIPE_THRESHOLD = 150;
    private static final int SWIPE_VELOCITY_THRESHOLD = 150;

    private final ListView listView;
    private final ArrayList<City> dataList;
    private final ArrayAdapter<City> adapter;
    public Boolean deleteMode;  // use a reference to toggle delete mode
    private CollectionReference citiesRef;
    public GestureListener(ListView listView, ArrayList<City> dataList,
                           ArrayAdapter<City> adapter, boolean deleteMode,CollectionReference citiesRef) {
        this.listView = listView;
        this.dataList = dataList;
        this.adapter = adapter;
        this.deleteMode = deleteMode;
        this.citiesRef =  citiesRef;

    }

    public void setDeleteMode(boolean deleteMode) {
        this.deleteMode = deleteMode;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        float diffX = Math.abs(e2.getX() - e1.getX());

        if ((diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {

            if (deleteMode) {
                int position = listView.pointToPosition((int) e1.getX(), (int) e1.getY());

                if (position != ListView.INVALID_POSITION && position < dataList.size()) {
                    
                    City city = dataList.get(position);
                    dataList.remove(position);
                    DocumentReference docRef = citiesRef.document(city.getName());
                    docRef.delete();

                    adapter.notifyDataSetChanged();
                }
            }
            return true;
        }
        return false;
    }
}
