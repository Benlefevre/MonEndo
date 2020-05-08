package com.benlefevre.monendo.doctor

import android.view.View
import com.benlefevre.monendo.R
import kotlinx.android.synthetic.main.rating_stars.view.*

fun defineNbStars(rating: Double, view: View) {
    when (rating) {
        in 0.1..0.5 -> {
            view.star1.setImageResource(R.drawable.half_star)
            view.star2.setImageResource(R.drawable.empty_star)
            view.star3.setImageResource(R.drawable.empty_star)
            view.star4.setImageResource(R.drawable.empty_star)
            view.star5.setImageResource(R.drawable.empty_star)
        }
        in 0.6..1.0 -> {
            view.star1.setImageResource(R.drawable.full_star)
            view.star2.setImageResource(R.drawable.empty_star)
            view.star3.setImageResource(R.drawable.empty_star)
            view.star4.setImageResource(R.drawable.empty_star)
            view.star5.setImageResource(R.drawable.empty_star)
        }
        in 1.1..1.5 -> {
            view.star1.setImageResource(R.drawable.full_star)
            view.star2.setImageResource(R.drawable.half_star)
            view.star3.setImageResource(R.drawable.empty_star)
            view.star4.setImageResource(R.drawable.empty_star)
            view.star5.setImageResource(R.drawable.empty_star)
        }
        in 1.6..2.0 -> {
            view.star1.setImageResource(R.drawable.full_star)
            view.star2.setImageResource(R.drawable.full_star)
            view.star3.setImageResource(R.drawable.empty_star)
            view.star4.setImageResource(R.drawable.empty_star)
            view.star5.setImageResource(R.drawable.empty_star)
        }
        in 2.1..2.5 -> {
            view.star1.setImageResource(R.drawable.full_star)
            view.star2.setImageResource(R.drawable.full_star)
            view.star3.setImageResource(R.drawable.half_star)
            view.star4.setImageResource(R.drawable.empty_star)
            view.star5.setImageResource(R.drawable.empty_star)
        }
        in 2.6..3.0 -> {
            view.star1.setImageResource(R.drawable.full_star)
            view.star2.setImageResource(R.drawable.full_star)
            view.star3.setImageResource(R.drawable.full_star)
            view.star4.setImageResource(R.drawable.empty_star)
            view.star5.setImageResource(R.drawable.empty_star)
        }
        in 3.1..3.5 -> {
            view.star1.setImageResource(R.drawable.full_star)
            view.star2.setImageResource(R.drawable.full_star)
            view.star3.setImageResource(R.drawable.full_star)
            view.star4.setImageResource(R.drawable.half_star)
            view.star5.setImageResource(R.drawable.empty_star)
        }
        in 3.6..4.0 -> {
            view.star1.setImageResource(R.drawable.full_star)
            view.star2.setImageResource(R.drawable.full_star)
            view.star3.setImageResource(R.drawable.full_star)
            view.star4.setImageResource(R.drawable.full_star)
            view.star5.setImageResource(R.drawable.empty_star)
        }
        in 4.1..4.5 -> {
            view.star1.setImageResource(R.drawable.full_star)
            view.star2.setImageResource(R.drawable.full_star)
            view.star3.setImageResource(R.drawable.full_star)
            view.star4.setImageResource(R.drawable.full_star)
            view.star5.setImageResource(R.drawable.half_star)
        }
        in 4.6..5.0 -> {
            view.star1.setImageResource(R.drawable.full_star)
            view.star2.setImageResource(R.drawable.full_star)
            view.star3.setImageResource(R.drawable.full_star)
            view.star4.setImageResource(R.drawable.full_star)
            view.star5.setImageResource(R.drawable.full_star)
        }
        else -> {
            view.star1.setImageResource(R.drawable.empty_star)
            view.star2.setImageResource(R.drawable.empty_star)
            view.star3.setImageResource(R.drawable.empty_star)
            view.star4.setImageResource(R.drawable.empty_star)
            view.star5.setImageResource(R.drawable.empty_star)
        }
    }
}