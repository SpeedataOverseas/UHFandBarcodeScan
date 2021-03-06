package com.speedata.uhf;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.speedata.libuhf.IUHFService;
import com.speedata.libuhf.UHFManager;
import com.speedata.libuhf.utils.CommonUtils;
import com.speedata.libuhf.utils.SharedXmlUtil;
import com.speedata.uhf.dialog.InventorySettingDialog;
import com.speedata.uhf.floatball.FloatBallManager;
import com.speedata.uhf.floatball.FloatListManager;
import com.speedata.uhf.libutils.ToastUtil;
import com.yhao.floatwindow.FloatWindow;

import java.text.DecimalFormat;


/**
 * 设置
 * Created by 张智超 on 2019/3/7
 *
 * @author 张智超
 */
public class InvSetActivity extends BaseActivity implements View.OnClickListener {

    private ImageView mIvQuitSet;
    private TextView tvSetFreq;
    private TextView tvSetS2;
    private TextView tvSetInvCon;
    private EditText etPower;
    private EditText etFreqPoint;
    Intent intent;
    Bundle bundle;
    private IUHFService iuhfService;
    private ToggleButton checkBoxService, openFloatWindow;
    private String model;
    private int freqRegion, s2Region, invConRegion;
    private TableLayout tableLayoutInvCon;
    private Button algorithmSetBtn, setBackBtn;
    private Boolean isExistServer;
    private Button setFreqBtn, setSessionBtn, setPowerBtn, setInvConBtn;
    private TextView tvPrefix, tvSuffix;
    private EditText etLoopTime;
    private CheckBox checkBoxLoop, checkBoxLongDown;
    private TableLayout tableLayout5, tableLayout4;
    private TableRow trReadTime, trSleep, trSession;
    private EditText etReadTime, etSleep;
    private Button setReadTimeBtn, setSleepBtn;
    private TextView mVersionTv;
    private final String yiXin = "yixin";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set);
        Log.e("zzc:", "onCreate()");
        initView();
        initData();
    }

    @SuppressLint("SetTextI18n")
    private void initData() {
        //获取模块型号
        model = UHFManager.getUHFModel();
        mVersionTv.append("-" + model);
        //判断服务是否存在
        isExistServer = SharedXmlUtil.getInstance(this).read("server", false);
        if (!isExistServer) {
            checkBoxService.setEnabled(false);
            tvPrefix.setEnabled(false);
            tvSuffix.setEnabled(false);
            checkBoxLoop.setEnabled(false);
            etLoopTime.setEnabled(false);
            checkBoxLongDown.setEnabled(false);
            tableLayout5.setVisibility(View.GONE);
            tableLayout4.setVisibility(View.GONE);
        } else {
            checkBoxLoop.setChecked(MyApp.isLoop);
            etLoopTime.setEnabled(MyApp.isLoop);
            checkBoxLongDown.setChecked(MyApp.isLongDown);
        }
        iuhfService = MyApp.getInstance().getIuhfService();
        if (iuhfService == null) {
            return;
        }
        if ("xinlian".equals(model)) {
            trReadTime.setVisibility(View.VISIBLE);
            trSleep.setVisibility(View.VISIBLE);
            etReadTime.setText("" + iuhfService.getReadTime());
            etSleep.setText("" + iuhfService.getSleep());
        } else if (yiXin.equals(model)) {
            trSession.setVisibility(View.GONE);
            trReadTime.setVisibility(View.GONE);
            trSleep.setVisibility(View.GONE);
        } else {
            trReadTime.setVisibility(View.GONE);
            trSleep.setVisibility(View.GONE);
        }
        if (MyApp.isFastMode && "xinlian".equals(model)) {
            setFreqBtn.setEnabled(false);
            setSessionBtn.setEnabled(false);
            setInvConBtn.setEnabled(false);
            tvSetFreq.setEnabled(false);
            tvSetS2.setEnabled(false);
            tvSetInvCon.setEnabled(false);
        } else {
            setFreqBtn.setEnabled(true);
            setSessionBtn.setEnabled(true);
            setInvConBtn.setEnabled(true);
            tvSetFreq.setEnabled(true);
            tvSetS2.setEnabled(true);
            tvSetInvCon.setEnabled(true);
            //获取定频
            getFreq();
            if (!UHFManager.getUHFModel().equals(yiXin)) {
                //获取通话项
                getSession();
            }
            //获取天线功率
            int ivp = iuhfService.getAntennaPower();
            Log.d("zzc:", "==天线功率==" + ivp);
            if (ivp > 0) {
                if (ivp > 30) {
                    etPower.setText("" + (ivp - 3));
                } else {
                    etPower.setText("" + ivp);
                }
                Log.d("zzc:", "==天线功率==获取成功==");
            }
        }
        //获取盘点模式
        if (!yiXin.equals(model)) {
            tableLayoutInvCon.setVisibility(View.VISIBLE);
            SystemClock.sleep(500);
            invConRegion = iuhfService.getInvMode(0);
            switch (invConRegion) {
                case 1:
                    tvSetInvCon.setText("EPC + TID");
                    break;
                case 2:
                    tvSetInvCon.setText("EPC + USER");
                    break;
                default:
                    tvSetInvCon.setText("Only EPC");
                    break;
            }
        } else {
            tableLayoutInvCon.setVisibility(View.GONE);
        }
        switch (MyApp.mPrefix) {
            case 0:
                tvPrefix.setText(getResources().getString(R.string.pix_newline));
                break;
            case 1:
                tvPrefix.setText(getResources().getString(R.string.pix_space));
                break;
            case 2:
                tvPrefix.setText(getResources().getString(R.string.pix_crlf));
                break;
            default:
                tvPrefix.setText(getResources().getString(R.string.pix_none));
                break;
        }
        switch (MyApp.mSuffix) {
            case 0:
                tvSuffix.setText(getResources().getString(R.string.pix_newline));
                break;
            case 1:
                tvSuffix.setText(getResources().getString(R.string.pix_space));
                break;
            case 2:
                tvSuffix.setText(getResources().getString(R.string.pix_crlf));
                break;
            default:
                tvSuffix.setText(getResources().getString(R.string.pix_none));
                break;
        }
        etLoopTime.setText(MyApp.mLoopTime);
    }

    public void initView() {
        mIvQuitSet = (ImageView) findViewById(R.id.set_title_iv);
        tvSetFreq = (TextView) findViewById(R.id.set_freq_tv);
        tvSetS2 = (TextView) findViewById(R.id.set_s2_tv);
        trSession = findViewById(R.id.tr_session);
        tvSetInvCon = (TextView) findViewById(R.id.set_onlyepc_tv);
        mIvQuitSet.setOnClickListener(this);
        tvSetFreq.setOnClickListener(this);
        tvSetS2.setOnClickListener(this);
        tvSetInvCon.setOnClickListener(this);
        etPower = (EditText) findViewById(R.id.et_power);
        etFreqPoint = (EditText) findViewById(R.id.et_freq_point);
        setBackBtn = (Button) findViewById(R.id.btn_set_back);
        setBackBtn.setOnClickListener(this);
        checkBoxService = findViewById(R.id.check_service);
        setFreqBtn = findViewById(R.id.btn_set_freq);
        setFreqBtn.setOnClickListener(this);
        setSessionBtn = findViewById(R.id.btn_set_session);
        setSessionBtn.setOnClickListener(this);
        setPowerBtn = findViewById(R.id.btn_set_power);
        setPowerBtn.setOnClickListener(this);
        setInvConBtn = findViewById(R.id.btn_set_invent);
        setInvConBtn.setOnClickListener(this);
        tableLayoutInvCon = findViewById(R.id.set_tab2);
        algorithmSetBtn = findViewById(R.id.btn_algorithm_set);
        algorithmSetBtn.setOnClickListener(this);
        tvPrefix = findViewById(R.id.set_server_prefix);
        tvPrefix.setOnClickListener(this);
        tvSuffix = findViewById(R.id.set_server_suffix);
        tvSuffix.setOnClickListener(this);
        etLoopTime = findViewById(R.id.et_loop_time);
        checkBoxLoop = findViewById(R.id.check_service_loop);
        checkBoxLoop.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                etLoopTime.setEnabled(isChecked);
            }
        });
        checkBoxLongDown = findViewById(R.id.check_long_down);
        tableLayout5 = findViewById(R.id.set_tab5);
        tableLayout4 = findViewById(R.id.set_tab4);
        openFloatWindow = findViewById(R.id.toggle_set_float);
        openFloatWindow.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    FloatBallManager.getInstance(getApplicationContext());
                    if (FloatBallManager.getFloatBallManager() != null) {
                        FloatWindow.get("FloatBallTag").show();
                    }
                    if (FloatListManager.getFloatListManager() != null) {
                        FloatWindow.get("FloatListTag").hide();
                    }
                    SharedXmlUtil.getInstance(InvSetActivity.this).write("floatWindow", "open");
                } else {
                    if (FloatBallManager.getFloatBallManager() != null) {
                        FloatBallManager.getFloatBallManager().closeFloatBall();
                    }
                    if (FloatListManager.getFloatListManager() != null) {
                        FloatListManager.getFloatListManager().closeFloatList();
                    }
                    SharedXmlUtil.getInstance(InvSetActivity.this).write("floatWindow", "close");
                }
            }
        });
        trReadTime = findViewById(R.id.set_tr_timeout);
        trSleep = findViewById(R.id.set_tr_sleep);
        etReadTime = findViewById(R.id.set_read_time);
        etSleep = findViewById(R.id.set_sleep);
        setReadTimeBtn = findViewById(R.id.btn_set_read_time);
        setSleepBtn = findViewById(R.id.btn_set_sleep);
        setReadTimeBtn.setOnClickListener(this);
        setSleepBtn.setOnClickListener(this);
        mVersionTv = (TextView) findViewById(R.id.tv_version_model);
        mVersionTv.setText(CommonUtils.getAppVersionName(this));
    }

    private void getFreq() {
        freqRegion = iuhfService.getFreqRegion();
        Log.d("zzc:", "===isFirstInit===setFreqRegion:==" + iuhfService.getFreqRegion());
        String r2k = "r2k";
        String xinLian = "xinlian";
        String yiXin = "yixin";
        if (r2k.equals(model)) {
            if (freqRegion == IUHFService.REGION_CHINA_920_925) {
                tvSetFreq.setText(getResources().getStringArray(R.array.r2k_freq)[1]);
            } else if (freqRegion == IUHFService.REGION_CHINA_840_845) {
                tvSetFreq.setText(getResources().getStringArray(R.array.r2k_freq)[0]);
            } else if (freqRegion == IUHFService.REGION_CHINA_902_928) {
                tvSetFreq.setText(getResources().getStringArray(R.array.r2k_freq)[2]);
            } else if (freqRegion == IUHFService.REGION_EURO_865_868) {
                tvSetFreq.setText(getResources().getStringArray(R.array.r2k_freq)[3]);
            } else if (freqRegion == -1) {
                tvSetFreq.setText("...");
                Log.e("r2000_kt45", "read region setting read failed");
            } else {
                tvSetFreq.setText(getResources().getString(R.string.set_freq_item1));
                etFreqPoint.setText(String.valueOf(new DecimalFormat("0.000").format(freqRegion / 1000.0)));
            }
        } else if (xinLian.equals(model)) {
            if (freqRegion == IUHFService.REGION_CHINA_920_925) {
                tvSetFreq.setText(getResources().getStringArray(R.array.freq)[1]);
            } else if (freqRegion == IUHFService.REGION_CHINA_840_845) {
                tvSetFreq.setText(getResources().getStringArray(R.array.freq)[0]);
            } else if (freqRegion == IUHFService.REGION_CHINA_902_928) {
                tvSetFreq.setText(getResources().getStringArray(R.array.freq)[2]);
            } else if (freqRegion == IUHFService.REGION_EURO_865_868) {
                tvSetFreq.setText(getResources().getStringArray(R.array.freq)[3]);
            } else {
                tvSetFreq.setText("...");
                Log.e("r2000_kt45", "read region setting read failed");
            }
        } else if (yiXin.equals(model)) {
            switch (freqRegion) {
                case 0x01:
                    tvSetFreq.setText(getResources().getStringArray(R.array.yi_xin_freq)[0]);
                    break;
                case 0x02:
                    tvSetFreq.setText(getResources().getStringArray(R.array.yi_xin_freq)[1]);
                    break;
                case 0x04:
                    tvSetFreq.setText(getResources().getStringArray(R.array.yi_xin_freq)[2]);
                    break;
                case 0x08:
                    tvSetFreq.setText(getResources().getStringArray(R.array.yi_xin_freq)[3]);
                    break;
                case 0x16:
                    tvSetFreq.setText(getResources().getStringArray(R.array.yi_xin_freq)[4]);
                    break;
                case 0x32:
                    tvSetFreq.setText(getResources().getStringArray(R.array.yi_xin_freq)[5]);
                    break;
                default:
                    tvSetFreq.setText("...");
                    break;
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private void getSession() {
        s2Region = iuhfService.getQueryTagGroup();
        if (s2Region != -1) {
            tvSetS2.setText("s" + s2Region);
            Log.d("zzc:", "==S2获取成功==" + s2Region);
        } else {
            Log.d("zzc:", "==S2获取失败==" + s2Region);
        }
    }

    private void unEnabled() {
        setFreqBtn.setEnabled(false);
        setSessionBtn.setEnabled(false);
        setPowerBtn.setEnabled(false);
        setInvConBtn.setEnabled(false);
        setBackBtn.setEnabled(false);
        algorithmSetBtn.setEnabled(false);
        mIvQuitSet.setEnabled(false);
        tvSetFreq.setEnabled(false);
        tvSetS2.setEnabled(false);
        tvSetInvCon.setEnabled(false);
    }

    private void enabled() {
        setFreqBtn.setEnabled(true);
        setSessionBtn.setEnabled(true);
        setPowerBtn.setEnabled(true);
        setInvConBtn.setEnabled(true);
        setBackBtn.setEnabled(true);
        algorithmSetBtn.setEnabled(true);
        mIvQuitSet.setEnabled(true);
        tvSetFreq.setEnabled(true);
        tvSetS2.setEnabled(true);
        tvSetInvCon.setEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //判断服务是否存在
        isExistServer = SharedXmlUtil.getInstance(this).read("server", false);
        if (isExistServer) {
            tableLayout5.setVisibility(View.VISIBLE);
            tableLayout4.setVisibility(View.VISIBLE);
        } else {
            tableLayout5.setVisibility(View.GONE);
            tableLayout4.setVisibility(View.GONE);
        }
        if ("open".equals(SharedXmlUtil.getInstance(this).read("floatWindow", "close"))) {
            openFloatWindow.setChecked(true);
        } else {
            openFloatWindow.setChecked(false);
            if (FloatBallManager.getFloatBallManager() != null) {
                FloatWindow.get("FloatBallTag").hide();
            }
            if (FloatListManager.getFloatListManager() != null) {
                FloatWindow.get("FloatListTag").hide();
            }
        }
    }

    @Override
    protected void onPause() {
        MyApp.isLoop = checkBoxLoop.isChecked();
        if (MyApp.isLoop) {
            MyApp.mLoopTime = etLoopTime.getText().toString();
        }
        MyApp.isLongDown = checkBoxLongDown.isChecked();
        super.onPause();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.set_title_iv:
                //退出
                finish();
                break;
            case R.id.set_freq_tv:
                //选择定频
                intent = new Intent(this, PopSetFreqActivity.class);
                startActivityForResult(intent, 1);
                break;
            case R.id.set_s2_tv:
                //选择s2
                intent = new Intent(this, PopSetS2Activity.class);
                startActivityForResult(intent, 2);
                break;
            case R.id.set_onlyepc_tv:
                //选择onlyepc
                intent = new Intent(this, PopSetInvContentActivity.class);
                startActivityForResult(intent, 3);
                break;
            case R.id.btn_set_back:
                //返回
                finish();
                break;
            case R.id.btn_set_freq:
                setFreq(freqRegion);
                break;
            case R.id.btn_set_session:
                setSession(s2Region);
                break;
            case R.id.btn_set_power:
                String power = etPower.getText().toString();
                setAntennaPower(power);
                break;
            case R.id.btn_set_invent:
                setInvCon(invConRegion);
                break;
            case R.id.btn_algorithm_set:
                setAlgorithm();
                break;
            case R.id.set_server_prefix:
                intent = new Intent();
                bundle = new Bundle();
                bundle.putString("send_fix", "prefix");
                intent.putExtras(bundle);
                intent.setClass(this, PopSetServiceActivity.class);
                startActivityForResult(intent, 4);
                break;
            case R.id.set_server_suffix:
                intent = new Intent();
                bundle = new Bundle();
                bundle.putString("send_fix", "suffix");
                intent.putExtras(bundle);
                intent.setClass(this, PopSetServiceActivity.class);
                startActivityForResult(intent, 5);
                break;
            case R.id.btn_set_read_time:
                String time = etReadTime.getText().toString();
                setInvTime(time);
                break;
            case R.id.btn_set_sleep:
                String sleep = etSleep.getText().toString();
                setInvSleep(sleep);
                break;
            default:
                break;
        }
    }


    private void setInvTime(String time) {
        try {
            if (time.isEmpty()) {
                time = "0";
            }
            int readTime = Integer.parseInt(time);
            if (readTime < 0) {
                readTime = 0;
                etReadTime.setText("0");
            }
            if (readTime > 3000) {
                etReadTime.setText("3000");
                ToastUtil.customToastView(InvSetActivity.this, getResources().getString(R.string.toast_readtime), Toast.LENGTH_SHORT
                        , (TextView) LayoutInflater.from(InvSetActivity.this).inflate(R.layout.layout_toast, null));
                return;
            }
            iuhfService.setReadTime(readTime);
            SharedXmlUtil.getInstance(InvSetActivity.this).write(MyApp.UHF_INV_TIME, readTime);
            ToastUtil.customToastView(InvSetActivity.this, getResources().getString(R.string.set_success), Toast.LENGTH_SHORT
                    , (TextView) LayoutInflater.from(InvSetActivity.this).inflate(R.layout.layout_toast, null));
        } catch (Exception e) {
            e.printStackTrace();
            ToastUtil.customToastView(InvSetActivity.this, getResources().getString(R.string.set_failed), Toast.LENGTH_SHORT
                    , (TextView) LayoutInflater.from(InvSetActivity.this).inflate(R.layout.layout_toast, null));
        }
    }

    private void setInvSleep(String sleep) {
        try {
            if (sleep.isEmpty()) {
                sleep = "0";
            }
            int sleepTime = Integer.parseInt(sleep);
            if (sleepTime < 0) {
                sleepTime = 0;
            }
            iuhfService.setSleep(sleepTime);
            SharedXmlUtil.getInstance(InvSetActivity.this).write(MyApp.UHF_INV_SLEEP, sleepTime);
            ToastUtil.customToastView(InvSetActivity.this, getResources().getString(R.string.set_success), Toast.LENGTH_SHORT
                    , (TextView) LayoutInflater.from(InvSetActivity.this).inflate(R.layout.layout_toast, null));
        } catch (Exception e) {
            e.printStackTrace();
            ToastUtil.customToastView(InvSetActivity.this, getResources().getString(R.string.set_failed), Toast.LENGTH_SHORT
                    , (TextView) LayoutInflater.from(InvSetActivity.this).inflate(R.layout.layout_toast, null));
        }
    }

    /**
     * @param region 设置定频
     */
    private void setFreq(final int region) {
        unEnabled();
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                final int i = iuhfService.setFreqRegion(region);
                if (i < 0) {
                    ToastUtil.customToastView(InvSetActivity.this, getResources().getString(R.string.set_failed), Toast.LENGTH_SHORT
                            , (TextView) LayoutInflater.from(InvSetActivity.this).inflate(R.layout.layout_toast, null));
                } else {
                    SharedXmlUtil.getInstance(InvSetActivity.this).write(MyApp.UHF_FREQ, region);
                    ToastUtil.customToastView(InvSetActivity.this, getResources().getString(R.string.set_success), Toast.LENGTH_SHORT
                            , (TextView) LayoutInflater.from(InvSetActivity.this).inflate(R.layout.layout_toast, null));
                }
                enabled();
            }
        });
    }

    /**
     * @param session 设置通话项
     */
    private void setSession(final int session) {
        unEnabled();
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                int setQueryTagGroup = iuhfService.setQueryTagGroup(0, session, 0);
                if (setQueryTagGroup == 0) {
                    SharedXmlUtil.getInstance(InvSetActivity.this).write(MyApp.UHF_SESSION, session);
                    ToastUtil.customToastView(InvSetActivity.this, getResources().getString(R.string.set_success), Toast.LENGTH_SHORT
                            , (TextView) LayoutInflater.from(InvSetActivity.this).inflate(R.layout.layout_toast, null));
                } else {
                    ToastUtil.customToastView(InvSetActivity.this, getResources().getString(R.string.set_failed), Toast.LENGTH_SHORT
                            , (TextView) LayoutInflater.from(InvSetActivity.this).inflate(R.layout.layout_toast, null));
                }
                enabled();
            }
        });
    }

    /**
     * @param power 设置天线功率
     */
    private void setAntennaPower(final String power) {
        if (TextUtils.isEmpty(power)) {
            ToastUtil.customToastView(this, getResources().getString(R.string.toast1), Toast.LENGTH_SHORT
                    , (TextView) LayoutInflater.from(this).inflate(R.layout.layout_toast, null));
            return;
        }
        unEnabled();
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                int p = Integer.parseInt(power);
                int p33 = p + 3;
                int m = 33;
                if ((p < 5) || (p33 > m)) {
                    ToastUtil.customToastView(InvSetActivity.this, getResources().getString(R.string.power_range), Toast.LENGTH_SHORT
                            , (TextView) LayoutInflater.from(InvSetActivity.this).inflate(R.layout.layout_toast, null));
                } else {
                    if (p33 > 30) {
                        int rv = iuhfService.setAntennaPower(p33);
                        if (rv < 0) {
                            int res = iuhfService.setAntennaPower(p);
                            if (res < 0) {
                                ToastUtil.customToastView(InvSetActivity.this, getResources().getString(R.string.set_power_fail), Toast.LENGTH_SHORT
                                        , (TextView) LayoutInflater.from(InvSetActivity.this).inflate(R.layout.layout_toast, null));
                                enabled();
                                return;
                            }
                        } else {
                            SharedXmlUtil.getInstance(InvSetActivity.this).write(MyApp.UHF_POWER, p33);
                            ToastUtil.customToastView(InvSetActivity.this, getResources().getString(R.string.set_power_ok), Toast.LENGTH_SHORT
                                    , (TextView) LayoutInflater.from(InvSetActivity.this).inflate(R.layout.layout_toast, null));
                            enabled();
                            return;
                        }
                    } else {
                        int rv = iuhfService.setAntennaPower(p);
                        if (rv < 0) {
                            ToastUtil.customToastView(InvSetActivity.this, getResources().getString(R.string.set_power_fail), Toast.LENGTH_SHORT
                                    , (TextView) LayoutInflater.from(InvSetActivity.this).inflate(R.layout.layout_toast, null));
                            enabled();
                            return;
                        }
                    }
                    SharedXmlUtil.getInstance(InvSetActivity.this).write(MyApp.UHF_POWER, p);
                    ToastUtil.customToastView(InvSetActivity.this, getResources().getString(R.string.set_power_ok), Toast.LENGTH_SHORT
                            , (TextView) LayoutInflater.from(InvSetActivity.this).inflate(R.layout.layout_toast, null));
                }
                enabled();
            }
        });

    }

    private void setInvCon(final int w) {
        unEnabled();
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                //设置盘点内容
                iuhfService.cancelMask();
                SharedXmlUtil.getInstance(InvSetActivity.this).write("U8", false);
                int caddr = 0, csize = 6;
                int mode = iuhfService.setInvMode(w, caddr, csize);
                if (mode == 0) {
                    SharedXmlUtil.getInstance(InvSetActivity.this).write(MyApp.UHF_INV_CON, w);
                    ToastUtil.customToastView(InvSetActivity.this, getResources().getString(R.string.set_success), Toast.LENGTH_SHORT
                            , (TextView) LayoutInflater.from(InvSetActivity.this).inflate(R.layout.layout_toast, null));

                } else {
                    ToastUtil.customToastView(InvSetActivity.this, getResources().getString(R.string.set_failed), Toast.LENGTH_SHORT
                            , (TextView) LayoutInflater.from(InvSetActivity.this).inflate(R.layout.layout_toast, null));
                }
                enabled();
            }
        });
    }

    private void setAlgorithm() {
        InventorySettingDialog inventorySettingDialog = new InventorySettingDialog(this, iuhfService);
        inventorySettingDialog.setTitle(getResources().getString(R.string.algorithm_set));
        inventorySettingDialog.show();
        inventorySettingDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (MyApp.isFastMode) {
                    setFreqBtn.setEnabled(false);
                    setSessionBtn.setEnabled(false);
                    setInvConBtn.setEnabled(false);
                    tvSetFreq.setEnabled(false);
                    tvSetS2.setEnabled(false);
                    tvSetInvCon.setEnabled(false);
                } else {
                    setFreqBtn.setEnabled(true);
                    setSessionBtn.setEnabled(true);
                    setInvConBtn.setEnabled(true);
                    tvSetFreq.setEnabled(true);
                    tvSetS2.setEnabled(true);
                    tvSetInvCon.setEnabled(true);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    if (bundle != null) {
                        String freq = bundle.getString("freq");
                        //选择的定频列表位置
                        freqRegion = bundle.getInt("position");
                        tvSetFreq.setText(freq);

                    }
                }
                break;
            case 2:
                if (resultCode == RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    assert bundle != null;
                    String s2 = bundle.getString("S2");
                    // 选择的S2列表位置
                    s2Region = bundle.getInt("position");
                    tvSetS2.setText(s2);

                }
                break;
            case 3:
                if (resultCode == RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    assert bundle != null;
                    String invCon = bundle.getString("InvCon");
                    //选择的盘点内容列表位置
                    invConRegion = bundle.getInt("position");
                    tvSetInvCon.setText(invCon);
                }
                break;
            case 4:
                if (resultCode == RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    assert bundle != null;
                    String fix = bundle.getString("prefix");
                    //选择的前缀列表位置
                    MyApp.mPrefix = bundle.getInt("position");
                    tvPrefix.setText(fix);
                }
                break;
            case 5:
                if (resultCode == RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    assert bundle != null;
                    String fix = bundle.getString("prefix");
                    //选择的后缀列表位置
                    MyApp.mSuffix = bundle.getInt("position");
                    tvSuffix.setText(fix);
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        if (checkBoxService.isChecked()) {
            SharedXmlUtil.getInstance(this).write("server", false);
            stopService(new Intent(this, MyService.class));
        }
        super.onDestroy();
    }
}
