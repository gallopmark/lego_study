package com.haoyu.app.filePicker;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.haoyu.app.base.BaseActivity;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.view.AppToolBar;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;

public class LFilePickerActivity extends BaseActivity {
    private LFilePickerActivity context = this;
    private int REQUEST_READ = 10;
    private boolean requestUI;
    @BindView(R.id.toolBar)
    AppToolBar toolBar;
    @BindView(R.id.rl_tips)
    RelativeLayout rl_tips;
    @BindView(R.id.tv_tips)
    TextView tv_tips;
    @BindView(R.id.bt_settings)
    Button bt_settings;
    @BindView(R.id.fileContent)
    RelativeLayout fileContent;
    @BindView(R.id.tv_parentName)
    TextView tv_parentName;
    @BindView(R.id.recylerview)
    RecyclerView mRecylerView;
    @BindView(R.id.emptyView)
    TextView emptyView;
    @BindView(R.id.btn_addbook)
    Button mBtnAddBook;
    private String mCurrentPath, mStartPath;
    private List<File> mListFiles;
    private ArrayList<String> mListNumbers = new ArrayList<>();//存放选中条目的数据地址
    private FileFilterAdapter filterAdapter;
    private LFileFilter mFilter;
    private boolean mMutilyMode;
    private Map<String, Integer[]> scrolls = new HashMap<>();

    @Override
    protected void onRestart() {   //用户点击设置，申请到了权限
        super.onRestart();
        if (checkSelfPermission() && !requestUI) {
            rl_tips.setVisibility(View.GONE);
            requestUI();
        }
    }

    @Override
    public int setLayoutResID() {
        return R.layout.activity_lfile_picker;
    }

