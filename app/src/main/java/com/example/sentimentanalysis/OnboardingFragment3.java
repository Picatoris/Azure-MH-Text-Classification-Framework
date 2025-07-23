package com.example.sentimentanalysis;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import reactor.util.annotation.NonNull;
import reactor.util.annotation.Nullable;

public class OnboardingFragment3 extends Fragment {
    public OnboardingFragment3() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_onboarding_3, container, false);
    }
}