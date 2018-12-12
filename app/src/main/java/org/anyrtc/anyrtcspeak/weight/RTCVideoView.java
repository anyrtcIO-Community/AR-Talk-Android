package org.anyrtc.anyrtcspeak.weight;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import org.anyrtc.anyrtcspeak.R;
import org.webrtc.EglBase;
import org.webrtc.EglRenderer;
import org.webrtc.PercentFrameLayout;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoRenderer;

import static android.view.View.VISIBLE;


/**
 */
public class RTCVideoView {









    private EglBase mRootEglBase;
    private VideoView videoRender;
    private Context context;

    public RTCVideoView(EglBase eglBase, Context context) {
        mRootEglBase = eglBase;
        videoRender = null;
        this.context=context;
    }

    public VideoView getVideoRender() {
        return videoRender;
    }

    public void setVideoRender(VideoView videoRender) {
        this.videoRender = videoRender;
    }

    public VideoRenderer openVideoRender(String peerId) {
        videoRender = new VideoView(peerId, context, mRootEglBase, 0, 0, 0, 100, 100);
        videoRender.mView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_BALANCED);
        videoRender.tvLoading.setVisibility(VISIBLE);
        videoRender.mView.addFrameListener(new EglRenderer.FrameListener() {
            @Override
            public void onFrame(Bitmap frame) {
                Log.d("surfaceView", frame.toString());
                videoRender.mView.post(new Runnable() {
                    @Override
                    public void run() {
                        videoRender.tvLoading.setVisibility(View.GONE);
                    }
                });

            }
        }, 1f);
        videoRender.mRenderer = new VideoRenderer(videoRender.mView);
        return videoRender.mRenderer;
    }


    public static class VideoView {

        public String strPeerId;
        public int index;
        public int x;
        public int y;
        public int w;
        public int h;
        public PercentFrameLayout mLayout = null;
        public SurfaceViewRenderer mView = null;
        public VideoRenderer mRenderer = null;
        private FrameLayout tvLoading;

        public VideoView(String strPeerId, Context ctx, EglBase eglBase, int index, int x, int y, int w, int h) {
            this.strPeerId = strPeerId;
            this.index = index;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;

            mLayout = new PercentFrameLayout(ctx);
            mLayout.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
            View view = View.inflate(ctx, R.layout.layout_top_right, null);
            tvLoading = view.findViewById(R.id.tv_loading);
            mView = (SurfaceViewRenderer) view.findViewById(R.id.suface_view);
            mView.init(eglBase.getEglBaseContext(), null);
            mView.setZOrderMediaOverlay(false);
            mView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
            mLayout.addView(view);
        }
    }





}
