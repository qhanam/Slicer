public class Scrollable {
	public Rectangle computeTrim (int x, int y, int width, int height) {
		checkWidget();
		int border = 0;
		int [] outMetric = new int [1];
		OS.GetThemeMetric (OS.kThemeMetricFocusRectOutset, outMetric);
		border += outMetric [0];
		OS.GetThemeMetric (OS.kThemeMetricEditTextFrameOutset, outMetric);
		border += outMetric [0];
		Rect rect = new Rect ();
		OS.GetDataBrowserScrollBarInset (handle, rect);
		x -= rect.left + border;
		y -= rect.top + border;
		width += rect.left + rect.right + border + border;
		height += rect.top + rect.bottom + border + border;
		return new Rectangle (x, y, width, height);
	}
}