package study.studyai.studyaiagent.course;

import java.util.ArrayList;
import java.util.List;

public class CourseFileCheckResult {

    private Boolean pass;

    private String reason;

    private List<String> chapterNameList = new ArrayList<>();

    public Boolean getPass() {
        return pass;
    }

    public void setPass(Boolean pass) {
        this.pass = pass;
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
