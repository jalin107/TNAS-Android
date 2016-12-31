package com.terramaster.player;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;
import com.facebook.cache.disk.DiskCacheConfig;
import com.facebook.common.internal.Supplier;
import com.facebook.common.util.ByteConstants;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeController;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.cache.MemoryCacheParams;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.terramaster.R;
import com.terramaster.utils.LogM;
import com.terramaster.utils.SDCardUtils;

import java.util.ArrayList;

import me.relex.photodraweeview.PhotoDraweeView;

/**
 * Created by Jaylin on 2016/12/22.
 */

public class ViewFlipperView extends FrameLayout{

    private Context context;                           // 调用方的上下文
    private int currentAdImgIndex;                     // 当前广告图片索引
    private Animation left2RightInAnimation;           // 广告图片从左到右进入屏幕动画
    private Animation left2RightOutAnimation;          // 广告图片从左到右出去屏幕动画
    private Animation right2LeftInAnimation;           // 广告图片从右到左进入屏幕动画
    private Animation right2LeftOutAnimation;          // 广告图片从右到左出去屏幕动画
    private int animationDuration = 500;               // 动画花费时间500毫秒
    private ViewFlipper mViewFlipper;                  // 滑动页面控件
    private RelativeLayout mHeader;                       // 上方返回控件
    private float startX = 0;                          // touch action down 时的x坐标
    private float endX = 0;                            // touch action up 时的x坐标
    private ArrayList<String> URLS;
    private ArrayList<String> NAMES;
    private String MYFILE;
    private MyImageView miv;
    private TextView PhotoHeader;
    private static int MAX_MEM = 30* ByteConstants.MB;
    private String TAB = "ViewFlipperView";

