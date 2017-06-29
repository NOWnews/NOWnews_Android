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
        private String formatStartedAt;
        private String formatUpdatedAt;
        private String formatCreatedAt;
        private String parseUrl;
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

        public String getStartedAt() {
            return startedAt;
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

        public String getFormatStartedAt() {
            return formatStartedAt;
        }

        public void setFormatStartedAt(String formatStartedAt) {
            this.formatStartedAt = formatStartedAt;
        }

        public String getFormatUpdatedAt() {
            return formatUpdatedAt;
        }

        public void setFormatUpdatedAt(String formatUpdatedAt) {
            this.formatUpdatedAt = formatUpdatedAt;
        }

        public String getFormatCreatedAt() {
            return formatCreatedAt;
        }

        public void setFormatCreatedAt(String formatCreatedAt) {
            this.formatCreatedAt = formatCreatedAt;
        }

        public String getParseUrl() {
            return parseUrl;
        }

        public void setParseUrl(String parseUrl) {
            this.parseUrl = parseUrl;
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
            private String updatedAt;
            private String createdAt;
            private String url;
            private String CreatedBy;
            private String UpdatedBy;
            private boolean isTrashed;
            private Object Tag;
            private boolean isDeliver;
            private int height;
            private int width;
            private String mimetype;
            private String mode;
            private String type;
            private String format;
            private String originalname;
            private String imageFrom;
            private Object keyword;
            private String desc;
            private String title;
            private String formatUpdatedAt;
            private String formatCreatedAt;
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

            public String getUpdatedAt() {
                return updatedAt;
            }

            public void setUpdatedAt(String updatedAt) {
                this.updatedAt = updatedAt;
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

            public String getCreatedBy() {
                return CreatedBy;
            }

            public void setCreatedBy(String CreatedBy) {
                this.CreatedBy = CreatedBy;
            }

            public String getUpdatedBy() {
                return UpdatedBy;
            }

            public void setUpdatedBy(String UpdatedBy) {
                this.UpdatedBy = UpdatedBy;
            }

            public boolean isIsTrashed() {
                return isTrashed;
            }

            public void setIsTrashed(boolean isTrashed) {
                this.isTrashed = isTrashed;
            }

            public Object getTag() {
                return Tag;
            }

            public void setTag(Object Tag) {
                this.Tag = Tag;
            }

            public boolean isIsDeliver() {
                return isDeliver;
            }

            public void setIsDeliver(boolean isDeliver) {
                this.isDeliver = isDeliver;
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

            public String getMimetype() {
                return mimetype;
            }

            public void setMimetype(String mimetype) {
                this.mimetype = mimetype;
            }

            public String getMode() {
                return mode;
            }

            public void setMode(String mode) {
                this.mode = mode;
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

            public String getOriginalname() {
                return originalname;
            }

            public void setOriginalname(String originalname) {
                this.originalname = originalname;
            }

            public String getImageFrom() {
                return imageFrom;
            }

            public void setImageFrom(String imageFrom) {
                this.imageFrom = imageFrom;
            }

            public Object getKeyword() {
                return keyword;
            }

            public void setKeyword(Object keyword) {
                this.keyword = keyword;
            }

            public String getDesc() {
                return desc;
            }

            public void setDesc(String desc) {
                this.desc = desc;
            }

            public String getTitle() {
                return title;
            }

            public void setTitle(String title) {
                this.title = title;
            }

            public String getFormatUpdatedAt() {
                return formatUpdatedAt;
            }

            public void setFormatUpdatedAt(String formatUpdatedAt) {
                this.formatUpdatedAt = formatUpdatedAt;
            }

            public String getFormatCreatedAt() {
                return formatCreatedAt;
            }

            public void setFormatCreatedAt(String formatCreatedAt) {
                this.formatCreatedAt = formatCreatedAt;
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
            private String updatedAt;
            private String createdAt;
            private String name;
            private String url;
            private String CreatedBy;
            private String UpdatedBy;
            private boolean isTrashed;
            private String status;
            private boolean isPermanented;
            private Object endedAt;
            private Object startedAt;
            private int weight;
            private int level;
            private Object ParentId;
            private boolean hasChild;
            private boolean isAdult;
            private boolean isExternal;
            private String categoryName;
            private String formatUpdatedAt;
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

            public String getUpdatedAt() {
                return updatedAt;
            }

            public void setUpdatedAt(String updatedAt) {
                this.updatedAt = updatedAt;
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

            public String getCreatedBy() {
                return CreatedBy;
            }

            public void setCreatedBy(String CreatedBy) {
                this.CreatedBy = CreatedBy;
            }

            public String getUpdatedBy() {
                return UpdatedBy;
            }

            public void setUpdatedBy(String UpdatedBy) {
                this.UpdatedBy = UpdatedBy;
            }

            public boolean isIsTrashed() {
                return isTrashed;
            }

            public void setIsTrashed(boolean isTrashed) {
                this.isTrashed = isTrashed;
            }

            public String getStatus() {
                return status;
            }

            public void setStatus(String status) {
                this.status = status;
            }

            public boolean isIsPermanented() {
                return isPermanented;
            }

            public void setIsPermanented(boolean isPermanented) {
                this.isPermanented = isPermanented;
            }

            public Object getEndedAt() {
                return endedAt;
            }

            public void setEndedAt(Object endedAt) {
                this.endedAt = endedAt;
            }

            public Object getStartedAt() {
                return startedAt;
            }

            public void setStartedAt(Object startedAt) {
                this.startedAt = startedAt;
            }

            public int getWeight() {
                return weight;
            }

            public void setWeight(int weight) {
                this.weight = weight;
            }

            public int getLevel() {
                return level;
            }

            public void setLevel(int level) {
                this.level = level;
            }

            public Object getParentId() {
                return ParentId;
            }

            public void setParentId(Object ParentId) {
                this.ParentId = ParentId;
            }

            public boolean isHasChild() {
                return hasChild;
            }

            public void setHasChild(boolean hasChild) {
                this.hasChild = hasChild;
            }

            public boolean isIsAdult() {
                return isAdult;
            }

            public void setIsAdult(boolean isAdult) {
                this.isAdult = isAdult;
            }

            public boolean isIsExternal() {
                return isExternal;
            }

            public void setIsExternal(boolean isExternal) {
                this.isExternal = isExternal;
            }

            public String getCategoryName() {
                return categoryName;
            }

            public void setCategoryName(String categoryName) {
                this.categoryName = categoryName;
            }

            public String getFormatUpdatedAt() {
                return formatUpdatedAt;
            }

            public void setFormatUpdatedAt(String formatUpdatedAt) {
                this.formatUpdatedAt = formatUpdatedAt;
            }

            public String getFormatCreatedAt() {
                return formatCreatedAt;
            }

            public void setFormatCreatedAt(String formatCreatedAt) {
                this.formatCreatedAt = formatCreatedAt;
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
