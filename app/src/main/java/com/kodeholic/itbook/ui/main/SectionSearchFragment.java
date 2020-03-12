package com.kodeholic.itbook.ui.main;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kodeholic.itbook.R;
import com.kodeholic.itbook.common.BookManager;
import com.kodeholic.itbook.common.MyCacheManager;
import com.kodeholic.itbook.common.MyIntent;
import com.kodeholic.itbook.common.PopupManager;
import com.kodeholic.itbook.common.Utils;
import com.kodeholic.itbook.common.data.Book;
import com.kodeholic.itbook.common.data.BookDetail;
import com.kodeholic.itbook.common.data.BookListRes;
import com.kodeholic.itbook.lib.http.HttpResponse;
import com.kodeholic.itbook.lib.util.Log;
import com.kodeholic.itbook.ui.base.BookItemViewHolder;
import com.kodeholic.itbook.ui.base.LoadingViewHolder;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class SectionSearchFragment extends SectionFragment {
    public static final String TAG = SectionSearchFragment.class.getSimpleName();

    private SearchAdapter mAdapter;
    private RecyclerView  mListView;
    private EditText      ed_input;
    private TextView      tv_no_result;

    public static SectionSearchFragment newInstance(SectionsPagerAdapter.TabInfo info) {
        Log.d(TAG, "newInstance() - " + info.index);
        SectionSearchFragment fragment = new SectionSearchFragment();
        fragment.setSectionInfo(info);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView()");

        View root = inflater.inflate(R.layout.fragment_search, container, false);

        //adapter
        mAdapter = new SearchAdapter(BookManager.getInstance(mContext).toSearchResultToArray());

        //list..
        mListView = root.findViewById(R.id.ll_list);
        mListView.setAdapter(mAdapter);
        mListView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

//                int lastVisibleItemPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition();
//                int itemTotalCount = recyclerView.getAdapter().getItemCount() - 1;
//                if (lastVisibleItemPosition == itemTotalCount) {
//                    Log.d(TAG, "last Position...");
//                    more();
//                }
            }

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                boolean isTop    = !recyclerView.canScrollVertically(-1); //위로 스크롤이 불가한 경우,
                boolean isBottom = !recyclerView.canScrollVertically( 1); //아래로 스크롤이 불가한 경우,
                boolean hasMoreToSearch  = BookManager.getInstance(mContext).hasMoreToSearch();
                Log.d(TAG, "onScrollStateChanged() - newState: " + newState
                        + ", isTop: " + isTop
                        + ", isBottom: " + isBottom
                        + ", hasMoreToSearch: " + hasMoreToSearch
                );

                //스크롤이 정지되었을 때 처리
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    if (isBottom && hasMoreToSearch) {
                        search(false);
                    }
                    updateView("onScrollStateChanged");
                }

                return;
            }
        });

        //no result
        tv_no_result = root.findViewById(R.id.tv_no_result);

        //edit text
        ed_input = root.findViewById(R.id.ed_input);
        ed_input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                Log.d(TAG, "onEditorAction() - actionId: " + actionId);
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    search(true);
                }
                return false;
            }
        });

        updateView("onCreateView");

        return root;
    }

    @Override
    public void onStop() {
        super.onStop();
        clearInput(true, "onStop");
    }

    @Override
    public void onEvent(int event, Object o) {
        Log.d(TAG, "onEvent() - event: " + event);
        if (event == MyIntent.Event.SEARCH_DEL_INPUT) {
            clearInput(true, "SEARCH_DEL_INPUT");
        }
        if (event == MyIntent.Event.SEARCH_REFRESHED) {
            updateView("SEARCH_REFRESHED");
        }
    }

    /**
     * Input을 clear한다.
     * @param f
     */
    private void clearInput(boolean keyboardOnly, String f) {
        if (ed_input.hasFocus()) {
            Utils.hideSoftInput(mContext, ed_input, f);
            ed_input.clearFocus();

            if (!keyboardOnly) {
                ed_input.setText("");
                BookManager.getInstance(mContext).clearSearchResult();
                updateView("clearInput");
            }
        }
    }

    private void updateView(String f) {
        Log.d(TAG, "updateView() - f: " + f);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                updateView();
            }
        });
    }

    private void updateView() {
        Book[] results = BookManager.getInstance(mContext).toSearchResultToArray();
        if (results == null) {
            Log.e(TAG, "updateView() - the results is null");
            return;
        }
        Log.d(TAG, "updateView() - results.length: " + results.length + ", tv_no_result: " + tv_no_result);

        //리스트를 갱신한다.
        mAdapter.setData(results);
        mAdapter.notifyDataSetChanged();

        //...
        if (results.length == 0) {
            tv_no_result.setVisibility(View.VISIBLE);
            mListView.setVisibility(View.GONE);
        }
        else {
            tv_no_result.setVisibility(View.GONE);
            mListView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Search를 조회한다.
     * @param initFlag
     */
    private void search(final boolean initFlag) {
        String s = ed_input.getText().toString();
        if (initFlag && TextUtils.isEmpty(s)) {
            PopupManager.getInstance(mContext).showToast("Empty queryString!");
            return;
        }

        if (initFlag) {
            showLoading("search");
            BookManager.getInstance(mContext).clearSearchResult();
        }
        BookManager.getInstance(mContext).loadSearch(s, new BookManager.Listener() {
            @Override
            public void onResponse(HttpResponse response) {
                if (initFlag) { hideLoading("search"); }
            }
        }, TAG);
    }

    public class SearchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int VIEW_TYPE_ITEM = 0;
        private static final int VIEW_TYPE_LOAD = 1;

        private Book[] data;
        private boolean detailStarting = false;

        public SearchAdapter(Book[] data) {
            this.data = data;
        }

        public void setData(Book[] data) {
            this.data = data;
        }

        @Override
        public int getItemViewType(int position) {
            // loader can't be at position 0
            // loader can only be at the last position
            if (position != 0 && position == getItemCount() - 1) {
                return VIEW_TYPE_LOAD;
            }

            return VIEW_TYPE_ITEM;
        }

        @Override
        public int getItemCount() {
            if (data == null || data.length == 0) {
                return 0;
            }

            //for loading view....
            return data.length + 1;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_LOAD) {
                View loadingView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_footer_loading, parent, false);
                return new LoadingViewHolder(loadingView);
            }
            else {
                View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_book, parent, false);
                return new BookItemViewHolder(itemView);
            }
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
            //로딩
            if (holder instanceof LoadingViewHolder) {
                onBindLoadingViewHolder((LoadingViewHolder)holder, position);
            }
            //아이템
            else {
                onBindBookItemViewHolder((BookItemViewHolder)holder, position);
            }
        }

        protected void onBindLoadingViewHolder(final LoadingViewHolder holder, final int position) {
            boolean requesting = BookManager.getInstance(mContext).isSearchRequesting();
            Log.d(TAG, "onBindLoadingViewHolder() - position: " + position + ", requesting: " + requesting);
            if (requesting) {
                holder.showLoading();
            }
            else {
                holder.hideLoading();
            }
        }

        /**
         * 아이템을 바인딩한다.
         * @param holder
         * @param position
         */
        protected void onBindBookItemViewHolder(final BookItemViewHolder holder, final int position) {
            final Book item = data[position];
            holder.tv_title.setText(item.getTitle());
            holder.tv_subtitle.setText(item.getSubTitle());
            holder.tv_isbn13.setText("(" + item.getIsbn13() + ")");
            holder.tv_price.setText(item.getPrice());
            //이미지를 view에 붙인다.
            MyCacheManager.getInstance(mContext).loadBitmap(
                    item.getImage(),
                    holder.iv_image,
                    TAG);
            holder.ll_link.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "onClick() - view: " + v + ", position: " + position + ", " + item);
                    MyIntent.goToURL(getContext(), item.getUrl());
                }
            });
            holder.rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "onClick() - view: " + v + ", position: " + position + ", " + item);

                    //반복 호출되는 상황 예외처리...
                    synchronized (this) {
                        if (detailStarting) {
                            Log.d(TAG, "already detailStarting!!");
                            return;
                        }
                        detailStarting = true;
                    }

                    //detail 조회후, 화면으로 이동한다.
                    showLoading("onClick");
                    BookManager.getInstance(mContext).loadDetail(item.getIsbn13(), new BookManager.DetailListener() {
                        @Override
                        public void onResult(BookDetail result) {
                            synchronized (this) {
                                detailStarting = false;
                            }
                            hideLoading("onClick");
                            if (result == null) {
                                PopupManager.getInstance(mContext).showToast("Failed to load detail!");
                                return;
                            }
                            MyIntent.startDetailActivity(mContext, MyIntent.Event.DETAIL_REFRESHED, result, TAG);
                        }
                    }, TAG);
                }
            });
        }
    }
}