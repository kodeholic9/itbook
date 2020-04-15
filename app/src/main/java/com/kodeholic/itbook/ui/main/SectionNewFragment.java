package com.kodeholic.itbook.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.kodeholic.itbook.R;
import com.kodeholic.itbook.common.BookManager;
import com.kodeholic.itbook.common.BitmapCacheManager;
import com.kodeholic.itbook.common.MyIntent;
import com.kodeholic.itbook.common.PopupManager;
import com.kodeholic.itbook.common.data.Book;
import com.kodeholic.itbook.common.data.BookDetail;
import com.kodeholic.itbook.databinding.FragmentNewListBinding;
import com.kodeholic.itbook.databinding.ListItemBookBinding;
import com.kodeholic.itbook.lib.http.HttpResponse;
import com.kodeholic.itbook.lib.util.Log;
import com.kodeholic.itbook.ui.base.BookItemViewHolder;
import com.kodeholic.itbook.ui.viewmodel.BookViewModel;


/**
 * A placeholder fragment containing a simple view.
 */
public class SectionNewFragment extends SectionFragment {
    public static final String TAG = SectionNewFragment.class.getSimpleName();

    private FragmentNewListBinding mBinding;
    private BookViewModel mBookViewModel;

    private NewListAdapter mAdapter;

    public static SectionNewFragment newInstance(SectionsPagerAdapter.TabInfo info) {
        Log.d(TAG, "newInstance() - " + info.index);
        SectionNewFragment fragment = new SectionNewFragment();
        fragment.setSectionInfo(info);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView()");

        //ViewModel
        mBookViewModel = ViewModelProviders.of(this).get(BookViewModel.class);
        mBookViewModel.getBookList().observe(this, new Observer<Book[]>() {
            @Override
            public void onChanged(Book[] books) {
                updateView(books,"observe()");
            }
        });

        //databinding
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_new_list, container, false);
        mBinding.setLifecycleOwner(this);

        //adapter
        mAdapter = new NewListAdapter(BookManager.getInstance(mContext).getNewResultToArray());

        //list..
        //mListView = mBinding.llList; //root.findViewById(R.id.ll_list);
        mBinding.llList.setHasFixedSize(true);
        mBinding.llList.setAdapter(mAdapter);

        //mPullToRefresh = mBinding.llRefresh; //root.findViewById(R.id.ll_refresh);
        mBinding.llRefresh.setOnRefreshListener(mRefreshListener);
        mBinding.llRefresh.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);

        return mBinding.getRoot();
    }

    private void updateView(Book[] books, String f) {
        Log.d(TAG, "updateView() - f: " + f);
        mAdapter.setData(books);
        mAdapter.notifyDataSetChanged();

        if (mBinding.llRefresh.isRefreshing()) {
            mBinding.llRefresh.setRefreshing(false);
        }
    }

    @Override
    public void onEvent(int event, Object o) {
        Log.d(TAG, "onEvent() - event: " + event);
    }

    private SwipeRefreshLayout.OnRefreshListener mRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            mBookViewModel.loadNewList(mContext);
        }
    };

    public class NewListAdapter extends RecyclerView.Adapter<BookItemViewHolder> {
        private Book[] data;
        private boolean detailStarting = false;

        public NewListAdapter(Book[] data) {
            this.data = data;
        }
        public void setData(Book[] data) {
            this.data = data;
        }

        @Override
        public BookItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            ListItemBookBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()), R.layout.list_item_book, parent, false
            );
            return new BookItemViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(final BookItemViewHolder holder, final int position) {
            final Book item = data[position];

            holder.binding.setBook(item);
            holder.binding.llLink.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "onClick() - view: " + v + ", position: " + position + ", " + item);
                    mBookViewModel.goToUrl(mContext, item.getUrl());
                }
            });
            holder.binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "onClick() - view: " + v + ", position: " + position + ", " + item);
                    mBookViewModel.loadAndGoDetail(mContext, item);
                }
            });
        }

        @Override
        public int getItemCount() {
            return data.length;
        }
    }
}