package org.anyrtc.anyrtcspeak.adapter;

import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import org.anyrtc.anyrtcspeak.R;
import org.anyrtc.anyrtcspeak.bean.VideoBean;
import org.anyrtc.common.utils.ScreenUtils;

/**
 * Created by liuxiaozhong on 2018/10/19.
 */
public class VideoAdapter extends BaseQuickAdapter<VideoBean,  BaseViewHolder> {

    public VideoAdapter() {
        super(R.layout.item_look_video);
    }

    @Override
    protected void convert(BaseViewHolder helper, VideoBean item) {
        RelativeLayout rlview = helper.getView(R.id.rl_video);
        if (item.getVideoView() != null) {
            ViewGroup parentViewGroup = (ViewGroup) item.getVideoView().getParent();
            if (parentViewGroup != null) {
                parentViewGroup.removeView(item.getVideoView());
            }
            int screenWidth = ScreenUtils.getScreenWidth(helper.itemView.getContext());
            float WIDTH =(screenWidth / 4f);
            float HEIGHT = (WIDTH* 1.333333f);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) rlview.getLayoutParams();
            params.width= (int) WIDTH;
            params.height= (int) HEIGHT;
            rlview.setLayoutParams(params);
            rlview.requestLayout();
            rlview.addView(item.getVideoView());
        }
    }


}
