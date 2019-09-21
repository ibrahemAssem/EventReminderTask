package com.example.calendartask.Fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;


//import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.calendartask.R;
public class ProfileFragment extends Fragment {


    /** SUPPOSE TO BE PROFILE INFO SHOWING, NO TIME :D*/

    public ProfileFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }


}
