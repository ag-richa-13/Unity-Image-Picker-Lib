package com.myteam11.imagepicker_lib;

import android.content.Context;
import android.graphics.*;
import android.view.MotionEvent;
import android.view.View;

public class CropOverlayView extends View {
    private Paint border, dim, handlePaint;
    private RectF rect;
    private Bitmap bmp;

    private static final int HANDLE_TOUCH_AREA = 60;
    private static final int HANDLE_RADIUS = 20;
    private enum Mode { NONE, MOVE, LT, RT, LB, RB }
    private Mode mode = Mode.NONE;
    private float lastX, lastY;

    public CropOverlayView(Context ctx) {
        super(ctx);
        setLayerType(LAYER_TYPE_HARDWARE, null);
        border = new Paint(); border.setColor(Color.WHITE); border.setStyle(Paint.Style.STROKE); border.setStrokeWidth(5);
        dim = new Paint(); dim.setColor(Color.parseColor("#A6000000"));
        handlePaint = new Paint(); handlePaint.setColor(Color.WHITE);
        rect = new RectF(200, 400, 800, 1000);
    }

    public void setBitmap(Bitmap bitmap) {
        this.bmp = bitmap;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas c) {
        super.onDraw(c);
        c.drawRect(0, 0, getWidth(), getHeight(), dim);
        Paint clear = new Paint(); clear.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        c.drawRect(rect, clear);
        clear.setXfermode(null);
        c.drawRect(rect, border);
        c.drawCircle(rect.left, rect.top, HANDLE_RADIUS, handlePaint);
        c.drawCircle(rect.right, rect.top, HANDLE_RADIUS, handlePaint);
        c.drawCircle(rect.left, rect.bottom, HANDLE_RADIUS, handlePaint);
        c.drawCircle(rect.right, rect.bottom, HANDLE_RADIUS, handlePaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        float x = e.getX(), y = e.getY();
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mode = detectMode(x, y);
                lastX = x; lastY = y;
                return true;
            case MotionEvent.ACTION_MOVE:
                float dx = x - lastX, dy = y - lastY;
                switch(mode) {
                    case MOVE: rect.offset(dx, dy); break;
                    case LT: rect.left += dx; rect.top += dy; break;
                    case RT: rect.right += dx; rect.top += dy; break;
                    case LB: rect.left += dx; rect.bottom += dy; break;
                    case RB: rect.right += dx; rect.bottom += dy; break;
                }
                constrain();
                lastX = x; lastY = y;
                invalidate();
                return true;
            case MotionEvent.ACTION_UP:
                mode = Mode.NONE;
                return true;
        }
        return false;
    }

    private Mode detectMode(float x, float y) {
        if (near(x, y, rect.left, rect.top)) return Mode.LT;
        if (near(x, y, rect.right, rect.top)) return Mode.RT;
        if (near(x, y, rect.left, rect.bottom)) return Mode.LB;
        if (near(x, y, rect.right, rect.bottom)) return Mode.RB;
        if (rect.contains(x, y)) return Mode.MOVE;
        return Mode.NONE;
    }

    private boolean near(float x, float y, float cx, float cy) {
        return Math.hypot(x - cx, y - cy) <= HANDLE_TOUCH_AREA;
    }

    private void constrain() {
        float min = 150;
        if (rect.width() < min) rect.right = rect.left + min;
        if (rect.height() < min) rect.bottom = rect.top + min;
        if (rect.left < 0) rect.offset(-rect.left, 0);
        if (rect.top < 0) rect.offset(0, -rect.top);
        if (rect.right > getWidth()) rect.offset(getWidth() - rect.right, 0);
        if (rect.bottom > getHeight()) rect.offset(0, getHeight() - rect.bottom);
    }

    public Bitmap getCroppedBitmap() {
        if (bmp == null) return null;
        float vw = getWidth(), vh = getHeight();
        float iw = bmp.getWidth(), ih = bmp.getHeight();
        float scale = Math.min(vw / iw, vh / ih);
        float dw = iw * scale, dh = ih * scale;
        float ox = (vw - dw) / 2f, oy = (vh - dh) / 2f;

        float lx = (rect.left - ox) / scale;
        float ty = (rect.top - oy) / scale;
        float rw = rect.width() / scale;
        float rh = rect.height() / scale;

        int x = Math.max(0, Math.round(lx));
        int y = Math.max(0, Math.round(ty));
        int w = Math.min(Math.round(rw), bmp.getWidth() - x);
        int h = Math.min(Math.round(rh), bmp.getHeight() - y);

        if (w <= 0 || h <= 0) return null;
        return Bitmap.createBitmap(bmp, x, y, w, h);
    }
}
