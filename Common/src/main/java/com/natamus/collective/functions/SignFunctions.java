package com.natamus.collective.functions;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.HangingSignBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SignFunctions {
    public static List<String> getSignText(BlockEntity blockEntity) {
        if (blockEntity instanceof HangingSignBlockEntity) {
            return getSignText((HangingSignBlockEntity)blockEntity);
        }
        else if (blockEntity instanceof SignBlockEntity) {
            return getSignText((SignBlockEntity)blockEntity);
        }
        return new ArrayList<String>();
    }

    public static List<String> getSignText(SignBlockEntity signBlockEntity) {
        return getSignText(Arrays.asList(signBlockEntity.getFrontText(), signBlockEntity.getBackText()));
    }
    public static List<String> getSignText(HangingSignBlockEntity hangingSignBlockEntity) {
        return getSignText(Arrays.asList(hangingSignBlockEntity.getFrontText(), hangingSignBlockEntity.getBackText()));
    }

    public static List<String> getSignText(List<SignText> signTextList) {
        List<String> lines = new ArrayList<String>();

        for (SignText signText : signTextList) {
            for (Component line : signText.getMessages(false)) {
                if (line.equals(Component.EMPTY)) {
                    lines.add("");
                    continue;
                }

                lines.add(line.getString());
            }
        }

        return lines;
    }
}
