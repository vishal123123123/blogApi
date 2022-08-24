package com.blogapi.config;

public enum CommonStatus {
	
	NEW("New"),
    ACTIVE("Active"),
    DEACTIVE("Deactive"),
    DELETED("Deleted");

    private String desc;

    CommonStatus(String desc) {
        this.desc = desc;
    }

    @Override
    public String toString() {
        return this.desc;
    }

}
