package lv_5;

public class Lv5_15596 {

	long sum(int[] a) {
		long ans = 0;
		
		for(int i=0 ; i < a.length; i++) {
			ans += a[i];
		}
		return ans;
		// int num : a  --> 반복조건 고려
	}
}
