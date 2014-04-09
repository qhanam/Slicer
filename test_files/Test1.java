public class Test {
	public void drawLine (int x1, int y1, int x2, int y2) {
		if (handle == 0) SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
		int cairo = data.cairo;

		if (cairo != 0) {
			Cairo.cairo_move_to(cairo, x1, y1);
			Cairo.cairo_line_to(cairo, x2, y2);
			Cairo.cairo_stroke(cairo);
			return;
		}
		OS.XDrawLine (data.display, data.drawable, handle, x1, y1, x2, y2);
	}
}