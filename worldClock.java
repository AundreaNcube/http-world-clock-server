import java.net.*;
import java.io.*;
import java.time.*;
import java.time.format.*;

public class worldClock {

    static final int PORT = 8888;

    static final String[][] CITIES = { // city name and corresponding time zone ID
            { "London", "Europe/London" },
            { "New York", "America/New_York" },
            { "Los Angeles", "America/Los_Angeles" },
            { "Tokyo", "Asia/Tokyo" },
            { "Sydney", "Australia/Sydney" },
            { "Dubai", "Asia/Dubai" },
            { "Beijing", "Asia/Shanghai" },
            { "Moscow", "Europe/Moscow" },
            { "Paris", "Europe/Paris" },
            { "Cairo", "Africa/Cairo" }
    };

    static final String SA_time_zone = "Africa/Johannesburg"; // South Africa time zone ID
    static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss EEE dd MMM yyyy");

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("World Clock Server is running on port " + PORT);

        while (true) {
            Socket client = serverSocket.accept();
            client.setSoTimeout(3000); //set a timeout for client requests
            System.out.println("Client connected: " + client.getInetAddress());
            try {
                handleRequest(client);

            } catch (IOException e) {
                System.out.println("Error" + e.getMessage());
            }
        }

    }

    static void handleRequest(Socket client) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        PrintWriter out = new PrintWriter(client.getOutputStream());

        String requestLine = in.readLine();
        if (requestLine == null || requestLine.isEmpty()) {
            client.close();
            return;
        }
        System.out.println("Request: " + requestLine);
        String headerLine;
        while ((headerLine = in.readLine()) != null && !headerLine.isEmpty()) {
            System.out.println("Header: " + headerLine);
        }

        String path = "/";
        if (requestLine.startsWith("GET") || requestLine.startsWith("HEAD")) {
            String[] parts = requestLine.split(" ");
            if (parts.length >= 2) {
                path = parts[1];
            }
        }

        if (path.equals("/favicon.ico")) {
            out.print("HTTP/1.1 404 Not Found\r\n");
            out.print("Content-Length: 0\r\n");
            out.print("\r\n");
            out.flush();
            client.close();
            return;
        }

        // handle request. return headers only, no body
        if (requestLine.startsWith("HEAD")) {
            out.print("HTTP/1.1 200 OK\r\n");
            out.print("Content-Type: text/html; charset=UTF-8\r\n");
            out.print("Server: WorldClockServer/1.0\r\n");
            out.print("Cache-Control: no-store\r\n");
            out.print("\r\n");
            out.flush();
            client.close();
            return;
        }

        path = java.net.URLDecoder.decode(path, "UTF-8");

        if (!path.equals("/")) {
            boolean validCity = false;
            String cityName = path.substring(1); // remove leading '/'
            for (String[] city : CITIES) {
                if (city[0].equalsIgnoreCase(cityName)) {
                    validCity = true;
                    break;
                }
            }
            if (!validCity) {
                String errorPage = "<!DOCTYPE html><html><head><title>404 Not Found</title></head>" +
                        "<body style = 'font-family:Arial;background:#1a1a2e;color:#eee;padding:40px;'>" +
                        "<h1 style='color:#e94560;'>404 Not Found</h1>" +
                        "<p>The path <strong>" + path + "</strong> was not found on this server.</p>" +
                        "<a href='/' style='color:#f5a623;'>&#8592; Back to World Clock</a>"
                        + "</body></html>";

                String httpDate = ZonedDateTime.now(ZoneId.of("UTC"))
                        .format(DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss z"));

                out.print("HTTP/1.1 404 Not Found\r\n");
                out.print("Content-Type: text/html; charset=UTF-8\r\n");
                out.print("Content-Length: " + errorPage.getBytes("UTF-8").length + "\r\n");
                out.print("Connection: close\r\n");
                out.print("Date: " + httpDate + "\r\n");
                out.print("Server: WorldClockServer/1.0\r\n");
                out.print("Cache-Control: no-store\r\n");
                out.print("\r\n");
                out.print(errorPage);
                out.flush();
                client.close();
                return;
            }
        }

        String html = buildHTML(path);

        String httpDate = ZonedDateTime.now(ZoneId.of("UTC"))
                .format(DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss z"));

        out.print("HTTP/1.1 200 OK\r\n");
        out.print("Content-Type: text/html; charset=UTF-8\r\n");
        out.print("Content-Length: " + html.getBytes("UTF-8").length + "\r\n");
        out.print("Connection: close\r\n");
        out.print("Date: " + httpDate + "\r\n");
        out.print("Server: WorldClockServer/1.0\r\n");
        out.print("Cache-Control: no-store\r\n");
        out.print("\r\n");
        out.print(html);
        out.flush();
        client.close();
    }

    static String buildHTML(String path) {
        String selectedCity = null;
        String selectedZone = null;

        if (!path.equals("/")) {
            String cityName = path.substring(1); // remove leading '/'
            for (String[] city : CITIES) {
                if (city[0].equalsIgnoreCase(cityName)) {
                    selectedCity = city[0];
                    selectedZone = city[1];
                    break;
                }
            }
        }

        ZonedDateTime saTime = ZonedDateTime.now(ZoneId.of(SA_time_zone));
        String saFormatted = saTime.format(FORMATTER);
        String saOffset = formatOffset(saTime);

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<meta http-equiv='refresh' content='1'>");
        html.append("<title>World Clock</title>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; background: #1a1a2e; color: #eee; padding: 40px; }");
        html.append("h1 { color: #e94560; }");
        html.append(
                ".clock-box { background: #16213e; border-radius: 10px; padding: 20px; margin: 10px 0; display: inline-block; min-width: 350px; }");
        html.append(".city-name { font-size: 1.1em; color: #a8a8b3; }");
        html.append(".time { font-size: 2em; font-weight: bold; color: #e94560; }");
        html.append(".offset { font-size: 0.9em; color: #f5a623; margin-top: 4px; }");
        html.append(".diff { font-size: 0.95em; color: #88f018; margin-top: 6px; }");
        html.append(".cities { margin-top: 30px; }");
        html.append(
                "a { display: inline-block; margin: 6px; padding: 10px 18px; background: #16213e; color: #eee; text-decoration: none; border-radius: 6px; border: 1px solid #e94560; }");
        html.append("a:hover { background: #e94560; color: white; }");
        html.append("</style></head><body>");

        html.append("<h1>World Clock</h1>");

        html.append("<div class='clock-box'>");
        html.append("<div class='city-name'>South Africa (Johannesburg)</div>");
        html.append("<div class='time'>").append(saFormatted).append("</div>");
        html.append("<div class='offset'>").append(saOffset).append("</div>");
        html.append("</div><br><br>");

        if (selectedCity != null) {
            ZonedDateTime cityTime = ZonedDateTime.now(ZoneId.of(selectedZone));
            String cityFormatted = cityTime.format(FORMATTER);
            String cityOffset = formatOffset(cityTime);
            String diffMessage = getTimeDifference(saTime, cityTime);

            html.append("<div class='clock-box'>");
            html.append("<div class='city-name'>").append(selectedCity).append("</div>");
            html.append("<div class='time'>").append(cityFormatted).append("</div>");
            html.append("<div class='offset'>").append(cityOffset).append("</div>");
            html.append("<div class='diff'>").append(diffMessage).append("</div>");
            html.append("</div><br><br>");
        }
            html.append("<div class='cities'><strong>Click a city to see its time:</strong><br><br>");
            for (String[] city : CITIES) {
                html.append("<a href='/").append(city[0].replace(" ", "%20")).append("'>")
                        .append(city[0]).append("</a>");
            }
            html.append("</div>");
            html.append("</body></html>");
            return html.toString();
    }

    static String formatOffset(ZonedDateTime time) {
        ZoneOffset offset = time.getOffset();
        int totalSeconds = offset.getTotalSeconds();
        int hours = totalSeconds / 3600;
        int minutes = (Math.abs(totalSeconds) % 3600) / 60;

        if (minutes == 0) {
            return String.format("UTC%+d:00", hours);
        } else {
            return String.format("UTC%+d:%02d", hours, minutes);
        }
    }

    static String getTimeDifference(ZonedDateTime saTime, ZonedDateTime cityTime) {
        int saOffsetSeconds = saTime.getOffset().getTotalSeconds();
        int cityOffsetSeconds = cityTime.getOffset().getTotalSeconds();
        int diffSeconds = cityOffsetSeconds - saOffsetSeconds;
        int diffhours = diffSeconds / 3600;
        int diffMinutes = Math.abs((diffSeconds % 3600) / 60);

        if (diffSeconds == 0) {
            return "Same time as South Africa";
        }

        String direction = diffSeconds > 0 ? "ahead of" : "behind";
        diffhours = Math.abs(diffhours);

        if (diffMinutes == 0) {
            return String.format("&#9201; %d hour%s %s South Africa",
                    diffhours, diffhours > 1 ? "s" : "", direction);
        } else {
            return String.format("&#9201; %d hour%s %d min %s South Africa",
                    diffhours, diffhours == 1 ? "" : "s", diffMinutes, direction);
        }
    }
}
