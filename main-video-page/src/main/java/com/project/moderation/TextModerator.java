package com.project.moderation;

import com.project.entity.ModerationRule;
import com.project.entity.RuleCategory;
import com.project.repository.ModerationRuleRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

/**
 * Проверка текста по black-листу (правила в БД, админ меняет их через /moderation/rules)
 */
@Component
public class TextModerator {

    private final ModerationRuleRepository ruleRepository;
    private final PatternCompiler patternCompiler;
    private final TextNormalizer textNormalizer;

    public TextModerator(ModerationRuleRepository ruleRepository, PatternCompiler patternCompiler,
                         TextNormalizer textNormalizer) {
        this.ruleRepository = ruleRepository;
        this.patternCompiler = patternCompiler;
        this.textNormalizer = textNormalizer;
    }

    public List<Match> check(String text) {
        List<Match> matches = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return matches;
        }
        String normalized = textNormalizer.normalize(text);
        for (ModerationRule rule : ruleRepository.findAll()) {
            Matcher matcher = patternCompiler.compile(rule.getPattern()).matcher(normalized);
            if (matcher.find()) {
                matches.add(new Match(rule.getCategory(), matcher.group()));
            }
        }
        return matches;
    }

    public record Match(RuleCategory category, String fragment) { }
}
