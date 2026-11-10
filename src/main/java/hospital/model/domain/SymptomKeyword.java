package hospital.model.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SymptomKeyword {

    private Long keywordId;
    private String keyword;
    private int weight;
    private Long deptId;

    // 주요 메서드: 증상 문장에 키워드가 포함되어 있는지 
    //(임시 메서드, 추후 수정 필요)
    public boolean matches(String sentence) {
        return sentence != null
                && keyword != null
                && sentence.contains(keyword);
    }
}