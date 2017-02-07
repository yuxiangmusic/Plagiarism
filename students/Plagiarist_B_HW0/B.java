public class B {
	public static int[] getLIS(int[] arr) {
		if (arr.length == 0)
			return new int[0];
		int l[] = new int[arr.length];
		int p[] = new int[arr.length];
		int maxIndex = 0;
		for (int i = 0; i < arr.length; i++) {
			l[i] = 1;
			p[i] = -1;
			for (int j = 0; j < i; j++) {
				if (arr[j] < arr[i] && l[i] < l[j] + 1) {
					l[i] = l[j] + 1;
					p[i] = j;
				}
			}
			if (l[i] > l[maxIndex])
				maxIndex = i;
		}
		int lis[] = new int[l[maxIndex]], arrIndex = maxIndex;
		for (int i = lis.length - 1; i >= 0; i--) {
			lis[i] = arr[arrIndex];
			arrIndex = p[arrIndex];
		}
		return lis;
	}
}