package org.pmedv.core.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.platform.unix.X11;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;

/**
 * Application icon. GNOME matches a {@code .desktop} file via WM_CLASS.
 * Linux follows the desktop color scheme; Windows uses the opposite of the
 * taskbar theme ({@code SystemUsesLightTheme}) for contrast.
 */
public final class AppIcon {

	public static final String APP_ID = "org.pmedv.blackboard";

	private static List<Image> cached;
	private static Boolean cachedDark;

	private AppIcon() {
	}

	/** Call from {@code main} after AWT is up, before the first JFrame is shown. */
	public static void applyEarly() {
		if (!isLinux()) {
			return;
		}
		try {
			Class<?> cls = Class.forName("sun.awt.X11.XToolkit");
			java.lang.reflect.Field f = cls.getDeclaredField("awtAppClassName");
			f.setAccessible(true);
			f.set(null, APP_ID);
		}
		catch (Exception ignored) {
		}
		try {
			installGnomeLauncher();
		}
		catch (Exception ignored) {
		}
	}

	public static void apply(Window window) {
		if (window == null) {
			return;
		}
		if (!isLinux()) {
			List<Image> icons = images();
			if (!icons.isEmpty()) {
				window.setIconImages(icons);
			}
			return;
		}
		try {
			if (!window.isDisplayable()) {
				window.addNotify();
			}
			setX11WmClass(window);
			installGnomeLauncher();
		}
		catch (Exception ignored) {
		}
	}

	public static List<Image> images() {
		boolean dark = useDarkIcon();
		if (cached != null && cachedDark != null && cachedDark.booleanValue() == dark) {
			return cached;
		}
		cachedDark = Boolean.valueOf(dark);
		cached = render(dark);
		return cached;
	}

	private static boolean useDarkIcon() {
		if (isWindows()) {
			return !windowsSystemDark();
		}
		return linuxOsDark();
	}

	private static boolean linuxOsDark() {
		try {
			String scheme = gsettings("color-scheme").toLowerCase();
			if (scheme.indexOf("prefer-dark") >= 0) {
				return true;
			}
			if (scheme.indexOf("prefer-light") >= 0) {
				return false;
			}
			return gsettings("gtk-theme").toLowerCase().indexOf("dark") >= 0;
		}
		catch (Exception ignored) {
			return false;
		}
	}

	private static String gsettings(String key) throws Exception {
		Process p = new ProcessBuilder("gsettings", "get", "org.gnome.desktop.interface", key).start();
		return new String(p.getInputStream().readAllBytes()).trim();
	}

	private static boolean windowsSystemDark() {
		try {
			String path = "Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize";
			if (Advapi32Util.registryValueExists(WinReg.HKEY_CURRENT_USER, path, "SystemUsesLightTheme")) {
				return Advapi32Util.registryGetIntValue(WinReg.HKEY_CURRENT_USER, path, "SystemUsesLightTheme") == 0;
			}
		}
		catch (Exception ignored) {
		}
		return false;
	}

	private static void installGnomeLauncher() throws Exception {
		boolean dark = useDarkIcon();
		BufferedImage buf = paint(dark, 48);
		if (buf == null) {
			return;
		}
		File dir = new File(System.getProperty("user.home"), ".local/share/blackboard");
		if (!dir.exists() && !dir.mkdirs()) {
			return;
		}
		File png = new File(dir, dark ? "icon-dark.png" : "icon-light.png");
		ImageIO.write(buf, "png", png);
		new File(dir, "app.png").delete();
		new File(dir, "app-dark.png").delete();
		new File(dir, "app-light.png").delete();
		new File(dir, "bb-dark.png").delete();
		new File(dir, "bb-light.png").delete();

		File apps = new File(System.getProperty("user.home"), ".local/share/applications");
		if (!apps.exists() && !apps.mkdirs()) {
			return;
		}
		File desktop = new File(apps, APP_ID + ".desktop");
		OutputStreamWriter w = new OutputStreamWriter(new FileOutputStream(desktop), StandardCharsets.UTF_8);
		try {
			w.write("[Desktop Entry]\n");
			w.write("Type=Application\n");
			w.write("Name=BlackBoard\n");
			w.write("Exec=true\n");
			w.write("Icon=" + png.getAbsolutePath() + "\n");
			w.write("StartupWMClass=" + APP_ID + "\n");
			w.write("NoDisplay=true\n");
			w.write("X-BlackBoard-Appearance=" + (dark ? "dark" : "light") + "\n");
		}
		finally {
			w.close();
		}
	}

	private static void setX11WmClass(Window window) {
		long xid = Native.getWindowID(window);
		if (xid == 0L) {
			return;
		}
		X11 x11 = X11.INSTANCE;
		X11.Display dpy = x11.XOpenDisplay(null);
		if (dpy == null) {
			return;
		}
		try {
			byte[] bytes = (APP_ID + '\0' + APP_ID + '\0').getBytes(StandardCharsets.ISO_8859_1);
			Memory mem = new Memory(bytes.length);
			mem.write(0, bytes, 0, bytes.length);
			x11.XChangeProperty(dpy, new X11.Window(xid), X11.XA_WM_CLASS, X11.XA_STRING, 8,
					X11.PropModeReplace, mem, bytes.length);
			x11.XFlush(dpy);
		}
		finally {
			x11.XCloseDisplay(dpy);
		}
	}

	private static List<Image> render(boolean dark) {
		List<Image> images = new ArrayList<Image>();
		int[] sizes = { 16, 24, 32, 48 };
		for (int i = 0; i < sizes.length; i++) {
			BufferedImage img = paint(dark, sizes[i]);
			if (img != null) {
				images.add(img);
			}
		}
		return images;
	}

	private static BufferedImage paint(boolean dark, int size) {
		InputStream in = null;
		try {
			in = openSvg(dark);
			if (in == null) {
				return null;
			}
			FlatSVGIcon svg = new FlatSVGIcon(in);
			int src = svg.getIconWidth() > 0 ? svg.getIconWidth() : 300;
			int pad = 1;
			int inner = Math.max(1, size - pad * 2);
			BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = img.createGraphics();
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			g.setColor(Color.BLACK);
			g.fillOval(pad, pad, inner, inner);
			g.translate(pad, pad);
			g.scale(inner / (double) src, inner / (double) src);
			svg.paintIcon(null, g, 0, 0);
			g.dispose();
			return img;
		}
		catch (Exception ignored) {
			return null;
		}
		finally {
			if (in != null) {
				try {
					in.close();
				}
				catch (Exception ignored) {
				}
			}
		}
	}

	private static InputStream openSvg(boolean dark) throws Exception {
		String name = dark ? "icons/svg/bb_dark.svg" : "icons/svg/bb.svg";
		InputStream in = AppIcon.class.getClassLoader().getResourceAsStream(name);
		if (in != null) {
			return in;
		}
		File dir = new File(System.getProperty("user.dir"));
		File[] candidates = {
				new File(dir, "resources/" + name),
				new File(dir, name)
		};
		for (int i = 0; i < candidates.length; i++) {
			if (candidates[i].isFile()) {
				return new FileInputStream(candidates[i]);
			}
		}
		return null;
	}

	private static boolean isLinux() {
		return System.getProperty("os.name", "").toLowerCase().indexOf("linux") >= 0;
	}

	private static boolean isWindows() {
		return System.getProperty("os.name", "").toLowerCase().indexOf("win") >= 0;
	}
}
