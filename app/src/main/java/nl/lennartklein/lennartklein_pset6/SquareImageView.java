package nl.lennartklein.lennartklein_pset6;

/*  */

import android.content.Context;
import android.util.AttributeSet;

/**
 * An ImageView that is always squared and still responsive in width.
 * Created by Xingrz. https://gist.github.com/xingrz/c95cdedf57f45f60dd28
 */
public class SquareImageView extends android.support.v7.widget.AppCompatImageView {

    public SquareImageView(Context context) {
        super(context);
    }

    public SquareImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = getMeasuredWidth();
        setMeasuredDimension(width, width);
    }

}