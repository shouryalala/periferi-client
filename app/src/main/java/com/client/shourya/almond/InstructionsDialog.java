package com.client.shourya.almond;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Layout;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.LeadingMarginSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/*
 * Created by shourya on 17/7/17.
 */

public class InstructionsDialog extends DialogFragment {
    public static final int CREATE_PERIFERI_INSTRUCTIONS = 69;
    public static final int PERIFERI_REQUESTS_INSTRUCTIONS = 96;

    public InstructionsDialog() {

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int choice = getArguments().getInt("ins_reqd");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View li = getActivity().getLayoutInflater().inflate(R.layout.instructions_dialog, null);
        TextView txt = (TextView)li.findViewById(R.id.instructions_text);
        TextView head = (TextView)li.findViewById(R.id.instructions_header);
        if(choice == CREATE_PERIFERI_INSTRUCTIONS) {
            final String[] bulletpoint = {"Press and hold the map to start creating a Periferi.",
                    "Press and hold any corner of the selection and drag to update the area."};
            head.setText("Create");
            /*SpannableString span1 = new SpannableString(text1);
            span1.setSpan(new BulletSpan(16),0,text1.length(), 0);
            SpannableString span2 = new SpannableString(text2);
            span2.setSpan(new BulletSpan(16),0,text2.length(), 0);
            SpannableString span3 = new SpannableString(text3);
            span3.setSpan(new BulletSpan(16),0,text3.length(), 0);
            txt.setText(TextUtils.concat(span1, span2, span3));
            */
            CharSequence allText = "";
            for (String aBulletpoint : bulletpoint) {
                String text = aBulletpoint.trim();
                SpannableString spannableString = new SpannableString(text + "\n");
                spannableString.setSpan(new LeadingMarginSpan() {
                    @Override
                    public int getLeadingMargin(boolean first) {
                        return 50;
                    }
                    @Override
                    public void drawLeadingMargin(Canvas c, Paint p, int x, int dir, int top, int baseline, int bottom, CharSequence text, int start, int end, boolean first, Layout layout) {
                        if (first) {
                            Paint.Style orgStyle = p.getStyle();
                            p.setStyle(Paint.Style.FILL);
                            c.drawText("\u25CF ", 0, bottom - p.descent(), p);
                            p.setStyle(orgStyle);
                        }
                    }
                }, 0, text.length(), 0);
                allText = TextUtils.concat(allText, spannableString);
            }
            txt.setText(allText);
        }
        else if(choice == PERIFERI_REQUESTS_INSTRUCTIONS) {
            head.setText("Request");
            final String[] bulletpoint = {"Swipe lower panel to check out all requests.", "A Periferi is created on 10 approvals."};
            CharSequence allText = "";
            for (String aBulletpoint : bulletpoint) {
                String text = aBulletpoint.trim();
                SpannableString spannableString = new SpannableString(text + "\n");
                spannableString.setSpan(new LeadingMarginSpan() {
                    @Override
                    public int getLeadingMargin(boolean first) {
                        return 50;
                    }
                    @Override
                    public void drawLeadingMargin(Canvas c, Paint p, int x, int dir, int top, int baseline, int bottom, CharSequence text, int start, int end, boolean first, Layout layout) {
                        if (first) {
                            Paint.Style orgStyle = p.getStyle();
                            p.setStyle(Paint.Style.FILL);
                            c.drawText("\u25CF ", 0, bottom - p.descent(), p);
                            p.setStyle(orgStyle);
                        }
                    }
                }, 0, text.length(), 0);
                allText = TextUtils.concat(allText, spannableString);
            }
            txt.setText(allText);
        }
        builder.setView(li);
        return builder.create();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
