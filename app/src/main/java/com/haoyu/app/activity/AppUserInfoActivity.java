package com.haoyu.app.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.support.v4.util.ArrayMap;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.haoyu.app.adapter.DepartmentAdapter;
import com.haoyu.app.adapter.DictEntryAdapter;
import com.haoyu.app.adapter.RegionAdapter;
import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.dialog.FileUploadDialog;
import com.haoyu.app.entity.DepartmentListResult;
import com.haoyu.app.entity.DictEntryMobileEntity;
import com.haoyu.app.entity.DictEntryResult;
import com.haoyu.app.entity.FileUploadResult;
import com.haoyu.app.entity.MDepartment;
import com.haoyu.app.entity.MobileUser;
import com.haoyu.app.entity.RegionListResult;
import com.haoyu.app.entity.RegionModule;
import com.haoyu.app.entity.UserInfoResult;
import com.haoyu.app.imageloader.GlideImgManager;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.pickerlib.CropImageView;
import com.haoyu.app.pickerlib.MediaOption;
import com.haoyu.app.pickerlib.MediaPicker;
import com.haoyu.app.rxBus.MessageEvent;
import com.haoyu.app.rxBus.RxBus;
import com.haoyu.app.utils.Action;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.utils.ScreenUtils;
import com.haoyu.app.utils.SharePreferenceHelper;
import com.haoyu.app.view.AppToolBar;
import com.haoyu.app.view.LoadFailView;
import com.haoyu.app.view.LoadingView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import butterknife.BindView;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Request;

/**
 * 创建日期：2017/2/27 on 13:42
 * 描述:
 * 作者:马飞奔 Administrator
 */
public class AppUserInfoActivity extends BaseActivity implements View.OnClickListener {
    private AppUserInfoActivity context = this;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;       //返回按钮
    @BindView(R.id.loadingView)
    LoadingView loadingView;   //正在加载视图
    @BindView(R.id.loadFailView)
    LoadFailView loadFailView;  //加载失败视图
    @BindView(R.id.contentView)
    View contentView;  //数据显示布局
    @BindView(R.id.ll_userIco)
    LinearLayout ll_userIco;
    @BindView(R.id.ll_userName)
    LinearLayout ll_userName;
    @BindView(R.id.ll_address)
    LinearLayout ll_address;
    @BindView(R.id.ll_unit)
    LinearLayout ll_unit;
    @BindView(R.id.ll_subject)
    LinearLayout ll_subject;
    @BindView(R.id.ll_email)
    LinearLayout ll_email;
    @BindView(R.id.ll_phone)
    LinearLayout ll_phone;
    @BindView(R.id.userIco)
    ImageView userIco;
    @BindView(R.id.tv_userName)
    TextView tv_userName;
    @BindView(R.id.tv_address)
    TextView tv_address;
    @BindView(R.id.tv_unit)
    TextView tv_unit;
    @BindView(R.id.tv_subject)
    TextView tv_subject;
    @BindView(R.id.tv_email)
    TextView tv_email;
    @BindView(R.id.tv_phone)
    TextView tv_phone;
    private File mFile;
    private boolean alterName, alterDept, alterSs, alterEmail;
    private boolean initRegion; //是否已经加载省份
    private boolean initSS; //是否已经加载学段学科
    private List<DictEntryMobileEntity> subjectDatas = new ArrayList<>(); // 学科集合
    private List<DictEntryMobileEntity> stageDatas = new ArrayList<>(); // 学段集合
    private DictEntryMobileEntity mStage, mSubject;
    private int selectStage = -1, selectSubject = -1;
    private String stageValue, subjectValue, stageName, subjectName;
    private String deptName;
    private String icoUrl;

    @Override
    public int setLayoutResID() {
        return R.layout.activity_userinfo;
    }

    @Override
    public void initView() {
        icoUrl = getAvatar();
    }

    public void initData() {
        String url = Constants.OUTRT_NET + "/m/user/" + getUserId();
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<UserInfoResult>() {
            @Override
            public void onBefore(Request request) {
                loadingView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError(Request request, Exception e) {
                loadingView.setVisibility(View.GONE);
                loadFailView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onResponse(UserInfoResult response) {
                loadingView.setVisibility(View.GONE);
                if (response != null && response.getResponseData() != null) {
                    contentView.setVisibility(View.VISIBLE);
                    updateUI(response.getResponseData());
                }
            }

        }));
    }

