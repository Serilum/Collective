package com.natamus.collective.implementations.networking.data;

import net.minecraft.world.entity.player.Player;

/** The networking code is based on the MIT licensed
 *   <a href="https://github.com/mysticdrew/common-networking">common-networking</a> v1.0.6
 *  by MysticDrew */

public record PacketContext<T>(Player sender, T message, Side side) {
    public PacketContext(T message, Side side) {
        this(null, message, side);
    }
}
