package com.myteam11.imagepicker_lib;

import android.content.Context;
import android.graphics.*;
import android.view.MotionEvent;
import android.view.View;

public class CropOverlayView extends View {
    private Paint border, dim, handlePaint;
    private RectF rect;
    private Bitmap bmp;

    private static final int HANDLE_TOUCH_AREA = 40;
    private static final int HANDLE_RADIUS = 15;

    private enum DragMode { NONE, MOVE, RESIZE_LT, RESIZE_RT, RESIZE_LB, RESIZE_RB }
    private DragMode mode = DragMode.NONE;
    private float lastX, lastY;

    public CropOverlayView(Context ctx) {
        super(ctx);
        setLayerType(LAYER_TYPE_HARDWARE, null);
        border = new Paint(); border.setColor(Color.WHITE);
        border.setStyle(Paint.Style.STROKE); border.setStrokeWidth(4);
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

        // Draw handles
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
                switch (mode) {
                    case MOVE: rect.offset(dx, dy); break;
                    case RESIZE_LT: rect.left += dx; rect.top += dy; break;
                    case RESIZE_RT: rect.right += dx; rect.top += dy; break;
                    case RESIZE_LB: rect.left += dx; rect.bottom += dy; break;
                    case RESIZE_RB: rect.right += dx; rect.bottom += dy; break;
                    default: break;
                }
                constrainRect();
                lastX = x; lastY = y;
                invalidate();
                return true;
            case MotionEvent.ACTION_UP:
                mode = DragMode.NONE;
                return true;
        }
        return false;
    }

    private DragMode detectMode(float x, float y) {
        if (near(x, y, rect.left, rect.top)) return DragMode.RESIZE_LT;
        if (near(x, y, rect.right, rect.top)) return DragMode.RESIZE_RT;
        if (near(x, y, rect.left, rect.bottom)) return DragMode.RESIZE_LB;
        if (near(x, y, rect.right, rect.bottom)) return DragMode.RESIZE_RB;
        if (rect.contains(x, y)) return DragMode.MOVE;
        return DragMode.NONE;
    }

    private boolean near(float x, float y, float cx, float cy) {
        return Math.hypot(x - cx, y - cy) <= HANDLE_TOUCH_AREA;
    }

    private void constrainRect() {
        float minSize = 100;
        if (rect.width() < minSize) rect.right = rect.left + minSize;
        if (rect.height() < minSize) rect.bottom = rect.top + minSize;
        if (rect.left < 0) { rect.right -= rect.left; rect.left = 0; }
        if (rect.top < 0) { rect.bottom -= rect.top; rect.top = 0; }
        if (rect.right > getWidth()) rect.right = getWidth();
        if (rect.bottom > getHeight()) rect.bottom = getHeight();
    }

    public Bitmap getCroppedBitmap() {
        if (bmp == null) return null;
        float sx = (float) bmp.getWidth() / getWidth();
        float sy = (float) bmp.getHeight() / getHeight();
        int x = Math.max(0, (int)(rect.left * sx));
        int y = Math.max(0, (int)(rect.top * sy));
        int w = Math.min((int)(rect.width() * sx), bmp.getWidth() - x);
        int h = Math.min((int)(rect.height() * sy), bmp.getHeight() - y);
        return Bitmap.createBitmap(bmp, x, y, w, h);
    }
}