    /**
     * 初始化控件
     */
    @Override
    public void initView() {
        if (!checkSelfPermission()) {
            //第一请求权限被取消显示的判断，一般可以不写
            ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ);
        } else {
            requestUI();
        }
    }

    private boolean checkSelfPermission() {
        //判断是否6.0以上的手机   不是就不用
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    //判断授权的方法  授权成功直接调用写入方法  这是监听的回调
    //参数  上下文   授权结果的数组   申请授权的数组
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_READ && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            requestUI();
        } else {
            rl_tips.setVisibility(View.VISIBLE);
            tv_tips.setText("存储权限已被禁止，选择系统文件需要打开存储权限，请重新打开！");
        }
    }

    private void requestUI() {
        mMutilyMode = getIntent().getBooleanExtra("mMutilyMode", false);
        String[] mFileTypes = getIntent().getStringArrayExtra("mFileTypes");
        if (!mMutilyMode) {
            mBtnAddBook.setVisibility(View.GONE);
        }
        if (!checkSDState()) {
            rl_tips.setVisibility(View.VISIBLE);
            tv_tips.setText("手机存储卡不可用，请确认存储卡已经挂载！");
            bt_settings.setVisibility(View.GONE);
            return;
        }
        fileContent.setVisibility(View.VISIBLE);
        mStartPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        mCurrentPath = mStartPath;
        tv_parentName.setText(mCurrentPath);
        mFilter = new LFileFilter(mFileTypes);
        mListFiles = getFileList(mStartPath);
        filterAdapter = new FileFilterAdapter(mListFiles, mFilter, mMutilyMode);
        mRecylerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mRecylerView.setAdapter(filterAdapter);
        setOnClickListener();
        requestUI = true;
    }

    private void setOnClickListener() {
        // 返回目录上一级
        tv_parentName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tempPath = new File(mCurrentPath).getParent();
                if (tempPath == null || !new File(tempPath).exists())
                    return;
                mCurrentPath = tempPath;
                updateData();
            }
        });
        filterAdapter.setOnItemClickListener(new FileFilterAdapter.OnItemClickListener() {
            @Override
            public void click(int position) {
                if (mMutilyMode) {
                    if (mListFiles.get(position).isDirectory()) {
                        //如果当前是目录，则进入继续查看目录
                        chekInDirectory(position);
                    } else {
                        //如果已经选择则取消，否则添加进来
                        if (mListNumbers.contains(mListFiles.get(position).getAbsolutePath())) {
                            mListNumbers.remove(mListFiles.get(position).getAbsolutePath());
                        } else {
                            mListNumbers.add(mListFiles.get(position).getAbsolutePath());
                        }
                        mBtnAddBook.setText("选中" + "( " + mListNumbers.size() + " )");
                    }
                } else {
                    //单选模式直接返回
                    if (mListFiles.get(position).isDirectory()) {
                        chekInDirectory(position);
                    } else {
                        mListNumbers.clear();
                        mListNumbers.add(mListFiles.get(position).getAbsolutePath());
                        chooseDone();
                    }
                }

            }
        });

        mBtnAddBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListNumbers.size() < 1) {
                    toast(context, "请选择导入的图书");
                } else {
                    //返回
                    chooseDone();
                }
            }
        });
    }

    @Override
    public void setListener() {
        toolBar.setOnLeftClickListener(new AppToolBar.OnLeftClickListener() {
            @Override
            public void onLeftClick(View view) {
                finish();
            }
        });
        bt_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openSettings();
            }
        });
        //监听RecyclerView滚动状态
        mRecylerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (recyclerView.getLayoutManager() != null) {
                    getPositionAndOffset();
                }
            }
        });
    }

    /**
     * 记录RecyclerView当前位置
     */
    private void getPositionAndOffset() {
        LinearLayoutManager layoutManager = (LinearLayoutManager) mRecylerView.getLayoutManager();
        //获取可视的第一个view
        View topView = layoutManager.getChildAt(0);
        if (topView != null) {
            //获取与该view的顶部的偏移量
            int lastOffset = topView.getTop();
            //得到该View的数组位置
            int lastPosition = layoutManager.getPosition(topView);
            scrolls.put(mCurrentPath, new Integer[]{lastPosition, lastOffset});
        }
    }

    /**
     * 让RecyclerView滚动到指定位置
     */
    private void scrollToPosition() {
        if (mRecylerView.getLayoutManager() != null && scrolls.get(mCurrentPath) != null) {
            Integer[] offs = scrolls.get(mCurrentPath);
            ((LinearLayoutManager) mRecylerView.getLayoutManager()).scrollToPositionWithOffset(offs[0], offs[1]);
        }
    }

    private void openSettings() {
        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 点击进入目录
     *
     * @param position
     */
    private void chekInDirectory(int position) {
        mCurrentPath = mListFiles.get(position).getAbsolutePath();
        updateData();
    }


    private void updateData() {
        tv_parentName.setText(mCurrentPath);
        mListFiles = getFileList(mCurrentPath);
        if (mListFiles.size() > 0) {
            emptyView.setVisibility(View.GONE);
            mRecylerView.setVisibility(View.VISIBLE);
            filterAdapter.setmListData(mListFiles);
            filterAdapter.notifyDataSetChanged();
            scrollToPosition();
        } else {
            mRecylerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 完成提交
     */
    private void chooseDone() {
        Intent intent = new Intent();
        intent.putStringArrayListExtra(RESULT_INFO, mListNumbers);
        setResult(RESULT_OK, intent);
        this.finish();
    }

    /**
     * 根据地址获取当前地址下的所有目录和文件，并且排序
     *
     * @param path
     * @return List<File>
     */
    private List<File> getFileList(String path) {
        if (path == null || !new File(path).exists())
            return new ArrayList<>();
        List<File> list = FileUtils.getFileListByDirPath(path, mFilter);
        return list;
    }

    /**
     * 检测SD卡是否可用
     */
    private boolean checkSDState() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && mCurrentPath != null) {
            String tempPath = new File(mCurrentPath).getParent();
            if (tempPath != null && new File(tempPath).exists()) {
                mCurrentPath = tempPath;
                updateData();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
