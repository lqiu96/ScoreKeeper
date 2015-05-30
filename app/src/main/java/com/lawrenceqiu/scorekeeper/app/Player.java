package com.lawrenceqiu.scorekeeper.app;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: Lawrence
 * Date: 5/27/2015
 * Time: 2:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class Player implements Serializable, Parcelable {
    public static final Parcelable.Creator<Player> CREATOR = new Creator<Player>() {
        @Override
        public Player createFromParcel(Parcel source) {
            return new Player(source);
        }

        @Override
        public Player[] newArray(int size) {
            return new Player[size];
        }
    };
    private String name;
    private int score;

    /**
     * Player constructor which initializes name
     * and sets score by default to zero
     *
     * @param name Player's name
     */
    public Player(String name) {
        this.name = name;
        this.score = 0;
    }

    private Player(Parcel in) {
        name = in.readString();
        score = in.readInt();
    }

    /**
     * Gets the score
     *
     * @return Player's score
     */
    public int getScore() {
        return score;
    }

    /**
     * Gets the name
     *
     * @return Player's name
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Increase user's score by 1
     */
    public void addPoint() {
        score++;
    }

    /**
     * Decrease user's score by 1
     */
    public void subtractPoint() {
        score--;
    }

    /**
     * Tests if two Player classes are equal
     *
     * @param o Object being compared to
     * @return If the object passed into is the same or has the same name it is true, otherwise it is false
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Player player = (Player) o;

        return name.equals(player.getName());

    }

    /**
     * Create a generic hashcode
     *
     * @return Intellij's created hashcode based on the user's name
     */
    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    /**
     * String representation of the class
     *
     * @return String with player's name and score
     */
    @Override
    public String toString() {
        return "Player{" +
                "name='" + name + '\'' +
                ", score=" + score +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeInt(score);
    }
}
