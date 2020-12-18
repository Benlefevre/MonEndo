package com.benlefevre.monendo.doctor.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.benlefevre.monendo.R
import com.benlefevre.monendo.databinding.CommentaryListItemBinding
import com.benlefevre.monendo.doctor.defineNbStars
import com.benlefevre.monendo.doctor.models.Commentary
import com.benlefevre.monendo.utils.NO_PHOTO_URL
import com.benlefevre.monendo.utils.formatDateWithYear
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class CommentaryAdapter(
    private val commentaries: List<Commentary>,
    private val listener: CommentaryListAdapterListener? = null
) : RecyclerView.Adapter<CommentaryAdapter.CommentaryViewHolder>() {

    interface CommentaryListAdapterListener {
        fun onCommentarySelected(commentary: Commentary)
    }

    private lateinit var context: Context
    var index = -1

    class CommentaryViewHolder(val binding: CommentaryListItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentaryViewHolder {
        context = parent.context
        val binding = CommentaryListItemBinding.inflate(
            LayoutInflater.from(context), parent, false
        )
        return CommentaryViewHolder(binding)
    }

    override fun getItemCount(): Int = commentaries.size

    override fun onBindViewHolder(holder: CommentaryViewHolder, position: Int) {
        val commentary = commentaries[position]
        with(holder) {
            listener?.let {
                binding.itemCheck.visibility = View.VISIBLE
                binding.itemCheck.setOnClickListener {
                    listener.onCommentarySelected(it.tag as Commentary)
                }
            }
            binding.itemDivider.visibility =
                if (commentaries.size < 2 || commentaries.indexOf(commentary) == commentaries.lastIndex) View.GONE else View.VISIBLE
            binding.itemCheck.tag = commentary
            binding.root.tag = commentary
            binding.itemDate.text = formatDateWithYear(commentary.date)
            binding.itemContent.text = commentary.userInput
            binding.userName.text = commentary.authorName
            binding.doctorName.text = commentary.doctorName
            if (commentary.authorPhotoUrl != NO_PHOTO_URL) {
                Glide.with(context)
                    .load(commentary.authorPhotoUrl)
                    .apply(RequestOptions.circleCropTransform())
                    .into(binding.userPhoto)
            } else {
                Glide.with(context)
                    .load(R.drawable.ic_girl)
                    .apply(RequestOptions.circleCropTransform())
                    .into(binding.userPhoto)
            }
            defineNbStars(commentary.rating, binding.itemRating)
        }
    }
}
