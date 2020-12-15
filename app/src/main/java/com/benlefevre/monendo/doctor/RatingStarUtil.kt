package com.benlefevre.monendo.doctor

import com.benlefevre.monendo.R
import com.benlefevre.monendo.databinding.RatingStarsBinding

fun defineNbStars(rating: Double, binding: RatingStarsBinding) {
    when (rating) {
        in 0.1..0.5 -> {
            binding.star1.setImageResource(R.drawable.half_star)
            binding.star2.setImageResource(R.drawable.empty_star)
            binding.star3.setImageResource(R.drawable.empty_star)
            binding.star4.setImageResource(R.drawable.empty_star)
            binding.star5.setImageResource(R.drawable.empty_star)
        }
        in 0.6..1.0 -> {
            binding.star1.setImageResource(R.drawable.full_star)
            binding.star2.setImageResource(R.drawable.empty_star)
            binding.star3.setImageResource(R.drawable.empty_star)
            binding.star4.setImageResource(R.drawable.empty_star)
            binding.star5.setImageResource(R.drawable.empty_star)
        }
        in 1.1..1.5 -> {
            binding.star1.setImageResource(R.drawable.full_star)
            binding.star2.setImageResource(R.drawable.half_star)
            binding.star3.setImageResource(R.drawable.empty_star)
            binding.star4.setImageResource(R.drawable.empty_star)
            binding.star5.setImageResource(R.drawable.empty_star)
        }
        in 1.6..2.0 -> {
            binding.star1.setImageResource(R.drawable.full_star)
            binding.star2.setImageResource(R.drawable.full_star)
            binding.star3.setImageResource(R.drawable.empty_star)
            binding.star4.setImageResource(R.drawable.empty_star)
            binding.star5.setImageResource(R.drawable.empty_star)
        }
        in 2.1..2.5 -> {
            binding.star1.setImageResource(R.drawable.full_star)
            binding.star2.setImageResource(R.drawable.full_star)
            binding.star3.setImageResource(R.drawable.half_star)
            binding.star4.setImageResource(R.drawable.empty_star)
            binding.star5.setImageResource(R.drawable.empty_star)
        }
        in 2.6..3.0 -> {
            binding.star1.setImageResource(R.drawable.full_star)
            binding.star2.setImageResource(R.drawable.full_star)
            binding.star3.setImageResource(R.drawable.full_star)
            binding.star4.setImageResource(R.drawable.empty_star)
            binding.star5.setImageResource(R.drawable.empty_star)
        }
        in 3.1..3.5 -> {
            binding.star1.setImageResource(R.drawable.full_star)
            binding.star2.setImageResource(R.drawable.full_star)
            binding.star3.setImageResource(R.drawable.full_star)
            binding.star4.setImageResource(R.drawable.half_star)
            binding.star5.setImageResource(R.drawable.empty_star)
        }
        in 3.6..4.0 -> {
            binding.star1.setImageResource(R.drawable.full_star)
            binding.star2.setImageResource(R.drawable.full_star)
            binding.star3.setImageResource(R.drawable.full_star)
            binding.star4.setImageResource(R.drawable.full_star)
            binding.star5.setImageResource(R.drawable.empty_star)
        }
        in 4.1..4.5 -> {
            binding.star1.setImageResource(R.drawable.full_star)
            binding.star2.setImageResource(R.drawable.full_star)
            binding.star3.setImageResource(R.drawable.full_star)
            binding.star4.setImageResource(R.drawable.full_star)
            binding.star5.setImageResource(R.drawable.half_star)
        }
        in 4.6..5.0 -> {
            binding.star1.setImageResource(R.drawable.full_star)
            binding.star2.setImageResource(R.drawable.full_star)
            binding.star3.setImageResource(R.drawable.full_star)
            binding.star4.setImageResource(R.drawable.full_star)
            binding.star5.setImageResource(R.drawable.full_star)
        }
        else -> {
            binding.star1.setImageResource(R.drawable.empty_star)
            binding.star2.setImageResource(R.drawable.empty_star)
            binding.star3.setImageResource(R.drawable.empty_star)
            binding.star4.setImageResource(R.drawable.empty_star)
            binding.star5.setImageResource(R.drawable.empty_star)
        }
    }
}