package dev.nebula.visuals;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class NebulaConfig {
	public static final String[] KEYS = {"fps", "ping", "cps", "coords", "armor", "keystrokes", "target", "cooldown"};

	public static class Pos {
		public int x, y;
		public boolean enabled = true;
		public Pos() {}
		public Pos(int x, int y, boolean enabled) { this.x = x; this.y = y; this.enabled = enabled; }
	}

	public boolean hudEnabled = true;
	public Map<String, Pos> hud = new LinkedHashMap<>();

	public boolean fullbright = false;
	public boolean glowEsp = false;
	public boolean toggleSprint = false;
	public boolean damageNumbers = true;
	public boolean hitMarker = true;
	public boolean chatFilter = true;
	public boolean customTitle = true;
	public int popupMs = 1200;
	public String lang = "ru";
	public float hue1 = 0.76f;
	public float hue2 = 0.30f;
	public List<String> bannedWords = new ArrayList<>();

	public boolean fly = false;
	public float flySpeed = 1.0f;
	public boolean speed = false;
	public float speedValue = 1.5f;
	public boolean noFall = false;
	public boolean jesus = false;
	public boolean noSlow = false;
	public boolean killAura = false;
	public float auraRange = 3.2f;
	public boolean autoTool = false;
	public float reach = 4.5f;

	public int color1() { return Gfx.hsv(hue1, 0.72f, 1f); }
	public int color2() { return Gfx.hsv(hue2, 0.72f, 1f); }

	private static NebulaConfig INSTANCE;
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private static Path file() {
		return FabricLoader.getInstance().getConfigDir().resolve("nebula_visuals.json");
	}

	public static NebulaConfig get() {
		if (INSTANCE == null) {
			NebulaConfig loaded = null;
			try {
				Path f = file();
				if (Files.exists(f)) loaded = GSON.fromJson(Files.readString(f), NebulaConfig.class);
			} catch (Throwable ignored) {}
			INSTANCE = loaded != null ? loaded : new NebulaConfig();
			INSTANCE.fillDefaults();
			INSTANCE.save();
		}
		return INSTANCE;
	}

	public void fillDefaults() {
		if (hud == null) hud = new LinkedHashMap<>();
		if (bannedWords == null) bannedWords = new ArrayList<>();
		if (lang == null) lang = "ru";
		if (flySpeed <= 0f) flySpeed = 1.0f;
		if (speedValue <= 0f) speedValue = 1.5f;
		if (auraRange <= 0f) auraRange = 3.2f;
		if (reach <= 0f) reach = 4.5f;
		defaultPos("fps", 4, 4);
		defaultPos("ping", 4, 22);
		defaultPos("cps", 4, 40);
		defaultPos("coords", 4, 58);
		defaultPos("armor", 4, 80);
		defaultPos("keystrokes", 4, 160);
		defaultPos("target", 200, 8);
		defaultPos("cooldown", 190, 230);
	}

	private void defaultPos(String key, int x, int y) {
		hud.putIfAbsent(key, new Pos(x, y, true));
	}

	public void resetPositions() {
		hud.clear();
		fillDefaults();
	}

	public void save() {
		try {
			Files.writeString(file(), GSON.toJson(this));
		} catch (Throwable ignored) {}
	}
}
