/**
 * Copyright Google Inc. All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.firebase.udacity.friendlychat;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import static android.R.attr.data;
import static android.R.attr.y;
import static android.os.Build.ID;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    public static final String ANONYMOUS = "anonymous";
    public static final int DEFAULT_MSG_LENGTH_LIMIT = 1000;

    private ListView mMessageListView;
    private MessageAdapter mMessageAdapter;
    private ProgressBar mProgressBar;
    private ImageButton mPhotoPickerButton;
    private EditText mMessageEditText;
    private Button mSendButton;

    private String mUsername;


    /*Two classes from Firebase Database API.*/
    //Firebase database object is the entry point for our app to access the database.
    private FirebaseDatabase mFirebaseDatabase;
    //Database Reference object is a class that reference a specific part of the database.
    // This will be referencing the messaging portion of our database.
    private DatabaseReference mMessagesDatabaseReference;

    /*Event listener that reacts to the Firebase database changes in the real-time.*/
    private ChildEventListener mChildEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mUsername = ANONYMOUS;

        /*instantiate the two firebase database object*/
        //getting instance to the firebase database
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        //getting reference to the specific part of the database.
        // getReference() will get the reference to the root, while child() will refer to the specific part i.e. "messages"
        mMessagesDatabaseReference = mFirebaseDatabase.getReference().child("messages");


        // Initialize references to views
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mMessageListView = (ListView) findViewById(R.id.messageListView);
        mPhotoPickerButton = (ImageButton) findViewById(R.id.photoPickerButton);
        mMessageEditText = (EditText) findViewById(R.id.messageEditText);
        mSendButton = (Button) findViewById(R.id.sendButton);

        // Initialize message ListView and its adapter
        List<FriendlyMessage> friendlyMessages = new ArrayList<>();
        mMessageAdapter = new MessageAdapter(this, R.layout.item_message, friendlyMessages);
        mMessageListView.setAdapter(mMessageAdapter);

        // Initialize progress bar
        mProgressBar.setVisibility(ProgressBar.INVISIBLE);

        // ImagePickerButton shows an image picker to upload a image for a message
        mPhotoPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: Fire an intent to show an image picker
            }
        });

        // Enable Send button when there's text to send
        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    mSendButton.setEnabled(true);
                } else {
                    mSendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        mMessageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(DEFAULT_MSG_LENGTH_LIMIT)});

        // Send button sends a message and clears the EditText
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //create a FriendlyMessage object for the message that the user typed in
                FriendlyMessage friendlyMessage = new FriendlyMessage(mMessageEditText.getText().toString(), mUsername, null);

                /*A push ID contains 120 bits of information.
                The first 48 bits are a timestamp, which both reduces the chance of collision
                and allows consecutively created push IDs to sort chronologically.
                The timestamp is followed by 72 bits of randomness,
                which ensures that even two people creating push IDs at the exact same millisecond
                are extremely unlikely to generate identical IDs.*/
                mMessagesDatabaseReference.push().setValue(friendlyMessage);

                // Clear input box
                mMessageEditText.setText("");
            }
        });

        /*Instantiate new ChildEventListener*/
        mChildEventListener = new ChildEventListener() {
            //This method is called whenever a new child is inserted into the messages list.
            //Importantly, it is also triggered for every child message in the list when the listener is attached.
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                /*DataSnapshot contains data from the firebase database at the specific location
                  at the exact time the listener is triggered*/
                //In this case, dataSnapshot will always contain the messag that has been added.
                //The getValue() method can take a parameter which is a class by passing this parameter
                //the code will deserialize the message from the database into our FriendlyMessage object.
                FriendlyMessage friendlyMessage = dataSnapshot.getValue(FriendlyMessage.class);
                //add the FriendlyMessage object to our adapter.
                mMessageAdapter.add(friendlyMessage);

            }

            //This is called when the content of the existing message gets changed.
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            //This method is called when an existing message is deleted.
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            //This method is called when whatever message changed position in the list.
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            //This method indicate that some sort of error occurred when you're trying to make changes.
            //Typically this means that you don't have permission to read it.
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        /*Add the listener to the database reference.*/
        //This will trigger when one of the node of messages changes.
        mMessagesDatabaseReference.addChildEventListener(mChildEventListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
