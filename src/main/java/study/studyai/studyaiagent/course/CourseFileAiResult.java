package study.studyai.studyaiagent.course;

import java.util.ArrayList;
import java.util.List;

public class CourseFileAiResult {

    private String markdown;

    private List<String> chapterNameList = new ArrayList<>();

    public String getMarkdown() {
        return markdown;
    }

    public void setMarkdown(String markdown) {
        this.markdown = markdown;
    }

    public List<String> getChapterNameList() {
        return chapterNameList;
    }

    public void setChapterNameList(List<String> chapterNameList) {
        this.chapterNameList = chapterNameList;
    }
}
