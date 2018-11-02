import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gson.*;

class FireWall {
    private ForbiddenHost ForbiddenHost;

    FireWall(String configPath) throws IOException {
        File file = new File(configPath);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        String line;
        StringBuilder FileContent = new StringBuilder();
        while ((line = bufferedReader.readLine()) != null) {
            FileContent.append(line);
        }
        Gson gson = new Gson();
        ForbiddenHost = gson.fromJson(FileContent.toString(), FireWall.ForbiddenHost.class);
    }

    public boolean isForbidden(String host) {
        return ForbiddenHost.getHosts().contains(host);
    }

    private class ForbiddenHost {
        private List<String> Forbidden_hosts = new ArrayList<>();

        ForbiddenHost(List<String> host) {
            this.Forbidden_hosts = host;
        }

        Set<String> getHosts() {
            return new HashSet<>(Forbidden_hosts);
        }
    }
}
