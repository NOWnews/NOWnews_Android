package com.nownews.mobile.Json;

import java.util.List;

public class NewsCategoryJson {

    private List<CategoryInfo> newsCategory;

    public List<CategoryInfo> getCategoryInfo() {
        return newsCategory;
    }

    public void setCategoryInfo(List<CategoryInfo> newsCategory) {
        this.newsCategory = newsCategory;
    }

    public static class CategoryInfo {
        /**
         * _id : 59158011fbe08771e3294779
         * sn : 41
         * updatedAt : 2017-05-24T04:30:00.723Z
         * createdAt : 2017-05-12T09:27:45.746Z
         * name : 總覽
         * url : /cat/news
         * CreatedBy : 5915646fc634cc5cc9f17066
         * UpdatedBy : 5915646fc634cc5cc9f17066
         * isTrashed : false
         * status : OPEN
         * isPermanented : true
         * endedAt : null
         * startedAt : null
         * weight : 0
         * level : 0
         * ParentId : null
         * hasChild : true
         * isAdult : false
         * isExternal : false
         * categoryName : news
         * child : [{"_id":"59158443fbe08771e32947aa","sn":42,"updatedAt":"2017-06-21T03:02:05.358Z","createdAt":"2017-05-12T09:45:39.359Z","name":"首頁","url":"/cat/news/r","CreatedBy":"5915646fc634cc5cc9f17066","UpdatedBy":"5915646fc634cc5cc9f17066","isTrashed":false,"status":"OPEN","isPermanented":true,"endedAt":null,"startedAt":null,"weight":0,"level":1,"ParentId":"59158011fbe08771e3294779","hasChild":false,"isAdult":false,"isExternal":false,"categoryName":"news/r"},{"_id":"591577b1fbe08771e3294711","sn":29,"updatedAt":"2017-06-21T03:02:05.357Z","createdAt":"2017-05-12T08:52:01.208Z","name":"名家論壇","url":"/cat/celebritycomment","CreatedBy":"5915646fc634cc5cc9f17068","UpdatedBy":"5915646fc634cc5cc9f17068","isTrashed":false,"status":"OPEN","isPermanented":true,"endedAt":null,"startedAt":null,"weight":1,"level":1,"ParentId":"59158011fbe08771e3294779","hasChild":false,"isAdult":false,"isExternal":false,"categoryName":"celebritycomment"},{"_id":"59158e04fbe08771e32947e6","sn":51,"updatedAt":"2017-06-21T03:02:05.360Z","createdAt":"2017-05-12T10:27:16.493Z","name":"讀者大聲公","url":"/cat/usertalk","CreatedBy":"5915646fc634cc5cc9f17066","UpdatedBy":"5915646fc634cc5cc9f17066","isTrashed":false,"status":"OPEN","isPermanented":true,"endedAt":null,"startedAt":null,"weight":2,"level":1,"ParentId":"59158011fbe08771e3294779","hasChild":false,"isAdult":false,"isExternal":false,"categoryName":"usertalk"},{"_id":"59157832fbe08771e3294714","sn":32,"updatedAt":"2017-06-21T03:02:05.359Z","createdAt":"2017-05-12T08:54:10.482Z","name":"公益行善","url":"/cat/public","CreatedBy":"5915646fc634cc5cc9f17066","UpdatedBy":"5915646fc634cc5cc9f17066","isTrashed":false,"status":"OPEN","isPermanented":true,"endedAt":null,"startedAt":null,"weight":3,"level":1,"ParentId":"59158011fbe08771e3294779","hasChild":false,"isAdult":false,"isExternal":false,"categoryName":"public"},{"_id":"5916b721fbe08771e3294981","sn":70,"updatedAt":"2017-06-21T03:02:05.350Z","createdAt":"2017-05-13T07:34:57.407Z","name":"圖輯","url":"/cat/photo","CreatedBy":"5915646fc634cc5cc9f17066","UpdatedBy":"5915646fc634cc5cc9f17066","isTrashed":false,"status":"OPEN","isPermanented":true,"endedAt":null,"startedAt":null,"weight":4,"level":1,"ParentId":"59158011fbe08771e3294779","hasChild":false,"isAdult":false,"isExternal":false,"categoryName":"photo"},{"_id":"59250d9200c9f1103777148b","sn":99,"updatedAt":"2017-06-21T03:02:05.358Z","createdAt":"2017-05-24T04:35:30.476Z","name":"影音","url":"/cat/video","CreatedBy":"5915646fc634cc5cc9f17066","UpdatedBy":"5915646fc634cc5cc9f17066","isTrashed":false,"status":"OPEN","isPermanented":true,"endedAt":null,"startedAt":null,"weight":5,"level":1,"ParentId":"59158011fbe08771e3294779","hasChild":false,"isAdult":false,"isExternal":false,"categoryName":"video"}]
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
        private List<ChildBean> child;

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

        public List<ChildBean> getChild() {
            return child;
        }

        public void setChild(List<ChildBean> child) {
            this.child = child;
        }

        public static class ChildBean {
            /**
             * _id : 59158443fbe08771e32947aa
             * sn : 42
             * updatedAt : 2017-06-21T03:02:05.358Z
             * createdAt : 2017-05-12T09:45:39.359Z
             * name : 首頁
             * url : /cat/news/r
             * CreatedBy : 5915646fc634cc5cc9f17066
             * UpdatedBy : 5915646fc634cc5cc9f17066
             * isTrashed : false
             * status : OPEN
             * isPermanented : true
             * endedAt : null
             * startedAt : null
             * weight : 0
             * level : 1
             * ParentId : 59158011fbe08771e3294779
             * hasChild : false
             * isAdult : false
             * isExternal : false
             * categoryName : news/r
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
            private String ParentId;
            private boolean hasChild;
            private boolean isAdult;
            private boolean isExternal;
            private String categoryName;

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

            public String getParentId() {
                return ParentId;
            }

            public void setParentId(String ParentId) {
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
        }
    }

}
