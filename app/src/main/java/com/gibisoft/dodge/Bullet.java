package com.gibisoft.dodge;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.util.TypedValue;

public class Bullet extends android.support.v7.widget.AppCompatImageView {
    final int params_width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics());
    final int params_height = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics());
    float xSpeed, ySpeed;

    public Bullet(Context context, int x, int y, int xSpeed, int ySpeed) {
        super(context);

        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(params_width, params_height);

        setImageResource(R.drawable.bullet);
        setLayoutParams(layoutParams);

        setX(x);setY(y);
        this.xSpeed = xSpeed / (float)10.0;
        this.ySpeed = ySpeed / (float)10.0;
    }

    public void move() {
        setX(getX() + xSpeed);
        setY(getY() + ySpeed);
    }

    public void reflection(float bottomY, float rightX) {
        if(getY() <= 0) {
            setY(0);
            ySpeed = -ySpeed;
        }
        else if(getY() >= bottomY) {
            setY(bottomY);
            ySpeed = -ySpeed;
        }
        if(getX() <= 0) {
            setX(0);
            xSpeed = -xSpeed;
        }
        else if(getX() >= rightX) {
            setX(rightX);
            xSpeed = -xSpeed;
        }
        this.move();
    }
}