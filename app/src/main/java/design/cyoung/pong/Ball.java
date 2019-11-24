package design.cyoung.pong;

import android.graphics.RectF;

import java.util.Random;

class Ball {
    private RectF mRect;
    private float mXVelocity;
    private float mYVelocity;
    private int screenX;
    private int screenY;
    private float mBallWidth;
    private float mBallHeight;

    Ball(int screenX, int screenY) {
        this.screenX = screenX;
        this.screenY = screenY;
        mBallWidth = screenX / 100;
        mBallHeight = screenX / 100;

        mYVelocity = screenY / 3;
        mXVelocity = screenY / 3;

        mRect = new RectF();
    }

    RectF getRect() {
        return mRect;
    }

    void update(long fps) {
        mRect.left = mRect.left + (mXVelocity / fps);
        mRect.top = mRect.top + (mYVelocity / fps);
        mRect.right = mRect.left + mBallWidth;
        mRect.bottom = mRect.top - mBallHeight;
    }

    void reverseYVelocity() {
        mYVelocity = -mYVelocity;
    }

    void reverseXVelocity() {
        mXVelocity = -mXVelocity;
    }

    void setRandomXVelocity() {
        Random random = new Random();

        if (random.nextInt(2) == 0) {
            reverseXVelocity();
        }
    }

    void increaseVelocity() {
        mXVelocity = mXVelocity + mXVelocity / 10;
        mYVelocity = mYVelocity + mYVelocity / 10;
    }

    void resetVelocity() {
        mXVelocity = screenY / 3;
        mYVelocity = screenY / 3;
    }

    void clearObstacleY(float y){
        mRect.bottom = y;
        mRect.top = y - mBallHeight;
    }

    void clearObstacleX(float x){
        mRect.left = x;
        mRect.right = x + mBallWidth;
    }

    void reset(int x, int y){
        mRect.left = x / 2;
        mRect.top = y - 20;
        mRect.right = x / 2 + mBallWidth;
        mRect.bottom = y - 20 - mBallHeight;
        resetVelocity();
    }
}
