package com.chatslau.adapter;

import android.content.Context;
import android.view.View;

import com.bumptech.glide.Glide;
import com.chatslau.R;

import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class RecyclerViewAdapterImage extends RecyclerView.ViewHolder{

    public RecyclerViewAdapterImage(View itemView) {
        super(itemView);
    }

    public void setDisplayImage(String imageUrl, Context context) {

        CircleImageView image = itemView.findViewById(R.id.gambar);

        Glide.with(context)
                .load(imageUrl)
                .into(image);
    }
}
