package com.example.mychatapp.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.services.events.TimeStamp;

import com.bumptech.glide.Glide;
import com.example.mychatapp.Model.Chats;
import com.example.mychatapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MyViewHolder> {

    Context context;
    List<Chats> chatsList;
    String imageURL, myId;
    FirebaseUser user;
    TimeStamp timeStamp;



    public static final int MessageRight = 0; //sender
    public static final int MessageLeft = 1; //receiver

    public MessageAdapter(Context context, List<Chats> chatsList, String imageURL) {
        this.context = context;
        this.chatsList = chatsList;
        this.imageURL = imageURL;
    }

    public MessageAdapter() {
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == MessageRight) {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_right, parent, false);
            return new MyViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_left, parent, false);
            return new MyViewHolder(view);
        }


    }


    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Chats chats = chatsList.get(position);
        String now = chats.getTime();
                if(!chats.isIsimage()) {
                    holder.messageText.setText(chats.getMessage());
                }else {
                    holder.messageText.setVisibility(View.GONE);
                    holder.seen.setVisibility(View.VISIBLE);
                    Glide.with(context).load(chats.getMessage()).into(holder.messageImage);
                }
        if (imageURL.equals("default")) {
            holder.imageView.setImageResource(R.drawable.user);
        } else {
            Glide.with(context).load(imageURL).into(holder.imageView);
        }
        if(position == chatsList.size() -1)
        {
            if(chats.isIsseen())
            {
                holder.seen.setText("Seen");
            }else{
                holder.seen.setText("Delivered: " + now);
            }
        }else {
            holder.seen.setVisibility(View.GONE);
        }
    }


    @Override
    public int getItemCount() {
        return chatsList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder{

        TextView messageText, seen;
        CircleImageView imageView;
        ImageView messageImage;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            messageText = itemView.findViewById(R.id.show_message);
            imageView = itemView.findViewById(R.id.chat_image);
            seen = itemView.findViewById(R.id.text_Seen);
            messageImage = itemView.findViewById(R.id.message_image);
        }
    }

    @Override
    public int getItemViewType(int position) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        assert user != null;
        if(chatsList.get(position).getSender().equals(user.getUid()))
        {
            return MessageRight;
        }
        else
            return MessageLeft;
    }
}
