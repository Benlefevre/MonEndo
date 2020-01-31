package com.benlefevre.monendo.injection

import android.content.Context
import com.benlefevre.monendo.data.repositories.FirestoreRepo

abstract class Injection {

    companion object{

        fun providerViewModelFactory(context: Context) : ViewModelFactory{
            val firestoreRepo:FirestoreRepo = FirestoreRepo.getInstance()
            return ViewModelFactory(firestoreRepo)
        }
    }
}