package com.speedata.uhf.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.speedata.libuhf.IUHFService;
import com.speedata.libuhf.UHFManager;
import com.speedata.libuhf.utils.SharedXmlUtil;
import com.speedata.uhf.MyApp;
import com.speedata.uhf.R;
import com.uhf.structures.DynamicQParams;
import com.uhf.structures.FixedQParams;

/**
 * R2000模块唯一的盘点设置选项，旗连模块没有本页面
 * <p>
 * 芯联模块已加入算法
 *
 * @author zzc
 */
public class InventorySettingDialog extends Dialog implements View.OnClickListener {

    /**
     * 动态算法
     */
    private RadioButton rbDynamicAlgorithm;
    /**
     * 固定算法
     */
    private RadioButton rbFixedAlgorithm;
    /**
     * 翻转
     */
    private Spinner spTarget;
    /**
     * 重试次数
     */
    private EditText etTry;
    /**
     * 起始Q值
     */
    private EditText etStartQ;
    /**
     * 最小Q值
     */
    private EditText etMinValue;
    /**
     * 最大Q值
     */
    private EditText etMaxValue;
    /**
     * 阀值
     */
    private EditText etThreshold;
    private LinearLayout dynamicAlgorithmLayout;
    /**
     * Q值
     */
    private EditText etQValue;
    /**
     * 重复
     */
    private Spinner spRepeat;
    private LinearLayout fixedAlgorithmLayout;
    private Context mContext;

    private LinearLayout r2kLayout, xinLianLayout, mLlLowPowerMode;
    private Spinner spQValue, spGen2Target;
    private IUHFService iuhfService;
    private Button btnFastMode, btnLowPowerMode;
    private EditText etKeepTime, etWorkTime, etSleepTime;

