package com.example.sayar.imageuploadingapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ShowingList extends AppCompatActivity {
    RecyclerView recyclerView;
    DatabaseReference mDatabaseReference;
    List<ImageUrl> uploadList=new ArrayList<>();
    PhotoViewAttacher pAttacher;
    ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showing_list);
        recyclerView = (RecyclerView) findViewById(R.id.listView);
// set a LinearLayoutManager with default orientation
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        getSupportActionBar().setTitle(getString(R.string.upload_image)); // for set actionbar title
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        progressBar=findViewById(R.id.progressbar);
        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                progressBar.setVisibility(View.VISIBLE);
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    ImageUrl upload = postSnapshot.getValue(ImageUrl.class);
                    uploadList.add(upload);
                }
                progressBar.setVisibility(View.GONE);

                String[] uploads = new String[uploadList.size()];

                for (int i = 0; i < uploads.length; i++) {
                    uploads[i] = uploadList.get(i).getName();
                    ImageUrl imageUrl=new ImageUrl(uploadList.get(i).getDownloadUrl(),uploadList.get(i).getName());
                    //stringList.add(imageUrl);


                    Log.d("url",uploadList.get(i).getDownloadUrl());
                    Log.d("id",uploadList.get(i).getName());
                }

                CustomAdapter customAdapter = new CustomAdapter(ShowingList.this, (ArrayList<ImageUrl>) uploadList);
                recyclerView.setAdapter(customAdapter);
                customAdapter.notifyDataSetChanged();



                //displaying it to list
//                    ArrayAdapter<ImageUrl> adapter = new ArrayAdapter<ImageUrl>(getApplicationContext(), android.R.layout.simple_list_item_1, stringList);
//                    listView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

        class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.MyViewHolder> {
        List<ImageUrl> imageUrlArrayList;
        Context context;
        public CustomAdapter(Context context, ArrayList<ImageUrl> imageUrlArrayList) {
            this.context = context;
            this.imageUrlArrayList = imageUrlArrayList;
        }
        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // infalte the item Layout
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_row, parent, false);
            // set the view's size, margins, paddings and layout parameters
            MyViewHolder vh = new MyViewHolder(v); // pass the view to View Holder
            return vh;
        }
        @Override
        public void onBindViewHolder(final MyViewHolder holder, final int position) {
            // set the data in items

            ImageUrl imageUrl=imageUrlArrayList.get(position);
            holder.name.setText(imageUrl.getName());
            //Glide.with(context).load(imageUrl.getDownloadUrl()).into(holder.imageView);
            Glide.with(context).load(imageUrl.getDownloadUrl()).apply(RequestOptions.circleCropTransform()).into(holder.imageView);
            Glide.with(context).load(imageUrl.getDownloadUrl()).into(holder.fullImageView);
            pAttacher = new PhotoViewAttacher(holder.fullImageView);
            pAttacher.update();
            holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d("clicked","yes");
                    //holder.fullImageView.setVisibility(View.VISIBLE);
                if (holder.fullImageView.getVisibility()==View.VISIBLE){
                    holder.fullImageView.setVisibility(View.GONE);

                }
                else{
                    holder.fullImageView.setVisibility(View.VISIBLE);
                }
                }
            });


        }
        @Override
        public int getItemCount() {
            return imageUrlArrayList.size();
        }
        @Override
        public int getItemViewType(int position) {
            return position;
        }
        public class MyViewHolder extends RecyclerView.ViewHolder {
            TextView name;// init the item view's
            ImageView imageView;
            PhotoView fullImageView;
            LinearLayout linearLayout;
            RelativeLayout relativeLayout;
            public MyViewHolder(View itemView) {
                super(itemView);
                // get the reference of item view's
                name = (TextView) itemView.findViewById(R.id.name);
                imageView=itemView.findViewById(R.id.icon);
                linearLayout=itemView.findViewById(R.id.linearWebView);
                relativeLayout=itemView.findViewById(R.id.parentLayout);
                fullImageView=itemView.findViewById(R.id.fullImage);
            }
        }
    }
}
