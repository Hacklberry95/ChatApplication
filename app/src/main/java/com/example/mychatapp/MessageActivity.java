package com.example.mychatapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.mychatapp.Adapters.MessageAdapter;
import com.example.mychatapp.Model.Chats;
import com.example.mychatapp.Model.Users;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageActivity extends AppCompatActivity {

    String friendId, myId, message;
    CircleImageView imageViewToolbar;
    TextView userNameOnToolbar;
    Toolbar toolbar;
    FirebaseUser firebaseUser;
    EditText editTextMessage;
    Button sendButton;
    Button cameraButton;
    List<Chats> chatsList;
    MessageAdapter mAdapter;
    RecyclerView recyclerView;
    ValueEventListener listener;
    DatabaseReference reference;
    public static final int CAMERA_CODE = 20;
    public static final int GALLERY_CODE = 100;
    Uri imageUri = null;
    SimpleDateFormat ISO_8601_FORMAT = new SimpleDateFormat("HH:mm");



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);


        toolbar = findViewById(R.id.toolbar_message);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        imageViewToolbar = findViewById(R.id.profile_image_toolbar_message);
        userNameOnToolbar = findViewById(R.id.username_ontoolbar_message);
        editTextMessage = findViewById(R.id.edit_message_text);
        sendButton = findViewById(R.id.send_messsage_btn);
        cameraButton = findViewById(R.id.camera_btn);
        recyclerView = findViewById(R.id.recyclerview_messages);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);



        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        myId = firebaseUser.getUid(); //id of the current user logged in
        friendId = getIntent().getStringExtra("friendId"); // this retrieves the friend id when clicked on the item

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(friendId);


        //This method adds the username to the toolbar of the user that has been clicked/tapped on
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Users users = snapshot.getValue(Users.class);

                assert users != null;
                userNameOnToolbar.setText(users.getUsername()); //this sets the value of the textview in the toolbar

                if (users.getImageURL().equals("default")) {
                    imageViewToolbar.setImageResource(R.drawable.user);
                } else {
                    Glide.with(getApplicationContext()).load(users.getImageURL()).into(imageViewToolbar);
                }
                readMessages(myId, friendId, users.getImageURL());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        seenMessage(friendId);



        editTextMessage.addTextChangedListener(new TextWatcher() {
            @Override
            //enabling/disabling send button depending on text length
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (s.toString().length() > 0) {
                    sendButton.setEnabled(true);
                } else {
                    sendButton.setEnabled(false);
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = editTextMessage.getText().toString();
                //moves the beginning of written text in the text field
                if (!text.startsWith(" ")) {
                    editTextMessage.getText().insert(0, " ");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                message = editTextMessage.getText().toString();

                sendMessage(myId, friendId, message);
                editTextMessage.setText(" ");

            }
        });
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Permissions();
                addImage();
            }
        });

    }


    private void addImage() {
        String[] options = {"Camera", "Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose from Gallery or create a new picture?");

        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    openCamera();
                }
                if (which == 1) {
                    openGallery();
                }
            }
        });
        builder.create().show();
    }
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*"); //allows all types of images
        startActivityForResult(intent, GALLERY_CODE);
    }

    private void openCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Temp pick");
        values.put(MediaStore.Images.Media.TITLE, "Temp Desc");
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, CAMERA_CODE);
    }
    private void Permissions() {
        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                ).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {/* ... */}

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {/* ... */}
        }).check();
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_CODE && resultCode == RESULT_OK) {
            assert data != null;
            imageUri = data.getData();
            String filePath = "Photos/" + "ImageBy: " + myId;

            StorageReference reference = FirebaseStorage.getInstance().getReference(filePath);

            reference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> task = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                    task.addOnSuccessListener(new OnSuccessListener<Uri>() {
                        public void onSuccess(Uri uri) {
                            String imageURL = uri.toString();

                            DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference();
                            String now = ISO_8601_FORMAT.format(new Date());
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("sender", myId);
                            hashMap.put("receiver", friendId);
                            hashMap.put("message", imageURL);
                            hashMap.put("time", now);
                            hashMap.put("isimage", true);
                            hashMap.put("isseen", false);

                            reference1.child("Chats").push().setValue(hashMap);
                            DatabaseReference reference2 = FirebaseDatabase.getInstance().getReference("Chatslist").child(myId).child(friendId);
                            reference1.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(!snapshot.exists())
                                    {
                                        reference2.child("id").setValue(friendId);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                        }
                    });
                }
            });
        }
        if (requestCode == CAMERA_CODE && resultCode == RESULT_OK) {
            Uri uri = imageUri;
            String filePath = "ChatImages/" + myId+"/";

            StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePath);

            storageReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> task = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                    task.addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String imageURL = uri.toString();

                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                            String now = ISO_8601_FORMAT.format(new Date());
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("sender", myId);
                            hashMap.put("receiver", friendId);
                            hashMap.put("message", imageURL);
                            hashMap.put("time", now);
                            hashMap.put("isimage", true);
                            hashMap.put("isseen", false);

                            databaseReference.child("Chats").push().setValue(hashMap);
                            DatabaseReference reference2 = FirebaseDatabase.getInstance().getReference("Chatslist").child(myId).child(friendId);
                            databaseReference.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(!snapshot.exists())
                                    {
                                        reference2.child("id").setValue(friendId);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                        }

                    });

                }
            });
        }
    }

    private void seenMessage(final String friendId)
    {
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        listener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren()){
                    Chats chats = ds.getValue(Chats.class);

                    assert chats != null;
                    if(chats.getReceiver().equals(myId) && chats.getSender().equals(friendId)){

                        HashMap<String, Object> hashMap = new HashMap<>();

                        hashMap.put("isseen", true);
                        ds.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //displays the active chat in the chats fragment
    private void readMessages(String myId, String friendId, String imageURL) {

        chatsList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatsList.clear();

                for(DataSnapshot ds : snapshot.getChildren())
                {

                    Chats chats = ds.getValue(Chats.class);
                    assert chats != null;

                    if(chats.getSender().equals(myId) && chats.getReceiver().equals(friendId) ||chats.getSender().equals(friendId) && chats.getReceiver().equals(myId))
                    {
                        chatsList.add(chats);
                    }

                    mAdapter = new MessageAdapter(MessageActivity.this, chatsList, imageURL);
                    recyclerView.setAdapter(mAdapter);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void sendMessage (String myId, String friendId, String message){
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            String now = ISO_8601_FORMAT.format(new Date());
            //storing the data in a hashmap before pushing to firebase
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("sender", myId);
            hashMap.put("receiver", friendId);
            hashMap.put("message", message);
            hashMap.put("time", now);
            hashMap.put("isseen", false);
            reference.child("Chats").push().setValue(hashMap); //assigning a unique ID for every message



            DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Chatslist").child(myId).child(friendId);
            reference1.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(!snapshot.exists())
                    {
                        reference1.child("id").setValue(friendId);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

    private void Status(final String status)
    {
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("status", status);

                reference.updateChildren(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Status("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Status("offline");
        reference.removeEventListener(listener);
    }

    }