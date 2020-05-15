package com.benlefevre.monendo.doctor

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.benlefevre.monendo.R
import com.benlefevre.monendo.doctor.models.Commentary
import com.benlefevre.monendo.utils.NO_PHOTO_URL
import com.benlefevre.monendo.utils.formatDateWithYear
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.textview.MaterialTextView
import kotlinx.android.synthetic.main.commentary_list_item.view.*

class CommentaryAdapter(
    private val commentaries: List<Commentary>,
    private val listener: CommentaryListAdapterListener? = null
) : RecyclerView.Adapter<CommentaryAdapter.CommentaryViewHolder>() {

    interface CommentaryListAdapterListener {
        fun onCommentarySelected(commentary: Commentary)
    }

    private lateinit var context: Context
    var index = -1
    val indexList = mutableListOf<Int>()

    class CommentaryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val commentLayout: ConstraintLayout = itemView.commentary_item_root
        val commentDate: MaterialTextView = itemView.commentary_item_date
        val commentContent: MaterialTextView = itemView.commentary_item_content
        val userName: MaterialTextView = itemView.commentary_item_user_name
        val userPhoto: AppCompatImageView = itemView.commentary_item_user_photo
        val ratingStar: View = itemView.commentary_item_rating
        val checkBox: CheckBox = itemView.commentary_item_check
        val divider: View = itemView.commentary_item_divider
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentaryViewHolder {
        context = parent.context
        val view =
            LayoutInflater.from(context).inflate(R.layout.commentary_list_item, parent, false)
        return CommentaryViewHolder(view)
    }

    override fun getItemCount(): Int = commentaries.size

    override fun onBindViewHolder(holder: CommentaryViewHolder, position: Int) {
        val commentary = commentaries[position]
        with(holder) {
            listener?.let {
                checkBox.visibility = View.VISIBLE
                checkBox.setOnClickListener {
                    listener.onCommentarySelected(it.tag as Commentary)
                }
            }
            divider.visibility =
                if (commentaries.size < 2 || commentaries.indexOf(commentary) == commentaries.lastIndex) View.GONE else View.VISIBLE
            checkBox.tag = commentary
            commentLayout.tag = commentary
            commentDate.text = formatDateWithYear(commentary.date)
            commentContent.text = commentary.userInput
            userName.text = commentary.authorName
            if (commentary.authorPhotoUrl != NO_PHOTO_URL) {
                Glide.with(context)
                    .load(commentary.authorPhotoUrl)
                    .apply(RequestOptions.circleCropTransform())
                    .into(userPhoto)
            } else {
                Glide.with(context)
                    .load(R.drawable.ic_girl)
                    .apply(RequestOptions.circleCropTransform())
                    .into(userPhoto)
            }
            defineNbStars(commentary.rating, ratingStar)
        }
    }
}