/**
 * Copyright (C) © 2014 深圳市掌玩网络技术有限公司
 * KoTvGameBox
 * PromptDialog.java
 **/
package cn.vszone.ko.plugin.sdk;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * @author Binbin.Jin
 * @firstCreate 2014年7月24日
 * @lastModify
 */
public class KoPromptDialog extends AlertDialog {

    // ===========================================================
    // Constants
    // +==========================================================

    // ===========================================================
    // Fields
    // +==========================================================

    private View                 mRootView;
    private Button               mRightBtn;
    private Button               mLeftBtn;
    private TextView             mMessageTv;
    private View.OnClickListener mLeftBtnOnClickListener;
    private View.OnClickListener mRightBtnOnClickListener;

    // ===========================================================
    // Constructors
    // +==========================================================

    public KoPromptDialog(Context context) {
        super(context, R.style.KoPromptDialog);
        mRootView = LayoutInflater.from(context).inflate(R.layout.ko_prompt_dialog, null, true);
        mRightBtn = (Button) mRootView.findViewById(R.id.ko_prompt_dialog_btn_right);
        mLeftBtn = (Button) mRootView.findViewById(R.id.ko_prompt_dialog_btn_left);
        mMessageTv = (TextView) mRootView.findViewById(R.id.ko_prompt_dialog_tv_message);
    }

    // ===========================================================
    // Getter & Setter
    // +==========================================================

    public void setMessage(String pMessage) {
        mMessageTv.setText(pMessage);
    }

    public void addLeftButton(String pCancelMessage, View.OnClickListener pL) {
        mLeftBtn.setVisibility(View.VISIBLE);
        mLeftBtn.setText(pCancelMessage);
        mLeftBtnOnClickListener = pL;
        mLeftBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dismiss();
                cancel();
                if (mLeftBtnOnClickListener != null) {
                    mLeftBtnOnClickListener.onClick(v);
                }
            }
        });
    }

    public void addRightButton(String pConfirmMessage, View.OnClickListener pL) {
        mRightBtn.setVisibility(View.VISIBLE);
        mRightBtn.setText(pConfirmMessage);
        mRightBtnOnClickListener = pL;
        mRightBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dismiss();
                cancel();
                if (mRightBtnOnClickListener != null) {
                    mRightBtnOnClickListener.onClick(v);
                }
            }
        });
    }

    // ===========================================================
    // Methods for/from SuperClass/Interfaces
    // +==========================================================

    // ===========================================================
    // Methods
    // +==========================================================
    
    public void initView() {
        setContentView(mRootView);
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // +==========================================================
}
