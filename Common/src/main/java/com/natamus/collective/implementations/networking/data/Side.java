package com.natamus.collective.implementations.networking.data;

/** The networking code is based on the MIT licensed
 *   <a href="https://github.com/mysticdrew/common-networking">common-networking</a> v1.0.6
 *  by MysticDrew */

public enum Side {
    /**
     * CLIENT is the client side.
     */
    CLIENT,
    /**
     * SERVER can be dedicated server, or logical server in singleplayer.
     */
    SERVER;

    /**
     * Gets the opposite side.
     *
     * @return - The opposite side
     */
    public Side opposite() {
        if (CLIENT.equals(this)) {
            return SERVER;
        }
        return CLIENT;
    }
}
