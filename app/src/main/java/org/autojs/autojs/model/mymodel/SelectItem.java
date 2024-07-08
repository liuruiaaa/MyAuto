package org.autojs.autojs.model.mymodel;

import com.google.gson.annotations.SerializedName;

public class SelectItem {
    @SerializedName("url")
    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @SerializedName("filename")
    private String filename;

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
    // 带参构造函数
    public SelectItem(String url, String filename) {
        this.url = url;
        this.filename = filename;
    }
}

