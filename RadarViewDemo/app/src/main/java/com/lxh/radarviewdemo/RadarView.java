package com.lxh.radarviewdemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

/**
 * 作者：luoxiaohui
 * 日期:2017/5/13 15:12
 * 文件描述: 自定义的雷达图(蜘蛛网图)
 */
public class RadarView extends View {
    private static final String TAG = "RadarView";
    private int count = 4;                //数据个数
    private float angle = (float) (Math.PI * 2 / count);
    private float radius;
    private float maxRadius;            //网格最大半径
    private int centerX;                  //中心X
    private int centerY;                  //中心Y
    private String[] titles = {"刘备", "关羽", "张飞", "赵云"};
    private double[] data = {40, 60, 80, 90}; //各维度分值
    private float maxValue = 100;             //数据最大值
    private Paint mainPaint;                //雷达区画笔
    private Paint valuePaint;               //数据区画笔
    private Paint textPaint;                //文本画笔
    private Paint pointPaint;               //圆点画笔

    public RadarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public RadarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RadarView(Context context) {
        super(context);
        init();
    }


    //初始化
    private void init() {

        mainPaint = new Paint();
        mainPaint.setAntiAlias(true);
        mainPaint.setColor(Color.parseColor("#bdbdbd"));
        mainPaint.setStyle(Paint.Style.STROKE);

        pointPaint = new Paint();
        pointPaint.setAntiAlias(true);
        pointPaint.setColor(Color.parseColor("#f38683"));
        pointPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        valuePaint = new Paint();
        valuePaint.setAntiAlias(true);
        valuePaint.setColor(Color.parseColor("#fdeded"));
        valuePaint.setStyle(Paint.Style.FILL_AND_STROKE);

        textPaint = new Paint();
        textPaint.setTextSize(30);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setColor(Color.parseColor("#757575"));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        radius = Math.min(h, w) / 2 * 0.5f;
        maxRadius = radius / (count - 1) * count;
        centerX = w / 2;
        centerY = h / 2;
        postInvalidate();
        super.onSizeChanged(w, h, oldw, oldh);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        drawPolygon(canvas);
        drawLines(canvas);
        drawText(canvas);
        drawRegion(canvas);
    }

    /*
     * 画中心原点
     */
    private void drawPoint(Canvas canvas, float x, float y) {

        //绘制小圆点
        canvas.drawCircle(x, y, 6, pointPaint);
    }

    /**
     * 绘制正多边形
     */
    private void drawPolygon(Canvas canvas) {
        Path path = new Path();
        float r = radius / (count - 1);
        for (int i = 1; i <= count; i++) {
            float curR = r * i;
            path.reset();
            for (int j = 1; j <= count; j++) {

                float x = (float) (centerX + curR * Math.sin(angle * j));
                float y = (float) (centerY - curR * Math.cos(angle * j));
                if (j == 1) {

                    path.moveTo(x, y);
                } else {

                    path.lineTo(x, y);
                }

                if (i == count) {
                    drawPoint(canvas, x, y);
                }
            }
            path.close();
            canvas.drawPath(path, mainPaint);
        }
    }

    /**
     * 绘制直线
     */
    private void drawLines(Canvas canvas) {
        Path path = new Path();
        for (int i = 1; i <= count; i++) {
            path.reset();
            path.moveTo(centerX, centerY);
            float x = (float) (centerX + maxRadius * Math.sin(angle * i));
            float y = (float) (centerY - maxRadius * Math.cos(angle * i));
            path.lineTo(x, y);
            canvas.drawPath(path, mainPaint);
        }
    }

    /**
     * 绘制文字
     * 其实四个象限，是以(centerX, centerY)为圆点的象限
     *
     * @param canvas
     */
    private void drawText(Canvas canvas) {
        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        float fontHeight = fontMetrics.descent - fontMetrics.ascent;
        for (int i = 1; i <= count; i++) {
            float x = (float) (centerX + (maxRadius + fontHeight / 2) * Math.sin(angle * i));
            float y = (float) (centerY - (maxRadius + fontHeight / 2) * Math.cos(angle * i));

            float relateX = x - centerX;
            float relateY = y - centerY;

            float dis = textPaint.measureText(titles[i - 1]);//文本长度
            if (relateX >= 0 && relateY >= 0) {//第4象限

                canvas.drawText(titles[i - 1], x , y, textPaint);
            } else if (relateX < 0 && relateY >= 0) {//第3象限


                canvas.drawText(titles[i - 1], x - dis, y, textPaint);
            } else if (relateX < 0 && relateY < 0) {//第2象限

                canvas.drawText(titles[i - 1], x - dis, y, textPaint);
            } else if (relateX >= 0 && relateY < 0) {//第1象限

                canvas.drawText(titles[i - 1], x - dis / 2, y, textPaint);
            }
        }
    }

    /**
     * 绘制中间有效区域
     *
     * @param canvas
     */
    private void drawRegion(Canvas canvas) {
        Path path = new Path();
        valuePaint.setAlpha(255);
        for (int i = 1; i <= count; i++) {
            double percent = data[i - 1] / maxValue;
            float x = (float) (centerX + maxRadius * Math.sin(angle * i) * percent);
            float y = (float) (centerY - maxRadius * Math.cos(angle * i) * percent);

            if (i == 1) {
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
            }
        }
        valuePaint.setStyle(Paint.Style.STROKE);
        canvas.drawPath(path, valuePaint);
        valuePaint.setAlpha(127);
        //绘制填充区域
        valuePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        canvas.drawPath(path, valuePaint);
    }

    //设置标题
    public void setTitles(String[] titles) {
        this.titles = titles;
    }

    //设置数值
    public void setData(double[] data) {
        this.data = data;
    }


    public float getMaxValue() {
        return maxValue;
    }

    //设置最大数值
    public void setMaxValue(float maxValue) {
        this.maxValue = maxValue;
    }

    //设置蜘蛛网颜色
    public void setMainPaintColor(int color) {
        mainPaint.setColor(color);
    }

    //设置标题颜色
    public void setTextPaintColor(int color) {
        textPaint.setColor(color);
    }

    //设置覆盖局域颜色
    public void setValuePaintColor(int color) {
        valuePaint.setColor(color);
    }
}
