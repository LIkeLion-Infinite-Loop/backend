package infinite_loop.sejonghack.domain;

import jakarta.persistence.*;
import lombok.Getter; import lombok.Setter;

@Entity @Table(name="quiz_session_item")
@Getter @Setter
public class QuizSessionItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long sessionId;
    private int itemOrder;         // 1..3

    @Column(columnDefinition = "TEXT")
    private String prompt;

    private String choice1;
    private String choice2;
    private String choice3;
    private String choice4;

    private int correctIndex;      // 1..4 (셔플 후 기준)

    private Integer userAnswerIndex;
    private Boolean isCorrect;
    private int awardedPoints;
}
