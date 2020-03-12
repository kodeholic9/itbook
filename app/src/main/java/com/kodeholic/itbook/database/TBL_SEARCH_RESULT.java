package com.kodeholic.itbook.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.kodeholic.itbook.common.data.BookDetail;
import com.kodeholic.itbook.common.data.Bookmark;
import com.kodeholic.itbook.common.data.SearchResult;
import com.kodeholic.itbook.lib.util.Log;

import java.util.ArrayList;
import java.util.List;

public class TBL_SEARCH_RESULT extends TABLE<SearchResult> {
	public static final String TABLE_NAME = "tbl_search_result";

	public static final String URL         = "_id";
	public static final String JSON_RESULT = "_json_result";
	public static final String SEARCH_TIME = "_search_time";

	public static final String CREATE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " " +
			"(" +
			URL         + " TEXT NOT NULL UNIQUE PRIMARY KEY," +
			JSON_RESULT + " TEXT NOT NULL," +
			SEARCH_TIME + " INTEGER NOT NULL DEFAULT CURRENT_TIMESTAMP" +
			");";

	public static final String DROP = "DROP TABLE IF EXISTS " + TABLE_NAME;

	private static class INDEX {
		public int URL, JSON_RESULT, SEARCH_TIME;
	}

	private static INDEX cursorToIndex(Cursor c) throws Exception {
		INDEX idx = new INDEX();
		idx.URL         = c.getColumnIndex(URL);
		idx.JSON_RESULT = c.getColumnIndex(JSON_RESULT);
		idx.SEARCH_TIME = c.getColumnIndex(SEARCH_TIME);

		return idx;
	}

	public TBL_SEARCH_RESULT(SQLiteDatabase db) {
		super(db, TABLE_NAME);
	}

	@Override
	public ContentValues fetchObject2Values(SearchResult o) {
		ContentValues values = new ContentValues();
		values.put(URL        , o.getUrl());
		values.put(JSON_RESULT, o.getJsonResult());
		values.put(SEARCH_TIME, o.getSearchTime());

		return values;
	}

	@Override
	public List<SearchResult> fetchCursor2List(Cursor c) throws Exception {
		List<SearchResult> list = new ArrayList<SearchResult>();
		try {
			if (c.moveToFirst()) {
				INDEX idx = cursorToIndex(c);
				do {
					list.add(fetchCursor2Object(idx, c));
				}
				while (c.moveToNext());
			}
		}
		catch (Exception e){
			e.printStackTrace();
		}
		return list;
	}

	public SearchResult fetchCursor2Object(INDEX idx, Cursor c) throws Exception {
		SearchResult o = new SearchResult();
		if (idx.URL         != -1) o.setUrl(c.getString(idx.URL));
		if (idx.JSON_RESULT != -1) o.setJsonResult(c.getString(idx.JSON_RESULT));
		if (idx.SEARCH_TIME != -1) o.setSearchTime(c.getInt(idx.SEARCH_TIME));

		return o;
	}

	public SearchResult getSearchResult(String url, String f) {
		Log.d(TABLE_NAME, "getSearchResult() - f: " + f + ", url: " + url);
		String where =  URL + "='" + url + "'";

		return select1(null, where, null);
	}

	public boolean addSearchResult(SearchResult result, String f) {
		Log.d(TABLE_NAME, "addSearchResult() - f: " + f);
		String where =  URL + "='" + result.getUrl() + "'";
		int count = getCount(where, null);
		if (count > 0) {
			//이미 테이블에 존재하는 경우...
			return true;
		}

		Log.d(TABLE_NAME, "addSearchResult() - insert!");
		return insert(result) > 0;
	}
}