    public InventorySettingDialog(@NonNull Context context, IUHFService iuhfService) {
        super(context);
        this.mContext = context;
        this.iuhfService = iuhfService;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.inventory_setting);
        initView();
        initData();
    }

    private void initData() {
        String model = UHFManager.getUHFModel();
        if ("r2k".equals(model)) {
            r2kLayout.setVisibility(View.VISIBLE);
            xinLianLayout.setVisibility(View.GONE);
            if (MyApp.isFastMode) {
                btnLowPowerMode.setText(mContext.getString(R.string.btn_close_low_power));
                mLlLowPowerMode.setVisibility(View.VISIBLE);
            }else {
                btnLowPowerMode.setText(mContext.getString(R.string.btn_open_low_power));
                mLlLowPowerMode.setVisibility(View.GONE);
            }
        } else {
            r2kLayout.setVisibility(View.GONE);
            xinLianLayout.setVisibility(View.VISIBLE);
            if (MyApp.isFastMode) {
                btnFastMode.setText(mContext.getResources().getString(R.string.btn_stop_fast));
            } else {
                btnFastMode.setText(mContext.getResources().getString(R.string.btn_start_fast));
            }
        }
    }

    private void initView() {
        rbDynamicAlgorithm = findViewById(R.id.rb_dynamic_algorithm);
        rbDynamicAlgorithm.setOnClickListener(this);
        rbFixedAlgorithm = findViewById(R.id.rb_fixed_algorithm);
        rbFixedAlgorithm.setOnClickListener(this);
        spTarget = findViewById(R.id.sp_target);
        etTry = findViewById(R.id.et_try);
        dynamicAlgorithmLayout = findViewById(R.id.dynamic_algorithm_layout);
        etStartQ = findViewById(R.id.et_start_q);
        etMinValue = findViewById(R.id.et_min_value);
        etMaxValue = findViewById(R.id.et_max_value);
        etThreshold = findViewById(R.id.et_threshold);
        fixedAlgorithmLayout = findViewById(R.id.fixed_algorithm_layout);
        etQValue = findViewById(R.id.et_q_value);
        spRepeat = findViewById(R.id.sp_repeat);

        r2kLayout = findViewById(R.id.ll_r2k);
        xinLianLayout = findViewById(R.id.ll_xinlian);

        //设置算法
        findViewById(R.id.set_algorithm).setOnClickListener(this);
        //获取算法
        findViewById(R.id.get_algorithm).setOnClickListener(this);

        //芯联模块设置
        spQValue = findViewById(R.id.sp_qvalue);
        spGen2Target = findViewById(R.id.sp_gen2_target);
        findViewById(R.id.set_qvalue).setOnClickListener(this);
        findViewById(R.id.set_gen2_target).setOnClickListener(this);
        findViewById(R.id.btn_get_value).setOnClickListener(this);
        btnFastMode = findViewById(R.id.btn_fast_mode);
        btnFastMode.setOnClickListener(this);
        mLlLowPowerMode = findViewById(R.id.ll_low_power);
        btnLowPowerMode = findViewById(R.id.btn_low_power);
        btnLowPowerMode.setOnClickListener(this);
        etKeepTime = findViewById(R.id.keep_work_time);
        etWorkTime = findViewById(R.id.et_work_time);
        etSleepTime = findViewById(R.id.et_sleep_time);
        findViewById(R.id.set_keep_work_time).setOnClickListener(this);
        findViewById(R.id.set_time).setOnClickListener(this);
        findViewById(R.id.get_all_time).setOnClickListener(this);
    }

    /**
     * 设置固定算法
     */
    private void setFixedAlgorithm() {
        if (TextUtils.isEmpty(etTry.getText().toString())) {
            Toast.makeText(mContext, R.string.toast_try, Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(etQValue.getText().toString())) {
            Toast.makeText(mContext, R.string.toast_try, Toast.LENGTH_SHORT).show();
            return;
        }
        int tryCount = Integer.parseInt(etTry.getText().toString().trim());
        int q = Integer.parseInt(etQValue.getText().toString().trim());

        if (q < 0 || q > 15) {
            Toast.makeText(mContext, R.string.toast_invalid_q, Toast.LENGTH_SHORT).show();
            return;
        }
        if (tryCount < 0 || tryCount > 10) {
            Toast.makeText(mContext, R.string.toast_invalid_try, Toast.LENGTH_SHORT).show();
            return;
        }

        int fixedResult = iuhfService.setFixedAlgorithm(q, tryCount, (int) spTarget.getSelectedItemId(), (int) spRepeat.getSelectedItemId());
        if (fixedResult == 0) {
            Toast.makeText(mContext, R.string.toast_set_success, Toast.LENGTH_SHORT).show();
        } else if (fixedResult == -1000) {
            Toast.makeText(mContext, R.string.toast_inventory, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(mContext, R.string.toast_set_fail, Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * 设置动态算法
     */
    private void setDynamicAlgorithm() {
        if (TextUtils.isEmpty(etTry.getText().toString())) {
            Toast.makeText(mContext, R.string.toast_try, Toast.LENGTH_SHORT).show();
            return;

        } else if (TextUtils.isEmpty(etStartQ.getText().toString())) {
            Toast.makeText(mContext, R.string.toast_start_q, Toast.LENGTH_SHORT).show();
            return;
        } else if (TextUtils.isEmpty(etMinValue.getText().toString())) {
            Toast.makeText(mContext, R.string.toast_min_q, Toast.LENGTH_SHORT).show();
            return;
        } else if (TextUtils.isEmpty(etMaxValue.getText().toString())) {
            Toast.makeText(mContext, R.string.toast_max_q, Toast.LENGTH_SHORT).show();
            return;
        } else if (TextUtils.isEmpty(etThreshold.getText().toString())) {
            Toast.makeText(mContext, R.string.toast_threshold, Toast.LENGTH_SHORT).show();
            return;
        }
        int tryCount = Integer.parseInt(etTry.getText().toString().trim());
        int startQ = Integer.parseInt(etStartQ.getText().toString().trim());
        int minQ = Integer.parseInt(etMinValue.getText().toString().trim());
        int maxQ = Integer.parseInt(etMaxValue.getText().toString().trim());
        int threshold = Integer.parseInt(etThreshold.getText().toString().trim());
        if (startQ < 0 || startQ > 15 || minQ < 0 || minQ > 15 || maxQ < 0 || maxQ > 15) {
            Toast.makeText(mContext, R.string.toast_invalid_q, Toast.LENGTH_SHORT).show();
            return;
        }

        if (minQ > maxQ) {
            Toast.makeText(mContext, R.string.toast_q_range, Toast.LENGTH_SHORT).show();
            return;
        }
        if (threshold < 0 || threshold > 255) {
            Toast.makeText(mContext, R.string.toast_invalid_threshold, Toast.LENGTH_SHORT).show();
            return;
        }
        if (tryCount < 0 || tryCount > 10) {
            Toast.makeText(mContext, R.string.toast_invalid_try, Toast.LENGTH_SHORT).show();
            return;
        }

        int dynamicResult = iuhfService.setDynamicAlgorithm(startQ, minQ, maxQ, tryCount, (int) spTarget.getSelectedItemId(), threshold);

        if (dynamicResult == 0) {
            Toast.makeText(mContext, R.string.toast_set_success, Toast.LENGTH_SHORT).show();
        } else if (dynamicResult == -1000) {
            Toast.makeText(mContext, R.string.toast_inventory, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(mContext, R.string.toast_set_fail, Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onClick(View view) {
        int result;
        switch (view.getId()) {
            case R.id.set_algorithm:
                if (rbDynamicAlgorithm.isChecked()) {
                    //动态算法
                    setDynamicAlgorithm();
                } else if (rbFixedAlgorithm.isChecked()) {
                    //固定算法
                    setFixedAlgorithm();
                }
                break;
            case R.id.get_algorithm:
                getAlgorithm();
                break;
            case R.id.rb_dynamic_algorithm:
                dynamicAlgorithmLayout.setVisibility(View.VISIBLE);
                fixedAlgorithmLayout.setVisibility(View.GONE);
                break;
            case R.id.rb_fixed_algorithm:
                dynamicAlgorithmLayout.setVisibility(View.GONE);
                fixedAlgorithmLayout.setVisibility(View.VISIBLE);
                break;
            case R.id.set_qvalue:
                result = iuhfService.setGen2QValue((int) spQValue.getSelectedItemId());
                if (result == 0) {
                    Toast.makeText(mContext, R.string.toast_set_q_ok, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(mContext, R.string.toast_set_q_fail, Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.set_gen2_target:
                result = iuhfService.setGen2Target((int) spGen2Target.getSelectedItemId());
                if (result == 0) {
                    Toast.makeText(mContext, R.string.toast_set_target_ok, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(mContext, R.string.toast_set_target_fail, Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.btn_get_value:
                getGen2Value();
                break;
            case R.id.btn_fast_mode:
                setFastMode();
                break;
            case R.id.btn_low_power:
                setLowPowerMode();
                break;
            case R.id.set_keep_work_time:
                setDwellTime();
                break;
            case R.id.set_time:
                setTime();
                break;
            case R.id.get_all_time:
                getAllTime();
                break;
            default:
                break;
        }
    }

    @SuppressLint("SetTextI18n")
    private void getAllTime() {
        int result = MyApp.getInstance().getIuhfService().getDwellTime();
        if (result > 0) {
            etKeepTime.setText("" + result);
        }else {
            Toast.makeText(mContext, R.string.toast_get_fail, Toast.LENGTH_LONG).show();
            return;
        }
        int[] res = MyApp.getInstance().getIuhfService().getLowpowerScheduler();
        if (res != null) {
            etWorkTime.setText("" + res[0]);
            etSleepTime.setText("" + res[1]);
            Toast.makeText(mContext, R.string.toast_get_success, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(mContext, R.string.toast_get_fail, Toast.LENGTH_LONG).show();
        }
    }

    private void setTime() {
        String workTime = etWorkTime.getText().toString();
        String sleepTime = etSleepTime.getText().toString();
        if (workTime.isEmpty()) {
            workTime = "0";
            etWorkTime.setText("0");
        }
        if (sleepTime.isEmpty()) {
            sleepTime = "0";
            etSleepTime.setText("0");
        }
        int invOnTime = Integer.parseInt(workTime);
        int invOffTime = Integer.parseInt(sleepTime);
        int res = MyApp.getInstance().getIuhfService().setLowpowerScheduler(invOnTime, invOffTime);
        if (res == 0) {
            Toast.makeText(mContext, R.string.toast_set_success, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(mContext, R.string.toast_set_fail, Toast.LENGTH_LONG).show();
        }
    }

    private void setDwellTime() {
        String keepTime = etKeepTime.getText().toString();
        if (keepTime.isEmpty()) {
            keepTime = "0";
            etKeepTime.setText("0");
        }
        int dwellTime = Integer.parseInt(keepTime);
        int res = MyApp.getInstance().getIuhfService().setDwellTime(dwellTime);
        if (res == 0) {
            Toast.makeText(mContext, R.string.toast_set_success, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(mContext, R.string.toast_set_fail, Toast.LENGTH_LONG).show();
        }
    }

    private void setLowPowerMode() {
        if (!MyApp.isFastMode) {
            MyApp.getInstance().getIuhfService().setInvMode(2);
            btnLowPowerMode.setText(mContext.getString(R.string.btn_close_low_power));
            mLlLowPowerMode.setVisibility(View.VISIBLE);
            MyApp.isFastMode = true;
        } else {
            MyApp.getInstance().getIuhfService().setInvMode(1);
            btnLowPowerMode.setText(mContext.getString(R.string.btn_open_low_power));
            mLlLowPowerMode.setVisibility(View.GONE);
            MyApp.isFastMode = false;
        }
    }

    private void setFastMode() {
        if (!MyApp.isFastMode) {
            //没有启用,启用，改变标志位
            int result = MyApp.getInstance().getIuhfService().startFastMode();
            if (result == 0) {
                MyApp.isFastMode = true;
                btnFastMode.setText(mContext.getResources().getString(R.string.btn_stop_fast));
                Toast.makeText(mContext, mContext.getResources().getString(R.string.toast_start_fast_success), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(mContext, mContext.getResources().getString(R.string.toast_start_fast_failed), Toast.LENGTH_LONG).show();
            }

        } else {
            //已经启用了，需要停止，改变标志位
            int result = MyApp.getInstance().getIuhfService().stopFastMode();
            if (result == 0) {
                MyApp.isFastMode = false;
                btnFastMode.setText(mContext.getResources().getString(R.string.btn_start_fast));
                Toast.makeText(mContext, mContext.getResources().getString(R.string.toast_stop_fast_success), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(mContext, mContext.getResources().getString(R.string.toast_stop_fast_failed), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void getAlgorithm() {
        if (rbDynamicAlgorithm.isChecked()) {
            DynamicQParams dynamicQParams = new DynamicQParams();
            int dynamicState = iuhfService.getDynamicAlgorithm(dynamicQParams);
            if (dynamicState == 0) {
                spTarget.setSelection(dynamicQParams.toggleTarget);
                etTry.setText(String.valueOf(dynamicQParams.retryCount));
                etStartQ.setText(String.valueOf(dynamicQParams.startQValue));
                etMinValue.setText(String.valueOf(dynamicQParams.minQValue));
                etMaxValue.setText(String.valueOf(dynamicQParams.maxQValue));
                etThreshold.setText(String.valueOf(dynamicQParams.thresholdMultiplier));
            }
        } else if (rbFixedAlgorithm.isChecked()) {
            FixedQParams fixedQParams = new FixedQParams();
            int fixedState = iuhfService.getFixedAlgorithm(fixedQParams);
            if (fixedState == 0) {
                spTarget.setSelection(fixedQParams.toggleTarget);
                etTry.setText(String.valueOf(fixedQParams.retryCount));
                spRepeat.setSelection(fixedQParams.repeatUntiNoTags);
                etQValue.setText(String.valueOf(fixedQParams.qValue));
            }
        }
    }

    private void getGen2Value() {
        int[] res = iuhfService.getGen2AllValue();
        if (res != null) {
            if (res[0] >= 0) {
                spQValue.setSelection(res[0]);
            } else {
                Toast.makeText(mContext, R.string.toast_get_q_fail, Toast.LENGTH_LONG).show();
            }
            if (res[1] >= 0) {
                spGen2Target.setSelection(res[1]);
            } else {
                Toast.makeText(mContext, R.string.toast_get_target_fail, Toast.LENGTH_LONG).show();
            }
            if (res[0] >= 0 && res[1] >= 0) {
                Toast.makeText(mContext, R.string.toast_get_success, Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(mContext, R.string.toast_get_fail, Toast.LENGTH_LONG).show();
        }
    }


}
