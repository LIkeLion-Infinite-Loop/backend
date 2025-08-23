package infinite_loop.hack.service;

import infinite_loop.hack.dto.QuizDtos.*;
import infinite_loop.hack.domain.QuizSession;
import infinite_loop.hack.domain.QuizSession.Status;
import infinite_loop.hack.domain.QuizSessionItem;
import infinite_loop.hack.openai.OpenAiClient;
import infinite_loop.hack.openai.OpenAiClient.GptQuizResponse;
import infinite_loop.hack.repository.QuizSessionItemRepository;
import infinite_loop.hack.repository.QuizSessionRepository;
import infinite_loop.hack.support.QuizConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
public class QuizService {
    private final OpenAiClient openai;
    private final QuizSessionRepository sessionRepo;
    private final QuizSessionItemRepository itemRepo;
    private final PointService pointService;

    // 세션 생성: 무작위 카테고리, 3문항, 1일 3회 제한
    @Transactional
    public CreateSessionRes startSession(Long userId) {
        // 1) 1일 3회 제한(Asia/Seoul)
        LocalDate today = QuizConstants.todayKST();
        Instant start = today.atStartOfDay(ZoneId.of("Asia/Seoul")).toInstant();
        Instant end   = today.plusDays(1).atStartOfDay(ZoneId.of("Asia/Seoul")).toInstant();
        int usedToday = sessionRepo.findByUserIdAndStartedAtBetween(userId, start, end).size();
        if (usedToday >= 3) throw new IllegalStateException("DAILY_LIMIT_REACHED");

        // 2) 활성 세션 중복 금지
        var active = sessionRepo.findFirstByUserIdAndStatus(userId, Status.ACTIVE);
        if (active.isPresent())
            throw new infinite_loop.hack.exception.ActiveSessionConflictException(active.get().getId());


        // 3) 무작위 카테고리
        String category = QuizConstants.pickRandomCategory();

        // 4) GPT 3문항 생성
        GptQuizResponse g = openai.createThreeQuestions(category);
        if (g == null || g.questions == null || g.questions.size() != 3)
            throw new IllegalStateException("NOT_ENOUGH_QUESTIONS");

        // 5) 세션 저장(만료 10분)
        QuizSession s = new QuizSession();
        s.setUserId(userId);
        s.setStatus(Status.ACTIVE);
        s.setCategory(category);
        s.setNumQuestions(3);
        s.setExpiresAt(Instant.now().plus(Duration.ofMinutes(10)));
        s = sessionRepo.save(s);

        // 6) 아이템 저장(보기 셔플 + 정답 인덱스 재계산)
        List<CreateSessionRes.Item> outItems = new ArrayList<>();
        int order = 1;
        for (var q : g.questions) {
            List<String> choices = new ArrayList<>(q.choices);
            Collections.shuffle(choices);
            int correctIdx = choices.indexOf(q.choices.get(q.correct_index - 1)) + 1;

            QuizSessionItem it = new QuizSessionItem();
            it.setSessionId(s.getId());
            it.setItemOrder(order);
            it.setPrompt(q.prompt);
            it.setChoice1(choices.get(0));
            it.setChoice2(choices.get(1));
            it.setChoice3(choices.get(2));
            it.setChoice4(choices.get(3));
            it.setCorrectIndex(correctIdx);
            it = itemRepo.save(it);

            outItems.add(new CreateSessionRes.Item(it.getId(), order++, q.prompt, choices));
        }

        int attemptsLeft = Math.max(0, 3 - usedToday - 1);
        return new CreateSessionRes(s.getId(), s.getExpiresAt(), 3, category, attemptsLeft, outItems);
    }

    // 제출: 채점 + 포인트 적립 + SUBMITTED
    @Transactional
    public SubmitRes submit(Long userId, Long sessionId, List<SubmitReq.Answer> answers) {
        QuizSession s = sessionRepo.findById(sessionId).orElseThrow();
        if (!Objects.equals(s.getUserId(), userId)) throw new SecurityException("FORBIDDEN");
        if (s.getStatus() != Status.ACTIVE) throw new IllegalStateException("ALREADY_SUBMITTED");
        if (s.isExpired()) { s.setStatus(Status.EXPIRED); sessionRepo.save(s); throw new IllegalStateException("EXPIRED"); }

        var items = itemRepo.findBySessionIdOrderByItemOrder(sessionId);
        Map<Long, Integer> ansMap = new HashMap<>();
        for (var a : answers) ansMap.put(a.itemId(), a.answerIdx());

        int correct = 0, totalPts = 0;
        for (var it : items) {
            int userAns = ansMap.getOrDefault(it.getId(), 0);
            boolean ok = (userAns == it.getCorrectIndex());
            it.setUserAnswerIndex(userAns);
            it.setIsCorrect(ok);
            int pts = ok ? 10 : 0; // 정책: 정답 10점
            it.setAwardedPoints(pts);
            itemRepo.save(it);
            if (ok) correct++;
            totalPts += pts;
        }

        s.setTotalAwardedPoints(totalPts);
        s.setStatus(Status.SUBMITTED);
        sessionRepo.save(s);

        pointService.addPoints(userId, totalPts, "QUIZ");

        return new SubmitRes(sessionId, s.getCategory(), correct, items.size(), totalPts, Instant.now());
    }

    @Transactional(readOnly = true)
    public CreateSessionRes getActive(Long userId) {
        var active = sessionRepo.findFirstByUserIdAndStatus(userId, Status.ACTIVE).orElse(null);
        if (active == null) return null;
        if (active.isExpired()) {
            active.setStatus(Status.EXPIRED);
            sessionRepo.save(active);
            return null;
        }
        return toCreateRes(active);
    }

    @Transactional(readOnly = true)
    public CreateSessionRes getById(Long userId, Long sessionId) {
        var s = sessionRepo.findById(sessionId).orElse(null);
        if (s == null || !s.getUserId().equals(userId)) return null;
        return toCreateRes(s);
    }

    private CreateSessionRes toCreateRes(QuizSession s) {
        var items = itemRepo.findBySessionIdOrderByItemOrder(s.getId()).stream()
                .map(it -> new CreateSessionRes.Item(
                        it.getId(),
                        it.getItemOrder(),
                        it.getPrompt(),
                        java.util.List.of(it.getChoice1(), it.getChoice2(), it.getChoice3(), it.getChoice4())
                ))
                .toList();

        // 오늘 남은 횟수(1일 3회 기준)
        java.time.LocalDate today = infinite_loop.hack.support.QuizConstants.todayKST();
        java.time.Instant start = today.atStartOfDay(java.time.ZoneId.of("Asia/Seoul")).toInstant();
        java.time.Instant end   = today.plusDays(1).atStartOfDay(java.time.ZoneId.of("Asia/Seoul")).toInstant();
        int usedToday = sessionRepo.findByUserIdAndStartedAtBetween(s.getUserId(), start, end).size();
        int attemptsLeft = Math.max(0, 3 - usedToday);

        return new CreateSessionRes(
                s.getId(), s.getExpiresAt(), s.getNumQuestions(), s.getCategory(),
                attemptsLeft, items
        );
    }

}
