package com.nownews.mobile.Json;

import java.util.List;

public class RelationsNewsInfoJson {

    private List<RelationsNewsBean> relationsNews;

    public List<RelationsNewsBean> getRelationsNews() {
        return relationsNews;
    }

    public void setRelationsNews(List<RelationsNewsBean> relationsNews) {
        this.relationsNews = relationsNews;
    }

    public static class RelationsNewsBean {
        /**
         * _id : 591583fefbe08771e32947a7
         * sn : 32
         * title : 賴雅妍帥氣短髮現身英倫精品　母親節送媽越來越「大包」
         * type : NEWS
         * startedAt : 2017-05-12T09:55:51.832Z
         * MainPhoto : {"_id":"591583d1fbe08771e32947a1","sn":48,"updatedAt":"2017-05-12T09:43:47.167Z","createdAt":"2017-05-12T09:43:47.167Z","url":"http://img.nownews.com/nownews_staging/images/591583d1fbe08771e32947a1-201705121743.jpg","CreatedBy":"5915646fc634cc5cc9f1706b","UpdatedBy":"5915646fc634cc5cc9f1706b","isTrashed":false,"Tag":null,"isDeliver":true,"height":960,"width":640,"mimetype":"image/jpeg","mode":"NORMAl","type":"NEWS","format":"jpg","originalname":"下載 (9).jpg","imageFrom":"INTERNAL","keyword":null,"desc":"▲女星賴雅妍以一頭俐落短髮及中性風格現身。（圖／BURBERRY提供）","title":"▲女星賴雅妍以一頭俐落短髮及中性風格現身。（圖／BURBERRY提供）","formatUpdatedAt":"2017-05-12 17:43:47","formatCreatedAt":"2017-05-12 17:43:47","thumbnail":"https://imgapiv2.nownews.com/?w=300&q=70&src=http%3A%2F%2Fimg.nownews.com%2Fnownews_staging%2Fimages%2F591583d1fbe08771e32947a1-201705121743.jpg","id":"591583d1fbe08771e32947a1"}
         * MainMenu : {"_id":"59156debfbe08771e3294689","sn":16,"updatedAt":"2017-06-26T05:31:54.480Z","createdAt":"2017-05-12T08:10:19.384Z","name":"消費","url":"/cat/fashion","CreatedBy":"5915646fc634cc5cc9f1706b","UpdatedBy":"5915646fc634cc5cc9f17066","isTrashed":false,"status":"OPEN","isPermanented":true,"endedAt":null,"startedAt":null,"weight":8,"level":0,"ParentId":null,"hasChild":true,"isAdult":false,"isExternal":false,"categoryName":"fashion","formatUpdatedAt":"2017-06-26 13:31:54","formatCreatedAt":"2017-05-12 16:10:19","id":"59156debfbe08771e3294689"}
         * shortTitle : 賴雅妍母親節送媽咪越來越「大包」
         * formatStartedAt : 2017-05-12 17:55:51
         * formatUpdatedAt : 2017-06-27 00:28:19
         * formatCreatedAt : 2017-06-27 00:28:19
         * parseUrl : /news/20170627/32
         * id : 591583fefbe08771e32947a7
         */

        private String _id;
        private int sn;
        private String title;
        private String type;
        private String startedAt;
        private MainPhotoBean MainPhoto;
        private MainMenuBean MainMenu;
        private String shortTitle;
        private String formatCreatedAt;
        private String id;

        public String get_id() {
            return _id;
        }

        public void set_id(String _id) {
            this._id = _id;
        }

        public int getSn() {
            return sn;
        }

