package com.natamus.collective.functions;

import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.level.block.entity.SignBlockEntity;

import java.util.ArrayList;
import java.util.List;

public class SignFunctions {
    public static List<String> getSignText(SignBlockEntity signentity) {
        List<String> lines = new ArrayList<String>();

        for (Component line : signentity.getMessages(false)) {
            if (line.equals(TextComponent.EMPTY)) {
                lines.add("");
                continue;
            }

            lines.add(line.getString());
        }

        return lines;
    }
}
