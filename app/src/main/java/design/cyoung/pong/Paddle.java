package design.cyoung.pong;

import android.graphics.RectF;

class Paddle {
    final int STOPPED = 0;
    final int LEFT = -1;
    final int RIGHT = 1;
    private RectF mRect;
    private float mLength;
    private float mHeight;
    private float mXCoord;
    private float mYCoord;

    private float mBatSpeed;
    private int mPaddleMoving = STOPPED;

    private int mScreenX;
    private int mScreenY;

    Paddle(int x, int y) {

        mScreenX = x;
        mScreenY = y;

        // paddle length
        mLength = mScreenX / 6;

        // paddle height
        mHeight = mScreenY / 25;

        // Start in center of screen
        mXCoord = mScreenX / 2 - mLength / 2;
        mYCoord = mScreenY - 20;

        mRect = new RectF(mXCoord, mYCoord, mXCoord + mLength, mYCoord + mHeight);

        mBatSpeed = mScreenX;
    }

    RectF getRect() {
        return mRect;
    }

    void setMovementState(int state) {
        mPaddleMoving = state;
    }

    void update(long fps) {
        if (mPaddleMoving == LEFT) {
            mXCoord = mXCoord - mBatSpeed / fps;
        }

        if (mPaddleMoving == RIGHT) {
            mXCoord = mXCoord + mBatSpeed / fps;
        }

        if (mRect.left < 0) {
            mXCoord = 0;
        }
        if (mRect.right > mScreenX) {
            mXCoord = mScreenX - (mRect.right - mRect.left);
        }

        // update paddle
        mRect.left = mXCoord;
        mRect.right = mXCoord + mLength;
    }
}
