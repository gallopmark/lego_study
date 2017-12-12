package com.haoyu.app.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.haoyu.app.activity.CreateOrAlterNoteActivity;
import com.haoyu.app.adapter.PageNoteAdapter;
import com.haoyu.app.base.BaseFragment;
import com.haoyu.app.basehelper.BaseRecyclerAdapter;
import com.haoyu.app.entity.NoteEntity;
import com.haoyu.app.entity.NoteListResult;
import com.haoyu.app.lego.student.R;
import com.haoyu.app.rxBus.MessageEvent;
import com.haoyu.app.utils.Action;
import com.haoyu.app.utils.Constants;
import com.haoyu.app.utils.OkHttpClientManager;
import com.haoyu.app.view.LoadFailView;
import com.haoyu.app.view.LoadingView;
import com.haoyu.app.xrecyclerview.XRecyclerView;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Request;

/**
 * 笔记fragment
 *
 * @author xiaoma
 */
public class PageNoteFragment extends BaseFragment implements XRecyclerView.LoadingListener, LoadFailView.OnRetryListener {
    private PageNoteAdapter adapter; // 笔记适配器
    private RelativeLayout rl_select_node;   //选节点
    private Button bt_createNote; // 创建笔记按钮
    //    private LoadingDialog dialog;
    private LoadingView loadView;
    private LoadFailView loadFailView;
    //    private XListView xListView;
    private XRecyclerView xRecyclerView;
    private LinearLayout emptyNote; // 空笔记
    private boolean isLoadMore = false;
    private boolean isRefresh = false;
    private List<NoteEntity> notes = new ArrayList<NoteEntity>(); // 笔记集合
    private String orders = "CREATE_TIME.DESC"; // 按创建时间排序，最新创建排前
    private int page = 1; // 按页数查询
    private int clickPosition;
    private String entityId;