    private void updateUI(MobileUser user) {
        if (user.getAvatar() == null)
            icoUrl = getAvatar();
        else
            icoUrl = user.getAvatar();
        GlideImgManager.loadCircleImage(context.getApplicationContext(), icoUrl, R.drawable.user_default,
                R.drawable.user_default, userIco);
        tv_userName.setText(user.getRealName());
        tv_address.setText(null);
        if (user.getmDepartment() != null)
            tv_unit.setText(user.getmDepartment().getDeptName());
        else
            tv_unit.setText(user.getDeptName());
        String address = "";
        if (user.getmDepartment() != null) {
            if (user.getmDepartment().getProvince() != null) {
                address += user.getmDepartment().getProvince() + "\u3000";
            }
            if (user.getmDepartment().getCity() != null) {
                address += user.getmDepartment().getCity() + "\u3000";
            }
            if (user.getmDepartment().getCounties() != null) {
                address += user.getmDepartment().getCounties();
            }
        }
        tv_address.setText(address);
        StringBuilder sb = new StringBuilder();
        if (user.getmStage() != null) {
            mStage = user.getmStage();
            stageValue = user.getmStage().getTextBookValue();
            stageName = user.getmStage().getTextBookName();
            sb.append(user.getmStage().getTextBookName());
            sb.append("\u3000\u3000");
        }
        if (user.getmSubject() != null) {
            mSubject = user.getmSubject();
            selectSubject = subjectDatas.indexOf(user.getmSubject());
            subjectValue = user.getmSubject().getTextBookValue();
            subjectName = user.getmSubject().getTextBookName();
            sb.append(user.getmSubject().getTextBookName());
        }
        tv_subject.setText(sb.toString());
        tv_email.setText(user.getEmail());
        tv_phone.setText(user.getPhone());
    }

    @Override
    public void setListener() {
        toolBar.setOnLeftClickListener(new AppToolBar.OnLeftClickListener() {
            @Override
            public void onLeftClick(View view) {
                finish();
            }
        });
        userIco.setOnClickListener(context);
        ll_userIco.setOnClickListener(context);
        ll_userName.setOnClickListener(context);
        ll_address.setOnClickListener(context);
        ll_unit.setOnClickListener(context);
        ll_subject.setOnClickListener(context);
        ll_email.setOnClickListener(context);
        ll_phone.setOnClickListener(context);
        loadFailView.setOnRetryListener(new LoadFailView.OnRetryListener() {
            @Override
            public void onRetry(View v) {
                initData();
            }
        });
    }

    @Override
    public void onClick(View v) {
        alterName = false;
        alterEmail = false;
        alterDept = false;
        alterSs = false;
        switch (v.getId()) {
            case R.id.ll_userIco:
                pickerPicture();  //设置头像剪裁，单选
                break;
            case R.id.userIco:
                if (icoUrl != null && icoUrl.length() > 0) {
                    Intent intent = new Intent(context, AppMultiImageShowActivity.class);
                    ArrayList<String> imgList = new ArrayList<>();
                    imgList.add(icoUrl);
                    intent.putStringArrayListExtra("photos", imgList);
                    intent.putExtra("position", 0);
                    intent.putExtra("isUser", true);
                    startActivity(intent);
                    overridePendingTransition(R.anim.zoom_in, 0);
                }
                break;
            case R.id.ll_userName:   //更换用户名
                alterName = true;
                alterDialog(tv_userName.getText().toString());
                break;
            case R.id.ll_address:   //选择区域
                if (initRegion) {
                    showRegionDialog();
                } else {
                    initRegion();
                }
                break;
            case R.id.ll_unit:      //选择单位
                if (regionType != null && regionCode != null) {
                    if (unitMap.get(regionCode) == null) {
                        initUnit();
                    } else {
                        showUnitDialog(unitMap.get(regionCode));
                    }
                } else {
                    toast(context, "请先选择区域");
                }
                break;
            case R.id.ll_subject:  //选择学段学科
                alterSs = true;
                if (initSS) {
                    showSsDialog();
                } else {
                    initStageSubject();
                }
                break;
            case R.id.ll_email:
                alterEmail = true;
                alterDialog(tv_email.getText().toString());
                break;
            case R.id.ll_phone:

                break;
        }
    }

