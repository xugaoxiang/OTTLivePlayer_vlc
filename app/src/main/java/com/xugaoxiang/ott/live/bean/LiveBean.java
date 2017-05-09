package com.xugaoxiang.ott.live.bean;

import java.util.List;

/**
 * Created by user on 2016/9/30.
 */
public class LiveBean {

    /**
     * type : null
     * num : 1
     * name : 亚太第一卫视
     * url : rtmp:/1.one-tv.com/live/mpegts.stream
     */

    private List<DataBean> data;

    public List<DataBean> getData() {
        return data;
    }

    public void setData(List<DataBean> data) {
        this.data = data;
    }

    public static class DataBean {
        private Object type;
        private String num;
        private String name;
        private String url;

        public Object getType() {
            return type;
        }

        public void setType(Object type) {
            this.type = type;
        }

        public String getNum() {
            return num;
        }

        public void setNum(String num) {
            this.num = num;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }
}
