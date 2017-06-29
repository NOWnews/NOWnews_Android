package com.nownews.mobile.Json;


public class SplashImageJson {

    /**
     * _id : 59510c49e0de863f488416b0
     * updatedAt : 2017-06-26T13:29:45.228Z
     * createdAt : 2017-06-26T13:29:45.228Z
     * device : PHONE
     * CreatedBy : 5913d7cb11f62d4a32be69c5
     * UpdatedBy : 5913d7cb11f62d4a32be69c5
     * isTrashed : false
     * Image : {"_id":"59510c48e0de863f488416af","url":"https://img.nownews.com/nownews_staging/images/59510c48e0de863f488416af_201706262129.jpg","formatUpdatedAt":"2017-06-26 23:12:25","formatCreatedAt":"2017-06-26 23:12:25","thumbnail":"https://imgapiv2.nownews.com/?w=300&q=70&src=https%3A%2F%2Fimg.nownews.com%2Fnownews_staging%2Fimages%2F59510c48e0de863f488416af_201706262129.jpg","id":"59510c48e0de863f488416af"}
     * formatUpdatedAt : 2017-06-26 21:29:45
     * formatCreatedAt : 2017-06-26 21:29:45
     * id : 59510c49e0de863f488416b0
     */

    private String _id;
    private String updatedAt;
    private String createdAt;
    private String device;
    private String CreatedBy;
    private String UpdatedBy;
    private boolean isTrashed;
    private ImageBean Image;
    private String formatUpdatedAt;
    private String formatCreatedAt;
    private String id;

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
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

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
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

    public ImageBean getImage() {
        return Image;
    }

    public void setImage(ImageBean Image) {
        this.Image = Image;
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

    public static class ImageBean {
        /**
         * _id : 59510c48e0de863f488416af
         * url : https://img.nownews.com/nownews_staging/images/59510c48e0de863f488416af_201706262129.jpg
         * formatUpdatedAt : 2017-06-26 23:12:25
         * formatCreatedAt : 2017-06-26 23:12:25
         * thumbnail : https://imgapiv2.nownews.com/?w=300&q=70&src=https%3A%2F%2Fimg.nownews.com%2Fnownews_staging%2Fimages%2F59510c48e0de863f488416af_201706262129.jpg
         * id : 59510c48e0de863f488416af
         */

        private String _id;
        private String url;
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

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
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
}
