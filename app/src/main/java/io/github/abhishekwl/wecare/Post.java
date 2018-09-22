package io.github.abhishekwl.wecare;

import android.os.Parcel;
import android.os.Parcelable;

public class Post implements Parcelable {

    private User user;
    private double latitude;
    private double longitude;
    private String postContent;
    private String areaName;

    Post(User user, double latitude, double longitude, String postContent, String areaName) {
        this.user = user;
        this.latitude = latitude;
        this.longitude = longitude;
        this.postContent = postContent;
        this.areaName = areaName;
    }

    public Post() {}

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getPostContent() {
        return postContent;
    }

    public void setPostContent(String postContent) {
        this.postContent = postContent;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.user, flags);
        dest.writeDouble(this.latitude);
        dest.writeDouble(this.longitude);
        dest.writeString(this.postContent);
    }

    private Post(Parcel in) {
        this.user = in.readParcelable(User.class.getClassLoader());
        this.latitude = in.readDouble();
        this.longitude = in.readDouble();
        this.postContent = in.readString();
    }

    public static final Creator<Post> CREATOR = new Creator<Post>() {
        @Override
        public Post createFromParcel(Parcel source) {
            return new Post(source);
        }

        @Override
        public Post[] newArray(int size) {
            return new Post[size];
        }
    };

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }
}
