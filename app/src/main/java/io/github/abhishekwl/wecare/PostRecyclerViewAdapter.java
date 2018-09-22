package io.github.abhishekwl.wecare;

import android.content.Context;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PostRecyclerViewAdapter extends RecyclerView.Adapter<PostRecyclerViewAdapter.PostViewHolder> {

    private ArrayList<Post> postArrayList;
    private Location userLocation;

    public PostRecyclerViewAdapter(ArrayList<Post> postArrayList) {
        this.postArrayList = postArrayList;
    }

    @NonNull
    @Override
    public PostRecyclerViewAdapter.PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_list_item, parent, false);
        return new PostViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PostRecyclerViewAdapter.PostViewHolder holder, int position) {
        Post post = postArrayList.get(position);
        holder.render(holder.itemView.getContext(), post);
    }

    @Override
    public int getItemCount() {
        return postArrayList.size();
    }

    class PostViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.postListItemAuthorNameTextView)
        TextView authorNameTextView;
        @BindView(R.id.postListItemProfilePictureImageView)
        ImageView profilePictureImageView;
        @BindView(R.id.postListItemDistanceTextView)
        TextView distanceTextView;
        @BindView(R.id.postListItemPostContentTextView)
        TextView postContentTextView;

        PostViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void render(Context context, Post post) {
            Glide.with(context).load(post.getUser().getImage()).into(profilePictureImageView);
            authorNameTextView.setText(post.getUser().getName());
            postContentTextView.setText(post.getPostContent());
            distanceTextView.setText(post.getAreaName());

        }
    }
}
