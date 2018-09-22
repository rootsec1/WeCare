package io.github.abhishekwl.wecare;

import android.os.Parcel;
import android.os.Parcelable;

class User implements Parcelable {

    private String uid;
    private String name;
    private String image;
    private String contactNumber;
    private String emergencyContact1;
    private String emergencyContact2;
    private String getEmergencyContact3;

    public User() {}

    public User(String uid, String name, String image, String contactNumber, String emergencyContact1, String emergencyContact2, String getEmergencyContact3) {
        this.uid = uid;
        this.name = name;
        this.image = image;
        this.contactNumber = contactNumber;
        this.emergencyContact1 = emergencyContact1;
        this.emergencyContact2 = emergencyContact2;
        this.getEmergencyContact3 = getEmergencyContact3;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getEmergencyContact1() {
        return emergencyContact1;
    }

    public void setEmergencyContact1(String emergencyContact1) {
        this.emergencyContact1 = emergencyContact1;
    }

    public String getEmergencyContact2() {
        return emergencyContact2;
    }

    public void setEmergencyContact2(String emergencyContact2) {
        this.emergencyContact2 = emergencyContact2;
    }

    public String getGetEmergencyContact3() {
        return getEmergencyContact3;
    }

    public void setGetEmergencyContact3(String getEmergencyContact3) {
        this.getEmergencyContact3 = getEmergencyContact3;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.uid);
        dest.writeString(this.name);
        dest.writeString(this.image);
        dest.writeString(this.contactNumber);
        dest.writeString(this.emergencyContact1);
        dest.writeString(this.emergencyContact2);
        dest.writeString(this.getEmergencyContact3);
    }

    private User(Parcel in) {
        this.uid = in.readString();
        this.name = in.readString();
        this.image = in.readString();
        this.contactNumber = in.readString();
        this.emergencyContact1 = in.readString();
        this.emergencyContact2 = in.readString();
        this.getEmergencyContact3 = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel source) {
            return new User(source);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
}
