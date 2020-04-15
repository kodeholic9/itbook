package com.kodeholic.itbook.ui.viewmodel;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.kodeholic.itbook.common.BookManager;
import com.kodeholic.itbook.common.data.Book;
import com.kodeholic.itbook.lib.http.HttpResponse;

public class BookViewModel extends ViewModel {
    public static final String TAG = BookViewModel.class.getSimpleName();

    private MutableLiveData<Book[]> bookList = new MutableLiveData<>();

    public LiveData<Book[]> getBookList() {
        return bookList;
    }

    public void loadNewList(final Context context) {
        BookManager.getInstance(context).loadNewList(new BookManager.Listener() {
            @Override
            public void onResponse(HttpResponse response) {
                bookList.setValue(BookManager.getInstance(context).getNewResultToArray());
            }
        }, TAG);
    }
}
