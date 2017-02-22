package com.restmockserver.core;

import java.util.Random;

/**
 * Created by aa069w on 2/19/2017.
 */
public class WildCardsResolver {

    public  static final    String                  WILD_CARD_DECORATOR = "%";
    private static          WildCardsResolver       wildCardsResolver;

    private WildCardsResolver() {}

    public static WildCardsResolver getInstance() {
        if (wildCardsResolver == null) {
            wildCardsResolver = new WildCardsResolver();
        }
        return wildCardsResolver;
    }

    public String resolveText(String text) {
        for (WildCard wildCard : WildCard.values()) {
            String decoratedWildCard = WILD_CARD_DECORATOR + wildCard.name() + WILD_CARD_DECORATOR;
            return text.replace(decoratedWildCard, resolve(wildCard));
        }

        return text;
    }

    private String resolve(WildCard wildCard) {
        if (wildCard.equals(WildCard.RANDOM)) {
            Random random = new Random();
            return String.valueOf(random.nextInt(1000 - 1) + 1);
        }

        return null;
    }
}
