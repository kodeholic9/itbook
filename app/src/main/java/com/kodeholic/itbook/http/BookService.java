package com.kodeholic.itbook.http;

import com.kodeholic.itbook.common.data.BookDetailRes;
import com.kodeholic.itbook.common.data.BookListRes;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface BookService {
    //public static final String URL_NEW_LIST= "https://api.itbook.store/1.0/new";
    //public static final String URL_SEARCH  = "https://api.itbook.store/1.0/search"; // + /{query}/{page}
    //public static final String URL_DETAIL  = "https://api.itbook.store/1.0/books";  // + /{isbn13}

    @GET("new")
    Call<BookListRes> getNewList();

    @GET("search/{query}/{page}")
    Call<BookListRes> getSearch(
            @Path("query") String query,
            @Path("page") int page
    );

    @GET("books/{isbn13}")
    Call<BookDetailRes> getDetail(
            @Path("isbn13") String isbn13
    );

}