    private void pickerPicture() {
        MediaOption option = new MediaOption.Builder()
                .setSelectType(MediaOption.TYPE_IMAGE)
                .isMultiMode(false)
                .setCrop(true)
                .setShowCamera(false)
                .setSaveRectangle(true)
                .setStyle(CropImageView.Style.CIRCLE)
                .setFocusWidth(ScreenUtils.getScreenWidth(context) / 4 * 3)
                .setFocusHeight(ScreenUtils.getScreenWidth(context) / 4 * 3)
                .build();
        MediaPicker.getInstance().init(option).selectMedia(context, new MediaPicker.onSelectMediaCallBack() {
            @Override
            public void onSelected(String path) {
                mFile = new File(path);
                uploadUserIco();
            }
        });
    }

    private void uploadUserIco() {
        if (mFile.exists()) {
            String url = Constants.OUTRT_NET + "/m/file/uploadFileInfoRemote";
            final FileUploadDialog uploadDialog = new FileUploadDialog(context, mFile.getName(), "正在上传头像");
            uploadDialog.setCancelable(false);
            uploadDialog.setCanceledOnTouchOutside(false);
            uploadDialog.show();
            Flowable.just(url).map(new Function<String, FileUploadResult>() {
                @Override
                public FileUploadResult apply(String url) throws Exception {
                    return commitUserIco(url, uploadDialog);
                }
            }).map(new Function<FileUploadResult, UserInfoResult>() {
                @Override
                public UserInfoResult apply(FileUploadResult fileUploadResult) throws Exception {
                    if (fileUploadResult != null && fileUploadResult.getResponseData() != null) {
                        String url = Constants.OUTRT_NET + "/m/user/" + getUserId();
                        Map<String, String> map = new HashMap<>();
                        map.put("_method", "put");
                        map.put("avatar", fileUploadResult.getResponseData().getRelativeUrl());
                        Gson gson = new GsonBuilder().create();
                        String userStr = OkHttpClientManager.postAsString(context, url, map);
                        UserInfoResult userInfoResult = gson.fromJson(userStr, UserInfoResult.class);
                        return userInfoResult;
                    }
                    return null;
                }
            }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<UserInfoResult>() {
                @Override
                public void accept(UserInfoResult result) throws Exception {
                    uploadDialog.dismiss();
                    if (result != null && result.getResponseData() != null) {
                        GlideImgManager.loadCircleImage(context, result.getResponseData().getAvatar(),
                                R.drawable.user_default, R.drawable.user_default, userIco);
                        saveUserInfo(result.getResponseData().getAvatar(), null, null, null, null, null);
                        MessageEvent event = new MessageEvent();
                        event.action = Action.CHANGE_USER_ICO;
                        event.obj = result.getResponseData().getAvatar();
                        RxBus.getDefault().post(event);
                    } else {
                        toastFullScreen("头像上传失败", false);
                    }
                }
            }, new Consumer<Throwable>() {
                @Override
                public void accept(Throwable throwable) throws Exception {
                    uploadDialog.dismiss();
                    toastFullScreen("头像上传失败", false);
                }
            });
        } else {
            showMaterialDialog("提示", "选择的图片不存在，请重新选择");
        }
    }

