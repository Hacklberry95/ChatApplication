package com.example.mychatapp.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mychatapp.MessageActivity;
import com.example.mychatapp.Model.Chats;
import com.example.mychatapp.Model.Users;
import com.example.mychatapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.MyHolder> {

    Context context;
    List<Users> usersList;
    boolean isChat;
    String friendId;
    String theLastMessage;
    boolean isLastMessageImage;
    FirebaseUser firebaseUser;

    public UserAdapter(Context context, List<Users> usersList, boolean isChat) {
        this.context = context;
        this.usersList = usersList;
        this.isChat = isChat;
    }

    public UserAdapter(Context context, List<Users> mUsers) {
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.layoutofusers, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        Users user = usersList.get(position);

        friendId = user.getId();

        holder.username.setText(user.getUsername());

        if (user.getImageURL().equals("default")) {
            holder.imageView.setImageResource(R.drawable.user);
        } else {
            Glide.with(context).load(user.getImageURL()).into(holder.imageView);
        }

        if(isChat){
            if(user.getStatus().equals("online"))
            {
                holder.image_online.setVisibility(View.VISIBLE);
                holder.image_offline.setVisibility(View.GONE);
            }else
            {
                holder.image_online.setVisibility(View.GONE);
                holder.image_offline.setVisibility(View.VISIBLE);
            }
        }else{
            holder.image_online.setVisibility(View.GONE);
            holder.image_offline.setVisibility(View.GONE);
        }
        if(isChat){
            LastMessage(user.getId(), holder.lastMsg);
        }else {
            holder.lastMsg.setVisibility(View.GONE);
        }
    }


    @Override
    public int getItemCount() {
        return usersList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView username, lastMsg;
        CircleImageView imageView, image_online, image_offline;
        ImageView backgroundView;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //binding the views
            username = itemView.findViewById(R.id.username_userfrag);
            imageView = itemView.findViewById(R.id.image_user_userfrag);
            image_online = itemView.findViewById(R.id.image_online);
            image_offline = itemView.findViewById(R.id.image_offline);
            lastMsg = itemView.findViewById(R.id.lastMessage);
            backgroundView = itemView.findViewById(R.id.profileBackground);
            itemView.setOnClickListener(this);
        }

        //displays the message activity depending on which user was clicked/tapped
        @Override
        public void onClick(View v) {
            Users users = usersList.get(getAdapterPosition());
            friendId = users.getId();
            Intent intent = new Intent(context, MessageActivity.class);
            //Toast.makeText(context, "ID is " + friendId, Toast.LENGTH_SHORT).show(); //Checking whether it returns the correct ID of the user clicked on
            intent.putExtra("friendId", friendId);

            context.startActivity(intent);
        }
    }

    private void LastMessage(String friendId, TextView lastMsg)
    {
        theLastMessage = "default";

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren())
                {
                    Chats chats = ds.getValue(Chats.class);

                    if(firebaseUser!=null && chats!=null){
                        if(chats.getSender().equals(friendId) && chats.getReceiver().equals(firebaseUser.getUid())
                                || chats.getSender().equals(firebaseUser.getUid()) && chats.getReceiver().equals(friendId))
                        {
                            theLastMessage = chats.getMessage();
                            isLastMessageImage = chats.isIsimage();
                        }
                    }
                }

                switch (theLastMessage)
                {
                    case "default":
                        lastMsg.setText("No message");
                        break;
                    default:
                        if(isLastMessageImage){
                         lastMsg.setText("IMAGE");
                        }else{
                        lastMsg.setText(theLastMessage);
                        }
                }
                theLastMessage = "default";
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}

