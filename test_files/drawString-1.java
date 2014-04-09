public class Test {
	public void drawString (String string, int x, int y, boolean isTransparent) {
		if (handle == 0) SWT.error(SWT.ERROR_GRAPHIC_DISPOSED);
		if (string == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
		int length = string.length();
		if (length == 0) return;
		char[] buffer = new char [length];
		string.getChars(0, length, buffer, 0);
		if (data.gdipGraphics != 0) {
			initGdip(true, !isTransparent);
			int font = Gdip.Font_new(handle, OS.GetCurrentObject(handle, OS.OBJ_FONT));
			if (font == 0) SWT.error(SWT.ERROR_NO_HANDLES);
			PointF pt = new PointF();
			pt.X = x;
			pt.Y = y;
			int brush = Gdip.Pen_GetBrush(data.gdipPen);
			int format = Gdip.StringFormat_GenericTypographic();
			if (!isTransparent) {
				RectF bounds = new RectF();
				Gdip.Graphics_MeasureString(data.gdipGraphics, buffer, length, font, pt, format, bounds);
				Gdip.Graphics_FillRectangle(data.gdipGraphics, data.gdipBrush, (int)bounds.X, (int)bounds.Y, (int)Math.round(bounds.Width), (int)Math.round(bounds.Height));
			}
			Gdip.Graphics_DrawString(data.gdipGraphics, buffer, length, font, pt, format, brush);
			Gdip.Font_delete(font);
			return;
		}
		int rop2 = 0;
		if (OS.IsWinCE) {
			rop2 = OS.SetROP2(handle, OS.R2_COPYPEN);
			OS.SetROP2(handle, rop2);
		} else {
			rop2 = OS.GetROP2(handle);
		}
		int oldBkMode = OS.SetBkMode(handle, isTransparent ? OS.TRANSPARENT : OS.OPAQUE);
		if (rop2 != OS.R2_XORPEN) {
			OS.ExtTextOutW(handle, x, y, 0, null, buffer, length, null);
		} else {
			int foreground = OS.GetTextColor(handle);
			if (isTransparent) {
				SIZE size = new SIZE();
				OS.GetTextExtentPoint32W(handle, buffer, length, size);
				int width = size.cx, height = size.cy;
				int hBitmap = OS.CreateCompatibleBitmap(handle, width, height);
				if (hBitmap == 0) SWT.error(SWT.ERROR_NO_HANDLES);
				int memDC = OS.CreateCompatibleDC(handle);
				int hOldBitmap = OS.SelectObject(memDC, hBitmap);
				OS.PatBlt(memDC, 0, 0, width, height, OS.BLACKNESS);
				OS.SetBkMode(memDC, OS.TRANSPARENT);
				OS.SetTextColor(memDC, foreground);
				OS.SelectObject(memDC, OS.GetCurrentObject(handle, OS.OBJ_FONT));
				OS.ExtTextOutW(memDC, 0, 0, 0, null, buffer, length, null);
				OS.BitBlt(handle, x, y, width, height, memDC, 0, 0, OS.SRCINVERT);
				OS.SelectObject(memDC, hOldBitmap);
				OS.DeleteDC(memDC);
				OS.DeleteObject(hBitmap);
			} else {
				int background = OS.GetBkColor(handle);
				OS.SetTextColor(handle, foreground ^ background);
				OS.ExtTextOutW(handle, x, y, 0, null, buffer, length, null);
				OS.SetTextColor(handle, foreground);
			}
		}
		OS.SetBkMode(handle, oldBkMode);
	}

}