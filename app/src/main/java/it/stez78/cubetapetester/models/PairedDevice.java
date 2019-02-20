package it.stez78.cubetapetester.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Stefano Zanotti on 20/02/2019.
 */
public class PairedDevice implements Parcelable {

    private String name;
    private String macAddress;
    private boolean paired;

    public PairedDevice(){
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public boolean isPaired() {
        return paired;
    }

    public void setPaired(boolean paired) {
        this.paired = paired;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.macAddress);
        dest.writeByte(this.paired ? (byte) 1 : (byte) 0);
    }

    protected PairedDevice(Parcel in) {
        this.name = in.readString();
        this.macAddress = in.readString();
        this.paired = in.readByte() != 0;
    }

    public static final Creator<PairedDevice> CREATOR = new Creator<PairedDevice>() {
        @Override
        public PairedDevice createFromParcel(Parcel source) {
            return new PairedDevice(source);
        }

        @Override
        public PairedDevice[] newArray(int size) {
            return new PairedDevice[size];
        }
    };
}
