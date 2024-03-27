package Classes;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerViewItemDecoration extends RecyclerView.ItemDecoration {
    private Drawable divider;
    private int orientation;

    public RecyclerViewItemDecoration(Context context, @DrawableRes int resId) {
        divider = ContextCompat.getDrawable(context, resId);
    }

    @Override
    public void onDraw(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
        if (parent.getLayoutManager() instanceof LinearLayoutManager) {
            LinearLayoutManager layoutManager = (LinearLayoutManager) parent.getLayoutManager();
            orientation = layoutManager.getOrientation();

            if (orientation == LinearLayoutManager.VERTICAL) {
                drawVertical(canvas, parent);
            }
        }
    }

    private void drawVertical(Canvas canvas, RecyclerView parent) {
        int paddingLeft = 64;
        int paddingRight = 64;
        int paddingBottom = 16;

        int childCount = parent.getChildCount();

        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            int top = child.getBottom() + params.bottomMargin;

            if (i < childCount - 1 || hasSpaceForLastItem(parent)) {
                int left = parent.getPaddingLeft() + paddingLeft;
                int right = parent.getWidth() - parent.getPaddingRight() - paddingRight;
                int bottom = top + divider.getIntrinsicHeight() + paddingBottom;
                divider.setBounds(left, top, right, bottom);
                divider.draw(canvas);
            }
        }
    }

    private boolean hasSpaceForLastItem(RecyclerView parent) {
        return parent.getHeight() - parent.getPaddingBottom() - parent.getPaddingTop() > parent.getChildAt(parent.getChildCount() - 1).getBottom();
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        if (orientation == LinearLayoutManager.VERTICAL) {
            outRect.set(0, 0, 0, divider.getIntrinsicHeight());
        }
    }
}
