package com.peihua8858.logfileserver.entity;

import java.util.List;
import java.util.Map;

public class FolderMenu {
    private String text;
    private String href;
    private List<FolderMenu> nodes;
    private List<String> tags;
    private List<FileModel> files;
    private Map<String,Boolean> state;
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public List<FolderMenu> getNodes() {
        return nodes;
    }

    public void setNodes(List<FolderMenu> nodes) {
        this.nodes = nodes;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<FileModel> getFiles() {
        return files;
    }

    public void setFiles(List<FileModel> files) {
        this.files = files;
    }

    public Map<String, Boolean> getState() {
        return state;
    }

    public void setState(Map<String, Boolean> state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "FolderMenu{" + "text='" + text + '\'' + ", href='" + href + '\'' + ", nodes=" + nodes + ", tags=" + tags + ", files=" + files + '}';
    }
}
