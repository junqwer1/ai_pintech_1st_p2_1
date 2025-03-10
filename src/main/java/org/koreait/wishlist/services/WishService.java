package org.koreait.wishlist.services;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.koreait.global.exceptions.BadRequestException;
import org.koreait.global.libs.Utils;
import org.koreait.member.entities.Member;
import org.koreait.member.libs.MemberUtil;
import org.koreait.member.repositories.MemberRepository;
import org.koreait.wishlist.constants.WishType;
import org.koreait.wishlist.entities.QWish;
import org.koreait.wishlist.entities.Wish;
import org.koreait.wishlist.entities.WishId;
import org.koreait.wishlist.repositories.WishRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.List;

@Lazy
@Service
@RequiredArgsConstructor
@Transactional
public class WishService {
    private final Utils utils;
    private final MemberUtil memberUtil;
    private final WishRepository repository;
    private final JPAQueryFactory queryFactory;
    private final MemberRepository memberRepository;
    private final SpringTemplateEngine templateEngine;

    public void process(String mode, Long seq, WishType type) {
        if (!memberUtil.isLogin()){ return;}

        mode = StringUtils.hasText(mode) ? mode : "add";
        Member member = memberUtil.getMember();
        member = memberRepository.findByEmail(member.getEmail()).orElse(null);
        try {
            if (mode.equals("remove")) { // 찜 해제
                WishId wishId = new WishId(seq, type, member);
                repository.deleteById(wishId);
            } else { // 찜 추가
                /* 포켓몬 찜 추가면 갯수를 6개로 제한*/
                if (type == WishType.MYPOKEMON) {
                    QWish wish = QWish.wish; // 조건을 생성하기 위해서
                    BooleanBuilder builder = new BooleanBuilder();
                    builder.and(wish.member.eq(member))
                            .and(wish.type.eq(type));




                    long total = repository.count(builder);
                    if (total >= 6L) {
                        throw new BadRequestException(utils.getMessage("OverMaxWish.Pokemon"));
                    }
                }

                Wish wish = new Wish();
                wish.setSeq(seq);
                wish.setType(type);
                wish.setMember(member);
                repository.save(wish);
            }
        } catch (Exception e) {
            if (e instanceof  BadRequestException) {
                throw e;
            }
            e.printStackTrace();

        }
    }

    public List<Long> getMyWish(WishType type) {
        if (!memberUtil.isLogin()) {
            return List.of();
        }
        QWish wish = QWish.wish;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(wish.member.eq(memberUtil.getMember()))
                .and(wish.type.eq(type));
        List<Long> items = queryFactory.select(wish.seq)
                .from(wish)
                .where(builder)
                .orderBy(wish.createdAt.asc())
                .fetch();

        return items;
    }

    public String showWish(Long seq, String type) {
        return showWish(seq, type, null);
    }

    public String showWish(Long seq, String type, List<Long> myWishes) {

        Context context = commonContext(seq, type, myWishes);

        return templateEngine.process("common/_wish", context);
    }

    public String viewMy(Long seq, String type) {
        return viewMy(seq, type, null);
    }

    public String viewMy(Long seq, String type, List<Long> myWishes) {

        Context context = commonContext(seq, type, myWishes);

        return templateEngine.process("common/_mypokemon", context);
    }

    public Context commonContext(Long seq, String type, List<Long> myWishes) {

        WishType _type = WishType.valueOf(type);
        myWishes = myWishes == null || myWishes.isEmpty() ? getMyWish(_type) : myWishes;

        Context context = new Context();
        context.setVariable("seq", seq);
        context.setVariable("type", _type);
        context.setVariable("myWishes", myWishes);
        context.setVariable("isMine", myWishes.contains(seq));
        context.setVariable("isLogin", memberUtil.isLogin());

        return context;
    }

}