    public ViewFlipperView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        setView();
    }

    public ViewFlipperView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        setView();
    }

    public ViewFlipperView(Context context) {
        super(context);
        miv = (MyImageView) context;
        this.context = context;
        this.init(context, 50 * ByteConstants.MB);
        this.URLS = miv.getURLS();
        this.NAMES = miv.getNAMES();
        this.MYFILE = miv.getFile();
        setView();
    }
    /**
     * 初始化操作，建议在子线程中进行
     * @param context
     * @param cacheSizeInM  磁盘缓存的大小，以M为单位
     */
    public static void init(final Context context,int cacheSizeInM){
        final MemoryCacheParams bitmapCacheParams = new MemoryCacheParams(
                MAX_MEM,// 内存缓存中总图片的最大大小,以字节为单位。
                Integer.MAX_VALUE,// 内存缓存中图片的最大数量。
                MAX_MEM,// 内存缓存中准备清除但尚未被删除的总图片的最大大小,以字节为单位。
                Integer.MAX_VALUE,// 内存缓存中准备清除的总图片的最大数量。
                Integer.MAX_VALUE);// 内存缓存中单个图片的最大大小。
        Supplier<MemoryCacheParams> mSupplierMemoryCacheParams = new Supplier<MemoryCacheParams>() {
            @Override
            public MemoryCacheParams get() {
                return bitmapCacheParams;
            }
        };
        DiskCacheConfig diskCacheConfig = DiskCacheConfig.newBuilder(context)
                .setMaxCacheSize(cacheSizeInM)//最大缓存
                .setMaxCacheSizeOnLowDiskSpace(30 * ByteConstants.MB)
                .setBaseDirectoryPath(SDCardUtils.getCacheDir())
                .setBaseDirectoryName("fresco")//子目录
                .build();
        ImagePipelineConfig config = ImagePipelineConfig.newBuilder(context)
                .setBitmapMemoryCacheParamsSupplier(mSupplierMemoryCacheParams)
                .setMainDiskCacheConfig(diskCacheConfig)
                .setDownsampleEnabled(true)
                .setBitmapsConfig(Bitmap.Config.RGB_565)
                .build();
        Fresco.initialize(context, config);
    }
    /**
     * 显示View
     */
    private void setView(){
        // 初始化
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        mViewFlipper = new ViewFlipper(context);
        mHeader = new RelativeLayout(context);
        mHeader.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        // 初始化动画
        left2RightInAnimation = new TranslateAnimation(-screenWidth, 0, 0, 0);
        left2RightInAnimation.setDuration(animationDuration);
        left2RightOutAnimation = new TranslateAnimation(0, screenWidth, 0, 0);
        left2RightOutAnimation.setDuration(animationDuration);
        right2LeftInAnimation = new TranslateAnimation(screenWidth, 0, 0, 0);
        right2LeftInAnimation.setDuration(animationDuration);
        right2LeftOutAnimation = new TranslateAnimation(0, -screenWidth, 0, 0);
        right2LeftOutAnimation.setDuration(animationDuration);

        //将广告图片加入ViewFlipper
        for(int i=0; i<URLS.size(); i++){
            String url = URLS.get(i);
            LogM.e("Image i = "+url);
            if(NAMES.get(i).equals(MYFILE)){
                currentAdImgIndex = i;
            }
            // init SimpleDraweeView...
            SimpleDraweeView draweeView = new SimpleDraweeView(this.context);
            AnimationDrawable animationDrawable = new AnimationDrawable();
            Drawable drawable = context.getResources().getDrawable(R.drawable.loading_image);
            if(drawable != null){
                animationDrawable.addFrame(drawable, 200);
                animationDrawable.setOneShot(false);
            }
            GenericDraweeHierarchy hierarchy = draweeView.getHierarchy();
            hierarchy.setProgressBarImage(animationDrawable, ScalingUtils.ScaleType.CENTER);
            hierarchy.setActualImageScaleType(ScalingUtils.ScaleType.CENTER_INSIDE);
            hierarchy.setFailureImage(R.drawable.ic_error, ScalingUtils.ScaleType.CENTER);
            showPicture(Uri.parse(url.replaceAll("\\+","%20")), draweeView);
            mViewFlipper.addView(draweeView);
        }
        addView(mViewFlipper);
        mViewFlipper.setDisplayedChild(currentAdImgIndex);
        PhotoHeader = miv.getTextView(NAMES.get(currentAdImgIndex));
        mHeader.addView(PhotoHeader);
        addView(mHeader);
    }

    private void showPicture(Uri uri, SimpleDraweeView view){
        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(uri)
                .setAutoRotateEnabled(true)
                .setProgressiveRenderingEnabled(false)
                .setLocalThumbnailPreviewsEnabled(true)
                .build();
        PipelineDraweeController controller = (PipelineDraweeController) Fresco.newDraweeControllerBuilder()
                .setLowResImageRequest(request)
                .setImageRequest(request)
                .setAutoPlayAnimations(true) //自动播放gif动画
                .setOldController(view.getController())
                .build();
        view.setController(controller);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = event.getX();
                break;
            case MotionEvent.ACTION_UP:
                endX = event.getX();
                if(currentAdImgIndex > 0 && endX > startX){// 查看前一页的广告
                    mViewFlipper.setInAnimation(left2RightInAnimation);
                    mViewFlipper.setOutAnimation(left2RightOutAnimation);
                    mViewFlipper.showPrevious();
                    currentAdImgIndex--;
                    if(currentAdImgIndex < 0){
                        currentAdImgIndex = 0;
                    }
                }
                if(currentAdImgIndex < URLS.size()-1 && endX < startX){// 查看后一页的广告
                    mViewFlipper.setInAnimation(right2LeftInAnimation);
                    mViewFlipper.setOutAnimation(right2LeftOutAnimation);
                    mViewFlipper.showNext();
                    currentAdImgIndex++;
                    if(currentAdImgIndex > URLS.size()-1){
                        currentAdImgIndex = URLS.size()-1;
                    }
                }
                PhotoHeader.setText(NAMES.get(currentAdImgIndex));
                break;
        }
        return true;
    }

}