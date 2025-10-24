package org.sopt.makers.internal.member.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class UserFavor {

    @Column(name = "is_pour_sauce_lover")
    private Boolean isPourSauceLover;

    @Column(name = "is_hard_peach_lover")
    private Boolean isHardPeachLover;

    @Column(name = "is_mint_choco_lover")
    private Boolean isMintChocoLover;

    @Column(name = "is_red_bean_fish_bread_lover")
    private Boolean isRedBeanFishBreadLover;

    @Column(name = "is_soju_lover")
    private Boolean isSojuLover;

    @Column(name = "is_rice_tteok_lover")
    private Boolean isRiceTteokLover;
}
