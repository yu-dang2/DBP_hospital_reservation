package hospital.model.domain;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SymptomRecommendation
        implements Comparable<SymptomRecommendation> {

    private Department department;   // 추천 진료과
    private int score;               // 매칭된 키워드 가중치 합
    private List<String> matchedKeywords = new ArrayList<>();

    public SymptomRecommendation() {
    }

    public SymptomRecommendation(Department department) {
        this.department = department;
    }

    // 매칭된 키워드를 추가하고 가중치를 점수에 더함
    public void addMatch(String keyword, int weight) {
        matchedKeywords.add(keyword);
        score += weight;
    }

    public void addMatchedKeyword(String keyword) {
        matchedKeywords.add(keyword);
    }

    public String getMatchedKeywordText() {
        return String.join(", ", matchedKeywords);
    }

    // 점수 내림차순, 동점이면 진료과명 오름차순
    @Override
    public int compareTo(SymptomRecommendation other) {
        int byScore = Integer.compare(other.score, this.score);
        if (byScore != 0) {
            return byScore;
        }
        return deptNameOf(this).compareTo(deptNameOf(other));
    }

    private static String deptNameOf(SymptomRecommendation rec) {
        if (rec.department == null || rec.department.getDeptName() == null) {
            return "";
        }
        return rec.department.getDeptName();
    }
}
