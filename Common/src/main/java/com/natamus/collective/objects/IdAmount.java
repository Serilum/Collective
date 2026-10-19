package com.natamus.collective.objects;

import net.minecraft.util.RandomSource;

public class IdAmount {
	public final String id;
	public final int minimum;
	public final int maximum;

	public IdAmount(String entry) {
		String strippedEntry = entry.strip();
		int separatorIndex = strippedEntry.lastIndexOf('=');
		if (separatorIndex == -1) {
			this.id = strippedEntry;
			this.minimum = 1;
			this.maximum = 1;
			return;
		}

		this.id = strippedEntry.substring(0, separatorIndex).strip();

		String amountText = strippedEntry.substring(separatorIndex + 1).strip();
		String[] rangeParts = amountText.split("-", 2);
		int firstAmount = parseAmount(rangeParts[0]);
		int secondAmount = firstAmount;
		if (rangeParts.length == 2) {
			secondAmount = parseAmount(rangeParts[1]);
		}

		this.minimum = Math.min(firstAmount, secondAmount);
		this.maximum = Math.max(firstAmount, secondAmount);
	}

	public static String getId(String entry) {
		return new IdAmount(entry).id;
	}

	public static boolean isValidAmountText(String amountText) {
		return amountText.matches("\\d{1,9}(-\\d{1,9})?");
	}

	public String getAmountText() {
		if (minimum == maximum) {
			return String.valueOf(minimum);
		}
		return minimum + "-" + maximum;
	}

	public String toEntry() {
		return id + "=" + getAmountText();
	}

	public int getRandomAmount(RandomSource randomSource) {
		return randomSource.nextIntBetweenInclusive(minimum, maximum);
	}

	private static int parseAmount(String text) {
		String strippedText = text.strip();
		if (!strippedText.matches("\\d{1,9}")) {
			return 1;
		}
		return Integer.parseInt(strippedText);
	}
}