    @Override
    public void obBusEvent(MessageEvent event) {
        String action = event.getAction();
        if (action.equals(Action.ALTER_COURSE_NOTE)) {
            NoteEntity entity = (NoteEntity) event.obj;
            notes.remove(clickPosition);
            notes.add(clickPosition, entity);
            adapter.notifyDataSetChanged();
        } else if (action.equals(Action.CREATE_COURSE_NOTE)) {
            NoteEntity entity = (NoteEntity) event.obj;
            notes.add(0, entity);
            adapter.notifyDataSetChanged();
//            if (xListView.getVisibility() == View.GONE) {
//                xListView.setVisibility(View.VISIBLE);
//                emptyNote.setVisibility(View.GONE);
//            }
            if (xRecyclerView.getVisibility() == View.GONE) {
                xRecyclerView.setVisibility(View.VISIBLE);
                emptyNote.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int createView() {
        return R.layout.fragment_page_note;
    }

    @Override
    public void initView(View view) {
        Bundle bundle = getArguments();
        if (bundle != null) {
            entityId = bundle.getString("entityId");
        }
        view.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        rl_select_node = view.findViewById(R.id.rl_select_node);
        loadView = view.findViewById(R.id.loadView);
        loadFailView = view.findViewById(R.id.loadFailView);
//        xListView = (XListView) view.findViewById(R.id.xListView);
        xRecyclerView = view.findViewById(R.id.xRecyclerView);
        emptyNote = view.findViewById(R.id.li_emptyNote);
        bt_createNote = view.findViewById(R.id.bt_createNote);
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        xRecyclerView.setLayoutManager(layoutManager);
        adapter = new PageNoteAdapter(notes);
        xRecyclerView.setAdapter(adapter);
        xRecyclerView.setLoadingListener(this);
//        xListView.setAdapter(adapter);
//        xListView.setXListViewListener(this);
    }

    /**
     * 加载笔记列表
     */
    @Override
    public void initData() {
        String url = Constants.OUTRT_NET + "/notes" + "?page=" + page
                + "&orders=" + orders;
        OkHttpClientManager.getAsyn(context, url, new OkHttpClientManager.ResultCallback<NoteListResult>() {

            @Override
            public void onBefore(Request request) {
                super.onBefore(request);
                if (!isRefresh && !isLoadMore) {
                    loadView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(Request request, Exception e) {
                onResponseError(context.getResources().getString(R.string.load_fail_message));
            }

            @Override
            public void onResponse(NoteListResult response) {
                if (response == null) {
                    onResponseError(null);
                    return;
                }
                if (response.getResponseCode() != null && !response.getResponseCode().equals("00")) {
                    onResponseError(response.getResponseMsg());
                    return;
                }
                onUpdateUI(response.getResponseData());
            }
        });
    }

    private void onResponseError(String errorMsg) {
        if (isRefresh) {
//            xListView.stopRefresh(false);
            xRecyclerView.refreshComplete(false);
        } else if (isLoadMore) {
            page -= 1;
//            xListView.stopLoadMore();
            xRecyclerView.loadMoreComplete(false);
        } else {
            loadView.setVisibility(View.GONE);
            loadFailView.setVisibility(View.VISIBLE);
            if (errorMsg != null) {
                loadFailView.setErrorMsg(errorMsg);
            }
        }
    }

    private void onUpdateUI(List<NoteEntity> responseData) {
        loadView.setVisibility(View.GONE);
//        xListView.setVisibility(View.VISIBLE);
        xRecyclerView.setVisibility(View.VISIBLE);
        if (responseData != null && responseData.size() > 0) {
            loadFailView.setVisibility(View.GONE);
            emptyNote.setVisibility(View.GONE);
            if (isRefresh) {
//                xListView.stopRefresh(true);
                xRecyclerView.refreshComplete(true);
                notes.clear();
            } else if (isLoadMore) {
//                xListView.stopLoadMore();
                xRecyclerView.loadMoreComplete(true);
            }
            notes.addAll(responseData);
            adapter.notifyDataSetChanged();
            if (responseData.size() >= 10) {
                xRecyclerView.setLoadingMoreEnabled(true);
//                xListView.setPullLoadEnable(true);
            } else {
//                xListView.setPullLoadEnable(false);
                xRecyclerView.setLoadingMoreEnabled(false);
            }
        } else {
            if (isLoadMore) {
//                xListView.setPullLoadEnable(false);
                xRecyclerView.setLoadingMoreEnabled(false);
            } else {
                emptyNote.setVisibility(View.VISIBLE);
//                xListView.setVisibility(View.GONE);
                xRecyclerView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void setListener() {
        loadFailView.setOnRetryListener(this);
        rl_select_node.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        bt_createNote.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                startActivity(
                        new Intent(getActivity(),
                                CreateOrAlterNoteActivity.class));
            }
        });
        adapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseRecyclerAdapter adapter, BaseRecyclerAdapter.RecyclerHolder holder, View view, int position) {
                try {
                    clickPosition = position - 1;
                    Intent intent = new Intent();
                    intent.setClass(context,
                            CreateOrAlterNoteActivity.class);
                    intent.putExtra("isAlter", true);
                    intent.putExtra("note", notes.get(clickPosition));
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        adapter.setOnItemLongClickListener(new BaseRecyclerAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(BaseRecyclerAdapter adapter, BaseRecyclerAdapter.RecyclerHolder holder, View view, int position) {
                clickPosition = position - 1;
            }
        });
//        xListView.setOnItemClickListener(new OnItemClickListener() {
//            public void onItemClick(AdapterView<?> adapterView, View view,
//                                    int position, long id) {
//                try {
//                    clickPosition = position - 1;
//                    Intent intent = new Intent();
//                    intent.setClass(context,
//                            CreateOrAlterNoteActivity.class);
//                    intent.putExtra("isAlter", true);
//                    intent.putExtra("note", (Serializable) notes.get(position - 1));
//                    startActivity(intent);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//
//        xListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//            @Override
//            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {
//                clickPosition = position;
//                showDisposeDialog(position);
//                return true;
//            }
//        });
    }

//    /* 删除某条笔记记录 */
//    private void deletePosition(final int position) {
//        String url = Constants.OUTRT_NET + "notes/" + notes.get(position).getId();
//        OkHttpClientManager.deleteAsyn(context, url, new OkHttpClientManager.ResultCallback<BaseResponseResult>() {
//            public void onError(Request request, Exception exception) {
//              onNetWorkError();
//            }
//
//            public void onResponse(BaseResponseResult response) {
//                if (response == null) {
//                    return;
//                }
//                if (response.getResponseCode() != null && response.getResponseCode().equals("00")) {
//                    notes.remove(position);
//                    adapter.notifyDataSetChanged();
//                    if (notes.size() == 0) {
//                        emptyNote.setVisibility(View.VISIBLE);
////                                xListView.setVisibility(View.GONE);
//                        xRecyclerView.setVisibility(View.GONE);
//                    }
//                } else {
//                    if (response.getResponseMsg() != null) {
//                        toast(response.getResponseMsg());
//                    }
//                }
//            }
//        }, null);
//    }

    @Override
    public void onRetry(View v) {
        initData();
    }

    @Override
    public void onRefresh() {
        isRefresh = true;
        isLoadMore = false;
        page = 1;
        initData();
    }

    @Override
    public void onLoadMore() {
        isRefresh = false;
        isLoadMore = true;
        page += 1;
        initData();
    }

}
