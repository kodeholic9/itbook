package com.kodeholic.itbook.ui.viewmodel;

import android.content.Context;

import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.kodeholic.itbook.common.BookManager;
import com.kodeholic.itbook.common.MyIntent;
import com.kodeholic.itbook.common.PopupManager;
import com.kodeholic.itbook.common.data.Book;
import com.kodeholic.itbook.common.data.BookDetail;
import com.kodeholic.itbook.lib.http.HttpResponse;
import com.kodeholic.itbook.lib.util.Log;
import com.kodeholic.itbook.ui.base.IBase;

public class BookViewModel extends ViewModel {
    public static final String TAG = BookViewModel.class.getSimpleName();

    private MutableLiveData<Book[]> _bookList = new MutableLiveData<>();
    private LiveData<Book[]> bookList = Transformations.map(_bookList, new Function<Book[], Book[]>() {
        @Override
        public Book[] apply(Book[] input) {
            return input;
        }
    });

    public LiveData<Book[]> getBookList() {
        return bookList;
    }

    public void loadNewList(final Context context) {
        BookManager.getInstance(context).loadNewList(new BookManager.Listener() {
            @Override
            public void onResponse(HttpResponse response) {
                _bookList.setValue(BookManager.getInstance(context).getNewResultToArray());
            }
        }, TAG);
    }


    private boolean detailStarting = false;
    public void loadAndGoDetail(Context context, Book item) {
        //반복 호출되는 상황 예외처리...
        synchronized (this) {
            if (detailStarting) {
                Log.d(TAG, "already detailStarting!!");
                return;
            }
            detailStarting = true;
        }

        //detail 조회후, 화면으로 이동한다.
        //base.showLoading();
        BookManager.getInstance(context).loadDetail(item.getIsbn13(), new BookManager.DetailListener() {
            @Override
            public void onResult(BookDetail result) {
                //hideLoading("onClick");
                synchronized (this) {
                    detailStarting = false;
                }
                if (result == null) {
                    PopupManager.getInstance(context).showToast("Failed to load detail!");
                    return;
                }
                MyIntent.startDetailActivity(context, MyIntent.Event.DETAIL_REFRESHED, result.getIsbn13(), TAG);
            }
        }, TAG);
    }

    public void goToUrl(Context context, String url) {
        MyIntent.goToURL(context, url);
    }
}
