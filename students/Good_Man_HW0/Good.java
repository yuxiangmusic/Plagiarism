public class Good {
	public static int[] getLIS(int[] arr) {
		if (arr.length == 0)
			return new int[0];
		int length = 0, dp[] = new int[arr.length];
		int p[] = new int[arr.length];
		for (int i = 0; i < arr.length; i++) {
			int l = 0, h = length - 1;
			while (l <= h) {
				int mid = (l + h) >>> 1;
				if (arr[dp[mid]] == arr[i]) {
					l = mid;
					break;
				} else if (arr[dp[mid]] < arr[i]) {
					l = mid + 1;
				} else {
					h = mid - 1;
				}
			}
			dp[l] = i;
			if (length == l)
				length++;
			if (l > 0)
				p[i] = dp[l - 1];
		}
		int lis[] = new int[length], arrIndex = dp[length - 1];
		for (int i = length - 1; i >= 0; i--) {
			lis[i] = arr[arrIndex];
			arrIndex = p[arrIndex];
		}
		return lis;
	}
}
