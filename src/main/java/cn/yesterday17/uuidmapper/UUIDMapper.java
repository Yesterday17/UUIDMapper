package cn.yesterday17.uuidmapper;

import com.mojang.authlib.GameProfile;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class UUIDMapper {
    private static String UnTrimUUID(String uuid) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 32; i++) {
            if (i == 8 || i == 12 || i == 16 || i == 20) {
                builder.append('-');
            }
            builder.append(uuid.charAt(i));
        }
        return builder.toString();
    }

    private static void tryGetUUIDFromMojang(String name) {
        try {
            URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + name);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            int status = connection.getResponseCode();
            if (status == 200) {
                Scanner scanner = new Scanner(connection.getInputStream()).useDelimiter("\\A");
                String body = scanner.hasNext() ? scanner.next() : "";
                if (body.charAt(2) == 'n') {
                    // name
                    uuidMap.put(name, UnTrimUUID(body.substring(17 + name.length(), 17 + name.length() + 32)));
                } else {
                    // id
                    uuidMap.put(name, UnTrimUUID(body.substring(7, 39)));
                }
            } else if (status == 204) {
                nonExistList.add(name);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static final Map<String, String> uuidMap = new HashMap<>();
    private static final List<String> nonExistList = new LinkedList<>();

    public static GameProfile getOfflineProfile(GameProfile original) {
        String name = original.getName();
        if (!nonExistList.contains(name) && !uuidMap.containsKey(name)) {
            tryGetUUIDFromMojang(name);
        }

        UUID uuid;
        if (uuidMap.containsKey(name)) {
            uuid = UUID.fromString(uuidMap.get(name));
        } else {
            uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
        }
        return new GameProfile(uuid, original.getName());
    }
}
