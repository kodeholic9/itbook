package com.kodeholic.itbook.common.data;


public class SearchResult {
    private String url;
    private String jsonResult; //json format
    private long   searchTime;

    public SearchResult() {}
    public SearchResult(String url, String jsonResult) {
        this.url = url;
        this.jsonResult = jsonResult;
        this.searchTime = System.currentTimeMillis();
    }

    public int length() {
        int ln = ((url != null) ? url.length() : 0)
               + ((jsonResult != null) ? jsonResult.length() : 0);
        return ln;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getJsonResult() {
        return jsonResult;
    }

    public void setJsonResult(String jsonResult) {
        this.jsonResult = jsonResult;
    }

    public long getSearchTime() {
        return searchTime;
    }

    public void setSearchTime(long searchTime) {
        this.searchTime = searchTime;
    }

    @Override
    public String toString() {
        return "SearchResult{" +
                "url='" + url + '\'' +
                ", jsonResult='" + jsonResult + '\'' +
                ", searchTime=" + searchTime +
                '}';
    }
}
