package com.dolling.view_model.another

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.dolling.listeners.FirestoreListener
import com.dolling.modal.ReviewModal
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ReviewViewModel(application: Application) : AndroidViewModel(application) {

    private val _reviewList = MutableLiveData<List<ReviewModal>>()

    val _reviewTotal = MutableLiveData<Int>()

    val _reviewAverage = MutableLiveData<Double>()

    fun getReviewListFromFirestore(sellerId: String) {
        Firebase.firestore.collection("_seller")
            .document(sellerId)
            .collection("review")
            .get()
            .addOnSuccessListener {
                var ratingTempList = arrayListOf<ReviewModal>()

                if (it.documents.size != 0) {
                    for (document in it.documents) {
                        ratingTempList.add(document.toObject(ReviewModal::class.java)!!)
                    }
                    _reviewList.value = ratingTempList

                    _reviewTotal.value = _reviewList.value!!.size

                    _reviewAverage.value = _reviewList.value!!.map { review ->
                        review.value
                    }.average()
                }
            }
    }

    fun insertReviewIntoFirestore(
        review: ReviewModal,
        sellerId: String,
        firestoreListener: FirestoreListener
    ) {
        var reviewTotal: Int = _reviewTotal.value ?: 0
        var reviewAverage: Double = _reviewAverage.value ?: 0.0

        Firebase.firestore.collection("_seller")
            .document(sellerId)
            .collection("review")
            .document(System.currentTimeMillis().toString())
            .set(review)
            .addOnSuccessListener {
                if (reviewTotal != 0) {
                    reviewTotal++
                    var tempReviewTotal = reviewTotal
                    var tempReviewAverage: Double =
                        ((reviewAverage * (reviewTotal - 1)) + review.value) / tempReviewTotal

                    updateSellerRatingInfo(
                        sellerId,
                        tempReviewTotal,
                        String.format("%.2f", tempReviewAverage).toDouble(),
                        firestoreListener
                    )
                } else {
                    updateSellerRatingInfo(
                        sellerId,
                        1,
                        review.value,
                        firestoreListener
                    )
                }
            }
            .addOnFailureListener {
                firestoreListener.onFailureListener(it.localizedMessage!!)
            }
    }

    private fun updateSellerRatingInfo(
        sellerId: String,
        ratingTotal: Int,
        rating: Double,
        firestoreListener: FirestoreListener
    ) {
        Firebase.firestore.collection("_seller")
            .document(sellerId)
            .update("rating", rating)
            .addOnSuccessListener {
                Firebase.firestore.collection("_seller")
                    .document(sellerId)
                    .update("rating_total", ratingTotal)
                    .addOnSuccessListener {
                        firestoreListener.onSuccessListener("")
                    }
                    .addOnFailureListener {
                        firestoreListener.onFailureListener(it.localizedMessage!!)
                    }
            }
            .addOnFailureListener {
                firestoreListener.onFailureListener(it.localizedMessage!!)
            }
    }
}