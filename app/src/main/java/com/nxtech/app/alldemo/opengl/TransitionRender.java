package com.nxtech.app.alldemo.opengl;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.nxtech.app.alldemo.R;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class TransitionRender implements GLSurfaceView.Renderer {

    private TransitionDrawer mTransitionDrawer;
    private Context mContext;

    private int mTextureId;
    private int mTextureId2;
    private float[] mMMatrix;

    private float mProgress = 0f;

    public TransitionRender(Context context){
        this.mContext = context;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mTransitionDrawer = new TransitionDrawer(mContext.getResources());
        mMMatrix = new float[16];
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mTextureId = TextureHelper.loadTexture(mContext, R.mipmap.texture);
        mTextureId2 = TextureHelper.loadTexture(mContext, R.mipmap.drawpen);
        Matrix.setIdentityM(mMMatrix, 0);

        mTransitionDrawer.setDirection(1, 0);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        mTransitionDrawer.setProgress(mProgress);
        mTransitionDrawer.drawSelf(mTextureId, mTextureId2, mMMatrix);
        if (mProgress >= 1.0f) {
            mProgress = 0.0f;
        } else {
            mProgress += 0.01;
        }
    }
}
