package hospital.model.domain;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SymptomRecommendation
        implements Comparable<SymptomRecommendation> {

    private int score;
    private List<String> matchedKeywords = new ArrayList<>();

    public void addMatchedKeyword(String keyword) {
        matchedKeywords.add(keyword);
    }

    public String getMatchedKeywordText() {
        return String.join(", ", matchedKeywords);
    }

    @Override
    public int compareTo(SymptomRecommendation other) {
        return Integer.compare(other.score, this.score);
    }
}