        public void setSn(int sn) {
            this.sn = sn;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public void setStartedAt(String startedAt) {
            this.startedAt = startedAt;
        }

        public MainPhotoBean getMainPhoto() {
            return MainPhoto;
        }

        public void setMainPhoto(MainPhotoBean MainPhoto) {
            this.MainPhoto = MainPhoto;
        }

        public MainMenuBean getMainMenu() {
            return MainMenu;
        }

        public void setMainMenu(MainMenuBean MainMenu) {
            this.MainMenu = MainMenu;
        }

        public String getShortTitle() {
            return shortTitle;
        }

        public void setShortTitle(String shortTitle) {
            this.shortTitle = shortTitle;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public static class MainPhotoBean {
            /**
             * _id : 591583d1fbe08771e32947a1
             * sn : 48
             * updatedAt : 2017-05-12T09:43:47.167Z
             * createdAt : 2017-05-12T09:43:47.167Z
             * url : http://img.nownews.com/nownews_staging/images/591583d1fbe08771e32947a1-201705121743.jpg
             * CreatedBy : 5915646fc634cc5cc9f1706b
             * UpdatedBy : 5915646fc634cc5cc9f1706b
             * isTrashed : false
             * Tag : null
             * isDeliver : true
             * height : 960
             * width : 640
             * mimetype : image/jpeg
             * mode : NORMAl
             * type : NEWS
             * format : jpg
             * originalname : 下載 (9).jpg
             * imageFrom : INTERNAL
             * keyword : null
             * desc : ▲女星賴雅妍以一頭俐落短髮及中性風格現身。（圖／BURBERRY提供）
             * title : ▲女星賴雅妍以一頭俐落短髮及中性風格現身。（圖／BURBERRY提供）
             * formatUpdatedAt : 2017-05-12 17:43:47
             * formatCreatedAt : 2017-05-12 17:43:47
             * thumbnail : https://imgapiv2.nownews.com/?w=300&q=70&src=http%3A%2F%2Fimg.nownews.com%2Fnownews_staging%2Fimages%2F591583d1fbe08771e32947a1-201705121743.jpg
             * id : 591583d1fbe08771e32947a1
             */

            private String _id;
            private int sn;
            private String createdAt;
            private String url;
            private Object Tag;
            private int height;
            private int width;
            private String type;
            private String format;
            private String title;
            private String thumbnail;
            private String id;

            public String get_id() {
                return _id;
            }

            public void set_id(String _id) {
                this._id = _id;
            }

            public int getSn() {
                return sn;
            }

            public void setSn(int sn) {
                this.sn = sn;
            }

            public String getCreatedAt() {
                return createdAt;
            }

            public void setCreatedAt(String createdAt) {
                this.createdAt = createdAt;
            }

            public String getUrl() {
                return url;
            }

            public void setUrl(String url) {
                this.url = url;
            }

            public Object getTag() {
                return Tag;
            }

            public void setTag(Object Tag) {
                this.Tag = Tag;
            }

            public int getHeight() {
                return height;
            }

            public void setHeight(int height) {
                this.height = height;
            }

            public int getWidth() {
                return width;
            }

            public void setWidth(int width) {
                this.width = width;
            }

            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }

            public String getFormat() {
                return format;
            }

            public void setFormat(String format) {
                this.format = format;
            }

            public String getTitle() {
                return title;
            }

            public void setTitle(String title) {
                this.title = title;
            }

            public String getThumbnail() {
                return thumbnail;
            }

            public void setThumbnail(String thumbnail) {
                this.thumbnail = thumbnail;
            }

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }
        }

        public static class MainMenuBean {
            /**
             * _id : 59156debfbe08771e3294689
             * sn : 16
             * updatedAt : 2017-06-26T05:31:54.480Z
             * createdAt : 2017-05-12T08:10:19.384Z
             * name : 消費
             * url : /cat/fashion
             * CreatedBy : 5915646fc634cc5cc9f1706b
             * UpdatedBy : 5915646fc634cc5cc9f17066
             * isTrashed : false
             * status : OPEN
             * isPermanented : true
             * endedAt : null
             * startedAt : null
             * weight : 8
             * level : 0
             * ParentId : null
             * hasChild : true
             * isAdult : false
             * isExternal : false
             * categoryName : fashion
             * formatUpdatedAt : 2017-06-26 13:31:54
             * formatCreatedAt : 2017-05-12 16:10:19
             * id : 59156debfbe08771e3294689
             */

            private String _id;
            private int sn;
            private String createdAt;
            private String name;
            private String url;
            private String status;
            private String categoryName;
            private String id;

            public String get_id() {
                return _id;
            }

            public void set_id(String _id) {
                this._id = _id;
            }

            public int getSn() {
                return sn;
            }

            public void setSn(int sn) {
                this.sn = sn;
            }

            public String getCreatedAt() {
                return createdAt;
            }

            public void setCreatedAt(String createdAt) {
                this.createdAt = createdAt;
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

            public String getStatus() {
                return status;
            }

            public void setStatus(String status) {
                this.status = status;
            }

            public String getCategoryName() {
                return categoryName;
            }

            public void setCategoryName(String categoryName) {
                this.categoryName = categoryName;
            }

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }
        }
    }
}
