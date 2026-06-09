package study.studyai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan("study.studyai.mapper")
public class StudyAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(StudyAiApplication.class, args);
    }

}
