package com.project.dto;

import com.project.entity.ModerationFlag;

public record ModerationFlagDto(String source, String ruleCategory, String ruleCategoryTitle, String detail,
                                Double timecodeSeconds) {
    public static ModerationFlagDto from(ModerationFlag flag) {
        return new ModerationFlagDto(flag.getSource().name(),
                flag.getRuleCategory() == null ? null : flag.getRuleCategory().name(),
                flag.getRuleCategory() == null ? null : flag.getRuleCategory().getTitle(),
                flag.getDetail(), flag.getTimecodeSeconds());
    }
}
