package com.natamus.collective.objects;

import java.util.HashSet;
import java.util.Set;

public class PlayerPlacedRegion {
	public final Set<Long> positions = new HashSet<>();
	public boolean hasUnsavedChanges = false;
	public long lastUsedGameTime = 0;
}
