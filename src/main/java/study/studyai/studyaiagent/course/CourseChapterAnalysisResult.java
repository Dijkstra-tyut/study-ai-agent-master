package study.studyai.studyaiagent.course;

import java.util.ArrayList;
import java.util.List;

public class CourseChapterAnalysisResult {

    private Boolean hasDirectory;

    private String reason;

    private List<String> chapterNameList = new ArrayList<>();

    public Boolean getHasDirectory() {
        return hasDirectory;
    }

    public void setHasDirectory(Boolean hasDirectory) {
        this.hasDirectory = hasDirectory;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public List<String> getChapterNameList() {
        return chapterNameList;
    }

    public void setChapterNameList(List<String> chapterNameList) {
        this.chapterNameList = chapterNameList;
    }
}
