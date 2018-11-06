package Lab1;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gson.*;

class FireWall {
    private ForbiddenForm ForbiddenForm;

    FireWall(String configPath) throws IOException {
        File file = new File(configPath);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        String line;
        StringBuilder FileContent = new StringBuilder();
        while ((line = bufferedReader.readLine()) != null) {
            FileContent.append(line);
        }
        Gson gson = new Gson();
        ForbiddenForm = gson.fromJson(FileContent.toString(), ForbiddenForm.class);
    }

    boolean isHostForbidden(String host) {
        return ForbiddenForm.getHosts().contains(host);
    }

    boolean isClientForbidden(String client) {
        return ForbiddenForm.getClients().contains(client);
    }

    private class ForbiddenForm {
        private List<String> forbidden_hosts = new ArrayList<>();
        private List<String> forbidden_clients = new ArrayList<>();

        ForbiddenForm(List<String> hosts, List<String> clients) {
            this.forbidden_hosts = hosts;
            this.forbidden_clients = clients;
        }

        Set<String> getHosts() {
            return new HashSet<>(forbidden_hosts);
        }

        Set<String> getClients() {
            return new HashSet<>(forbidden_clients);
        }
    }

}
