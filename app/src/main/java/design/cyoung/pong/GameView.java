package design.cyoung.pong;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.appcompat.app.AlertDialog;

class GameView extends SurfaceView implements Runnable {
    Context mContext;

    Thread mGameThread = null;
    SurfaceHolder mOurHolder;
    volatile boolean mPlaying;

    boolean mPaused = true;

    Canvas mCanvas;
    Paint mPaint;

    long mFPS;

    int mScreenX;
    int mScreenY;

    Paddle mPaddle;

    Ball mBall;

    SoundPool sp;
    int bounceID;
    int gameOverID;
    int damageID;

    int mScore = 0;
    int mLives = 2;

    public GameView(Context context, int x, int y) {
        super(context);
        mContext = context;

        mScreenX = x;
        mScreenY = y;

        mOurHolder = getHolder();
        mPaint = new Paint();

        mPaddle = new Paddle(mScreenX, mScreenY);

        mBall = new Ball(mScreenX, mScreenY);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            sp = new SoundPool.Builder()
                    .setMaxStreams(5)
                    .setAudioAttributes(audioAttributes)
                    .build();

        } else {
            sp = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        }

        bounceID = sp.load(context, R.raw.bounce, 1);
        damageID = sp.load(context, R.raw.damage, 1);
        gameOverID = sp.load(context, R.raw.game_over, 1);

        setupAndRestart();
    }

    public void setupAndRestart() {
        mBall.reset(mScreenX, mScreenY);
        if (mLives == 0) {
            mScore = 0;
            mLives = 2;
        }
    }

    @Override
    public void run() {
        while (mPlaying) {
            long startFrameTime = System.currentTimeMillis();

            if (!mPaused) {
                update();
            }

            draw();

            long timeThisFrame = System.currentTimeMillis() - startFrameTime;
            if (timeThisFrame >= 1) {
                mFPS = 1000 / timeThisFrame;
            }
        }
    }

    public void update() {
        mPaddle.update(mFPS);
        mBall.update(mFPS);

        // ball and paddle collision
        if (RectF.intersects(mPaddle.getRect(), mBall.getRect())) {
            mBall.setRandomXVelocity();
            mBall.reverseYVelocity();
            mBall.clearObstacleY(mPaddle.getRect().top - 2);

            mScore++;
            mBall.increaseVelocity();

            sp.play(bounceID, 1, 1, 0, 0, 1);
        }

        // bottom screen collision
        if (mBall.getRect().bottom > mScreenY) {
            mBall.reverseYVelocity();
            mBall.clearObstacleY(mScreenY - 2);

            // take damage
            mLives--;

            if (mLives == 0) {
                mPaused = true;
                sp.play(gameOverID, 1, 1, 0, 0, 1);

                final SharedPreferences sPrefs = mContext.getSharedPreferences("pong", Context.MODE_PRIVATE);
                if (mScore > sPrefs.getInt("best_score", 0)) {
                    SharedPreferences.Editor prefsEditor = sPrefs.edit();
                    prefsEditor.putInt("best_score", mScore);
                    prefsEditor.apply();
                }

                // display end game message
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog.Builder builder =
                                new AlertDialog.Builder(mContext)
                                        .setTitle("Game Over!")
                                        .setMessage("You scored: " + mScore + "\n Best score: " + sPrefs.getInt("best_score", 0))
                                        .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                                setupAndRestart();
                                            }
                                        })
                                        .setCancelable(false);
                        builder.create().show();
                    }
                });
            } else {
                sp.play(damageID, 1, 1, 0, 0, 1);
            }
        }

        // top screen collision
        if (mBall.getRect().top < 0) {
            mBall.reverseYVelocity();
            mBall.clearObstacleY(12);

            sp.play(bounceID, 1, 1, 0, 0, 1);
        }

        // left wall collision
        if (mBall.getRect().left < 0) {
            mBall.reverseXVelocity();
            mBall.clearObstacleX(2);

            sp.play(bounceID, 1, 1, 0, 0, 1);
        }

        // right wall collision
        if (mBall.getRect().right > mScreenX) {
            mBall.reverseXVelocity();
            mBall.clearObstacleX(mScreenX - 22);

            sp.play(bounceID, 1, 1, 0, 0, 1);
        }
    }

    public void draw() {
        if (mOurHolder.getSurface().isValid()) {
            mCanvas = mOurHolder.lockCanvas();

            // set bg color to prevent previous draws from being visible
            mCanvas.drawColor(Color.argb(255, 0, 0, 0));

            // ball and paddle color
            mPaint.setColor(Color.argb(255, 255, 255, 255));

            // ball
            mCanvas.drawRect(mBall.getRect(), mPaint);

            // paddle
            mCanvas.drawRect(mPaddle.getRect(), mPaint);

            // score color
            mPaint.setColor(Color.argb(255, 255, 255, 255));

            // Displays score
            mPaint.setTextSize(40);
            mCanvas.drawText("Score: " + mScore + "    Lives: " + mLives, 50, 50, mPaint);

            mOurHolder.unlockCanvasAndPost(mCanvas);
        }
    }

    public void pause() {
        mPlaying = false;
        try {
            mGameThread.join();
        } catch (InterruptedException ignored) { }

    }

    public void resume() {
        mPlaying = true;
        mGameThread = new Thread(this);
        mGameThread.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mPaused = false;
                mPaddle.setMovementState(motionEvent.getX() > mScreenX / 2.0 ? mPaddle.RIGHT : mPaddle.LEFT);
                break;

            case MotionEvent.ACTION_UP:
                mPaddle.setMovementState(mPaddle.STOPPED);
                break;
        }
        return true;
    }
}
