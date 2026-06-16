package study.studyai.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class LearningProfileVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long userId;

    private Integer knowledgeLevel;

    private String learningStyle;

    private String interest;

    private List<String> weakness;

    private String errorPreference;

    private String learningSpeed;

    private String learningRoute;

    private String behaviorAnalysis;

    private Date createTime;

    private Date updateTime;
}
