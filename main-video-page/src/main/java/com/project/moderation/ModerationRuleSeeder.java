package com.project.moderation;

import com.project.entity.ModerationRule;
import com.project.entity.RuleCategory;
import com.project.repository.ModerationRuleRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Стартовый black-лист, если таблица правил пустая; дальше правила ведёт админ.
 * <p>Выражения намеренно узкие: слово «выбор» без контекста не ловим, иначе «выбор плитки» уйдёт на модерацию.
 */
@Component
public class ModerationRuleSeeder implements ApplicationRunner {

    private final ModerationRuleRepository ruleRepository;

    public ModerationRuleSeeder(ModerationRuleRepository ruleRepository) {
        this.ruleRepository = ruleRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (ruleRepository.count() > 0) {
            return;
        }
        ruleRepository.saveAll(List.of(
                new ModerationRule(RuleCategory.VIOLENCE,
                        "\\b(убей(те)?|убить (всех|их)|убивай(те)?|прирежь(те)?|зарежь(те)?|избей(те)?|бей(те)? их)\\b",
                        "призывы убивать и бить"),
                new ModerationRule(RuleCategory.VIOLENCE,
                        "\\bсмерть (им|всем|ему|ей)\\b", "«смерть им»"),
                new ModerationRule(RuleCategory.SUICIDE,
                        "\\b(суицид\\w*|самоубийств\\w*|поконч\\w* с собой|вскры\\w* вены|убей себя|убить себя|выпилиться)\\b",
                        "суицид"),
                new ModerationRule(RuleCategory.TERRORISM,
                        "\\b(теракт\\w*|террорист\\w*|терроризм\\w*|джихад\\w*|смертник\\w*|шахид\\w*)\\b",
                        "терроризм"),
                new ModerationRule(RuleCategory.TERRORISM,
                        "\\b(взорвать (здание|людей|школу|метро|толпу)|самодельн\\w* взрывн\\w*|пояс шахида)\\b",
                        "подготовка взрывов"),
                new ModerationRule(RuleCategory.PORNOGRAPHY,
                        "\\b(порн\\w*|xxx|секс[- ]?видео|интим[- ]?услуг\\w*|эскорт\\w*)\\b",
                        "порнография"),
                new ModerationRule(RuleCategory.POLITICS,
                        "\\b(голосуй(те)?|проголосуй(те)?|голосовать за|отдай(те)? (свой )?голос)\\b",
                        "призывы голосовать"),
                new ModerationRule(RuleCategory.POLITICS,
                        "\\b(выбор\\w* (президента|в думу|губернатора|мэра|депутат\\w*)|предвыборн\\w*|избирательн\\w*|бюллетен\\w*|явк\\w* на выбор\\w*)\\b",
                        "выборы"),
                new ModerationRule(RuleCategory.POLITICS,
                        "\\b(референдум\\w*|волеизъявлени\\w*|агитаци\\w*|митинг\\w*|выйди(те)? на (улиц\\w*|площад\\w*))\\b",
                        "референдумы, митинги, агитация")
        ));
    }
}