    /*上传资源到临时文件*/
    private FileUploadResult commitUserIco(String url, final FileUploadDialog dialog) throws Exception {
        Gson gson = new GsonBuilder().create();
        String resultStr = OkHttpClientManager.post(context, url, mFile, mFile.getName(), new OkHttpClientManager.ProgressListener() {
            @Override
            public void onProgress(long totalBytes, long remainingBytes, boolean done, File file) {
                Flowable.just(new long[]{totalBytes, remainingBytes}).observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<long[]>() {
                            @Override
                            public void accept(long[] params) throws Exception {
                                dialog.setUploadProgressBar(params[0], params[1]);
                                dialog.setUploadText(params[0], params[1]);
                            }
                        });
            }
        });
        FileUploadResult mResult = gson.fromJson(resultStr, FileUploadResult.class);
        return mResult;
    }

    private void initStageSubject() {
        showTipDialog();
        Flowable.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return initSS();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Boolean>() {
            @Override
            public void accept(Boolean success) throws Exception {
                hideTipDialog();
                initSS = success;
                if (initSS) {
                    showSsDialog();
                } else {
                    onNetWorkError(context);
                }
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                hideTipDialog();
                onNetWorkError(context);
            }
        });
    }

    private boolean initSS() throws Exception {
        String url1 = Constants.OUTRT_NET + "/m/textBook?textBookTypeCode=STAGE";
        String url2 = Constants.OUTRT_NET + "/m/textBook?textBookTypeCode=SUBJECT";
        Gson gson = new Gson();
        String stageStr = OkHttpClientManager.getAsString(context, url1);
        DictEntryResult response = gson.fromJson(stageStr, DictEntryResult.class);
        if (response != null && response.getResponseData() != null) {
            stageDatas.addAll(response.getResponseData());
            if (mStage != null) {
                selectStage = stageDatas.indexOf(mStage);
            }
        }
        String subjectStr = OkHttpClientManager.getAsString(context, url2);
        response = gson.fromJson(subjectStr, DictEntryResult.class);
        if (response != null && response.getResponseData() != null) {
            subjectDatas.addAll(response.getResponseData());
            if (mSubject != null) {
                selectSubject = subjectDatas.indexOf(mSubject);
            }
        }
        return true;
    }

    private void showSsDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_dict_select, null);
        final AlertDialog dialog = new AlertDialog.Builder(context).create();
        dialog.setCanceledOnTouchOutside(false);
        ListView stageList = view.findViewById(R.id.stageList);
        final ListView subjectList = view.findViewById(R.id.subjectList);
        Button makesure = view.findViewById(R.id.makesure);
        Button cancel = view.findViewById(R.id.cancel);
        final DictEntryAdapter stageAdapter = new DictEntryAdapter(context, stageDatas, selectStage);
        stageList.setAdapter(stageAdapter);
        final DictEntryAdapter subjectAdapter = new DictEntryAdapter(context, subjectDatas, selectSubject);
        subjectList.setAdapter(subjectAdapter);
        stageList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                selectStage = position;
                stageAdapter.setSelectItem(position);
                stageValue = stageDatas.get(position).getTextBookValue();
                stageName = stageDatas.get(position).getTextBookName();
            }
        });
        subjectList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                selectSubject = position;
                subjectAdapter.setSelectItem(position);
                subjectValue = subjectDatas.get(position).getTextBookValue();
                subjectName = subjectDatas.get(position).getTextBookName();
            }
        });
        makesure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!(stageValue == null && subjectValue == null)) {
                    alterInfo(null, null, stageValue, subjectValue, null);
                }
                dialog.dismiss();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ScreenUtils.getScreenWidth(context) / 6 * 5,
                ScreenUtils.getScreenHeight(context) / 3 * 2);
        dialog.setContentView(view, params);
    }


    /*获取省份列表*/
    private void initRegion() {
        /*获取省份列表url*/
        showLoadingDialog("加载中");
        String url = Constants.OUTRT_NET + "/m/regions?level=1";
        Flowable.just(url).map(new Function<String, Boolean>() {
            @Override
            public Boolean apply(String url) throws Exception {
                return initPcc(url);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<Boolean>() {
            @Override
            public void accept(Boolean success) throws Exception {
                hideLoadingDialog();
                initRegion = success;
                if (initRegion) {
                    showRegionDialog();
                } else {
                    onNetWorkError(context);
                }
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                hideLoadingDialog();
                onNetWorkError(context);
            }
        });
    }

    private List<RegionModule> provinceList = new ArrayList<>();
    private ArrayMap<String, List<RegionModule>> cityMap = new ArrayMap<>();
    private ArrayMap<String, List<RegionModule>> countryMap = new ArrayMap<>();

    private boolean initPcc(String url) throws Exception {
        Gson gson = new Gson();
        String regionStr = OkHttpClientManager.getAsString(context, url);
                /*省份列表*/
        RegionListResult provinceResult = gson.fromJson(regionStr, RegionListResult.class);
        if (provinceResult != null && provinceResult.getResponseData().size() > 0) {  //取省份的第一个获取所有市
            provinceList = provinceResult.getResponseData();
            selectProvince = 0;
            RegionModule region = provinceList.get(0);
            String regionsCode = region.getRegionsCode();
            url = Constants.OUTRT_NET + "/m/regions?level=2&parentCode=" + regionsCode;
            regionStr = OkHttpClientManager.getAsString(context, url);
            RegionListResult cityResult = gson.fromJson(regionStr, RegionListResult.class);
                    /*取市列表的第一个元素获取所有地区*/
            if (cityResult != null && cityResult.getResponseData().size() > 0) {
                cityMap.put(regionsCode, cityResult.getResponseData());
                region = cityResult.getResponseData().get(0);
                regionsCode = region.getRegionsCode();
                url = Constants.OUTRT_NET + "/m/regions?level=3&parentCode=" + regionsCode;
                regionStr = OkHttpClientManager.getAsString(context, url);
                RegionListResult countryResult = gson.fromJson(regionStr, RegionListResult.class);
                if (countryResult != null && countryResult.getResponseData().size() > 0) {
                    countryMap.put(regionsCode, countryResult.getResponseData());
                }
            }
        }
        return true;
    }

    private RegionAdapter cityAdapter, countryAdapter;
    private List<RegionModule> cityList = new ArrayList<>();
    private List<RegionModule> countryList = new ArrayList<>();
    private int selectProvince = -1;
    private String regionType, regionCode;
    private String province = "", city = "", country = "";

    private void showRegionDialog() {
        if (provinceList.size() == 0) {
            toast(context, "没有查询到区域信息！");
            return;
        }
        cityAdapter = new RegionAdapter(context, cityList);
        countryAdapter = new RegionAdapter(context, countryList);
        province = city = country = "";
        regionType = regionCode = null;
        View view = getLayoutInflater().inflate(R.layout.dialog_select_region, null);
        final AlertDialog dialog = new AlertDialog.Builder(context).create();
        dialog.setCanceledOnTouchOutside(false);
        ListView lvProvince = view.findViewById(R.id.lvProvince);
        final ListView lvCity = view.findViewById(R.id.lvCity);
        final ListView lvCounties = view.findViewById(R.id.lvCounties);
        Button makesure = view.findViewById(R.id.makesure);
        Button cancel = view.findViewById(R.id.cancel);
        final RegionAdapter provinceAdapter = new RegionAdapter(context, provinceList, selectProvince);
        lvProvince.setAdapter(provinceAdapter);
        lvCity.setAdapter(cityAdapter);
        lvCounties.setAdapter(countryAdapter);
        if (provinceList.size() > 0 && provinceList.get(selectProvince) != null
                && cityMap.get(provinceList.get(selectProvince).getRegionsCode()) != null) {
            province = provinceList.get(selectProvince).getRegionsName() + "\u3000";
            cityList.clear();
            cityList.addAll(cityMap.get(provinceList.get(selectProvince).getRegionsCode()));
            cityAdapter.notifyDataSetChanged();
            if (cityList.size() > 0 && cityList.get(0) != null
                    && countryMap.get(cityList.get(0).getRegionsCode()) != null) {
                countryList.clear();
                countryList.addAll(countryMap.get(cityList.get(0).getRegionsCode()));
                countryAdapter.notifyDataSetChanged();
            }
        } else {
            province = "";
        }

        lvProvince.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                selectProvince = position;
                String regionsCode = provinceList.get(position).getRegionsCode();
                provinceAdapter.setSelectItem(position);
                regionType = "province";
                regionCode = regionsCode;
                province = provinceList.get(position).getRegionsName() + "\u3000";
                cityList.clear();
                cityAdapter.notifyDataSetChanged();
                countryList.clear();
                countryAdapter.notifyDataSetChanged();
                if (cityMap.get(regionsCode) != null) {
                    cityList.addAll(cityMap.get(regionsCode));
                    cityAdapter.notifyDataSetChanged();
                    if (cityList.size() > 0 && cityList.get(0) != null
                            && countryMap.get(cityList.get(0).getRegionsCode()) != null) {
                        countryList.addAll(countryMap.get(cityList.get(0).getRegionsCode()));
                        countryAdapter.notifyDataSetChanged();
                    }
                } else {
                    getCityList(regionsCode);
                }
            }
        });

        lvCity.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                String regionsCode = cityList.get(position).getRegionsCode();
                cityAdapter.setSelectItem(position);
                regionType = "city";
                regionCode = regionsCode;
                city = cityList.get(position).getRegionsName() + "\u3000";
                countryList.clear();
                countryAdapter.notifyDataSetChanged();
                if (countryMap.get(regionsCode) != null) {
                    countryList.addAll(countryMap.get(regionsCode));
                    countryAdapter.notifyDataSetChanged();
                } else {
                    getCountryList(regionsCode);
                }
            }
        });

        lvCounties.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                String regionsCode = countryList.get(position).getRegionsCode();
                countryAdapter.setSelectItem(position);
                regionType = "counties";
                regionCode = regionsCode;
                country = countryList.get(position).getRegionsName();
            }
        });

        makesure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tv_address.setText(province + city + country);
                dialog.dismiss();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                regionType = null;
                regionCode = null;
                dialog.dismiss();
            }
        });
        dialog.show();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ScreenUtils.getScreenWidth(context) / 7 * 6,
                ScreenUtils.getScreenHeight(context) / 3 * 2);
        dialog.setContentView(view, params);
    }

    /*根据省编码获取市列表*/
    private void getCityList(final String regionsCode) {
        String url = Constants.OUTRT_NET + "/m/regions?level=2&parentCode=" + regionsCode;
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<RegionListResult>() {
            @Override
            public void onBefore(Request request) {
                showTipDialog();
            }

            @Override
            public void onError(Request request, Exception e) {
                hideTipDialog();
                onNetWorkError(context);
            }

            @Override
            public void onResponse(RegionListResult response) {
                hideTipDialog();
                if (response != null && response.getResponseData() != null) {
                    cityList.clear();
                    cityList.addAll(response.getResponseData());
                    cityAdapter.setSelectItem(-1);
                    cityAdapter.notifyDataSetChanged();
                    cityMap.put(regionsCode, response.getResponseData());
                    if (cityList.size() > 0) {
                        String regionsCode = cityList.get(0).getRegionsCode();
                        countryList.clear();
                        countryAdapter.notifyDataSetChanged();
                        if (countryMap.get(regionsCode) != null) {
                            countryList.addAll(countryMap.get(regionsCode));
                            countryAdapter.notifyDataSetChanged();
                        } else {
                            getCountryList(regionsCode);
                        }
                    }
                }
            }
        }));
    }

    /*根据市编码获取地区列表*/
    private void getCountryList(final String regionsCode) {
        String url = Constants.OUTRT_NET + "/m/regions?level=3&parentCode=" + regionsCode;
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<RegionListResult>() {
            @Override
            public void onBefore(Request request) {
                showTipDialog();
            }

            @Override
            public void onError(Request request, Exception e) {
                hideTipDialog();
                onNetWorkError(context);
            }

            @Override
            public void onResponse(RegionListResult response) {
                hideTipDialog();
                if (response != null && response.getResponseData() != null) {
                    countryList.clear();
                    countryList.addAll(response.getResponseData());
                    countryAdapter.setSelectItem(-1);
                    countryAdapter.notifyDataSetChanged();
                    countryMap.put(regionsCode, response.getResponseData());
                }
            }
        }));
    }

    private List<MDepartment> unitList = new ArrayList<>();
    private ArrayMap<String, List<MDepartment>> unitMap = new ArrayMap<>();

    /*获取所有单位信息*/
    private void initUnit() {
        String url = Constants.OUTRT_NET + "/m/department?type=" + regionType + "&code=" + regionCode;
        addSubscription(OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<DepartmentListResult>() {
            @Override
            public void onBefore(Request request) {
                showTipDialog();
            }

            @Override
            public void onError(Request request, Exception e) {
                hideTipDialog();
                onNetWorkError(context);
            }

            @Override
            public void onResponse(DepartmentListResult response) {
                hideTipDialog();
                if (response != null && response.getResponseData() != null) {
                    unitList = response.getResponseData();
                    unitMap.put(regionCode, response.getResponseData());
                    showUnitDialog(unitList);
                }
            }
        }));
    }

    private void showUnitDialog(final List<MDepartment> unitList) {
        if (unitList.size() == 0) {
            toast(context, "没有查询到单位信息！");
            return;
        }
        View view = getLayoutInflater().inflate(R.layout.dialog_select_depart, null);
        final AlertDialog dialog = new AlertDialog.Builder(context).create();
        dialog.setCanceledOnTouchOutside(true);
        ListView listView = view.findViewById(R.id.listView);
        final DepartmentAdapter adapter = new DepartmentAdapter(context, unitList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                String deptId = unitList.get(position).getId();
                deptName = unitList.get(position).getDeptName();
                alterDept = true;
                alterInfo(null, deptId, null, null, null);
                dialog.dismiss();
            }
        });
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ScreenUtils.getScreenWidth(context) / 7 * 6,
                ScreenUtils.getScreenHeight(context) / 3 * 2);
        dialog.show();
        dialog.setContentView(view, params);
    }

    private void alterDialog(String text) {
        View view = getLayoutInflater().inflate(R.layout.dialog_alter_userinfo, null);
        final AlertDialog dialog = new AlertDialog.Builder(context).create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.setView(view);
        dialog.show();
        Window window = dialog.getWindow();
        window.setContentView(R.layout.dialog_alter_userinfo);
        final EditText et_imput = dialog.findViewById(R.id.et_content);
        et_imput.setText(text);
        et_imput.requestFocus();
        et_imput.setFocusable(true);
        final Button bt_save = dialog.findViewById(R.id.bt_save);
        bt_save.setEnabled(false);
        et_imput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() > 0) {
                    bt_save.setEnabled(true);
                } else {
                    bt_save.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        bt_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = et_imput.getText().toString().trim();
                if (alterName) {
                    alterInfo(content, null, null, null, null);
                } else if (alterEmail) {
                    alterInfo(null, null, null, null, content);
                }
                dialog.dismiss();
            }
        });
        window.setLayout(ScreenUtils.getScreenWidth(context), WindowManager.LayoutParams.WRAP_CONTENT);
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        window.setGravity(Gravity.BOTTOM);
    }

    private void alterInfo(String realName, String deptId, String stage, String subject, String email) {
        String url = Constants.OUTRT_NET + "/m/user/" + getUserId();
        Map<String, String> map = new HashMap<>();
        map.put("_method", "put");
        if (realName != null) {
            map.put("realName", realName);
        }
        if (deptId != null) {
            map.put("department.id", deptId);
        }
        if (stage != null) {
            map.put("stage", stage);
        }
        if (subject != null) {
            map.put("subject", subject);
        }
        if (email != null) {
            map.put("email", email);
        }
        OkHttpClientManager.postAsyn(context, url, new OkHttpClientManager.ResultCallback<UserInfoResult>() {
            @Override
            public void onError(Request request, Exception e) {
                onNetWorkError(context);
            }

            @Override
            public void onResponse(UserInfoResult response) {
                if (response != null && response.getResponseData() != null) {
                    MobileUser user = response.getResponseData();
                    if (alterName) {
                        tv_userName.setText(user.getRealName());
                        saveUserInfo(null, user.getRealName(), null, null, null, null);
                        MessageEvent event = new MessageEvent();
                        event.action = Action.CHANGE_USER_NAME;
                        event.obj = user.getRealName();
                        RxBus.getDefault().post(event);
                    } else if (alterEmail) {
                        tv_email.setText(user.getEmail());
                        saveUserInfo(null, null, null, null, null, user.getEmail());
                    } else if (alterSs) {
                        StringBuilder sb = new StringBuilder();
                        if (stageName != null) {
                            sb.append(stageName);
                            sb.append("\u3000\u3000");
                        }
                        if (subjectName != null) {
                            sb.append(subjectName);
                        }
                        tv_subject.setText(sb.toString());
                        saveUserInfo(null, null, null, stageName, subjectName, null);
                    } else if (alterDept) {
                        tv_unit.setText(deptName);
                        saveUserInfo(null, null, deptName, null, null, null);
                        MessageEvent event = new MessageEvent();
                        event.action = Action.CHANGE_DEPT_NAME;
                        event.obj = deptName;
                        RxBus.getDefault().post(event);
                    }
                }
            }
        }, map);
    }

    private void saveUserInfo(String avatar, String realName, String deptName, String stage, String subject, String email) {
        SharePreferenceHelper sharePreferenceHelper = new SharePreferenceHelper(context);
        Map<String, Object> map = new HashMap<>();
        if (avatar != null) {
            map.put("avatar", avatar);
        }
        if (realName != null) {
            map.put("realName", realName);
        }
        if (deptName != null) {
            map.put("deptName", deptName);
        }
        if (stage != null) {
            map.put("stage", stage);
        }
        if (subject != null) {
            map.put("subject", subject);
        }
        if (email != null) {
            map.put("email", email);
        }
        sharePreferenceHelper.saveSharePreference(map);
    }
}
